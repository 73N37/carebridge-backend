package com.carebridge;

import com.carebridge.controllers.security.SecurityController;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.DynamicDtoAdvice;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.dao.security.ISecurityDAO;
import com.carebridge.security.TokenSecurity;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests (no Spring context) covering the last remaining missed branches
 * that cannot be reached via REST integration tests alone.
 */
class FinalCoverageBoostTest {

    // ─── Inner entity subclasses used for MappingService.toMap() branch coverage ──

    /** Parent entity with a field that will be shadowed by ChildForShadow. */
    static class ParentForShadow extends BaseEntity {
        String shadow = "parent-value";
    }

    /**
     * Child entity whose 'shadow' field shadows the identically-named field in
     * ParentForShadow.  When toMap() processes the superclass fields it will find
     * that "shadow" is already in the result map and execute the {@code continue}
     * branch (result.containsKey() == true).
     */
    static class ChildForShadow extends ParentForShadow {
        String shadow = "child-value";
    }

    /**
     * Entity with a {@code List<String>} collection field.  String is NOT a
     * BaseEntity, so when toMap() iterates the collection the inner
     * {@code item instanceof BaseEntity} check is false for every element and the
     * {@code ids} list stays empty, covering the {@code !ids.isEmpty() == false}
     * branch as well.
     */
    static class EntityWithNonEntityCollection extends BaseEntity {
        List<String> tags = List.of("alpha", "beta", "gamma");
    }

    private static final String TEST_JWT_SECRET = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private MappingService buildMappingService() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new MappingService(mapper);
    }

    // ─── MappingService.toMap() – duplicate field (result.containsKey == true) ───

    /**
     * Given a ChildForShadow entity with a 'shadow' field that also exists in
     * ParentForShadow, when toMap() is called, then the superclass duplicate is
     * skipped via {@code result.containsKey(field.getName()) → continue}.
     */
    @Test
    void testToMap_duplicateFieldInSuperclass_isSkipped() {
        ChildForShadow child = new ChildForShadow();
        MappingService ms = buildMappingService();

        Map<String, Object> result = ms.toMap(child);

        assertNotNull(result);
        // The child's value wins; the parent duplicate is skipped.
        assertEquals("child-value", result.get("shadow"),
                "Child field value should be kept; superclass duplicate should be skipped");
    }

    // ─── MappingService.toMap() – non-entity collection items (ids stays empty) ──

    /**
     * Given an entity with a List<String> field, when toMap() is called, then
     * {@code item instanceof BaseEntity == false} is exercised for every element and
     * the resulting empty ids list means {@code !ids.isEmpty() == false → skip}.
     */
    @Test
    void testToMap_collectionWithNonEntityItems_keysNotAdded() {
        EntityWithNonEntityCollection entity = new EntityWithNonEntityCollection();
        MappingService ms = buildMappingService();

        Map<String, Object> result = ms.toMap(entity);

        assertNotNull(result);
        // Non-entity items are never added to ids → empty ids → nothing put into result.
        assertFalse(result.containsKey("tagsIds"),
                "Non-entity collection items should produce no '*Ids' key");
    }

    // ─── DynamicDtoAdvice – 'return body' path (not null, not entity/collection/page) ─

    /**
     * Given a plain String body (not a BaseEntity, Collection, or BaseService.Page),
     * when beforeBodyWrite() is called, then the method falls through all type checks
     * and returns the body unchanged.
     */
    @Test
    void testDynamicDtoAdvice_returnBodyUnchanged_whenBodyIsPlainObject() {
        MappingService mockMs = mock(MappingService.class);
        DynamicDtoAdvice advice = new DynamicDtoAdvice(mockMs);

        String plainBody = "plain string response";
        Object result = advice.beforeBodyWrite(
                plainBody, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null);

        assertEquals(plainBody, result);
        verify(mockMs, never()).toMap(any());
    }

    // ─── DynamicDtoAdvice – Page lambda false branch (item is not a BaseEntity) ──

    /**
     * Given a BaseService.Page whose content is a list of Maps (not BaseEntity),
     * when beforeBodyWrite() is called, then the page-content lambda executes the
     * {@code instanceof BaseEntity == false} branch and returns the Map item as-is.
     */
    @Test
    void testDynamicDtoAdvice_pageWithNonEntityContent_lambdaFalseBranch() {
        MappingService mockMs = mock(MappingService.class);
        DynamicDtoAdvice advice = new DynamicDtoAdvice(mockMs);

        Map<String, Object> mapItem = Map.of("key", "value");
        BaseService.Page<Object> page = new BaseService.Page<>(List.of(mapItem), 1L);

        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) advice.beforeBodyWrite(
                page, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null);

        assertNotNull(resultMap);
        @SuppressWarnings("unchecked")
        List<Object> content = (List<Object>) resultMap.get("content");
        assertEquals(1, content.size());
        assertEquals(mapItem, content.get(0),
                "Non-entity page item should be cast to Map and returned unchanged");
        verify(mockMs, never()).toMap(any());
    }

    // ─── TokenSecurity – rolesCsv == null branch ─────────────────────────────────

    /**
     * Given a JWT that carries no 'roles' claim, when getUserWithRolesFromToken() is
     * called, then it throws ParseException because rolesCsv is null.
     */
    @Test
    void testGetUserWithRolesFromToken_noRolesClaim_throwsParseException() throws Exception {
        // Build a minimal JWT without the 'roles' claim.
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("username", "user@test.com")
                // Intentionally omit 'roles' → rolesCsv will be null.
                .build();
        JWSObject jws = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256),
                new Payload(claims.toJSONObject()));
        jws.sign(new MACSigner(TEST_JWT_SECRET));
        String tokenWithoutRoles = jws.serialize();

        TokenSecurity ts = new TokenSecurity();

        assertThrows(ParseException.class,
                () -> ts.getUserWithRolesFromToken(tokenWithoutRoles),
                "Should throw ParseException when 'roles' claim is absent");
    }

    // ─── SecurityController.login() – catch(Exception e) block ──────────────────

    /**
     * Given a SecurityDAO that throws a plain RuntimeException (not a
     * ValidationException), when login() is called, then the generic catch block is
     * hit and a 500 INTERNAL_SERVER_ERROR response is returned.
     */
    @Test
    void testLogin_unexpectedRuntimeException_returns500() throws Exception {
        ISecurityDAO mockDao = mock(ISecurityDAO.class);
        when(mockDao.getVerifiedUser(any(), any()))
                .thenThrow(new RuntimeException("Unexpected DB failure"));

        SecurityController ctrl = new SecurityController(mockDao);
        // The @Value fields (issuer, secret, expireMillis) are null in a unit test,
        // but the RuntimeException is thrown before createTokenInternal() is reached.
        ResponseEntity<Map<String, String>> response =
                ctrl.login(Map.of("email", "test@x.com", "password", "pass"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal error", response.getBody().get("msg"));
    }

    // ─── CareBridgeApplication.main() ────────────────────────────────────────────

    /**
     * Given a mocked SpringApplication, when CareBridgeApplication.main() is called,
     * then SpringApplication.run() is invoked with the correct arguments, covering
     * the main() method instruction that is otherwise unreachable in normal test runs.
     */
    @Test
    void testMain_delegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked =
                     mockStatic(SpringApplication.class)) {

            mocked.when(() -> SpringApplication.run(
                            any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            assertDoesNotThrow(() -> CareBridgeApplication.main(new String[]{}));

            mocked.verify(() ->
                    SpringApplication.run(CareBridgeApplication.class, new String[]{}));
        }
    }
}
