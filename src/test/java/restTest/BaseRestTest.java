package restTest;

import com.carebridge.CareBridgeApplication;
import com.carebridge.config.Populator;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CareBridgeApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseRestTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected Populator populator;

    protected static String userToken;
    protected static String adminToken;

    @BeforeAll
    public void setupBase() {
        RestAssured.baseURI = "http://localhost:" + port + "/api";
        
        populator.populate();

        // Get tokens
        adminToken = login("admin@carebridge.io", "admin");
        
        // Create and login as Alice
        register("Alice", "alice@carebridge.io", "password123");
        userToken = login("alice@carebridge.io", "password123");
    }

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(java.util.Map.of("email", email, "password", password))
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    private void register(String name, String email, String password) {
        try {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .body(java.util.Map.of(
                        "name", name, 
                        "email", email, 
                        "password", password, 
                        "role", "USER"
                    ))
                    .post("/auth/register");
        } catch (Exception ignored) {}
    }

    static {
        // Shutdown hook not needed for Spring Boot Test as it handles lifecycle
    }
}
