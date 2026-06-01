# API Key Client Claim And Endpoint Access Lifecycle

This graphic shows how a client gets the API key they will use to access protected FMT REST endpoints. It separates internal approval, one-time claim, runtime endpoint access, and lifecycle controls.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"fontSize": "18px", "fontFamily": "Arial"}}}%%
flowchart TD
    start([Client needs API access])

    subgraph approval["1. Internal approval"]
        admin["Register API client"]
        invite["Create claim invitation<br/>identifier, env, scopes, expiration"]
        code["Generate claim code<br/>shown once to admin"]
        deliver["Deliver code out-of-band"]
    end

    subgraph claim["2. Client claim"]
        claimStart["Open /key-claim/ui"]
        submit["Enter identifier<br/>and claim code"]
        review["Review approved access"]
        complete["Confirm claim"]
        key["Generate API key<br/>shown once to client"]
        store["Store key in secret manager"]
    end

    subgraph runtime["3. Endpoint access"]
        apiCall["Call protected endpoint<br/>with X-API-Key"]
        auth{"API key valid?"}
        scope{"Required scope?"}
        endpoint["Endpoint returns data"]
    end

    subgraph outcomes["4. Rejection and lifecycle"]
        deny401["401<br/>invalid, expired, revoked,<br/>or inactive client"]
        deny403["403<br/>missing scope<br/>or admin route"]
        lifecycle["Admin controls<br/>view, revoke, rotate, expire, audit"]
    end

    start --> admin --> invite --> code --> deliver
    deliver --> claimStart --> submit --> review --> complete --> key --> store
    store --> apiCall --> auth
    auth -->|yes| scope
    auth -->|no| deny401
    scope -->|yes| endpoint
    scope -->|no| deny403
    endpoint --> lifecycle
    lifecycle -->|rotate| invite
    lifecycle -->|revoke / expire| deny401

    classDef actor fill:#e8f1ff,stroke:#4b75bd,color:#10223f,stroke-width:2px;
    classDef secret fill:#fff4d6,stroke:#b7791f,color:#3b2600,stroke-width:2px;
    classDef success fill:#e7f7ef,stroke:#2f855a,color:#102a1d,stroke-width:2px;
    classDef deny fill:#fde8e8,stroke:#c53030,color:#3b0d0d,stroke-width:2px;
    classDef neutral fill:#eef2f7,stroke:#64748b,color:#172033,stroke-width:2px;

    class admin,invite,deliver,claimStart,submit,review,complete,store,apiCall,auth,scope actor;
    class code,key secret;
    class endpoint success;
    class deny401,deny403 deny;
    class lifecycle neutral;
```

## Lifecycle Notes

- The raw claim code is shown once to the internal admin and is never stored, logged, or audited.
- The raw API key is generated only after successful claim completion and is shown once to the client-side admin.
- The client application uses the full API key in the `X-API-Key` header for normal endpoint access.
- API-key validation uses the stored key prefix for lookup and HMAC-SHA-256 for verification.
- Endpoint authorization uses scopes such as `read:aircraft`, `read:maintenance`, `read:pairs`, and `read:usage`.
- API keys never authorize admin routes; admin access remains separate from endpoint access.
- If the key is lost, revoked, expired, or rotated, the replacement path is a new claim invitation.
