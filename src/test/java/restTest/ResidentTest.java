package restTest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentTest extends BaseRestTest {

    @Test
    @Order(1)
    public void testCreateResident() {
        java.util.Map<String, Object> req = java.util.Map.of(
            "firstName", "John",
            "lastName", "Doe",
            "cprNr", "010101-1234"
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/residents/create")
                .then()
                .statusCode(201)
                .header("Location", containsString("/api/residents/"))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("journalId", notNullValue());
    }
}
