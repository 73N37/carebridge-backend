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
        
        // Branches in readAll (query params)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "today")
                .queryParam("tz", "UTC")
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "tomorrow")
                .queryParam("tz", "invalid/zone") 
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2026-12-31")
                .queryParam("tz", "Europe/Copenhagen")
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);
        
        // Branch: value null/blank in parseDateKeywordOrIso
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "")
                .queryParam("to", "")
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
        
        // Branch: startAt absent in body
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(Map.of("title", "NoStartAt", "eventTypeId", eventTypeId))
                .when()
                .post("/api/events")
                .then()
                .statusCode(400); // DAO requires startAt

        // Branch: EventType not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(Map.of("title", "Fail", "eventTypeId", 999999, "startAt", futureStartAt))
                .when()
                .post("/api/events")
                .then()
                .statusCode(404);
        
        // Branch: Unauthorized (creator not found in DB)
        String tempEmail = "eventcreator-" + nextId() + "@test.com";
        ensureUserExists("Ev", tempEmail, "p");
        String tempToken = login(tempEmail, "p");
        userDAO.delete(userDAO.readByEmail(tempEmail).getId());
        
        given()
                .header("Authorization", "Bearer " + tempToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/events")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(3)
    public void testReadEventById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/events/" + createdId)
                .then()
                .statusCode(200);
        
        // Branch: not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/events/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    public void testUpdateEvent() {
        Map<String, Object> updatePayload = Map.of(
            "title", "Updated Title", 
            "eventTypeId", eventTypeId
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/events/" + createdId)
                .then()
                .statusCode(200);
        
        // Branch: EventType not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(Map.of("eventTypeId", 999999))
                .when()
                .put("/api/events/" + createdId)
                .then()
                .statusCode(404);
        
        // Branch: no eventTypeId
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(Map.of("description", "New Desc"))
                .when()
                .put("/api/events/" + createdId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(5)
    public void testUpcoming() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/events/upcoming")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    public void testMarkSeen() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/api/events/" + createdId + "/mark-seen")
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/events/" + createdId + "/mark-seen")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(7)
    public void testDeleteEvent() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/events/" + createdId)
                .then()
                .statusCode(204);
    }
}
