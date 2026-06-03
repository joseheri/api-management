package com.lmco.jsf.fmt.dataprovider.security.apikey;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(ApiKeyDisabledEnforcementTest.DisabledProfile.class)
class ApiKeyDisabledEnforcementTest {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Test
    void disabledModeAllowsScopedEndpointWithoutApiKey() {
        given()
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void disabledModeAllowsScopedEndpointWithInvalidApiKeyButDoesNotRelaxAdminRoutes() {
        given()
                .header(API_KEY_HEADER, "ak_test_UNKNOWN1.not-secret")
                .when()
                .get("/api/v1/aircrafts")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(1));

        given()
                .header(API_KEY_HEADER, "ak_test_UNKNOWN1.not-secret")
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

    public static class DisabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("apikey.enforcement.mode", "disabled");
        }
    }
}
