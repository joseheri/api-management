# FMT Data Provider - Project Integration Report
## API Key Management Feature Integration Analysis

**Generated:** 2026-05-30  
**Purpose:** Document existing project conventions to guide API key feature integration  
**Status:** Analysis Complete - Implementation Already Integrated

---

## Executive Summary

The API Key Management feature has been **fully integrated** into the FMT Data Provider project. This report documents the existing conventions that were followed during integration and serves as a reference for understanding how the feature aligns with project architecture.

---

## 1. Package Structure Conventions

### Root Package
```
com.lmco.jsf.fmt.dataprovider
```

### Domain Package Pattern
Each domain module follows this structure:
```
com.lmco.jsf.fmt.dataprovider.<domain>/
├── model/          # JPA entities and domain objects
├── repository/     # Data access layer
├── resource/       # JAX-RS REST endpoints
└── service/        # Business logic layer
```

### Common Package Structure
```
com.lmco.jsf.fmt.dataprovider.common/
├── correlation/    # Request correlation ID handling
├── error/          # Error response and exception mapping
├── filtering/      # Query filtering utilities (time-based, etc.)
├── pagination/     # Pagination utilities and models
└── response/       # Standard API response wrappers
```

### Security Package (API Key Feature)
```
com.lmco.jsf.fmt.dataprovider.security/
└── apikey/
    ├── annotation/      # @RequiresScope for authorization
    ├── config/          # Security validation configuration
    ├── dto/             # Request/Response DTOs
    ├── filter/          # ApiKeyAuthenticationFilter
    ├── interceptor/     # ScopeEnforcementInterceptor
    ├── model/           # JPA entities (ApiKey, ApiClient, etc.)
    ├── repository/      # Data access for security entities
    ├── resource/        # REST endpoints for key management
    ├── scheduler/       # Scheduled maintenance tasks
    └── service/         # Business logic and utilities
```

---

## 2. Resource (REST Endpoint) Conventions

### Representative Example: AircraftResource

**Class:** `com.lmco.jsf.fmt.dataprovider.aircraft.resource.AircraftResource`

**Key Conventions:**

#### Class-Level Annotations
```java
@Path("/api/v1/aircrafts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Aircraft Resource", description = "Aircraft-specific endpoints")
public class AircraftResource {
```

#### Service Injection
```java
@Inject
private AircraftService aircraftService;
```
- Uses **CDI @Inject** (not @Autowired)
- Field injection pattern
- Service-oriented architecture

#### Method-Level Patterns
```java
@GET
@RequiresScope("read:aircraft")  // Security annotation
@Operation(summary = "...", description = "...")
@APIResponse(responseCode = "200", description = "...")
@APIResponse(responseCode = "401", description = "Unauthorized")
public ApiResponse<Aircraft> getAllAircraft(
    @Parameter(...) @QueryParam("uai") String uai,
    @QueryParam("limit") Integer limitParam,
    @QueryParam("offset") Integer offsetParam) {
    
    PageRequest pageRequest = PaginationUtil.createPageRequest(limitParam, offsetParam);
    return aircraftService.getAllAircraft(pageRequest);
}
```

#### Response Patterns
1. **List endpoints** return `ApiResponse<T>` wrapper
2. **Single entity endpoints** return entity directly (or throw NotFoundException)
3. **JAX-RS Response** used when custom status codes needed
4. **ApiResponse** structure:
   ```json
   {
     "query": { "limit": 100, "offset": 0 },
     "data": [...],
     "pagination": { "totalCount": 500, "currentPage": 1 },
     "links": { "self": "...", "next": "..." }
   }
   ```

#### Error Handling
- **NotFoundException** for 404 scenarios
- **Custom exceptions** caught by GlobalExceptionMapper
- **ValidationResult.throwIfInvalid()** for input validation
- Errors return `ErrorResponse` format

#### Security Integration
- `@RequiresScope("read:aircraft")` on protected endpoints
- Filter validates API key before method execution
- Interceptor enforces scope requirements
- **No auth required for:** `/health`, `/metrics`, `/swagger-ui`

---

## 3. Service Layer Conventions

### Representative Example: AircraftService

**Class:** `com.lmco.jsf.fmt.dataprovider.aircraft.service.AircraftService`

**Key Conventions:**

#### Class Declaration
```java
@ApplicationScoped
public class AircraftService {
    
    @Inject
    private AircraftRepository aircraftRepository;
    
    private static final int MAX_LIMIT = 500;
```
- **@ApplicationScoped** for service beans
- Repository injection via @Inject
- Constants for business rules

#### Method Patterns
```java
public ApiResponse<Aircraft> getAllAircraft(PageRequest pageRequest) {
    int limit = Math.min(pageRequest.getLimit(), MAX_LIMIT);
    int offset = pageRequest.getOffset();
    
    long totalCount = aircraftRepository.count();
    List<Aircraft> aircraft = aircraftRepository.findAllNative(offset, limit);
    
    QueryInfo queryInfo = QueryInfo.from(pageRequest, null);
    PaginationInfo paginationInfo = PaginationUtil.createPaginationInfo(totalCount, pageRequest);
    NavigationLinks links = PaginationUtil.createNavigationLinks("/api/v1/aircraft", pageRequest, paginationInfo);
    
    return new ApiResponse<>(queryInfo, aircraft, paginationInfo, links);
}
```

#### Transaction Management
- **No explicit @Transactional on read-only methods**
- Repositories handle transaction boundaries
- Write operations in services marked @Transactional if needed

#### Validation Style
- **Input sanitization** in service layer
- **Business rule validation** before repository calls
- **Exception throwing** for invalid states:
  ```java
  .orElseThrow(() -> new NotFoundException("Aircraft with UAI '" + uai + "' not found"));
  ```

#### Repository Usage
- **Count queries separate** from data queries
- **Pagination** handled by service layer
- **Multiple repository calls** composed in service methods

#### Logging Style
- **No explicit logging** in simple services
- Complex services use SLF4J logger
- Audit events logged separately (see ApiKeyAuditService)

---

## 4. Repository Layer Conventions

### Representative Example: AircraftRepository

**Class:** `com.lmco.jsf.fmt.dataprovider.aircraft.repository.AircraftRepository`

**Key Conventions:**

#### Class Declaration
```java
@ApplicationScoped
public class AircraftRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
```
- **@ApplicationScoped** scope
- **@PersistenceContext** for EntityManager
- **No @Repository** annotation (not using Spring)

#### Query Patterns

**Native SQL Preferred** for complex joins:
```java
@SuppressWarnings("unchecked")
public List<Aircraft> findAllNative(int offset, int limit) {
    String sql = """
        SELECT av.uai as uai,
               mi.PART_NUMBER as avPartNumber,
               ...
        FROM repo_owner.inventory_info mi
        INNER JOIN repo_owner.complex_instance_data ci 
            ON (...)
        INNER JOIN repo_owner.air_vehicle_data av 
            ON (...)
        WHERE mi.squadron_id NOT IN ('TR01', 'LD01')
        ORDER BY av.uai ASC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """;

    return entityManager.createNativeQuery(sql, "AircraftMapping")
            .setParameter("offset", offset)
            .setParameter("limit", limit)
            .getResultList();
}
```

**JPQL** for simple queries (when possible):
```java
String jpql = """
    SELECT NEW com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft(...)
    FROM Aircraft mi
    WHERE ...
    """;
```

#### Method Naming
- `findAll(...)` - retrieve all with pagination
- `findById(...)` - single entity by ID
- `findByXxx(...)` - filtered queries
- `count()` - total count for pagination
- `countByXxx(...)` - filtered count

#### Result Mapping
- **@SqlResultSetMapping** defined in entity class
- **Constructor-based mapping** preferred
- Named result set mappings: `"AircraftMapping"`

#### Transaction Assumptions
- **Read operations** no explicit transaction
- **Write operations** wrapped in @Transactional by caller
- EntityManager handles transaction participation

---

## 5. Model (Entity) Layer Conventions

### Representative Example: Aircraft

**Class:** `com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft`

**Key Conventions:**

#### Entity Declaration
```java
@Entity
@Table(name = "inventory_info", schema = "repo_owner")
@IdClass(AircraftId.class)
@SqlResultSetMapping(
    name = "AircraftMapping",
    classes = @ConstructorResult(...)
)
public class Aircraft {
```

#### Table/Column Mapping
- **Table names:** lowercase with underscores (Oracle convention)
- **Schema:** explicitly specified (`schema = "repo_owner"`)
- **Column names:** uppercase in database, mapped via `@Column(name="COLUMN_NAME")`
- **Entity fields:** camelCase in Java

#### Primary Key Patterns
**Composite Keys:**
```java
@Id
@Column(name = "source_node", nullable = false, length = 10)
private String sourceNode;

@Id
@Column(name = "part_number", nullable = false, length = 25)
private String partNumber;
```
- Uses **@IdClass** for composite keys
- Separate ID class with equals/hashCode

**Single Keys:**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id")
private Long id;
```

#### Enum Mapping
```java
@Enumerated(EnumType.STRING)
@Column(name = "status", length = 20)
private ApiKeyStatus status;
```
- **EnumType.STRING** for database storage
- Enums have uppercase database values

#### Temporal Fields
```java
@Column(name = "created_at")
private Instant createdAt;
```
- **Instant** for timestamps (not Timestamp or LocalDateTime)
- UTC timezone implied
- ISO-8601 format in JSON

#### Transient Fields
```java
@Transient
private String uai;  // Derived from joins
```
- Fields not persisted but populated from native queries

#### Constructors
1. **No-arg constructor** (required by JPA)
2. **All-args constructor** for result set mapping
3. **Partial constructors** for common use cases

---

## 6. Common API Utilities

### 6.1 Response Envelope

**Class:** `com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse<T>`

**Usage:**
```java
new ApiResponse<>(queryInfo, data, paginationInfo, links);
```

**Structure:**
- `query` - Request parameters metadata
- `data` - List of entities
- `pagination` - Page info (totalCount, limit, offset, currentPage, totalPages)
- `links` - HATEOAS navigation (self, next, prev, first, last)

### 6.2 Error Response

**Class:** `com.lmco.jsf.fmt.dataprovider.common.error.ErrorResponse`

**Usage:**
```java
ErrorResponse.builder()
    .code(ErrorCodes.RESOURCE_NOT_FOUND)
    .message("Aircraft not found")
    .detail("uai", requestedUai)
    .build();
```

**Structure:**
```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Aircraft not found",
    "details": { "uai": "AF-001" },
    "timestamp": "2026-05-30T...",
    "requestId": "req-12345"
  }
}
```

**Error Codes:** Defined in `ErrorResponse.ErrorCodes` constants class

### 6.3 Exception Mapping

**Class:** `com.lmco.jsf.fmt.dataprovider.common.error.GlobalExceptionMapper`

Maps Java exceptions to HTTP responses:
- `NotFoundException` → 404
- `ValidationException` → 400
- `SecurityException` → 401/403
- Generic exceptions → 500

### 6.4 Pagination

**Classes:**
- `PageRequest` - Input (limit, offset)
- `PaginationInfo` - Output (totalCount, currentPage, totalPages, hasMore)
- `NavigationLinks` - HATEOAS links
- `PaginationUtil` - Helper methods

**Usage:**
```java
PageRequest pageRequest = PaginationUtil.createPageRequest(limitParam, offsetParam);
PaginationInfo paginationInfo = PaginationUtil.createPaginationInfo(totalCount, pageRequest);
NavigationLinks links = PaginationUtil.createNavigationLinks(baseUrl, pageRequest, paginationInfo);
```

### 6.5 Filtering

**Classes:**
- `TimeFilter` - Time-based filtering (since, from, to)
- `TimeFilterValidator` - Validates time parameters
- `QueryFilterBuilder` - Builds query predicates

**Usage:**
```java
TimeFilterValidator validator = new TimeFilterValidator();
ValidationResult result = validator.validate(since, from, to);
result.throwIfInvalid();

Instant sinceInstant = TimeFilterValidator.parseAndValidate(since, "since");
TimeFilter timeFilter = TimeFilter.from(sinceInstant, fromInstant, toInstant);
```

### 6.6 Correlation ID

**Classes:**
- `CorrelationIdContext` - ThreadLocal storage
- `CorrelationIdGenerator` - UUID generation
- `CorrelationIdFilter` - Request filter

Automatically adds correlation IDs to requests for troubleshooting.

---

## 7. Configuration Conventions

### 7.1 Application Properties

**File:** `src/main/resources/application.properties`

#### Profile-Based Configuration
```properties
# Default settings
quarkus.http.port=8080

# Dev Profile - H2 in-memory
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:fmtdb;MODE=Oracle;INIT=CREATE SCHEMA IF NOT EXISTS repo_owner
%dev.quarkus.hibernate-orm.database.generation=drop-and-create
%dev.quarkus.hibernate-orm.sql-load-script=import-dev.sql

# Test Profile - H2
%test.quarkus.datasource.db-kind=h2
%test.quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;MODE=Oracle;...
%test.quarkus.hibernate-orm.database.generation=drop-and-create
%test.quarkus.hibernate-orm.sql-load-script=import-dev.sql

# Prod Profile - Oracle
%prod.quarkus.datasource.db-kind=oracle
%prod.quarkus.datasource.username=${DB_USERNAME}
%prod.quarkus.datasource.password=${DB_PASSWORD}
%prod.quarkus.datasource.jdbc.url=${DB_URL}
%prod.quarkus.hibernate-orm.database.generation=none
```

#### Hibernate Settings
- **Dev:** `drop-and-create` with `import-dev.sql`
- **Test:** `drop-and-create` with `import-dev.sql`
- **Prod:** `none` (validate only)

#### Security Configuration
```properties
# API Key HMAC secret
apikey.secret=${APIKEY_SECRET:INSECURE_DEFAULT_CHANGE_IN_PRODUCTION_MIN_32_CHARS}
```

#### CORS Configuration
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=*  # Restrict in production
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
```

### 7.2 Dev Data Loading

**File:** `src/main/resources/import-dev.sql`

- Loads test data automatically in dev/test modes
- SQL script with INSERT statements
- Oracle syntax (since H2 uses Oracle mode)

---

## 8. Test Conventions

### 8.1 Test Framework

**Framework:** JUnit 5 + Quarkus Test + RestAssured

**Example:** `ApiKeyAuthenticationTest`

#### Test Class Structure
```java
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiKeyAuthenticationTest {
    
    @Inject
    ApiClientRepository clientRepository;
    
    @BeforeAll
    @Transactional
    public void setup() { ... }
    
    @AfterAll
    @Transactional
    public void cleanup() { ... }
    
    @Test
    @Order(1)
    @DisplayName("Test 1: Valid API key succeeds")
    public void testValidKeySucceeds() { ... }
}
```

#### Annotations Used
- `@QuarkusTest` - Enables Quarkus test infrastructure
- `@TestMethodOrder` - Controls execution order
- `@TestInstance` - Lifecycle configuration
- `@Transactional` - For test data setup/cleanup
- `@Order` - Test execution sequence
- `@DisplayName` - Human-readable test names

#### RestAssured Usage
```java
given()
    .header("X-API-Key", validApiKey)
    .when()
    .get("/api/v1/aircrafts")
    .then()
    .statusCode(200)
    .body("data", hasSize(greaterThan(0)));
```

#### Test Data Setup
1. **@BeforeAll** creates test entities
2. **Injected repositories** used directly
3. **@Transactional** ensures cleanup
4. **@AfterAll** removes test data

#### H2 Test Database
- In-memory H2 with Oracle compatibility mode
- Schema auto-created: `INIT=CREATE SCHEMA IF NOT EXISTS repo_owner`
- Test data loaded from `import-dev.sql`
- Isolated per test class

#### Assertion Style
```java
Assertions.assertNotNull(storedKey.getKeyHash());
Assertions.assertNotEquals(rawKey, storedKey.getKeyHash());
```
- JUnit 5 `Assertions` class
- Hamcrest matchers in RestAssured
- Clear failure messages

---

## 9. API Key Feature Integration Details

### 9.1 Package Placement

**✅ Successfully Integrated As:**
```
com.lmco.jsf.fmt.dataprovider.security/
└── apikey/
    ├── annotation/      # @RequiresScope
    ├── config/          # Validation configuration
    ├── dto/             # 10 DTO classes
    ├── filter/          # Authentication filter
    ├── interceptor/     # Authorization interceptor
    ├── model/           # 10 JPA entities
    ├── repository/      # 5 repositories
    ├── resource/        # 3 REST endpoints
    ├── scheduler/       # Maintenance tasks
    └── service/         # 10 service classes
```

### 9.2 Integration Points

#### Authentication Flow
1. **ApiKeyAuthenticationFilter** (highest priority)
   - Intercepts all requests to `/api/v1/*`
   - Validates X-API-Key header
   - Sets SecurityContext for downstream components
   - Returns 401 if authentication fails

2. **@RequiresScope** Annotation
   - Applied to resource methods
   - Declares required OAuth-style scope (e.g., "read:aircraft")

3. **ScopeEnforcementInterceptor**
   - Runs after authentication
   - Checks SecurityContext for required scopes
   - Returns 403 if scope missing

#### Resource Patterns
```java
@Path("/api/v1/security/api-clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiClientResource {
    
    @Inject
    private ApiClientService apiClientService;
    
    @POST
    @Transactional
    public Response createApiClient(CreateApiClientRequest request) {
        ApiClientResponse response = apiClientService.createApiClient(request);
        return Response.status(201).entity(response).build();
    }
}
```

#### Service Patterns
```java
@ApplicationScoped
public class ApiKeyService {
    
    @Inject
    ApiKeyRepository keyRepository;
    
    @Inject
    ApiKeyHasher hasher;
    
    @Inject
    ApiKeyAuditService auditService;
    
    @Transactional
    public void revokeApiKey(Long keyId, String revokedBy, String reason) {
        ApiKey key = keyRepository.findById(keyId)
            .orElseThrow(() -> new NotFoundException("API key not found"));
        
        key.setStatus(ApiKeyStatus.REVOKED);
        key.setRevokedAt(Instant.now());
        key.setRevokedBy(revokedBy);
        key.setRevokedReason(reason);
        
        keyRepository.persist(key);
        auditService.logEvent(keyId, ApiKeyAuditEventType.KEY_REVOKED, revokedBy);
    }
}
```

#### Repository Patterns
```java
@ApplicationScoped
public class ApiKeyRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public Optional<ApiKey> findByPrefix(String prefix) {
        try {
            ApiKey key = entityManager
                .createQuery("SELECT k FROM ApiKey k WHERE k.keyPrefix = :prefix", ApiKey.class)
                .setParameter("prefix", prefix)
                .getSingleResult();
            return Optional.of(key);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
```

#### Model Patterns
```java
@Entity
@Table(name = "api_keys")
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_prefix", unique = true, nullable = false)
    private String keyPrefix;
    
    @Column(name = "key_hash", nullable = false)
    private String keyHash;  // NEVER store raw key!
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApiKeyStatus status;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
}
```

### 9.3 Common Utilities Usage

#### Error Response
```java
throw new SecurityException(
    ErrorResponse.builder()
        .code(ErrorCodes.API_KEY_EXPIRED)
        .message("API key has expired")
        .detail("expiresAt", key.getExpiresAt())
        .build()
);
```

#### Audit Logging
```java
auditService.logEvent(
    keyId,
    ApiKeyAuditEventType.KEY_ROTATED,
    performedBy,
    Map.of("oldKeyId", oldKeyId, "newKeyId", newKeyId)
);
```

---

## 10. Recommended Integration Best Practices

### 10.1 When Adding New Features

**✅ DO:**
1. Follow existing package structure (`<domain>/model/repository/resource/service`)
2. Use `ApiResponse<T>` for paginated list endpoints
3. Apply `@RequiresScope` to protected endpoints
4. Use `ErrorResponse` for structured errors
5. Leverage common utilities (PaginationUtil, TimeFilter, etc.)
6. Write QuarkusTest integration tests
7. Use H2 for dev/test, Oracle for production
8. Document with JavaDoc and OpenAPI annotations

**❌ DON'T:**
1. Mix authentication mechanisms (stick with API keys)
2. Store sensitive data unencrypted
3. Bypass security filters
4. Create custom response formats
5. Use different pagination patterns
6. Skip error code definitions

### 10.2 Security Considerations

1. **All `/api/v1/*` endpoints** require authentication (except health/metrics)
2. **Admin endpoints** (`/api/v1/security/*`) require additional validation
3. **Raw API keys never stored** - only hashed values
4. **Correlation IDs** automatically added for troubleshooting
5. **Audit logging** for all security-sensitive operations

### 10.3 Database Conventions

1. **Schema:** `repo_owner` for Oracle, created in H2
2. **Table names:** lowercase_with_underscores
3. **Column names:** UPPERCASE (Oracle convention)
4. **Primary keys:** `id` or composite keys with @IdClass
5. **Timestamps:** `Instant` type, UTC timezone
6. **Enums:** STRING mapping (not ORDINAL)

---

## 11. Conclusion

The API Key Management feature successfully integrates with the FMT Data Provider project by:

✅ **Following** all established naming and structural conventions  
✅ **Reusing** common utilities (ApiResponse, ErrorResponse, pagination)  
✅ **Extending** the security model without disrupting existing endpoints  
✅ **Maintaining** consistency with resource/service/repository patterns  
✅ **Supporting** all environments (dev H2, test H2, prod Oracle)  
✅ **Providing** comprehensive test coverage using QuarkusTest  

The feature is production-ready and maintains architectural integrity with the rest of the codebase.

---

## Appendix: Quick Reference

### Package Structure
- Models: `*.model`
- Repositories: `*.repository`
- Services: `*.service`
- Resources: `*.resource`
- DTOs: `*.dto`

### Common Annotations
- `@ApplicationScoped` - Services, repositories
- `@Inject` - Dependency injection
- `@Transactional` - Write operations
- `@Path` - REST endpoint paths
- `@RequiresScope` - Authorization

### Database
- Dev/Test: H2 in-memory with Oracle mode
- Prod: Oracle with environment variables
- Schema: `repo_owner`
- Generation: drop-and-create (dev/test), none (prod)

### Testing
- Framework: JUnit 5 + Quarkus + RestAssured
- Database: H2 in-memory per test class
- Data: Loaded from `import-dev.sql`
- Cleanup: `@AfterAll` with `@Transactional`

---

**Document Version:** 1.0  
**Last Updated:** 2026-05-30  
**Maintained By:** FMT Engineering Team
