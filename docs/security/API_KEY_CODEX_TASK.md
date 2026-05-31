# API Key Management Codex Task

Build the API key management feature for this Quarkus project.

The feature must follow the project’s existing layered architecture:

resource -> service -> repository -> model

Base package:

com.lmco.jsf.fmt.dataprovider.security.apikey

Subpackages:

annotation
config
dto
filter
interceptor
model
repository
resource
scheduler
service
ui

Use the existing project conventions:

- JAX-RS resources
- CDI @Inject
- EntityManager repositories, not Panache
- common.response.ApiResponse where appropriate
- common.error.ErrorResponse and GlobalExceptionMapper
- common.pagination for list endpoints
- existing test style with QuarkusTest and RestAssured

Use cases:

1. Admin workflow

- Admin creates an API client.
- Admin creates a claim invitation for an approved client.
- Claim invitation includes approved email/claim identifier, key name, environment, scopes, expiration, and approval reference.
- System generates a one-time claim code.
- Raw claim code is shown once only.
- Raw claim code is never stored or logged.
- Admin can list clients.
- Admin can view key metadata.
- Admin can revoke keys.
- Admin can rotate keys using a replacement claim invitation.

2. Client/user claim workflow

- Client goes to claim page or claim endpoint.
- Client submits approved email/claim identifier and claim code.
- System validates claim code.
- API key is generated only at successful claim completion.
- Raw API key is shown once only.
- Raw API key is never stored or logged.
- Claim code cannot be reused.

3. Runtime API authentication

- Clients call protected APIs using:
  X-API-Key: <api-key>
- API key format:
  ak*{env}*{publicId}.{secret}
- Stored prefix example:
  ak_prod_N7F3K92A
- Store only key prefix and HMAC hash.
- Use HMAC-SHA-256 for API key hashing.
- Use BCrypt for claim code hashing.
- Missing/invalid API key returns 401.
- Valid key missing required scope returns 403.
- API keys must never authorize admin endpoints.

4. Scope authorization

- Implement @RequiresScope.
- Implement scope enforcement.
- Add one reference protected endpoint or protect existing AircraftResource GET endpoint with:
  @RequiresScope("read:aircraft")

5. Admin auth

- Final admin login will be SSO / Windows AD later.
- Do not implement final SSO now.
- Implement temporary dev/test-only admin guard:
  X-Dev-Admin-User: <username>
- This must not work in prod.
- Admin endpoints must not be open in prod.

6. Persistence

- API key management tables are new for this project.
- It is acceptable to define the schema for this feature.
- Use explicit @Table, @Column, and @JoinColumn mappings.
- Do not rely on implicit Hibernate naming.
- Dev/test may use H2.
- Do not modify existing aircraft/maintenance/pairs/usage table mappings or queries.

7. Qute UI

- Build after backend tests pass.
- Admin UI:
  - list clients
  - create client
  - create claim invitation
  - show claim code once
  - list key metadata
  - revoke key
- Claim UI:
  - enter approved email and claim code
  - complete claim
  - show API key once
- Do not put secrets in URLs.
- Do not store raw secrets in session.

Required gates:

1. Run mvn clean compile.
2. Run mvn test -Dtest=ApiKey\*.
3. Report exact files changed.
4. Report commands run and test results.
5. Stop after each phase.
