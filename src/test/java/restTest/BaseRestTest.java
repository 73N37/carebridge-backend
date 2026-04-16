package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;

public abstract class BaseRestTest {

    protected static Javalin app;
    protected static String userToken;
    protected static String adminToken;

    @BeforeAll
    public static void setupBase() {
        if (app == null) {
            HibernateConfig.setTest(true);
            app = ApplicationConfig.startServer(7070);
            Populator.populate(HibernateConfig.getEntityManagerFactoryForTest());
            RestAssured.baseURI = "http://localhost:7070/api";

            // Get tokens
            adminToken = login("admin@carebridge.io", "admin");
            
            // Create and login as Alice
            register("Alice", "alice@carebridge.io", "password123");
            userToken = login("alice@carebridge.io", "password123");
        }
    }

    private static String login(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password))
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    private static void register(String name, String email, String password) {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format("{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"role\":\"USER\"}", name, email, password))
                .post("/auth/register")
                .then()
                .statusCode(201);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (app != null) app.stop();
        }));
    }
}
