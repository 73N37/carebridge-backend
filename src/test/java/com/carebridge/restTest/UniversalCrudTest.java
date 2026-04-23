package com.carebridge.restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UniversalCrudTest extends BaseRestTest {

    private Long createdId;
    private String uniqueName;

    @Test
    @Order(1)
    public void testGetMetadata() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/metadata")
                .then()
                .statusCode(200)
                .body("residents", notNullValue())
                .body("'event-types'", notNullValue());
    }

    @Test
    @Order(2)
    public void testCreateResource() {
        uniqueName = "Test Event Type " + nextId();
        Map<String, Object> body = Map.of(
                "name", uniqueName,
                "colorHex", "#FF0000"
        );

        Object id = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v3/event-types")
                .then()
                .statusCode(201)
                .body("name", equalTo(uniqueName))
                .extract().path("id");
        
        createdId = ((Number) id).longValue();
    }

    @Test
    @Order(3)
    public void testGetAll() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/event-types")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(4)
    public void testGetById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/event-types/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId.intValue()))
                .body("name", equalTo(uniqueName));
    }

    @Test
    @Order(5)
    public void testUpdateResource() {
        Map<String, Object> body = Map.of(
                "name", "Updated " + uniqueName,
                "colorHex", "#00FF00"
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/v3/event-types/" + createdId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated " + uniqueName))
                .body("colorHex", equalTo("#00FF00"));
    }

    @Test
    @Order(6)
    public void testDeleteResource() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v3/event-types/" + createdId)
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/event-types/" + createdId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    public void testInvalidResource() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v3/invalid-resource")
                .then()
                .statusCode(404);
    }
}
