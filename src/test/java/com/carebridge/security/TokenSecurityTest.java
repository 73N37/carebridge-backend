package com.carebridge.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TokenSecurityTest {

    private static final String SECRET = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";
    private final TokenSecurity tokenSecurity = new TokenSecurity();

    @Test
    void createValidateAndParseToken() throws Exception {
        String token = tokenSecurity.createToken("alice@example.com", "ADMIN,USER", "carebridge", "60000", SECRET);

        assertTrue(tokenSecurity.tokenIsValid(token, SECRET));
        assertTrue(tokenSecurity.tokenNotExpired(token));
        assertTrue(tokenSecurity.timeToExpire(token) > 0);

        Map<String, Object> claims = tokenSecurity.getUserWithRolesFromToken(token);
        assertEquals("alice@example.com", claims.get("username"));
        assertEquals(Set.of("ADMIN", "USER"), claims.get("roles"));
    }

    @Test
    void getUserWithRolesFromTokenThrowsWhenRolesMissing() throws Exception {
        String token = tokenWithoutRoles();

        ParseException exception = assertThrows(ParseException.class, () -> tokenSecurity.getUserWithRolesFromToken(token));
        assertEquals("No roles found in token", exception.getMessage());
    }

    @Test
    void getUserWithRolesFromTokenThrowsWhenRolesEmpty() throws Exception {
        String token = tokenWithEmptyRoles();

        ParseException exception = assertThrows(ParseException.class, () -> tokenSecurity.getUserWithRolesFromToken(token));
        assertEquals("No roles found in token", exception.getMessage());
    }

    @Test
    void tokenIsValidReturnsFalseWithWrongSecret() throws Exception {
        String token = tokenSecurity.createToken("bob@example.com", "USER", "carebridge", "60000", SECRET);
        assertFalse(tokenSecurity.tokenIsValid(token, SECRET + "_wrong"));
    }

    @Test
    void tokenIsValidThrowsForInvalidVerifierSecretLength() {
        String token = tokenSecurity.createToken("eve@example.com", "USER", "carebridge", "60000", SECRET);
        assertThrows(TokenVerificationException.class, () -> tokenSecurity.tokenIsValid(token, "short"));
    }

    @Test
    void tokenNotExpiredReturnsFalseWhenExpired() throws Exception {
        String token = tokenSecurity.createToken("old@example.com", "USER", "carebridge", "-1", SECRET);
        assertFalse(tokenSecurity.tokenNotExpired(token));
        assertTrue(tokenSecurity.timeToExpire(token) <= 0);
    }

    @Test
    void createTokenThrowsWhenSignerSecretTooShort() {
        assertThrows(TokenCreationException.class, () ->
                tokenSecurity.createToken("x@example.com", "USER", "carebridge", "1000", "short")
        );
    }

    private String tokenWithoutRoles() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("noroles@example.com")
                .issuer("carebridge")
                .claim("username", "noroles@example.com")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build();
        return sign(claims);
    }

    private String tokenWithEmptyRoles() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("emptyroles@example.com")
                .issuer("carebridge")
                .claim("username", "emptyroles@example.com")
                .claim("roles", "")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build();
        return sign(claims);
    }

    private String sign(JWTClaimsSet claims) throws Exception {
        JWSObject jws = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claims.toJSONObject()));
        jws.sign(new MACSigner(SECRET));
        return jws.serialize();
    }
}
