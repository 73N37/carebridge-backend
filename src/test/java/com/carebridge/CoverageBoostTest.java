package com.carebridge;

import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.ResourceMetadata;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.crud.logic.core.CrudInterceptor;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.GlobalExceptionHandler;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.security.TokenSecurity;
import com.carebridge.security.TokenVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests (no Spring context) targeting small classes and missed branches
 * that cannot easily be reached via REST tests alone.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoverageBoostTest {

    // ─── BaseService.Page ────────────────────────────────────────────────────

    @Test
    void testBaseServicePage_constructorAndGetters() {
        List<String> items = List.of("a", "b", "c");
        BaseService.Page<String> page = new BaseService.Page<>(items, 42L);
        assertEquals(items, page.getContent());
        assertEquals(42L, page.getTotalElements());
    }

    // ─── TokenVerificationException ──────────────────────────────────────────

    @Test
    void testTokenVerificationException() {
        Throwable cause = new RuntimeException("underlying cause");
        TokenVerificationException ex = new TokenVerificationException("verification failed", cause);
        assertEquals("verification failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // ─── CrudInterceptor default methods ─────────────────────────────────────

    @Test
    void testCrudInterceptorDefaultMethods() {
        // Creates an anonymous implementation using only the default methods.
        CrudInterceptor<User> interceptor = new CrudInterceptor<>() {};
        User u = new User();
        // All default methods are no-ops; calling them should not throw
        assertDoesNotThrow(() -> {
            interceptor.beforeCreate(u);
            interceptor.afterCreate(u);
            interceptor.beforeUpdate(u);
            interceptor.afterUpdate(u);
            interceptor.beforeDelete(1L);
            interceptor.afterDelete(1L);
        });
    }

    // ─── ResourceMetadata.FieldInfo getters ──────────────────────────────────

    @Test
    void testResourceMetadataFieldInfo_getters() {
        Map<String, Object> constraints = Map.of("min", 1, "max", 255);
        ResourceMetadata.FieldInfo fi = new ResourceMetadata.FieldInfo(
                "username", "String", true, constraints);

        assertEquals("username", fi.getName());
        assertEquals("String", fi.getType());
        assertTrue(fi.isRequired());
        assertEquals(constraints, fi.getConstraints());
    }

    @Test
    void testResourceMetadataFieldInfo_notRequired() {
        ResourceMetadata.FieldInfo fi = new ResourceMetadata.FieldInfo(
                "optionalField", "Integer", false, Map.of());
        assertFalse(fi.isRequired());
    }

    // ─── MappingService ───────────────────────────────────────────────────────

    private MappingService buildMappingService() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new MappingService(mapper);
    }

    @Test
    void testMappingService_toMapNull() {
        // Covers: if (entity == null) return null;
        MappingService ms = buildMappingService();
        assertNull(ms.toMap(null));
    }

    @Test
    void testMappingService_toMapListNull() {
        // Covers: if (entities == null) return Collections.emptyList();
        MappingService ms = buildMappingService();
        List<Map<String, Object>> result = ms.toMapList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMappingService_toMapListEmpty() {
        MappingService ms = buildMappingService();
        List<Map<String, Object>> result = ms.toMapList(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMappingService_toEntity_conversionError() {
        // Covers: catch (Exception e) → throw new RuntimeException("Conversion failed", e)
        MappingService ms = buildMappingService();
        // Passing data that cannot be converted to User (e.g., invalid enum)
        Map<String, Object> bad = Map.of("role", "INVALID_ROLE_VALUE_XYZ");
        assertThrows(RuntimeException.class, () -> ms.toEntity(bad, User.class));
    }

    // ─── GlobalExceptionHandler – null message branches ──────────────────────

    @Test
    void testGlobalExceptionHandler_handleNotFound_nullMessage() {
        // Covers: ex.getMessage() == null → msg = "Not found"
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<?> resp = handler.handleNotFound(new EntityNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("Not found", body.get("error"));
    }

    @Test
    void testGlobalExceptionHandler_handleApiRuntime_nullMessage() {
        // Covers: ex.getMessage() == null → msg = "API Error"
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<?> resp = handler.handleApiRuntime(new ApiRuntimeException(422, null));
        assertEquals(422, resp.getStatusCode().value());
    }

    // ─── TokenSecurity – JOSEException branch in tokenIsValid ────────────────

    @Test
    void testTokenIsValid_throwsTokenVerificationException_whenSecretTooShort() throws Exception {
        // MACVerifier constructor throws KeyLengthException (JOSEException) for secrets < 32 bytes,
        // which is caught and re-thrown as TokenVerificationException.
        TokenSecurity ts = new TokenSecurity();
        String longSecret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";
        String token = ts.createToken("user", "ADMIN", "issuer", "3600000", longSecret);
        // "x" is too short (1 byte) for HS256 HMAC → KeyLengthException → TokenVerificationException
        assertThrows(TokenVerificationException.class, () -> ts.tokenIsValid(token, "x"));
    }
}
