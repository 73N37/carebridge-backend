package com.carebridge.restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST tests for the UniversalCrudController (/v3 endpoints).
 * Covers BaseService, BaseService.Page, DynamicDtoAdvice (Page path),
 * UniversalCrudController, and related branches.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UniversalCrudControllerTest extends BaseRestTest {

    private static Long createdUserId;

    @Test
    @Order(1)
    public void testGetMetadata() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/metadata")
                .then()
                .statusCode(200)
                .body("users", notNullValue());
    }

    @Test
    @Order(2)
    public void testGetAllWithPagination() {
        // Covers BaseService.findAll(page, size) and BaseService.Page constructor/getters
        // Covers DynamicDtoAdvice body instanceof BaseService.Page branch
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/v3/users")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(3)
    public void testCreate() {
        // Covers BaseService.save() with a new entity (id == null → persist path)
        String email = "v3-create-" + nextId() + "@test.com";
        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "V3 User",
                        "email", email,
                        "password", "pass123",
                        "role", "USER"
                ))
                .when()
                .post("/api/v3/users")
                .then()
                .statusCode(201)
                .extract().path("id");
        createdUserId = ((Number) idObj).longValue();
    }

    @Test
    @Order(4)
    public void testGetByIdFound() {
        // Covers BaseService.findById() and DynamicDtoAdvice BaseEntity path
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/users/" + createdUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(5)
    public void testGetByIdNotFound() {
        // Covers BaseService.findById() returning empty Optional → 404
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/users/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    public void testUpdate() {
        // Covers BaseService.update() with existing entity
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "V3 Updated",
                        "email", "v3-upd-" + nextId() + "@test.com",
                        "role", "USER",
                        "password", "newpass"
                ))
                .when()
                .put("/api/v3/users/" + createdUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    public void testUpdateNotFound() {
        // Covers BaseService.update() → entity not found → RuntimeException → GlobalExceptionHandler
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "X", "email", "x-" + nextId() + "@test.com", "role", "USER"))
                .when()
                .put("/api/v3/users/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(8)
    public void testDelete() {
        // Covers BaseService.deleteById() when entity exists (entity != null → remove)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/users/" + createdUserId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(9)
    public void testDeleteNotFound() {
        // Covers BaseService.deleteById() when entity == null (no-op path)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/users/999999999")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(10)
    public void testInvalidResource() {
        // Covers getMetadataOrThrow() → metadata == null → RuntimeException "Resource not found"
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/nonexistent-resource")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(11)
    public void testCreateWithExistingId() {
        // Covers BaseService.save() when entity.getId() != null → throws RuntimeException
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "id", 1,
                        "name", "Has ID",
                        "email", "hasid-" + nextId() + "@test.com",
                        "role", "USER"
                ))
                .when()
                .post("/api/v3/users")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(12)
    public void testGetAllJournalEntries() {
        // Covers BaseService.findAll(page, size) for a second resource
        // Also helps DynamicDtoAdvice Page path with items that may not be BaseEntity (journal-entries)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v3/journal-entries")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }
}
