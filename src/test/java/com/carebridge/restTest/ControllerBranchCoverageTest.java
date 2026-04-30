package com.carebridge.restTest;

import com.carebridge.entities.User;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Covers missed branches in JournalEntryController, ResidentController that are not
 * exercised by the existing test suite.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerBranchCoverageTest extends BaseRestTest {

    private Long validJournalId;
    private Long orphanEntryId;

    @BeforeAll
    public void setupLocal() {
        // Create a resident (which auto-creates a journal) to get a valid journal ID
        Map<String, Object> residentReq = Map.of(
                "firstName", "BranchTest",
                "lastName", "Resident",
                "cprNr", "BTR-" + nextId()
        );
        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(residentReq)
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("journalId");
        validJournalId = ((Number) jId).longValue();

        // Create an orphan journal entry (no journal association) via the generic v3 endpoint
        User admin = userDAO.readByEmail("admin@carebridge.io");
        Map<String, Object> orphanReq = Map.of(
                "title", "OrphanForUpdate",
                "content", "Content",
                "entryType", EntryType.NOTE.name(),
                "riskAssessment", RiskAssessment.LOW.name(),
                "author", Map.of("id", admin.getId())
        );
        Object oId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(orphanReq)
                .when()
                .post("/api/v3/journal-entries")
                .then()
                .statusCode(201)
                .extract().path("id");
        orphanEntryId = ((Number) oId).longValue();
    }

    // ─── JournalEntryController.create branches ───────────────────────────

    @Test
    @Order(1)
    public void testCreateJournalEntry_titleIsBlank_returns400() {
        // entry.getTitle().isBlank() → true (empty string, not null) → 400
        User admin = userDAO.readByEmail("admin@carebridge.io");
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "",
                        "content", "SomeContent",
                        "entryType", EntryType.NOTE.name(),
                        "riskAssessment", RiskAssessment.LOW.name(),
                        "author", Map.of("id", admin.getId())
                ))
                .when()
                .post("/api/journals/" + validJournalId + "/journal-entries")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(2)
    public void testCreateJournalEntry_jwtUserNull_authorFromBody() {
        // jwtUser == null (no auth header) → skip JWT author block
        // Author provided in body so DB constraint is satisfied
        User admin = userDAO.readByEmail("admin@carebridge.io");
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "NoAuthTitle",
                        "content", "NoAuthContent",
                        "entryType", EntryType.NOTE.name(),
                        "riskAssessment", RiskAssessment.LOW.name(),
                        "author", Map.of("id", admin.getId())
                ))
                .when()
                .post("/api/journals/" + validJournalId + "/journal-entries")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(3)
    public void testCreateJournalEntry_jwtUserNotNull_userNotFoundInDB() {
        // jwtUser != null (token present) but user no longer in DB → user == null → skip setAuthor
        // Author provided in body so DB constraint is satisfied
        User admin = userDAO.readByEmail("admin@carebridge.io");
        String tempEmail = "jwtdel-" + nextId() + "@test.com";
        ensureUserExists("JwtDel", tempEmail, "pass");
        String tempToken = login(tempEmail, "pass");
        userDAO.delete(userDAO.readByEmail(tempEmail).getId());

        given()
                .header("Authorization", "Bearer " + tempToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "DeletedUserTitle",
                        "content", "Content",
                        "entryType", EntryType.NOTE.name(),
                        "riskAssessment", RiskAssessment.LOW.name(),
                        "author", Map.of("id", admin.getId())
                ))
                .when()
                .post("/api/journals/" + validJournalId + "/journal-entries")
                .then()
                .statusCode(201);
    }

    // ─── JournalEntryController.update branches ───────────────────────────

    @Test
    @Order(4)
    public void testUpdateJournalEntry_journalIsNull_returns400() {
        // entry.getJournal() == null → true (orphan entry) → 400
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("title", "Updated", "content", "NewContent"))
                .when()
                .put("/api/journals/" + validJournalId + "/journal-entries/" + orphanEntryId)
                .then()
                .statusCode(400);
    }

    // ─── ResidentController.create branches ───────────────────────────────

    @Test
    @Order(5)
    public void testCreateResident_firstNameIsBlank_returns400() {
        // firstName != null but isBlank("") → true → 400
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "", "lastName", "L", "cprNr", "CPR-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(6)
    public void testCreateResident_lastNameIsBlank_returns400() {
        // lastName != null but isBlank("") → true → 400
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "F", "lastName", "", "cprNr", "CPR-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);
    }
}
