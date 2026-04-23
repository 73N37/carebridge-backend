package com.carebridge.restTest;

import com.carebridge.enums.Role;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityControllerTest extends BaseRestTest {

    @Test
    @Order(1)
    public void testHealthCheck() {
        given()
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }

    @Test
    @Order(2)
    public void testLogin() {
        Map<String, String> loginReq = Map.of(
            "email", "admin@carebridge.io",
            "password", "admin"
        );
        given()
                .contentType(ContentType.JSON)
                .body(loginReq)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("admin@carebridge.io"));
    }

    @Test
    @Order(3)
    public void testRegister() {
        String email = "doctor" + nextId() + "@carebridge.io";
        Map<String, Object> regReq = Map.of(
            "name", "New Doc",
            "email", email,
            "password", "doc123",
            "displayName", "Dr. New",
            "role", Role.CAREWORKER.name()
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(regReq)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("email", equalTo(email));
    }

    @Test
    @Order(4)
    public void testLoginErrors() {
        // Wrong password
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@carebridge.io", "password", "wrong"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(401), is(500)));

        // Non-existent user
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "nonexistent@carebridge.io", "password", "pass"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(401), is(500)));
    }

    @Test
    @Order(5)
    public void testRegisterErrors() {
        // Missing fields
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "NoEmail"))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }
}
