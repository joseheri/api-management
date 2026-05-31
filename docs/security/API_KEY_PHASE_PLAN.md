# API Key Management Phase Plan

Use these phases for implementation. Do not combine phases unless explicitly approved.

## Phase 0 - Planning only

Inspect the project and produce an implementation plan. Do not modify files.

## Phase 0.5 - Workspace normalization

Fix or report workspace setup issues only. Do not implement API-key management.

## Phase 1 - Backend skeleton only

Create package structure and class skeletons only. No business logic, no repository queries, no UI.

Allowed:

- empty/skeletal classes under security/apikey
- compile-safe method stubs if needed

Not allowed:

- full service workflow
- real repository SQL/JPQL
- filters enforcing auth
- Qute UI
- tests beyond compile stubs

## Phase 2 - Models, DTOs, and config

Implement model/entity fields, enums, DTO fields, and API-key-specific config properties.

Allowed:

- @Entity mappings
- @Table, @Column, @JoinColumn
- enums
- request/response DTOs
- config property classes

Not allowed:

- repository implementation
- service workflow
- REST endpoint logic
- auth filters/interceptors
- Qute UI

## Phase 3 - Repositories

Implement EntityManager repositories only.

Allowed:

- ApiClientRepository
- ApiKeyRepository
- ApiKeyClaimInvitationRepository
- ApiKeyScopeRepository
- ApiKeyAuditRepository

Not allowed:

- service workflow
- resources
- filters/interceptors
- UI

## Phase 4 - Services

Implement business workflow services.

Allowed:

- create API client
- create claim invitation
- claim API key
- validate API key
- revoke key
- rotate key
- audit events
- hashing/generation logic

Not allowed:

- REST resources
- filters/interceptors
- Qute UI

## Phase 5 - Filters and scope enforcement

Implement runtime authentication and authorization.

Allowed:

- ApiKeyAuthenticationFilter
- @RequiresScope usage support
- ScopeEnforcementInterceptor
- dev/test-only admin guard

Not allowed:

- Qute UI
- broad changes to existing domain endpoints

## Phase 6 - JSON resources

Implement admin and claim JSON REST endpoints.

Allowed:

- ApiClientResource
- ApiKeyManagementResource
- ApiKeyClaimResource
- use services
- use project response/error conventions

Not allowed:

- Qute UI
- repository calls directly from resources

## Phase 7 - Protected reference endpoint

Create or update one endpoint to prove scope enforcement.

Preferred:

- create a self-contained reference endpoint if real aircraft classes are unavailable
- or protect AircraftResource if the real project has all dependencies

Not allowed:

- modifying sensitive aircraft repository SQL

## Phase 8 - Backend tests

Add backend tests for API-key management.

Allowed:

- QuarkusTest
- RestAssured
- claim workflow tests
- auth/scope tests
- admin guard tests

Not allowed:

- Qute UI

## Phase 9 - Qute UI

Add Qute UI after backend tests pass.

Allowed:

- admin UI
- claim UI
- templates
- UI resources calling services

Not allowed:

- changing backend behavior unless required by UI and approved
