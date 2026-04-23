package restTest;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JournalEntryTest extends BaseRestTest {

    private static Long createdEntryId;
    private static Long journalId;

    @BeforeAll
    public void setupLocal() {
        // Create a resident to get a journalId
        Map<String, Object> residentReq = Map.of(
            "firstName", "Børge",
            "lastName", "Børgesen",
            "cprNr", "121212-1212" + System.nanoTime()
        );

        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(residentReq)
                .when()
                .post("/residents/create")
                .then()
                .statusCode(201)
                .extract().path("journalId");
        journalId = ((Number) jId).longValue();
    }

    @Test
    @Order(1)
    public void testCreateJournalEntry() {
        Map<String, Object> req = Map.of(
            "title", "Morning Checkup",
            "content", "Everything looks good.",
            "entryType", EntryType.NOTE,
            "riskAssessment", RiskAssessment.LOW
        );

        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(201)
                .body("title", equalTo("Morning Checkup"))
                .extract().path("id");
        createdEntryId = ((Number) idObj).longValue();
    }

    @Test
    @Order(2)
    public void testReadJournalEntry() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/journals/" + journalId + "/journal-entries/" + createdEntryId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdEntryId.intValue()))
                .body("content", equalTo("Everything looks good."));
    }

    @Test
    @Order(3)
    public void testUpdateJournalEntry() {
        Map<String, Object> req = Map.of("content", "Updated content: Patient is resting.");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .put("/journals/" + journalId + "/journal-entries/" + createdEntryId)
                .then()
                .statusCode(200)
                .body("content", equalTo("Updated content: Patient is resting."));
    }

    @Test
    @Order(4)
    public void testFindAllEntriesByJournal() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(200)
                .body("$", hasItem(createdEntryId.intValue()));
    }

    @Test
    @Order(5)
    public void testCreateEntryErrors() {
        // Missing title
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("content", "Content", "entryType", EntryType.NOTE, "riskAssessment", RiskAssessment.LOW))
                .when()
                .post("/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(403); // Security/Validation block

        // Missing journal
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("title", "T", "content", "C", "entryType", EntryType.NOTE, "riskAssessment", RiskAssessment.LOW))
                .when()
                .post("/journals/9999/journal-entries")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(6)
    public void testUpdateEntryErrors() {
        // Wrong journal ID
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("content", "New"))
                .when()
                .put("/journals/9999/journal-entries/" + createdEntryId)
                .then()
                .statusCode(403);
        
        // Non-existent entry
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("content", "New"))
                .when()
                .put("/journals/" + journalId + "/journal-entries/9999")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(7)
    public void testReadEntryErrors() {
        // Wrong journal ID
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/journals/9999/journal-entries/" + createdEntryId)
                .then()
                .statusCode(anyOf(is(400), is(403)));
        
        // Not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/journals/" + journalId + "/journal-entries/9999")
                .then()
                .statusCode(404);
    }
}
