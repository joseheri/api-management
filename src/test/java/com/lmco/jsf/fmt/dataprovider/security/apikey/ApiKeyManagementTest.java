package com.lmco.jsf.fmt.dataprovider.security.apikey;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyScope;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ClaimInvitationStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ApiKeyManagementTest {

    private static final String ADMIN_HEADER = "X-Dev-Admin-User";
    private static final String API_KEY_HEADER = "X-API-Key";

    @Inject
    EntityManager entityManager;

    @Inject
    UserTransaction userTransaction;

    @BeforeEach
    void cleanApiKeyData() {
        inTransaction(() -> {
            removeAll(ApiKeyScope.class);
            removeAll(ApiKeyAuditEvent.class);
            removeAll(ApiKeyClaimInvitation.class);
            removeAll(ApiKey.class);
            removeAll(ApiClient.class);
        });
    }

    @Test
    void adminGuardRequiresDevAdminHeaderAndRejectsApiKeysOnAdminEndpoints() {
        given()
                .contentType(ContentType.JSON)
                .body(clientRequest("guard-admin@example.test"))
                .when()
                .post("/api/v1/admin/api-clients")
                .then()
                .statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .header(API_KEY_HEADER, "ak_test_ABCDEFGH.secret")
                .body(clientRequest("guard-apikey@example.test"))
                .when()
                .post("/api/v1/admin/api-clients")
                .then()
                .statusCode(403);

        given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "phase8-admin")
                .body(clientRequest("guard-success@example.test"))
                .when()
                .post("/api/v1/admin/api-clients")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("contactEmail", equalTo("guard-success@example.test"));
    }

    @Test
    void adminCanCreateListAndGetApiClients() {
        long clientId = createClient("workflow-client@example.test");

        given()
                .header(ADMIN_HEADER, "phase8-admin")
                .when()
                .get("/api/v1/admin/api-clients")
                .then()
                .statusCode(200)
                .body("data.id", hasItem((int) clientId))
                .body("data.contactEmail", hasItem("workflow-client@example.test"));

        given()
                .header(ADMIN_HEADER, "phase8-admin")
                .when()
                .get("/api/v1/admin/api-clients/{clientId}", clientId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) clientId))
                .body("clientName", equalTo("Phase 8 Client"))
                .body("contactEmail", equalTo("workflow-client@example.test"));
    }

    @Test
    void claimInvitationReturnsCodeOnceAndPersistsOnlyHashAndScopes() {
        long clientId = createClient("invitation-client@example.test");

        Response response = createInvitation(clientId, "claim-invite@example.test", "TEST",
                List.of("read:aircraft", "read:usage"), futureTime());

        response.then()
                .statusCode(201)
                .body("invitationId", notNullValue())
                .body("clientId", equalTo((int) clientId))
                .body("claimCode", notNullValue())
                .body("scopes", containsInAnyOrder("read:aircraft", "read:usage"));

        long invitationId = response.jsonPath().getLong("invitationId");
        String rawClaimCode = response.jsonPath().getString("claimCode");

        ApiKeyClaimInvitation invitation = find(ApiKeyClaimInvitation.class, invitationId);
        assertNotNull(invitation);
        assertNotNull(invitation.getClaimCodeHash());
        assertNotEquals(rawClaimCode, invitation.getClaimCodeHash());
        assertFalse(invitation.getClaimCodeHash().contains(rawClaimCode));
        assertEquals(ClaimInvitationStatus.PENDING, invitation.getStatus());
        List<String> persistedScopes = inTransaction(() -> entityManager
                .createQuery(
                        "select s from ApiKeyClaimInvitation i join i.scopes s where i.id = :invitationId order by s",
                        String.class)
                .setParameter("invitationId", invitationId)
                .getResultList());
        assertEquals(List.of("read:aircraft", "read:usage"), persistedScopes);
    }

    @Test
    void claimCreatesApiKeyScopesAndPreventsClaimCodeReuse() {
        long clientId = createClient("claim-client@example.test");
        Response invitationResponse = createInvitation(clientId, "claim-success@example.test", "TEST",
                List.of("read:aircraft", "read:maintenance"), futureTime());
        long invitationId = invitationResponse.jsonPath().getLong("invitationId");
        String claimCode = invitationResponse.jsonPath().getString("claimCode");

        Response claimResponse = claim("claim-success@example.test", claimCode);

        claimResponse.then()
                .statusCode(200)
                .body("keyId", notNullValue())
                .body("clientId", equalTo((int) clientId))
                .body("apiKey", notNullValue())
                .body("keyPrefix", notNullValue())
                .body("scopes", containsInAnyOrder("read:aircraft", "read:maintenance"));

        long keyId = claimResponse.jsonPath().getLong("keyId");
        String rawApiKey = claimResponse.jsonPath().getString("apiKey");
        String keyPrefix = claimResponse.jsonPath().getString("keyPrefix");

        ApiKey key = find(ApiKey.class, keyId);
        assertNotNull(key);
        assertEquals(keyPrefix, key.getKeyPrefix());
        assertNotNull(key.getKeyHash());
        assertNotEquals(rawApiKey, key.getKeyHash());
        assertFalse(key.getKeyHash().contains(rawApiKey));
        assertEquals(ApiKeyStatus.ACTIVE, key.getStatus());

        List<String> scopes = inTransaction(() -> entityManager
                .createQuery("select s.scope from ApiKeyScope s where s.apiKey.id = :keyId order by s.scope", String.class)
                .setParameter("keyId", keyId)
                .getResultList());
        assertEquals(List.of("read:aircraft", "read:maintenance"), scopes);

        ApiKeyClaimInvitation invitation = find(ApiKeyClaimInvitation.class, invitationId);
        assertEquals(ClaimInvitationStatus.CLAIMED, invitation.getStatus());

        claim("claim-success@example.test", claimCode)
                .then()
                .statusCode(409);
    }

    @Test
    void failedClaimsIncrementAttemptsLockInvitationAndRejectExpiredInvitations() {
        long lockedClientId = createClient("locked-client@example.test");
        Response lockedInvitationResponse = createInvitation(lockedClientId, "locked-claim@example.test", "TEST",
                List.of("read:aircraft"), futureTime());
        long lockedInvitationId = lockedInvitationResponse.jsonPath().getLong("invitationId");
        String validClaimCode = lockedInvitationResponse.jsonPath().getString("claimCode");

        for (int attempt = 1; attempt <= 5; attempt++) {
            claim("locked-claim@example.test", "WRONG-CODE-" + attempt)
                    .then()
                    .statusCode(401);
        }

        ApiKeyClaimInvitation lockedInvitation = find(ApiKeyClaimInvitation.class, lockedInvitationId);
        assertEquals(5, lockedInvitation.getFailedAttemptCount());
        assertEquals(ClaimInvitationStatus.LOCKED, lockedInvitation.getStatus());
        assertNotNull(lockedInvitation.getLockedAt());

        claim("locked-claim@example.test", validClaimCode)
                .then()
                .statusCode(403);

        long expiredClientId = createClient("expired-client@example.test");
        Response expiredInvitationResponse = createInvitation(expiredClientId, "expired-claim@example.test", "TEST",
                List.of("read:aircraft"), futureTime());
        long expiredInvitationId = expiredInvitationResponse.jsonPath().getLong("invitationId");
        String expiredClaimCode = expiredInvitationResponse.jsonPath().getString("claimCode");

        inTransaction(() -> {
            ApiKeyClaimInvitation invitation = entityManager.find(ApiKeyClaimInvitation.class, expiredInvitationId);
            invitation.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        });

        claim("expired-claim@example.test", expiredClaimCode)
                .then()
                .statusCode(410);

        ApiKeyClaimInvitation expiredInvitation = find(ApiKeyClaimInvitation.class, expiredInvitationId);
        assertEquals(ClaimInvitationStatus.EXPIRED, expiredInvitation.getStatus());
    }

    @Test
    void runtimeApiKeyAuthenticationEnforcesMissingInvalidScopeRevokedAndExpiredCases() {
        given()
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(401);

        given()
                .header(API_KEY_HEADER, "ak_test_UNKNOWN1.not-secret")
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(401);

        ClaimedKey readAircraftKey = createClaimedKey("runtime-read@example.test", List.of("read:aircraft"));
        given()
                .header(API_KEY_HEADER, readAircraftKey.rawApiKey())
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));

        ClaimedKey readUsageKey = createClaimedKey("runtime-usage@example.test", List.of("read:usage"));
        given()
                .header(API_KEY_HEADER, readUsageKey.rawApiKey())
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(403);

        given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "phase8-admin")
                .body(Map.of("reason", "phase 8 test revoke"))
                .when()
                .post("/api/v1/admin/api-keys/{keyId}/revoke", readAircraftKey.keyId())
                .then()
                .statusCode(200)
                .body("status", equalTo("REVOKED"));

        given()
                .header(API_KEY_HEADER, readAircraftKey.rawApiKey())
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(401);

        ClaimedKey expiredKey = createClaimedKey("runtime-expired@example.test", List.of("read:aircraft"));
        inTransaction(() -> {
            ApiKey apiKey = entityManager.find(ApiKey.class, expiredKey.keyId());
            apiKey.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        });

        given()
                .header(API_KEY_HEADER, expiredKey.rawApiKey())
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(401);
    }

    @Test
    void validApiKeyCannotAccessAdminEndpoints() {
        ClaimedKey claimedKey = createClaimedKey("admin-denied@example.test", List.of("read:aircraft"));

        given()
                .header(API_KEY_HEADER, claimedKey.rawApiKey())
                .when()
                .get("/api/v1/admin/api-clients")
                .then()
                .statusCode(403);
    }

    @Test
    void metadataResponsesDoNotExposeSecretsOrHashes() {
        ClaimedKey claimedKey = createClaimedKey("metadata-safe@example.test", List.of("read:aircraft"));

        Response listKeysResponse = given()
                .header(ADMIN_HEADER, "phase8-admin")
                .when()
                .get("/api/v1/admin/api-keys/clients/{clientId}", claimedKey.clientId());
        listKeysResponse.then()
                .statusCode(200)
                .body("data.id", hasItem(Math.toIntExact(claimedKey.keyId())));
        assertMetadataDoesNotExposeSecrets(listKeysResponse.asString(), claimedKey.rawApiKey());

        Response getKeyResponse = given()
                .header(ADMIN_HEADER, "phase8-admin")
                .when()
                .get("/api/v1/admin/api-keys/{keyId}", claimedKey.keyId());
        getKeyResponse.then()
                .statusCode(200)
                .body("id", equalTo(Math.toIntExact(claimedKey.keyId())))
                .body("keyPrefix", equalTo(claimedKey.keyPrefix()));
        assertMetadataDoesNotExposeSecrets(getKeyResponse.asString(), claimedKey.rawApiKey());

        Response listClientsResponse = given()
                .header(ADMIN_HEADER, "phase8-admin")
                .when()
                .get("/api/v1/admin/api-clients");
        listClientsResponse.then().statusCode(200);
        assertMetadataDoesNotExposeSecrets(listClientsResponse.asString(), claimedKey.rawApiKey());

        ApiKey key = find(ApiKey.class, claimedKey.keyId());
        assertNotNull(key.getKeyHash());
        assertFalse(listKeysResponse.asString().contains(key.getKeyHash()));
        assertFalse(getKeyResponse.asString().contains(key.getKeyHash()));
    }

    private long createClient(String contactEmail) {
        return given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "phase8-admin")
                .body(clientRequest(contactEmail))
                .when()
                .post("/api/v1/admin/api-clients")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Response createInvitation(
            long clientId,
            String approvedEmail,
            String environment,
            List<String> scopes,
            Instant expiresAt) {
        return given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "phase8-admin")
                .body(Map.of(
                        "approvedEmail", approvedEmail,
                        "keyName", "Phase 8 Key",
                        "environment", environment,
                        "scopes", scopes,
                        "expiresAt", expiresAt.toString(),
                        "approvalReference", "PHASE-8"))
                .when()
                .post("/api/v1/admin/api-keys/clients/{clientId}/invitations", clientId);
    }

    private Response claim(String approvedEmail, String claimCode) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "approvedEmail", approvedEmail,
                        "claimCode", claimCode))
                .when()
                .post("/api/v1/api-keys/claim");
    }

    private ClaimedKey createClaimedKey(String approvedEmail, List<String> scopes) {
        long clientId = createClient(UUID.randomUUID() + "-" + approvedEmail);
        Response invitation = createInvitation(clientId, approvedEmail, "TEST", scopes, futureTime());
        String claimCode = invitation.then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("claimCode");

        Response claim = claim(approvedEmail, claimCode);
        claim.then().statusCode(200);
        return new ClaimedKey(
                clientId,
                claim.jsonPath().getLong("keyId"),
                claim.jsonPath().getString("apiKey"),
                claim.jsonPath().getString("keyPrefix"));
    }

    private Map<String, Object> clientRequest(String contactEmail) {
        return Map.of(
                "clientName", "Phase 8 Client",
                "contactEmail", contactEmail,
                "owner", "Phase 8 Tests");
    }

    private Instant futureTime() {
        return Instant.now().plus(1, ChronoUnit.DAYS);
    }

    private <T> T find(Class<T> entityClass, Long id) {
        return inTransaction(() -> entityManager.find(entityClass, id));
    }

    private void removeAll(Class<?> entityClass) {
        List<?> entities = entityManager
                .createQuery("select e from " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
        for (Object entity : entities) {
            entityManager.remove(entity);
        }
    }

    private void assertMetadataDoesNotExposeSecrets(String json, String rawApiKey) {
        assertFalse(json.contains(rawApiKey));
        assertFalse(json.contains("apiKey"));
        assertFalse(json.contains("keyHash"));
        assertFalse(json.contains("claimCode"));
        assertFalse(json.contains("claimCodeHash"));
        assertFalse(json.contains("hmac"));
        assertFalse(json.contains("HMAC"));
        assertFalse(json.contains("bcrypt"));
        assertFalse(json.contains("BCrypt"));
        assertFalse(json.contains("$2a$"));
        assertFalse(json.contains("$2b$"));
        assertFalse(json.contains("$2y$"));
    }

    private void inTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }

    private <T> T inTransaction(Supplier<T> work) {
        try {
            userTransaction.begin();
            T result = work.get();
            userTransaction.commit();
            return result;
        } catch (Exception exception) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw new IllegalStateException(exception);
        }
    }

    private record ClaimedKey(Long clientId, Long keyId, String rawApiKey, String keyPrefix) {
    }
}
