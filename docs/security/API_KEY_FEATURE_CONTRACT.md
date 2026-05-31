# API Key Feature Contract

## Package

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

## API key format

Full key:

ak*{env}*{publicId}.{secret}

Examples:

ak_dev_N7F3K92A.<secret>
ak_test_N7F3K92A.<secret>
ak_stage_N7F3K92A.<secret>
ak_prod_N7F3K92A.<secret>

Stored keyPrefix:

ak_prod_N7F3K92A

Stored keyHash:

HMAC-SHA-256(full raw API key)

## Claim code

Format:

K7Q9-X2MP-84RA-LT6Z-Q8MN

Stored value:

BCrypt hash only

## Header

Normal API requests use:

X-API-Key: <full-api-key>

## Admin access

Admin endpoints are under:

/api/v1/admin/...

API keys must receive 403 if used against admin endpoints.

Final admin login will be SSO / Windows AD later.

For dev/test only, use:

X-Dev-Admin-User: <username>

This must not work in prod.

## Tables

Use these API-key feature tables:

api_clients
api_keys
api_key_scopes
api_key_claim_invitations
api_key_audit_events

## Scopes

read:aircraft
read:maintenance
read:pairs
read:usage

## Admin JSON endpoints

POST /api/v1/admin/api-clients
GET /api/v1/admin/api-clients
GET /api/v1/admin/api-clients/{clientId}
POST /api/v1/admin/api-clients/{clientId}/disable
POST /api/v1/admin/api-clients/{clientId}/enable

POST /api/v1/admin/api-keys/clients/{clientId}/invitations
GET /api/v1/admin/api-keys/clients/{clientId}
GET /api/v1/admin/api-keys/{keyId}
POST /api/v1/admin/api-keys/{keyId}/revoke
POST /api/v1/admin/api-keys/{keyId}/rotate

## Claim endpoint

POST /api/v1/api-keys/claim

## Qute UI routes

Admin UI:

GET /admin/ui/api-clients
GET /admin/ui/api-clients/new
POST /admin/ui/api-clients
GET /admin/ui/api-clients/{clientId}
GET /admin/ui/api-clients/{clientId}/claim-invitations/new
POST /admin/ui/api-clients/{clientId}/claim-invitations
GET /admin/ui/api-keys/{keyId}
GET /admin/ui/api-keys/{keyId}/revoke
POST /admin/ui/api-keys/{keyId}/revoke

Claim UI:

GET /key-claim/ui
POST /key-claim/ui/complete
