package com.carebridge.restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentTest extends BaseRestTest {

    private static int createdId;
    private static String cpr;

    @Test
    @Order(1)
    public void testCreateResident() {
        cpr = "RES" + nextId();
        Map<String, Object> payload = Map.of(
            "firstName", "Test",
            "lastName", "Resident",
            "cprNr", cpr
        );

        createdId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(2)
    public void testReadAllResidents() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void testReadResidentById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId));
    }

    @Test
    @Order(4)
    public void testReadByCpr() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/cpr/" + cpr)
                .then()
                .statusCode(200)
                .body("cprNr", equalTo(cpr));
    }

    @Test
    @Order(5)
    public void testResidentErrors() {
        // Not found by ID
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/999999")
                .then()
                .statusCode(404);

        // Not found by CPR
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/cpr/nonexistent")
                .then()
                .statusCode(404);

        // Create invalid (missing CPR)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "NoCPR"))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }
}
