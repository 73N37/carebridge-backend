package com.carebridge.restTest;

import org.junit.jupiter.api.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTest extends BaseRestTest {

    private static int createdId;
    private static int eventTypeId;

    @BeforeAll
    public void setupLocal() {
        // Create an event type to use for events
        Map<String, Object> typePayload = Map.of(
                "name", "Meeting-" + nextId(),
                "colorHex", "#ff0000"
        );
        
        eventTypeId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(typePayload)
                .when()
                .post("/api/event-types")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(1)
    public void testReadAllEvents() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    public void testCreateEvent() {
        String futureStartAt = Instant.now().plus(1, ChronoUnit.HOURS).toString();

        Map<String, Object> payload = Map.of(
            "title", "New Test Event",
            "description", "JUnit event",
            "startAt", futureStartAt,
            "showOnBoard", true,
            "eventTypeId", eventTypeId
        );

        createdId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .body("title", equalTo("New Test Event"))
                .extract().path("id");
    }

    @Test
    @Order(3)
    public void testReadEventById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/events/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId));
    }

    @Test
    @Order(4)
    public void testUpdateEvent() {
        Map<String, Object> updatePayload = Map.of("title", "Updated Title");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/events/" + createdId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"));
    }

    @Test
    @Order(5)
    public void testReadBetween() {
        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().plusDays(1).toString();
        
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", from)
                .queryParam("to", to)
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    public void testAddRemoveSeenBy() {
        // Add seen by
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/api/events/" + createdId + "/mark-seen")
                .then()
                .statusCode(204);

        // Remove seen by
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/events/" + createdId + "/mark-seen")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(7)
    public void testErrorPaths() {
        // mark-seen non-existent
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/api/events/999999/mark-seen")
                .then()
                .statusCode(anyOf(is(404), is(500)));
    }

    @Test
    @Order(8)
    public void testDeleteEvent() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/events/" + createdId)
                .then()
                .statusCode(204);
    }
}
