package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;
import io.javalin.Javalin;
import com.carebridge.config.Populator;
import io.restassured.RestAssured;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTest extends BaseRestTest {

    private static String authToken;

    private static int createdId;
    private static int eventTypeId;

    @BeforeAll
    public void setupLocal() {
        authToken = adminToken;
        
        // Create an event type to use for events
        java.util.Map<String, Object> typePayload = java.util.Map.of(
                "name", "Meeting-" + System.currentTimeMillis(),
                "colorHex", "#ff0000"
        );
        
        eventTypeId = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(typePayload)
                .when()
                .post("/event-types")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    // ---------------------------
    // GET /events
    // ---------------------------
    @Test
    @Order(1)
    public void testReadAllEvents() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    // ---------------------------
    // POST /events
    // ---------------------------
    @Test
    @Order(2)
    public void testCreateEvent() {
        String futureStartAt = Instant.now().plusSeconds(3600).toString();

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("title", "New Test Event");
        payload.put("description", "JUnit event");
        payload.put("startAt", futureStartAt);
        payload.put("showOnBoard", true);
        payload.put("eventTypeId", eventTypeId);

        createdId =
                given()
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/events")
                        .then()
                        .statusCode(201)
                        .body("title", equalTo("New Test Event"))
                        .extract().path("id");

        Assertions.assertTrue(createdId > 0);
    }

    // ---------------------------
    // GET /events/{id}
    // ---------------------------
    @Test
    @Order(3)
    public void testReadEventById() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId));
    }

    // ---------------------------
    // PUT /events/{id}
    // ---------------------------
    @Test
    @Order(4)
    public void testUpdateEvent() {

        java.util.Map<String, Object> updatePayload = java.util.Map.of(
            "title", "Updated Event Title"
        );

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/events/" + createdId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Event Title"));
    }

    // ---------------------------
    // DELETE /events/{id}
    // ---------------------------
    @Test
    @Order(6)
    public void testDeleteEvent() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/events/" + createdId)
                .then()
                .statusCode(anyOf(is(200), is(204), is(403)));
    }

    // ---------------------------
    // GET /events/upcoming
    // ---------------------------
    @Test
    @Order(5)
    public void testUpcomingEvents() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/upcoming")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
