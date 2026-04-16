package restTest;

import com.carebridge.dtos.CreateJournalEntryRequestDTO;
import com.carebridge.dtos.EditJournalEntryRequestDTO;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JournalEntryControllerTest extends BaseRestTest {

    private static Long createdEntryId;
    private static Long journalId;

    @BeforeAll
    public void setupLocal() {
        // Create a resident to get a journalId
        com.carebridge.dtos.CreateResidentRequestDTO residentReq = new com.carebridge.dtos.CreateResidentRequestDTO();
        residentReq.setFirstName("Børge");
        residentReq.setLastName("Børgesen");
        residentReq.setCprNr("121212-1212");

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
        CreateJournalEntryRequestDTO req = new CreateJournalEntryRequestDTO();
        req.setTitle("Morning Checkup");
        req.setContent("Everything looks good.");
        req.setEntryType(EntryType.NOTE);
        req.setRiskAssessment(RiskAssessment.LOW);

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
        EditJournalEntryRequestDTO req = new EditJournalEntryRequestDTO();
        req.setContent("Updated content: Patient is resting.");

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
}
