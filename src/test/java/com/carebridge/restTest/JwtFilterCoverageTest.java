package com.carebridge.restTest;

import com.carebridge.security.TokenSecurity;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST tests covering JwtFilter edge-case branches:
 *  1. Authorization header that is not "Bearer ..." (non-Bearer prefix)
 *  2. Bearer token that is unparseable/invalid (catch block)
 *  3. Bearer token that is valid but expired (tokenNotExpired returns false)
 *  4. Bearer token signed with wrong secret (tokenIsValid returns false)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JwtFilterCoverageTest extends BaseRestTest {

    private final TokenSecurity tokenSecurity = new TokenSecurity();
    private final String secret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";

    @Test
    @Order(1)
    public void testRequest_withNonBearerAuthHeader() {
        // header != null but !header.startsWith("Bearer ") → skip auth block
        given()
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }

    @Test
    @Order(2)
    public void testRequest_withMalformedBearerToken() {
        // token is present but unparseable → ParseException → catch(Exception e) in JwtFilter
        given()
                .header("Authorization", "Bearer this.is.not.a.valid.jwt.token")
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }

    @Test
    @Order(3)
    public void testRequest_withExpiredToken() {
        // tokenIsValid returns true (signature ok) but tokenNotExpired returns false
        String expiredToken = tokenSecurity.createToken(
                "admin@carebridge.io", "ADMIN", "carebridge-test", "-1000", secret);
        given()
                .header("Authorization", "Bearer " + expiredToken)
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }

    @Test
    @Order(4)
    public void testRequest_withWrongSecretToken() {
        // tokenIsValid returns false (wrong secret used to sign) → auth not set
        String wrongSecretToken = tokenSecurity.createToken(
                "admin@carebridge.io", "ADMIN", "carebridge-test", "3600000",
                "ANOTHER_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_XYZ");
        given()
                .header("Authorization", "Bearer " + wrongSecretToken)
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }
}
