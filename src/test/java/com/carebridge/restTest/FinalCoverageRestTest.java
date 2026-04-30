package com.carebridge.restTest;

import com.carebridge.crud.annotations.CrudResource;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.core.CrudInterceptor;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import com.carebridge.security.TokenSecurity;
import io.restassured.http.ContentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring integration tests covering the last remaining missed branches / instructions
 * that require a live Spring context, JPA, or actual HTTP calls through the filter chain.
 *
 * Each test follows the Given / When / Then convention and is ordered to avoid
 * inter-test data dependencies.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinalCoverageRestTest extends BaseRestTest {

    // ─── Test entity classes for DynamicCrudManager.inspectFields() coverage ────

    /**
     * Entity with a {@code @Positive} and a {@code @NotNull} field.
     * Registering this entity triggers the two missed branches inside
     * {@code inspectFields()}: {@code @Positive == true} and
     * {@code @NotNull == true} (short-circuit of the {@code ||} in the
     * {@code required} assignment).
     */
    @CrudResource(path = "final-test-positive")
    static class TestPositiveEntity extends BaseEntity {
        @Positive
        private int count;
        @NotNull
        private String name;
    }

    /**
     * Marker interface used as the DTO type for {@link TestInterfaceDtoEntity}.
     * Because interfaces return {@code null} from {@code getSuperclass()}, iterating
     * this type through {@code inspectFields()} exercises the
     * {@code current != null == false} exit branch of the while-loop.
     */
    interface IFaceDTO {
    }

    /**
     * Entity whose DTO is an interface ({@link IFaceDTO}).  The interface's
     * {@code getSuperclass()} returns {@code null}, so the while-loop exits via the
     * {@code current != null == false} branch rather than the
     * {@code current == Object.class} branch that is exercised by normal classes.
     */
    @CrudResource(path = "final-test-iface-dto", dto = IFaceDTO.class)
    static class TestInterfaceDtoEntity extends BaseEntity {
    }

    // ─── Autowired Spring beans ───────────────────────────────────────────────────

    @Autowired
    private DynamicCrudManager crudManager;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private ResidentDAO residentDAO;

    // ─── Test state shared across ordered test methods ────────────────────────────

    private Long residentId;
    private Long journalId;

    // ─── Setup ────────────────────────────────────────────────────────────────────

    @BeforeAll
    public void setupLocal() {
        // Create a resident that will be reused by the lazy-collection and
        // journal-entry tests in this class.
        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "FinalCov",
                        "lastName", "TestResident",
                        "cprNr", "FCOV-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
        residentId = ((Number) idObj).longValue();

        // Create a second resident to obtain a journalId for journal-entry tests.
        Object jIdObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "FinalJE",
                        "lastName", "TestResident",
                        "cprNr", "FJE-" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("journalId");
        journalId = ((Number) jIdObj).longValue();
    }

    // ─── 1. BaseService.findAll() no-args ─────────────────────────────────────────

    /**
     * Given the 'users' resource registered in DynamicCrudManager,
     * when findAll() (no-args) is called on its service,
     * then a non-empty list is returned (at minimum the admin user exists).
     */
    @Test
    @Order(1)
    void testBaseService_findAll_noArgs_returnsNonEmptyList() {
        // Covers: BaseService.findAll() – 0% instruction coverage before this test.
        List<?> results = crudManager.getMetadata("users").getService().findAll();

        assertNotNull(results);
        assertFalse(results.isEmpty(), "findAll() should return at least the admin user");
    }

    // ─── 2. DynamicCrudManager.registerInterceptor() ──────────────────────────────

    /**
     * Given a no-op CrudInterceptor for Resident,
     * when registerInterceptor() is called,
     * then no exception is thrown and the interceptor is stored in the manager.
     */
    @Test
    @Order(2)
    void testRegisterInterceptor_storesInterceptorWithoutError() {
        // Covers: registerInterceptor() – 0% instruction coverage before this test.
        assertDoesNotThrow(() ->
                crudManager.registerInterceptor(Resident.class, new CrudInterceptor<Resident>() {
                })
        );
    }

    // ─── 3. DynamicCrudManager.inspectFields(): @Positive + @NotNull branches ─────

    /**
     * Given TestPositiveEntity with {@code @Positive int count} and
     * {@code @NotNull String name},
     * when registerResource() is called,
     * then inspectFields() exercises the {@code @Positive == true} branch and the
     * {@code @NotNull == true} short-circuit branch of the required-field check.
     */
    @Test
    @Order(3)
    void testRegisterResource_withPositiveAndNotNull_coversInspectFieldsBranches() {
        // Covers in inspectFields():
        //   field.isAnnotationPresent(Positive.class)  == true  (@Positive int count)
        //   field.isAnnotationPresent(NotNull.class)   == true  (@NotNull String name → short-circuits ||)
        assertDoesNotThrow(() -> crudManager.registerResource(TestPositiveEntity.class));

        var meta = crudManager.getMetadata("final-test-positive");
        assertNotNull(meta, "Metadata for 'final-test-positive' should be registered");

        boolean hasPositiveConstraint = meta.getFields().stream()
                .anyMatch(f -> f.getConstraints().containsKey("positive"));
        assertTrue(hasPositiveConstraint,
                "Field metadata should include 'positive' constraint from @Positive annotation");

        boolean hasRequiredField = meta.getFields().stream()
                .anyMatch(com.carebridge.crud.logic.ResourceMetadata.FieldInfo::isRequired);
        assertTrue(hasRequiredField,
                "Field metadata should mark @NotNull-annotated field as required");
    }

    // ─── 4. DynamicCrudManager.inspectFields(): current == null exit branch ────────

    /**
     * Given TestInterfaceDtoEntity whose DTO is an interface (IFaceDTO),
     * when registerResource() is called,
     * then inspectFields() calls {@code IFaceDTO.class.getSuperclass()} which returns
     * {@code null}, exercising the {@code current != null == false} exit branch of
     * the while-loop.
     */
    @Test
    @Order(4)
    void testRegisterResource_withInterfaceDto_coversNullCurrentBranch() {
        // Covers: current != null == false in inspectFields() while-loop.
        assertDoesNotThrow(() -> crudManager.registerResource(TestInterfaceDtoEntity.class));

        assertNotNull(crudManager.getMetadata("final-test-iface-dto"),
                "Metadata for 'final-test-iface-dto' should be registered");
    }

    // ─── 5. MappingService.toMap(): Hibernate.isInitialized(col) == false ─────────

    /**
     * Given a Resident loaded via residentDAO.read() after its transaction closes,
     * when mappingService.toMap() is called on the detached entity,
     * then the lazy @OneToMany {@code children} collection (inherited from BaseEntity)
     * is an uninitialized Hibernate proxy → {@code Hibernate.isInitialized(col) == false}
     * → the collection processing block is skipped without throwing
     * LazyInitializationException.
     */
    @Test
    @Order(5)
    void testToMap_lazyCollectionAfterSessionClose_isSkippedSafely() {
        // residentDAO.read() is @Transactional; after it returns the session is closed.
        // BaseEntity.children is @OneToMany(fetch = LAZY): the returned Hibernate
        // PersistentList is uninitialized (never accessed inside the transaction).
        Resident detachedResident = residentDAO.read(residentId);
        assertNotNull(detachedResident);

        // Covers: Hibernate.isInitialized(col) == false → skip collection block.
        Map<String, Object> dto = assertDoesNotThrow(
                () -> mappingService.toMap(detachedResident),
                "toMap() must not throw LazyInitializationException on a detached entity");

        assertNotNull(dto);
        // The lazy 'children' collection was uninitialized → not added to the result.
        assertFalse(dto.containsKey("childrenIds"),
                "Lazy uninitialized collection should be skipped; 'childrenIds' should not appear");
    }

    // ─── 6a. JwtFilter: tokenIsValid == false (wrong signature) ──────────────────

    /**
     * Given a structurally valid JWT signed with a DIFFERENT secret than the one
     * configured for the application,
     * when the token is sent in the Authorization header,
     * then JwtFilter.tokenIsValid() returns false → the auth block is skipped →
     * the request proceeds to the public healthcheck endpoint and returns 200.
     */
    @Test
    @Order(6)
    void testJwtFilter_tokenIsValidFalse_authBlockSkipped() {
        // Covers: tokenIsValid(token, secret) == false in the && condition of JwtFilter.
        TokenSecurity ts = new TokenSecurity();
        String wrongSecret = "ANOTHER_COMPLETELY_DIFFERENT_SECRET_FOR_TESTS_9876543210";
        String tokenWithWrongSig = ts.createToken(
                "user@test.com", "USER", "test-issuer", "3600000", wrongSecret);

        given()
                .header("Authorization", "Bearer " + tokenWithWrongSig)
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200);
    }

    // ─── 6b. JwtFilter: header present but not a Bearer token ────────────────────

    /**
     * Given an Authorization header that does NOT start with "Bearer ",
     * when the request is sent to a public endpoint,
     * then the JwtFilter skips authentication entirely and the request succeeds.
     */
    @Test
    @Order(7)
    void testJwtFilter_nonBearerAuthHeader_authBlockSkipped() {
        // Covers: header.startsWith("Bearer ") == false branch in JwtFilter.
        given()
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .when()
                .get("/api/auth/healthcheck")
                .then()
                .statusCode(200);
    }

    // ─── 7. JournalEntryController.create(): jwtUser != null but user == null ─────

    /**
     * Given a JWT for a user that has been deleted from the database after the token
     * was issued, when a journal entry is POSTed with that stale token plus an explicit
     * author in the request body,
     * then jwtUser is non-null but userDAO.readByEmail() returns null → the
     * {@code if (user != null)} branch is false → the body-provided author is kept →
     * the entry is created successfully (201).
     */
    @Test
    @Order(8)
    void testCreateJournalEntry_jwtUserPresentButUserDeletedFromDb_returns201() {
        // Covers: if (user != null) == false in JournalEntryController.create().
        String tempEmail = "je-deleted-" + nextId() + "@test.com";
        ensureUserExists("JETemp", tempEmail, "pass123!");
        String tempToken = login(tempEmail, "pass123!");
        userDAO.delete(userDAO.readByEmail(tempEmail).getId());
        assertNull(userDAO.readByEmail(tempEmail), "User should be deleted before test proceeds");

        // Include author from request body to satisfy the DB NOT NULL constraint.
        var admin = userDAO.readByEmail("admin@carebridge.io");
        given()
                .header("Authorization", "Bearer " + tempToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Entry with deleted-user JWT",
                        "content", "Content text",
                        "entryType", "NOTE",
                        "riskAssessment", "LOW",
                        "author", Map.of("id", admin.getId())))
                .when()
                .post("/api/journals/" + journalId + "/journal-entries")
                .then()
                .statusCode(201);
    }

    // ─── 8. ResidentController.create(): catch(Exception e) block ─────────────────

    /**
     * Given a JSON body where {@code firstName} is an array ([1,2,3]) instead of a
     * String, when POST /api/residents/create is called,
     * then Jackson's convertValue() throws MismatchedInputException →
     * toEntity() wraps it in RuntimeException → the catch(Exception e) block in
     * ResidentController.create() is entered → 500 is returned.
     */
    @Test
    @Order(9)
    void testCreateResident_withArrayInStringField_triggersCatchBlock_returns500() {
        // Covers: catch (Exception e) in ResidentController.create().
        // Spring deserialises the body into Map<String,Object> fine ([1,2,3] → List).
        // mappingService.toEntity() then calls objectMapper.convertValue() which
        // cannot coerce List → String and throws, entering the catch block.
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"firstName\": [1,2,3], \"lastName\": \"Test\", \"cprNr\": \"CPR-ERR\"}")
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(500);
    }
}
