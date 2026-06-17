package com.kiniot.uflex.api.iam.infrastructure.tokens.jwt.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceImplTests {

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";

    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        ReflectionTestUtils.setField(tokenService, "expirationDays", 7);
    }

    @Test
    void generatedTokenIsValidAndPreservesUserSubject() {
        var token = tokenService.generateToken(
                "user-123",
                "patient@example.com",
                List.of("ROLE_PATIENT"),
                "tenant-456"
        );

        assertTrue(tokenService.validateToken(token));
        assertEquals("user-123", tokenService.getUsernameFromToken(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        var token = tokenService.generateToken("user-123");
        ReflectionTestUtils.setField(
                tokenService,
                "secret",
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ab"
        );

        assertFalse(tokenService.validateToken(token));
    }

    @Test
    void bearerTokenIsExtractedOnlyFromBearerAuthorizationHeader() {
        var bearerRequest = new MockHttpServletRequest();
        bearerRequest.addHeader("Authorization", "Bearer token-value");
        var basicRequest = new MockHttpServletRequest();
        basicRequest.addHeader("Authorization", "Basic credentials");

        assertEquals("token-value", tokenService.getBearerTokenFrom(bearerRequest));
        assertNull(tokenService.getBearerTokenFrom(basicRequest));
        assertNull(tokenService.getBearerTokenFrom(new MockHttpServletRequest()));
    }
}
