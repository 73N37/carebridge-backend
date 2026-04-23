package restTest;

import org.junit.jupiter.api.*;
import java.time.Instant;
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
                "name", "Meeting-" + System.currentTimeMillis(),
                "colorHex", "#ff0000"
        );
        
        eventTypeId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(typePayload)
                .when()
                .post("/event-types")
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
                .get("/events")
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
                .post("/events")
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
                .get("/events/" + createdId)
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
                .put("/events/" + createdId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"));
    }

    @Test
    @Order(5)
    public void testReadBetween() {
        String from = Instant.now().minus(1, ChronoUnit.DAYS).toString();
        String to = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", from)
                .queryParam("to", to)
                .when()
                .get("/events/between")
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
                .post("/events/" + createdId + "/seen")
                .then()
                .statusCode(200);

        // Remove seen by
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/events/" + createdId + "/seen")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    public void testErrorPaths() {
        // Invalid between dates
        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "invalid")
                .queryParam("to", "invalid")
                .when()
                .get("/events/between")
                .then()
                .statusCode(500);

        // Non-existent event update
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(Map.of("title", "X"))
                .when()
                .put("/events/999999")
                .then()
                .statusCode(500);

        // Seen by non-existent
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/events/999999/seen")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(8)
    public void testDeleteEvent() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/events/" + createdId)
                .then()
                .statusCode(204);
    }
}
