package com.carebridge.restTest;

import com.carebridge.entities.User;
import com.carebridge.security.TokenSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Miscellaneous REST tests that target specific missed branches in:
 * - JwtFilter (non-Bearer header, invalid token causing catch, expired token)
 * - JournalEntryController (blank title, update with null journal, create without auth)
 * - ResidentController (blank firstName/lastName)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MiscCoverageRestTest extends BaseRestTest {

    private static Long journalId;
    private static Long createdEntryId;

    @BeforeAll
    public void setupLocal() {
        // Create a resident to obtain a journalId
        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Misc",
                        "lastName", "Coverage",
                        "cprNr", "MISC-" + nextId()
                ))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("journalId");
        journalId = ((Number) jId).longValue();
    }

    // ─── JwtFilter branch coverage ───────────────────────────────────────────

    @Test
    @Order(1)
    public void testJwtFilter_nonBearerAuthorizationHeader() {
        // Covers: header.startsWith("Bearer ") = false → skip JWT processing
        given()
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    public void testJwtFilter_invalidToken_catchBlock() {
        // Covers: ParseException in SignedJWT.parse() → caught → continue filter chain
        given()
                .header("Authorization", "Bearer this.is.not.a.valid.jwt.token")
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    public void testJwtFilter_expiredToken() {
        // Covers: tokenIsValid=true AND tokenNotExpired=false → condition is false → auth NOT set
        TokenSecurity ts = new TokenSecurity();
        String secret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";
        String expiredToken = ts.createToken("admin@carebridge.io", "ADMIN", "carebridge-test", "-1000", secret);

        given()
                .header("Authorization", "Bearer " + expiredToken)
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200); // still accessible (permitAll), just without auth context
    }

    // ─── JournalEntryController branch coverage ──────────────────────────────

    @Test
    @Order(10)
    public void testCreateJournalEntry_blankTitle() {
        // Covers: entry.getTitle().isBlank() = true (title is non-null but blank)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("title", "", "content", "Some content"))
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(11)
    public void testCreateJournalEntry_withoutAuth_jwtUserNull() {
        // Covers: jwtUser == null branch in create() (no auth header → request attribute not set).
        // Author is supplied in the body so the DB constraint is satisfied even without JWT auth.
        User admin = userDAO.readByEmail("admin@carebridge.io");
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "No Auth Entry",
                        "content", "Created without JWT",
                        "entryType", "NOTE",
                        "riskAssessment", "LOW",
                        "author", Map.of("id", admin.getId())
                ))
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(12)
    public void testCreateJournalEntry_forUpdate() {
        // Create an entry so we can test update with null-journal entry below
        Map<String, Object> req = Map.of(
                "title", "Misc Update Entry",
                "content", "Content",
                "entryType", "NOTE",
                "riskAssessment", "LOW"
        );
        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(201)
                .extract().path("id");
        createdEntryId = ((Number) idObj).longValue();
    }

    @Test
    @Order(13)
    public void testUpdateJournalEntry_nullJournal() {
        // Covers: entry.getJournal() == null branch in update()
        // Create an orphan entry (no journal) via /v3, then try to update via journal endpoint
        User admin = userDAO.readByEmail("admin@carebridge.io");
        Object orphanIdObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Orphan For Update",
                        "content", "C",
                        "entryType", "NOTE",
                        "riskAssessment", "LOW",
                        "author", Map.of("id", admin.getId())
                ))
                .when()
                .post("/api/v3/journal-entries")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long orphanId = ((Number) orphanIdObj).longValue();

        // Now call update with this orphan (journal == null) → 400
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("title", "Updated Orphan", "content", "C", "entryType", "NOTE", "riskAssessment", "LOW"))
                .when()
                .put("/api/journals/" + journalId + "/journal-entries/" + orphanId)
                .then()
                .statusCode(400);
    }

    // ─── ResidentController branch coverage ──────────────────────────────────

    @Test
    @Order(20)
    public void testCreateResident_blankFirstName() {
        // Covers: resident.getFirstName().isBlank() = true
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "", "lastName", "Valid", "cprNr", "CPR-BLK-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(21)
    public void testCreateResident_blankLastName() {
        // Covers: resident.getLastName().isBlank() = true
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "Valid", "lastName", "", "cprNr", "CPR-BLK-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);
    }
}
