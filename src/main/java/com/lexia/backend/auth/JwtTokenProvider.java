package com.lexia.backend.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider for LEXIA authentication system.
 * Handles generation, validation, and parsing of JWT access and refresh tokens.
 *
 * Security features:
 * - Access tokens: 15 minutes expiration
 * - Refresh tokens: 7 days expiration
 * - HMAC-SHA256 signing
 * - Token hashing for secure storage
 */
@Component
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Token expiration times (in milliseconds)
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

    private final SecretKey jwtSecretKey;

    public JwtTokenProvider(
            @Value("${lexia.jwt.secret:lexia-default-jwt-secret-key-for-development-only-change-in-production}") String jwtSecret) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        LOG.info("JWT Token Provider initialized with configured secret");
    }

    /**
     * Generates JWT access token for authenticated user.
     *
     * @param userId user ID
     * @return JWT access token valid for 15 minutes
     */
    public String generateAccessToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer("lexia-backend")
                .audience().add("lexia-api").and()
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * Generates JWT refresh token for authenticated user.
     *
     * @param userId user ID
     * @return JWT refresh token valid for 7 days
     */
    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer("lexia-backend")
                .audience().add("lexia-refresh").and()
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * Validates JWT token signature and expiration.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            LOG.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOG.warn("Invalid JWT token format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOG.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOG.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param token JWT token
     * @return user ID as UUID
     * @throws IllegalArgumentException if token is invalid or user ID cannot be
     *                                  parsed
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userIdStr = claims.getSubject();
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid user ID format in JWT token: {}", userIdStr);
            throw new IllegalArgumentException("Invalid user ID in token");
        }
    }

    /**
     * Extracts email from JWT token.
     *
     * @param token JWT token
     * @return email as String
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    /**
     * Checks if JWT token is expired.
     *
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            LOG.warn("Error checking token expiration: {}", e.getMessage());
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Gets token expiration date.
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date getTokenExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /**
     * Hashes a token using SHA-256 for secure storage in database.
     * Used for refresh tokens to prevent token theft attacks.
     *
     * @param token raw token to hash
     * @return SHA-256 hash of the token
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    /**
     * Generates a cryptographically secure random token.
     * Used for refresh token generation.
     *
     * @return random UUID as string
     */
    public String generateRandomToken() {
        return UUID.randomUUID().toString();
    }
}