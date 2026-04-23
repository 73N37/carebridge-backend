package com.carebridge.restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTypeTest extends BaseRestTest {

    private static int createdId;

    @Test
    @Order(1)
    public void testCreateEventType() {
        Map<String, Object> payload = Map.of(
            "name", "TestType-" + nextId(),
            "colorHex", "#123456"
        );

        createdId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/event-types")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(2)
    public void testReadAll() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/event-types")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void testReadById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/event-types/" + createdId)
                .then()
                .statusCode(200);
        
        // Branch: not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/event-types/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    public void testUpdate() {
        // Case: valid update
        Map<String, Object> payload = Map.of("colorHex", "#000000");
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put("/api/event-types/" + createdId)
                .then()
                .statusCode(200);
        
        // Branch: updated fields null
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "New"))
                .when()
                .put("/api/event-types/" + createdId)
                .then()
                .statusCode(200);
                
        // Branch: not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put("/api/event-types/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    public void testDelete() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/event-types/" + createdId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
        
        // Branch: already deleted
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/event-types/" + createdId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}
