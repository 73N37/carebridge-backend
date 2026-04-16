package restTest;

import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UniversalCrudTest extends BaseRestTest {

    @Test
    @Order(1)
    public void testGetMetadata() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/v3/metadata")
                .then()
                .statusCode(200)
                .body("residents", notNullValue());
    }

    @Test
    @Order(2)
    public void testGetAllResidentsV3() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/v3/residents")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
