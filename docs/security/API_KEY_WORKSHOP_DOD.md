# API Key Management Definition Of Done

## Purpose

This document defines feature-level acceptance criteria for the API-key management capability. It is organized for workshop review and separates functional, security, endpoint/API, and verification expectations.

## 1. API Client Registration

Functional DoD:

- Internal admins can create, list, view, enable, and disable API clients.
- Client records include enough ownership and contact metadata to support operational review.
- Disabled clients cannot authenticate through their issued API keys.

Security DoD:

- API keys cannot authorize client-management admin routes.
- Temporary admin access is limited to dev/test guard behavior and is not considered production auth.

Endpoint/API DoD:

- Admin JSON routes are available under `/api/v1/admin/api-clients`.
- Admin UI routes support client list, create, and detail workflows.

Verification DoD:

- Backend tests cover client creation and admin guard behavior.
- `mvn clean compile` succeeds.

## 2. Claim Invitation Provisioning

Functional DoD:

- Internal admins can create a claim invitation for an active API client.
- Invitations capture approved claim identifier, key name, environment, scopes, expiration, and approval reference.
- The raw claim code is shown once after creation.

Security DoD:

- Raw claim codes are never stored, logged, or audited.
- Stored claim-code values are BCrypt hashes only.
- Unsupported scopes are rejected.

Endpoint/API DoD:

- Admin JSON route creates invitations under `/api/v1/admin/api-keys/clients/{clientId}/invitations`.
- Admin UI route creates invitations under `/admin/ui/api-clients/{clientId}/claim-invitations`.

Verification DoD:

- Tests cover invitation creation, one-time claim-code response behavior, invalid scopes, and inactive client handling where applicable.

## 3. Client Claim Workflow

Functional DoD:

- Client-side users can submit approved claim identifier and claim code.
- Successful claim generates the API key only at completion time.
- The raw API key is shown once after successful claim.
- Claim codes cannot be reused.

Security DoD:

- Raw API keys are never stored, logged, or audited.
- Raw claim codes are not placed in URLs.
- Invalid claim attempts increment failed-attempt tracking and can lock the invitation.
- Expired, locked, revoked, and already claimed invitations cannot be claimed.

Endpoint/API DoD:

- JSON claim remains available at `POST /api/v1/api-keys/claim`.
- Claim UI supports enter, review, and complete steps under `/key-claim/ui`.

Verification DoD:

- Tests cover successful claim, duplicate claim rejection, invalid claim code, locked invitation behavior, and expired invitation behavior.

## 4. API-Key Authentication For Endpoints

Functional DoD:

- Client applications can call protected endpoints with `X-API-Key`.
- Valid active keys authenticate and expose key, client, environment, and scope information to authorization logic.
- Key `lastUsedAt` or equivalent usage metadata can be updated when supported.

Security DoD:

- Missing, malformed, unknown, expired, revoked, rotated, inactive-client, or hash-invalid keys are rejected.
- Only key prefix and HMAC-SHA-256 hash are stored.
- Hash comparison avoids exposing raw key material.

Endpoint/API DoD:

- Normal API requests use `X-API-Key: <full-api-key>`.
- Admin endpoints reject API-key authorization with `403`.

Verification DoD:

- Tests cover successful authentication, missing key, invalid key, revoked key, expired key, disabled client, and admin route rejection.

## 5. Scope Authorization With `@RequiresScope`

Functional DoD:

- Protected endpoints declare required access with `@RequiresScope`.
- Authenticated keys with the required scope can access the endpoint.
- Authenticated keys without the required scope are rejected.

Security DoD:

- Valid API key but missing required scope returns `403`.
- Missing or invalid API key returns `401`.
- Scopes are assigned only through approved invitation or rotation workflows.

Endpoint/API DoD:

- Supported scopes include `read:aircraft`, `read:maintenance`, `read:pairs`, and `read:usage`.
- At least one endpoint path proves scope enforcement end to end.

Verification DoD:

- Tests cover allowed scope access and missing scope rejection.
- Future endpoint-scope expansion should include endpoint-specific tests.

## 6. Key Lifecycle Management

Functional DoD:

- Admins can view issued key metadata without seeing raw API keys.
- Admins can revoke active keys.
- Rotation creates a replacement claim invitation rather than directly returning a new raw key.
- Expired keys and invitations are not usable.

Security DoD:

- Revoked, expired, and rotated keys do not authenticate.
- Replacement keys are generated only after the replacement invitation is claimed.
- Raw key recovery is not supported.

Endpoint/API DoD:

- Admin JSON routes support key metadata lookup, revoke, and rotate workflows.
- Admin UI supports key metadata and revoke workflow.

Verification DoD:

- Tests cover revocation and post-revocation authentication failure.
- Tests cover rotation initiation or replacement invitation behavior where implemented.
- Expiration behavior is covered by service or endpoint tests.

## 7. Audit And Security Observability

Functional DoD:

- Security-sensitive lifecycle events create audit records.
- Audit events identify relevant client, key, actor, event type, and safe details.

Security DoD:

- Audit details do not include raw API keys, raw claim codes, HMAC hashes, BCrypt hashes, or submitted secrets.
- Failed authentication and failed claim events are recorded without leaking credentials.

Endpoint/API DoD:

- Audit is integrated into client, invitation, claim, authentication, revoke, and rotation workflows.

Verification DoD:

- Tests or code review confirm audit events are written for key workflows.
- Tests or code review confirm raw secrets are excluded from audit details.

## 8. Admin And Claim Qute UI

Functional DoD:

- Admin UI supports listing clients, creating clients, viewing client details, creating claim invitations, viewing key metadata, listing pending invitations where available, and revoking keys.
- Claim UI supports entering claim information, reviewing approved access, completing the claim, and showing the full API key once.

Security DoD:

- Admin UI routes use the temporary dev/test admin guard and clearly are not final production SSO.
- Claim UI routes are public only for claim completion and do not allow scope changes or client creation.
- Raw claim codes and raw API keys are displayed only in their allowed one-time screens.
- Secrets are not placed in URLs or stored in session.

Endpoint/API DoD:

- Admin UI routes are under `/admin/ui/...`.
- Claim UI routes are under `/key-claim/ui...`.
- Qute templates call service-backed UI resources, not repositories directly.

Verification DoD:

- `mvn clean compile` validates Qute templates and Java integration.
- API-key backend tests remain green after UI changes.

## Deferred Or Out Of Scope For Current DoD

- Production admin SSO/Windows AD integration.
- Email, SMTP, magic links, verification links, reset links, or claim-link delivery.
- Self-service scope requests by clients.
- Raw API-key retrieval after claim.
- Raw claim-code retrieval after invitation creation.
- Broad scope rollout to every domain endpoint beyond the protected reference coverage.
