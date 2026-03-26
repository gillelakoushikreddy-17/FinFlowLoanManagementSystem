package com.finflow.auth_service.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JWT token engine (JwtUtil).
 * Tests token generation, validation, expiry, and tamper detection.
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    // A valid 256-bit Base64-encoded secret key for testing
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JIVE1BQzI1Ng==";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }

    // ==================== TOKEN GENERATION ====================

    @Test
    void testGenerateToken_ReturnsNonNullToken() {
        String token = jwtUtil.generateToken("alice@test.com", "APPLICANT", 1L);

        assertNotNull(token, "Generated token must not be null");
        assertFalse(token.isEmpty(), "Generated token must not be empty");
    }

    @Test
    void testGenerateToken_HasThreeParts() {
        // A valid JWT always has exactly 3 dot-separated parts: header.payload.signature
        String token = jwtUtil.generateToken("alice@test.com", "APPLICANT", 1L);
        String[] parts = token.split("\\.");

        assertEquals(3, parts.length, "JWT must have exactly 3 parts (header.payload.signature)");
    }

    // ==================== TOKEN VALIDATION ====================

    @Test
    void testValidateToken_ValidToken_DoesNotThrowException() {
        String token = jwtUtil.generateToken("alice@test.com", "APPLICANT", 1L);

        // A valid, non-expired token must pass without any exception
        assertDoesNotThrow(() -> jwtUtil.validateToken(token),
                "Valid token should not throw any exception");
    }

    @Test
    void testValidateToken_TamperedSignature_ThrowsSignatureException() {
        String token = jwtUtil.generateToken("alice@test.com", "APPLICANT", 1L);

        // Tamper the signature (last part of the JWT)
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".INVALIDSIGNATURE";

        assertThrows(SignatureException.class,
                () -> jwtUtil.validateToken(tamperedToken),
                "Tampered token signature must throw SignatureException");
    }

    @Test
    void testValidateToken_ExpiredToken_ThrowsExpiredJwtException() {
        // Generate a token that expired 1 millisecond ago
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1L); // already expired

        String expiredToken = expiredJwtUtil.generateToken("alice@test.com", "APPLICANT", 1L);

        assertThrows(ExpiredJwtException.class,
                () -> jwtUtil.validateToken(expiredToken),
                "Expired token must throw ExpiredJwtException");
    }

    @Test
    void testValidateToken_TokenSignedWithWrongSecret_ThrowsSignatureException() {
        // Generate a token with a DIFFERENT secret
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secret",
                "ZGlmZmVyZW50U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaA==");
        ReflectionTestUtils.setField(otherJwtUtil, "expiration", 3600000L);

        String tokenFromOtherService = otherJwtUtil.generateToken("hacker@test.com", "ADMIN", 99L);

        // Our JwtUtil must reject a token signed by a different secret
        assertThrows(SignatureException.class,
                () -> jwtUtil.validateToken(tokenFromOtherService),
                "Token from different secret must be rejected");
    }
}
