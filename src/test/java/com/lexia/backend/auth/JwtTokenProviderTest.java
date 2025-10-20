package com.lexia.backend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "test-jwt-secret-key-for-unit-testing-only-not-for-production-use";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret);
    }

    @Test
    void testGenerateAccessToken_WithValidUserId_ReturnsValidToken() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtTokenProvider.generateAccessToken(userId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void testGenerateRefreshToken_WithValidUserId_ReturnsValidToken() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtTokenProvider.generateRefreshToken(userId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void testValidateToken_WithValidToken_ReturnsTrue() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WithInvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetUserIdFromToken_WithValidToken_ReturnsCorrectUserId() {
        // Arrange
        UUID expectedUserId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(expectedUserId);

        // Act
        UUID actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void testGetUserIdFromToken_WithInvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        });
    }

    @Test
    void testIsTokenExpired_WithValidToken_ReturnsFalse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void testGetTokenExpiration_WithValidToken_ReturnsFutureDate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        // Act
        Date expiration = jwtTokenProvider.getTokenExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testHashToken_WithValidToken_ReturnsHash() {
        // Arrange
        String token = "test-token-to-hash";

        // Act
        String hash = jwtTokenProvider.hashToken(token);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertEquals(64, hash.length()); // SHA-256 produces 64 character hex string
    }

    @Test
    void testGenerateRandomToken_ReturnsValidUUID() {
        // Act
        String randomToken = jwtTokenProvider.generateRandomToken();

        // Assert
        assertNotNull(randomToken);
        assertFalse(randomToken.isEmpty());
        // Should be valid UUID format
        assertDoesNotThrow(() -> UUID.fromString(randomToken));
    }
}