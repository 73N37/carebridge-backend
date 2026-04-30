package com.carebridge.security;

import org.junit.jupiter.api.*;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional coverage tests for TokenSecurity edge cases not covered by the main TokenSecurityTest.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenSecurityCoverageTest {

    private final TokenSecurity tokenSecurity = new TokenSecurity();
    private final String secret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";

    @Test
    @Order(1)
    void testTokenIsValid_catchesJoseException_shortKey() {
        // MACVerifier with a key shorter than 256 bits throws KeyLengthException (a JOSEException)
        // → caught → wrapped in TokenVerificationException
        String validToken = tokenSecurity.createToken("user", "ADMIN", "test", "3600000", secret);
        assertThrows(TokenVerificationException.class, () ->
                tokenSecurity.tokenIsValid(validToken, "SHORT")
        );
    }

    @Test
    @Order(2)
    void testGetUserWithRolesFromToken_nullRolesCsv() throws Exception {
        // createToken with null rolesCsv → claim("roles", null) → getStringClaim returns null
        // → rolesCsv == null branch → ParseException
        String nullRolesToken = tokenSecurity.createToken("user", null, "test", "3600000", secret);
        ParseException ex = assertThrows(ParseException.class, () ->
                tokenSecurity.getUserWithRolesFromToken(nullRolesToken)
        );
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    @Order(3)
    void testGetUserWithRolesFromToken_emptyRolesCsv() throws Exception {
        // createToken with empty roles → isEmpty() branch → ParseException
        String noRolesToken = tokenSecurity.createToken("user", "", "test", "3600000", secret);
        ParseException ex = assertThrows(ParseException.class, () ->
                tokenSecurity.getUserWithRolesFromToken(noRolesToken)
        );
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    @Order(4)
    void testTokenCreationException_message() {
        TokenCreationException ex = new TokenCreationException("test-msg", new RuntimeException("cause"));
        assertEquals("test-msg", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    @Order(5)
    void testTokenVerificationException_message() {
        TokenVerificationException ex = new TokenVerificationException("verify-msg", new RuntimeException("cause"));
        assertEquals("verify-msg", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}
