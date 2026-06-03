package com.lmco.jsf.fmt.dataprovider.security.apikey;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyScope;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
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
@TestProfile(ApiKeyReportOnlyEnforcementTest.ReportOnlyProfile.class)
class ApiKeyReportOnlyEnforcementTest {

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
    void reportOnlyAllowsScopedEndpointWithoutApiKey() {
        given()
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void reportOnlyAllowsScopedEndpointWithInvalidApiKey() {
        given()
                .header(API_KEY_HEADER, "ak_test_UNKNOWN1.not-secret")
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void reportOnlyAllowsValidApiKeyMissingScopeButDoesNotRelaxAdminRoutes() {
        ClaimedKey readUsageKey = createClaimedKey("report-only-usage@example.test", List.of("read:usage"));

        given()
                .header(API_KEY_HEADER, readUsageKey.rawApiKey())
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));

        given()
                .header(API_KEY_HEADER, readUsageKey.rawApiKey())
                .when()
                .get("/api/v1/admin/api-clients")
                .then()
                .statusCode(403);

        given()
                .when()
                .get("/api/v1/admin/api-clients")
                .then()
                .statusCode(401);
    }

    private ClaimedKey createClaimedKey(String approvedEmail, List<String> scopes) {
        long clientId = createClient(UUID.randomUUID() + "-" + approvedEmail);
        Response invitation = createInvitation(clientId, approvedEmail, scopes);
        String claimCode = invitation.then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("claimCode");

        Response claim = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "approvedEmail", approvedEmail,
                        "claimCode", claimCode))
                .when()
                .post("/api/v1/api-keys/claim");
        claim.then().statusCode(200);
        return new ClaimedKey(claim.jsonPath().getString("apiKey"));
    }

    private long createClient(String contactEmail) {
        return given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "report-only-admin")
                .body(Map.of(
                        "clientName", "Report Only Client",
                        "contactEmail", contactEmail,
                        "owner", "Report Only Tests"))
                .when()
                .post("/api/v1/admin/api-clients")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Response createInvitation(long clientId, String approvedEmail, List<String> scopes) {
        return given()
                .contentType(ContentType.JSON)
                .header(ADMIN_HEADER, "report-only-admin")
                .body(Map.of(
                        "approvedEmail", approvedEmail,
                        "keyName", "Report Only Key",
                        "environment", "TEST",
                        "scopes", scopes,
                        "expiresAt", Instant.now().plus(1, ChronoUnit.DAYS).toString(),
                        "approvalReference", "REPORT-ONLY"))
                .when()
                .post("/api/v1/admin/api-keys/clients/{clientId}/invitations", clientId);
    }

    private void removeAll(Class<?> entityClass) {
        List<?> entities = entityManager
                .createQuery("select e from " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
        for (Object entity : entities) {
            entityManager.remove(entity);
        }
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

    public static class ReportOnlyProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("apikey.enforcement.mode", "report-only");
        }
    }

    private record ClaimedKey(String rawApiKey) {
    }
}
