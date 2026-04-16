package restTest;

import com.carebridge.dtos.AuthRequest;
import com.carebridge.dtos.RegisterUserDTO;
import com.carebridge.enums.Role;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;

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
        AuthRequest loginReq = new AuthRequest("admin@carebridge.io", "admin");
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
        RegisterUserDTO regReq = new RegisterUserDTO(
            "New Doc", "doctor@carebridge.io", "doc123", "Dr. New", "dr@carebridge.io", "555-5555", "dr.internal@carebridge.io", "666-6666", Role.CAREWORKER
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
