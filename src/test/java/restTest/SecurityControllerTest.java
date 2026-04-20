package restTest;

import com.carebridge.enums.Role;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityControllerTest extends BaseRestTest {

    @Test
    @Order(1)
    public void testHealthCheck() {
        given()
                .when()
                .get("/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", containsString("API is up and running"));
    }

    @Test
    @Order(2)
    public void testLogin() {
        Map<String, String> loginReq = Map.of(
            "email", "admin@carebridge.io",
            "password", "admin"
        );
        given()
                .contentType(ContentType.JSON)
                .body(loginReq)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("admin@carebridge.io"));
    }

    @Test
    @Order(3)
    public void testRegister() {
        Map<String, Object> regReq = Map.of(
            "name", "New Doc",
            "email", "doctor@carebridge.io",
            "password", "doc123",
            "displayName", "Dr. New",
            "role", Role.CAREWORKER.name()
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(regReq)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("email", equalTo("doctor@carebridge.io"));
    }
}
