# API Key Management Technical Decisions

## Purpose

This document captures the major technical decisions behind API-key management, why each decision was made, and what benefit or tradeoff it creates. It is intended for a mixed product, security, and engineering workshop audience.

## Decisions

### Use `X-API-Key` For Runtime Authentication

Decision: Client applications authenticate normal API requests with:

```text
X-API-Key: <full-api-key>
```

Benefit: The header is simple for machine-to-machine clients, easy to document, and independent of browser login or user identity flows.

Tradeoff: API keys are bearer credentials. Client applications must store them in a secret manager or equivalent secure configuration and must rotate them if exposed.

### Use Structured API Key Format

Decision: Full API keys use:

```text
ak*{env}*{publicId}.{secret}
```

Example:

```text
ak_prod_N7F3K92A.<secret>
```

Benefit: The prefix identifies the environment and public lookup id without exposing the secret. It supports operational troubleshooting and efficient lookup while keeping the secret portion separate.

Tradeoff: The prefix is not secret, but it must still be treated as metadata and not as proof of authentication.

### Store Prefix Plus HMAC Hash, Not Raw API Keys

Decision: Store only the non-secret key prefix and an HMAC-SHA-256 hash of the full raw API key using the server-side secret.

Benefit: A database read does not reveal usable API keys. Runtime validation can recompute the HMAC and compare it to the stored hash.

Tradeoff: Lost API keys cannot be recovered. The correct operational response is revoke or rotate through a new claim invitation.

### Use BCrypt For Claim Codes

Decision: Store claim-code hashes with BCrypt.

Benefit: Claim codes are one-time human-entered secrets. BCrypt slows offline guessing if the claim-invitation table is exposed.

Tradeoff: Claim verification is intentionally slower than a simple hash. That is acceptable because claim completion is a low-volume administrative workflow, not high-throughput runtime authentication.

### Show Raw Secrets Once

Decision: Raw claim codes are shown once after invitation creation. Raw API keys are shown once after successful claim. Neither is stored, logged, audited, or returned again.

Benefit: The system minimizes long-term secret exposure while still allowing an admin to deliver a claim code and a client to capture the issued API key.

Tradeoff: Users must copy the secret at the right time. If it is lost, the recovery path is replacement, not retrieval.

### Use Claim Invitations Instead Of Self-Service Key Creation

Decision: Clients cannot create keys or request new scopes directly. An internal admin first creates a claim invitation with approved access.

Benefit: Access is approved before a credential exists. The claim user can only accept the approved environment, scopes, and expiration.

Tradeoff: Onboarding requires an internal approval and out-of-band claim-code delivery process.

### Keep Admin Auth Separate From API Keys

Decision: API keys must never authorize admin endpoints. Temporary admin access uses a dev/test-only guard with `X-Dev-Admin-User`; final production admin authentication is deferred to SSO/Windows AD.

Benefit: Machine credentials cannot be used to manage other credentials. This preserves a clean separation between runtime API access and administrative authority.

Tradeoff: The current guard is not production authentication and must be replaced before production admin use.

### Use EntityManager Repositories, Not Panache

Decision: API-key persistence follows the existing project repository style with EntityManager-backed repositories.

Benefit: The feature stays consistent with the project architecture and avoids introducing a second persistence pattern.

Tradeoff: Repository code is more explicit than Panache-style active record helpers.

### Use Layered Quarkus Components

Decision: Keep the API-key feature under `security.apikey` with model, repository, service, resource, filter, interceptor, DTO, config, scheduler, and UI packages.

Benefit: The code follows the existing resource -> service -> repository -> model architecture and keeps security concerns isolated from aircraft, maintenance, pairs, and usage logic.

Tradeoff: Some workflows require several classes, but the separation makes security review and testing clearer.

### Use Qute For Server-Rendered UI

Decision: Build admin and claim screens with Quarkus Qute templates and static CSS, without React, Vite, or an external frontend build.

Benefit: The UI remains simple, deploys with the backend, and is adequate for internal admin and claim workflows.

Tradeoff: Rich client-side interactions are limited by design. The current goal is functional workflow support, not a standalone frontend application.

### Enforce Scopes With `@RequiresScope`

Decision: Protected endpoints declare required access with `@RequiresScope`, and an interceptor compares the required scope to the authenticated key scopes.

Benefit: Authorization is visible at the endpoint boundary and can be added incrementally to endpoint groups.

Tradeoff: Endpoint owners must choose and apply the correct scope annotations as coverage expands.

## Deferred Decisions

- Production admin authentication must be implemented with SSO/Windows AD or the approved enterprise identity provider.
- Claim-code delivery remains an external out-of-band process; this feature does not send email, SMTP, magic links, reset links, or verification messages.
- Production audit reporting, dashboards, and retention policy can be designed after the event model is accepted.
- Operational defaults for rotation cadence, expiration windows, and emergency revocation process should be finalized before production rollout.
