# API Key Management Approach Summary

## Brief Summary

The API key management approach we have built is a strong, practical solution for giving approved client applications controlled access to FMT REST endpoints. It fits the current Quarkus architecture, keeps credential handling intentionally conservative, and gives the team a clear path from development validation to production hardening.

The design separates three important concerns: internal approval, client-side key claiming, and runtime endpoint enforcement. Internal admins approve clients and scopes before any usable API key exists. Client-side admins claim only the access that was already approved. Runtime requests are authenticated with an API key and authorized by explicit endpoint scopes. This gives us a clean, auditable model that is viable now and extensible later.

## Why This Is A Viable Solution

This approach works well because it solves the real operational problem without overbuilding the first version. Client applications need a machine-to-machine credential for API access, but the system also needs to avoid unmanaged shared secrets, overly broad access, and weak traceability. API-key management gives us that middle path: simple for clients to use, controlled by admins, and enforceable at the endpoint layer.

The claim-invitation workflow is one of the strongest parts of the design. Instead of letting clients self-create keys or request arbitrary scopes, an internal admin creates a pre-approved invitation. That invitation defines the client, claim identifier, environment, scopes, expiration, and approval reference. The client can only claim what was approved. This keeps authority with the internal team while still giving the client a smooth onboarding path.

## Good Design Decisions

- API keys are used only for normal endpoint access, not admin access.
- Admin workflows stay separate from client runtime credentials.
- API keys use a structured format with a non-secret prefix and secret value.
- The system stores only the key prefix and HMAC-SHA-256 hash, never the raw API key.
- Claim codes are stored only as BCrypt hashes.
- Raw claim codes are shown once after invitation creation.
- Raw API keys are shown once after successful claim.
- Lost keys are replaced through revoke or rotate workflows, not recovered from storage.
- Endpoint authorization is explicit through `@RequiresScope`.
- The implementation follows the project’s layered architecture and EntityManager repository pattern.
- The UI uses simple Qute server-rendered pages rather than adding a separate frontend build.

These choices are intentionally conservative. They reduce secret exposure, make access review easier, and keep the feature understandable for future maintainers.

## Strong Use Cases And Workflows

The solution supports the core lifecycle needed for API consumers:

- Register an API client that represents a consuming application or organization.
- Approve a claim invitation with specific scopes and expiration.
- Manually deliver a one-time claim code through an approved out-of-band process.
- Let the client claim the API key through JSON or the Qute claim UI.
- Require the client application to send `X-API-Key` on protected endpoint calls.
- Enforce endpoint-level scopes such as `read:aircraft`, `read:maintenance`, `read:pairs`, and `read:usage`.
- Revoke or rotate keys when access changes or a credential is lost.
- Audit key lifecycle and authentication events without storing secrets.

This is especially well suited for partner systems, internal service integrations, controlled test clients, and any application that needs scoped read access without direct database connectivity.

## Security Merits

The security model is strong for this phase because it limits what can be exposed, reused, or escalated.

The raw API key is never stored, so a database compromise does not directly expose usable credentials. Claim codes are also never stored raw, and BCrypt makes offline guessing harder. API-key authentication uses HMAC verification with a server-side secret, and authorization is enforced separately through endpoint scopes.

The design also prevents a common failure mode: using one machine credential as a backdoor into administration. API keys cannot authorize admin endpoints. Admin authentication is intentionally marked as temporary dev/test guard behavior until SSO/Windows AD is implemented.

Finally, the workflow produces useful audit events without leaking raw claim codes, raw API keys, HMAC hashes, or BCrypt hashes. That gives security and operations teams a review trail without turning the audit log into another secret store.

## Why This Is The Right Approach For Now

This solution is a very good fit for the project’s current maturity. It delivers the main security and operational benefits of API-key management while avoiding unnecessary complexity such as SMTP delivery, magic links, a separate frontend stack, or premature production SSO work.

It also creates a clean runway for future hardening. Production SSO can replace the dev admin guard. More endpoints can adopt `@RequiresScope`. Audit views can be expanded. Rotation and expiration policy can be refined. None of those future steps require changing the core model.

In short, this is a practical, secure, and maintainable foundation for endpoint API access. It gives clients a clear way to connect, gives admins control over what is approved, and gives the runtime a consistent mechanism to authenticate and enforce access.
