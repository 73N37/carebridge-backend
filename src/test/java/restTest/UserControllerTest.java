package restTest;

import com.carebridge.dtos.LinkResidentsRequest;
import com.carebridge.dtos.UserDTO;
import com.carebridge.enums.Role;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest extends BaseRestTest {

    private static Long createdUserId;

    @Test
    @Order(1)
    public void testReadAllUsers() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    public void testCreateUser() {
        java.util.Map<String, Object> userMap = java.util.Map.of(
                "name", "New User",
                "email", "newuser@example.com",
                "password", "password123",
                "role", "USER"
        );

        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(userMap)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("email", equalTo("newuser@example.com"))
                .extract().path("id");
        createdUserId = ((Number) idObj).longValue();
    }

    @Test
    @Order(3)
    public void testReadUser() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/" + createdUserId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdUserId.intValue()))
                .body("email", equalTo("newuser@example.com"));
    }

    @Test
    @Order(5)
    public void testMe() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("email", equalTo("alice@carebridge.io"));
    }

    @Test
    @Order(6)
    public void testLinkResidents() {
        java.util.Map<String, Object> guardianMap = java.util.Map.of(
                "name", "Guardian Joe",
                "email", "joe@example.com",
                "password", "password123",
                "role", "GUARDIAN"
        );

        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(guardianMap)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long joeId = ((Number) jId).longValue();

        // Create a resident to link
        com.carebridge.dtos.CreateResidentRequestDTO residentReq = new com.carebridge.dtos.CreateResidentRequestDTO(
            "Børge", "Børgesen", "121212-1212", null, null
        );

        Object rId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(residentReq)
                .when()
                .post("/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long residentId = ((Number) rId).longValue();

        LinkResidentsRequest linkRequest = new LinkResidentsRequest(List.of(residentId));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(linkRequest)
                .when()
                .post("/users/" + joeId + "/link-residents")
                .then()
                .statusCode(200)
                .body("msg", containsString("Beboere tilknyttet"));
    }

    @Test
    @Order(7)
    public void testPopulate() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/populate")
                .then()
                .statusCode(200)
                .body("msg", containsString("Database populated"));
    }

    @Test
    @Order(8)
    public void testDeleteUser() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/users/" + createdUserId)
                .then()
                .statusCode(204);
    }
}
