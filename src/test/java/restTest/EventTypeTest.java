package restTest;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTypeTest extends BaseRestTest {

    private static int createdEventTypeId;

    @Test
    @Order(1)
    public void testCreateEventType() {
        java.util.Map<String, Object> payload = java.util.Map.of(
            "name", "Test Type " + System.currentTimeMillis(),
            "colorHex", "#ff00ff"
        );

        createdEventTypeId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.javalin.http.ContentType.JSON)
                .body(payload)
                .when()
                .post("/event-types")
                .then()
                .statusCode(201)
                .body("name", startsWith("Test Type"))
                .extract().path("id");
    }

    @Test
    @Order(2)
    public void testReadEventType() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdEventTypeId));
    }

    @Test
    @Order(3)
    public void testUpdateEventType() {
        java.util.Map<String, Object> payload = java.util.Map.of(
            "colorHex", "#000000"
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(io.javalin.http.ContentType.JSON)
                .body(payload)
                .when()
                .put("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(200)
                .body("colorHex", equalTo("#000000"));
    }

    @Test
    @Order(4)
    public void testDeleteEventType() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}
