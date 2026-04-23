package restTest;

import com.carebridge.enums.Role;
import io.restassured.http.ContentType;
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
        String email = "doctor" + System.nanoTime() + "@carebridge.io";
        Map<String, Object> regReq = Map.of(
            "name", "New Doc",
            "email", email,
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
                .body("email", equalTo(email));
    }

    @Test
    @Order(4)
    public void testLoginErrors() {
        // Wrong password
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@carebridge.io", "password", "wrong"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

        // Non-existent user
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "nonexistent@carebridge.io", "password", "pass"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(5)
    public void testRegisterErrors() {
        // Missing fields
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "NoEmail"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(500);

        // Duplicate email
        String email = "dup" + System.nanoTime() + "@test.com";
        Map<String, Object> req = Map.of("name", "U", "email", email, "password", "p", "role", "USER");
        
        given().header("Authorization", "Bearer " + adminToken).contentType(ContentType.JSON).body(req).post("/auth/register");
        
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(500);
    }
}
