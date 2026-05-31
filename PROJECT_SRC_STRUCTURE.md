# FMT Data Provider - Source Code Structure

**Generated:** 2026-05-30  
**Project:** fmt-data-provider  
**Base Package:** com.lmco.jsf.fmt.dataprovider

## Directory Tree

```
src/
├── main/
│   ├── java/com/lmco/jsf/fmt/dataprovider/
│   │   ├── README.md
│   │   ├── aircraft/
│   │   │   ├── model/
│   │   │   │   ├── Aircraft.java
│   │   │   │   ├── AircraftId.java
│   │   │   │   ├── OperationalStatus.java
│   │   │   │   └── OperationalStatusId.java
│   │   │   ├── repository/
│   │   │   │   ├── AircraftRepository.java
│   │   │   │   └── OperationalStatusRepository.java
│   │   │   ├── resource/
│   │   │   │   └── AircraftResource.java
│   │   │   └── service/
│   │   │       ├── AircraftService.java
│   │   │       └── OperationalStatusService.java
│   │   ├── common/
│   │   │   ├── correlation/
│   │   │   │   ├── CorrelationIdContext.java
│   │   │   │   └── CorrelationIdGenerator.java
│   │   │   ├── error/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionMapper.java
│   │   │   │   └── TimeParameterException.java
│   │   │   ├── filtering/
│   │   │   │   ├── QueryFilterBuilder.java
│   │   │   │   ├── TimeFilter.java
│   │   │   │   └── TimeFilterValidator.java
│   │   │   ├── pagination/
│   │   │   │   ├── NavigationLinks.java
│   │   │   │   ├── PageRequest.java
│   │   │   │   ├── PageResponse.java
│   │   │   │   ├── PaginationInfo.java
│   │   │   │   └── PaginationUtil.java
│   │   │   └── response/
│   │   │       ├── ApiResponse.java
│   │   │       └── QueryInfo.java
│   │   ├── gateway/
│   │   │   └── filter/
│   │   │       └── CorrelationIdFilter.java
│   │   ├── health/
│   │   │   ├── DatabaseHealthCheck.java
│   │   │   └── ServiceHealthCheck.java
│   │   ├── maintenance/
│   │   │   ├── model/
│   │   │   │   ├── WorkOrder.java
│   │   │   │   └── WorkOrderId.java
│   │   │   ├── repository/
│   │   │   │   └── WorkOrderRepository.java
│   │   │   ├── resource/
│   │   │   │   └── WorkOrderResource.java
│   │   │   └── service/
│   │   │       └── WorkOrderService.java
│   │   ├── pairs/
│   │   │   ├── model/
│   │   │   │   ├── DisplayParamName.java
│   │   │   │   ├── Limit.java
│   │   │   │   ├── Pair.java
│   │   │   │   ├── PairData.java
│   │   │   │   ├── PairDataSet.java
│   │   │   │   ├── PairDatasetSummary.java
│   │   │   │   ├── PairDefinitionCotsDto.java
│   │   │   │   ├── PairDefinitionDto.java
│   │   │   │   ├── ParameterLimits.java
│   │   │   │   ├── ParameterTracking.java
│   │   │   │   ├── ReleasedPairInfo.java
│   │   │   │   ├── Threshold.java
│   │   │   │   └── Tracking.java
│   │   │   ├── repository/
│   │   │   │   ├── DisplayParamNameRepository.java
│   │   │   │   ├── LimitRepository.java
│   │   │   │   ├── PairDataRepository.java
│   │   │   │   ├── PairDataSetRepository.java
│   │   │   │   ├── PairRepository.java
│   │   │   │   ├── ParameterLimitsRepository.java
│   │   │   │   ├── ParameterTrackingRepository.java
│   │   │   │   ├── ReleasedPairInfoRepository.java
│   │   │   │   ├── ThresholdRepository.java
│   │   │   │   └── TrackingRepository.java
│   │   │   ├── resource/
│   │   │   │   └── PairResource.java
│   │   │   └── service/
│   │   │       └── PairService.java
│   │   ├── security/
│   │   │   └── apikey/
│   │   │       ├── annotation/
│   │   │       │   └── RequiresScope.java
│   │   │       ├── config/
│   │   │       │   └── ApiKeySecurityValidator.java
│   │   │       ├── dto/
│   │   │       │   ├── ApiClientResponse.java
│   │   │       │   ├── ApiKeyMetadataResponse.java
│   │   │       │   ├── ClaimInvitationCreatedResponse.java
│   │   │       │   ├── ClaimKeyRequest.java
│   │   │       │   ├── ClaimKeyResponse.java
│   │   │       │   ├── CreateApiClientRequest.java
│   │   │       │   ├── CreateClaimInvitationRequest.java
│   │   │       │   ├── DisableApiClientRequest.java
│   │   │       │   ├── RevokeApiKeyRequest.java
│   │   │       │   └── RotateApiKeyRequest.java
│   │   │       ├── filter/
│   │   │       │   └── ApiKeyAuthenticationFilter.java
│   │   │       ├── interceptor/
│   │   │       │   └── ScopeEnforcementInterceptor.java
│   │   │       ├── model/
│   │   │       │   ├── ApiClient.java
│   │   │       │   ├── ApiClientStatus.java
│   │   │       │   ├── ApiKey.java
│   │   │       │   ├── ApiKeyAuditEvent.java
│   │   │       │   ├── ApiKeyAuditEventType.java
│   │   │       │   ├── ApiKeyClaimInvitation.java
│   │   │       │   ├── ApiKeyScope.java
│   │   │       │   ├── ApiKeyStatus.java
│   │   │       │   ├── ClaimInvitationStatus.java
│   │   │       │   └── Environment.java
│   │   │       ├── repository/
│   │   │       │   ├── ApiClientRepository.java
│   │   │       │   ├── ApiKeyAuditRepository.java
│   │   │       │   ├── ApiKeyClaimInvitationRepository.java
│   │   │       │   ├── ApiKeyRepository.java
│   │   │       │   └── ApiKeyScopeRepository.java
│   │   │       ├── resource/
│   │   │       │   ├── ApiClientResource.java
│   │   │       │   ├── ApiKeyClaimResource.java
│   │   │       │   └── ApiKeyManagementResource.java
│   │   │       ├── scheduler/
│   │   │       │   └── ApiKeyMaintenanceScheduler.java
│   │   │       └── service/
│   │   │           ├── ApiClientService.java
│   │   │           ├── ApiKeyAuditService.java
│   │   │           ├── ApiKeyClaimService.java
│   │   │           ├── ApiKeyExpirationService.java
│   │   │           ├── ApiKeyGenerator.java
│   │   │           ├── ApiKeyHasher.java
│   │   │           ├── ApiKeyRotationService.java
│   │   │           ├── ApiKeyService.java
│   │   │           ├── ApiKeyValidator.java
│   │   │           └── ClaimCodeHasher.java
│   │   └── usage/
│   │       ├── model/
│   │       │   ├── UsageMeter.java
│   │       │   └── UsageMeterId.java
│   │       ├── repository/
│   │       │   └── UsageMeterRepository.java
│   │       ├── resource/
│   │       │   └── UsageMeterResource.java
│   │       └── service/
│   │           └── UsageMeterService.java
│   └── resources/
│       ├── application.properties
│       ├── import-dev.sql
│       └── deployment/
│           └── windows/
│               └── FMT-data-provider.ini
└── test/
    └── java/com/lmco/jsf/fmt/dataprovider/
        └── security/
            └── apikey/
                ├── ApiKeyAuthenticationTest.java
                └── ApiKeyScopeAndClaimTest.java
```

## Package Summary

### Domain Modules
- **aircraft** - Aircraft and operational status management
- **maintenance** - Work order and maintenance tracking
- **pairs** - Parameter pairs and telemetry data
- **usage** - Usage meter tracking

### Infrastructure Modules
- **common** - Shared utilities (pagination, filtering, error handling, correlation)
- **gateway** - Gateway filters and routing
- **health** - Health check endpoints
- **security** - API key authentication and authorization system

### Security Package Details
The `security.apikey` package implements a comprehensive API key management system:
- **10 DTOs** for request/response handling
- **10 Models** for data persistence
- **5 Repositories** for data access
- **10 Services** for business logic
- **3 Resources** for REST endpoints
- **1 Filter** for authentication
- **1 Interceptor** for authorization
- **1 Scheduler** for maintenance tasks
- **1 Annotation** for scope requirements
- **1 Config** for security validation

## File Statistics
- **Total Java Files:** 157
- **Main Source Files:** 155
- **Test Files:** 2
- **Configuration Files:** 2 (application.properties, import-dev.sql)
- **Deployment Files:** 1 (FMT-data-provider.ini)

## Architecture Pattern
The project follows a layered architecture:
1. **Resource Layer** (REST endpoints)
2. **Service Layer** (Business logic)
3. **Repository Layer** (Data access)
4. **Model Layer** (Domain entities)
5. **DTO Layer** (Data transfer objects)

Each domain module is self-contained with its own layers, promoting modularity and maintainability.
