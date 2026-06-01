# API Key Client Claim And Endpoint Access Lifecycle V2

This version is a compact middle-ground diagram for workshop slides or handouts. It keeps the lifecycle readable without making the flow too wide or too tall.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"fontSize": "16px", "fontFamily": "Arial"}}}%%
flowchart TB
    start([Client needs API access])

    subgraph phase1["1. Approve"]
        direction LR
        client["Register client"]
        invite["Create invitation"]
        code["Show claim code once"]
    end

    subgraph phase2["2. Claim"]
        direction LR
        enter["Enter identifier + code"]
        review["Review approved access"]
        key["Show API key once"]
    end

    subgraph phase3["3. Use"]
        direction LR
        store["Store key"]
        apiCall["Call endpoint<br/>X-API-Key"]
        validate{"Valid key<br/>and client?"}
        authorize{"Required<br/>scope?"}
    end

    subgraph phase4["4. Result"]
        direction LR
        success["Endpoint data"]
        reject401["401<br/>invalid key"]
        reject403["403<br/>missing scope"]
        manage["Admin lifecycle<br/>revoke / rotate / expire"]
    end

    start --> client --> invite --> code
    code --> enter --> review --> key
    key --> store --> apiCall --> validate
    validate -->|yes| authorize
    validate -->|no| reject401
    authorize -->|yes| success
    authorize -->|no| reject403
    success --> manage
    manage -->|rotate| invite
    manage -->|revoke / expire| reject401

    classDef normal fill:#e8f1ff,stroke:#4b75bd,color:#10223f,stroke-width:1.5px;
    classDef secret fill:#fff4d6,stroke:#b7791f,color:#3b2600,stroke-width:1.5px;
    classDef success fill:#e7f7ef,stroke:#2f855a,color:#102a1d,stroke-width:1.5px;
    classDef reject fill:#fde8e8,stroke:#c53030,color:#3b0d0d,stroke-width:1.5px;
    classDef neutral fill:#eef2f7,stroke:#64748b,color:#172033,stroke-width:1.5px;

    class client,invite,enter,review,store,apiCall,validate,authorize normal;
    class code,key secret;
    class success success;
    class reject401,reject403 reject;
    class manage neutral;
```

## Reading The Diagram

- The admin approves access first; the client does not self-select scopes.
- The claim code and API key are both one-time display secrets.
- The client application uses the issued API key in `X-API-Key`.
- Runtime access has two checks: key validity first, then endpoint scope.
- Revocation, expiration, or rotation sends the client back through a replacement invitation path.
