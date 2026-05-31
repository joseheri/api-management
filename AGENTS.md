# AGENTS.md

This is a Quarkus Java project.

Architecture:

- The project follows layered architecture:
  - resource
  - service
  - repository
  - model
  - dto
- Domain modules follow:
  com.lmco.jsf.fmt.dataprovider.<domain>.resource
  com.lmco.jsf.fmt.dataprovider.<domain>.service
  com.lmco.jsf.fmt.dataprovider.<domain>.repository
  com.lmco.jsf.fmt.dataprovider.<domain>.model
- The API key feature must follow the same structure under:
  com.lmco.jsf.fmt.dataprovider.security.apikey

Common API:

- Reuse existing common.response.ApiResponse where appropriate.
- Reuse existing common.error.ErrorResponse and GlobalExceptionMapper.
- Reuse existing common.pagination utilities for list endpoints.
- Reuse correlation ID conventions if available.
- Do not invent a second response or error format.

Persistence:

- This project uses EntityManager repositories, not Panache.
- Do not use Panache.
- Do not change existing production/native SQL queries.
- For the API key feature, table names are new and can be defined by this feature.
- Once table/column names are defined in the contract, do not rename them.

Security:

- Raw API keys must never be stored, logged, audited, or returned after initial claim.
- Raw claim codes must never be stored, logged, audited, or returned after invitation creation.
- Claim codes use BCrypt.
- API keys use HMAC-SHA-256 with server-side secret.
- API key format:
  ak*{env}*{publicId}.{secret}
- Stored keyPrefix:
  ak_prod_N7F3K92A
- API keys authenticate normal API requests only.
- API keys must never authorize admin endpoints.
- Admin auth will later be SSO / Windows AD.
- Until then, use a dev/test-only admin guard. Do not leave admin routes open in prod.

Testing:

- Run `mvn clean compile` after changes.
- Run `mvn test -Dtest=ApiKey*` for API key work.
- Do not continue to new features if compile fails.
- Fix only the failing area.

Rules:

1. Make small changes.
2. Do not refactor unrelated code.
3. Do not rename public endpoints unless explicitly asked.
4. Do not rename table/column names once the schema contract exists.
5. Do not modify aircraft/maintenance/pairs/usage except to add approved `@RequiresScope` annotations.
6. Stop and report after each phase.
