package com.carebridge.restTest;

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
            "cprNr", "121212-" + nextId()
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
        journalId = ((Number) jId).longValue();
    }

    @Test
    @Order(1)
    public void testCreateJournalEntry() {
        Map<String, Object> req = Map.of(
            "title", "Morning Checkup",
            "content", "Everything looks good.",
            "entryType", EntryType.NOTE.name(),
            "riskAssessment", RiskAssessment.LOW.name()
        );

        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(201)
                .body("title", equalTo("Morning Checkup"))
                .extract().path("id");
        createdEntryId = ((Number) idObj).longValue();
        
        // Branch: journal not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/journals/999999/journal-entries")
                .then()
                .statusCode(404);
        
        // Branch: title null/blank
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("content", "C"))
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(400);

        // Branch: jwtUser present (already covered by adminToken)
    }

    @Test
    @Order(2)
    public void testReadJournalEntry() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/journals/" + journalId + "/journal-entries/" + createdEntryId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdEntryId.intValue()))
                .body("content", equalTo("Everything looks good."));
        
        // Branch: entry not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/journals/" + journalId + "/journal-entries/999999")
                .then()
                .statusCode(404);
        
        // Branch: journalId mismatch
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/journals/111111/journal-entries/" + createdEntryId)
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    public void testUpdateJournalEntry() {
        Map<String, Object> req = Map.of(
            "content", "Updated content",
            "title", "Morning Checkup",
            "entryType", EntryType.NOTE.name(),
            "riskAssessment", RiskAssessment.LOW.name()
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .put("/api/journals/" + journalId + "/journal-entries/" + createdEntryId)
                .then()
                .statusCode(200)
                .body("content", equalTo("Updated content"));
        
        // Branch: entry not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .put("/api/journals/" + journalId + "/journal-entries/999999")
                .then()
                .statusCode(404);
        
        // Branch: journalId mismatch
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .put("/api/journals/111111/journal-entries/" + createdEntryId)
                .then()
                .statusCode(400);
    }
}
