package com.carebridge.restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST tests for UniversalCrudController (/api/v3) and BaseService.
 * Covers: getMetadata, getAll (paginated), getById, create, update, delete, unknown resource.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UniversalCrudControllerTest extends BaseRestTest {

    private static Long createdResidentId;

    @Test
    @Order(1)
    public void testGetMetadata() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/metadata")
                .then()
                .statusCode(200)
                .body("residents", notNullValue());
    }

    @Test
    @Order(2)
    public void testGetAllResidents_defaultPagination() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/residents")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(3)
    public void testGetAllResidents_customPagination() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/v3/residents")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(4)
    public void testCreateResident() {
        Map<String, Object> body = Map.of(
                "firstName", "V3-Test",
                "lastName", "Resident",
                "cprNr", "V3CPR-" + nextId()
        );
        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v3/residents")
                .then()
                .statusCode(201)
                .extract().path("id");
        createdResidentId = ((Number) idObj).longValue();
    }

    @Test
    @Order(5)
    public void testCreateResident_withIdInBody_throwsException() {
        // entity.getId() != null → BaseService.save() throws RuntimeException → 500
        Map<String, Object> body = Map.of(
                "id", 9999998L,
                "firstName", "WithId"
        );
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v3/residents")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(6)
    public void testGetById_found() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/residents/" + createdResidentId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    public void testGetById_notFound() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/residents/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(8)
    public void testUpdateResident_found() {
        Map<String, Object> body = Map.of("firstName", "Updated-V3");
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/v3/residents/" + createdResidentId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(9)
    public void testUpdateResident_notFound() {
        // BaseService.update() throws "Entity not found" → GlobalExceptionHandler returns 404
        Map<String, Object> body = Map.of("firstName", "X");
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/v3/residents/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(10)
    public void testDeleteResident_exists() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/residents/" + createdResidentId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(11)
    public void testDeleteResident_notExists() {
        // BaseService.deleteById(): entity == null → silently skips → 204
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/residents/999999999")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(12)
    public void testUnknownResource_get() {
        // getMetadataOrThrow returns null → throws RuntimeException("Resource not found: unknown")
        // GlobalExceptionHandler.handleRuntime: message contains "not found" → 404
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/unknown-resource-xyz")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    public void testUnknownResource_post() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("x", "y"))
                .when()
                .post("/api/v3/unknown-resource-xyz")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    public void testUnknownResource_put() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("x", "y"))
                .when()
                .put("/api/v3/unknown-resource-xyz/1")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(15)
    public void testUnknownResource_delete() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/unknown-resource-xyz/1")
                .then()
                .statusCode(404);
    }
}
