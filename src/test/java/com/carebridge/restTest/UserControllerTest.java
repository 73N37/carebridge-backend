package com.carebridge.restTest;

import com.carebridge.enums.Role;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest extends BaseRestTest {

    private static Long createdUserId;
    private static String createdEmail;

    @Test
    @Order(1)
    public void testReadAllUsers() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    public void testCreateUser() {
        createdEmail = "newuser" + nextId() + "@example.com";
        Map<String, Object> userMap = Map.of(
                "name", "New User",
                "email", createdEmail,
                "password", "password123",
                "role", "USER"
        );

        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(userMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("email", equalTo(createdEmail))
                .extract().path("id");
        createdUserId = ((Number) idObj).longValue();
    }

    @Test
    @Order(3)
    public void testReadUser() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/" + createdUserId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdUserId.intValue()))
                .body("email", equalTo(createdEmail));
    }

    @Test
    @Order(5)
    public void testMe() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    public void testLinkResidents() {
        String joeEmail = "joe" + nextId() + "@example.com";
        Map<String, Object> guardianMap = Map.of(
                "name", "Guardian Joe",
                "email", joeEmail,
                "password", "password123",
                "role", "GUARDIAN"
        );

        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(guardianMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long joeId = ((Number) jId).longValue();

        // Create a resident to link
        Map<String, Object> residentReq = Map.of(
            "firstName", "Børge",
            "lastName", "Børgesen",
            "cprNr", "121212-" + nextId()
        );

        Object rId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(residentReq)
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long residentId = ((Number) rId).longValue();

        Map<String, Object> linkRequest = Map.of("residentIds", List.of(residentId));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(linkRequest)
                .when()
                .post("/api/users/" + joeId + "/link-residents")
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
                .post("/api/users/populate")
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
                .delete("/api/users/" + createdUserId)
                .then()
                .statusCode(204);
    }
}
