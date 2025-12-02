package com.lexia.backend.email.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing unsubscribe tokens.
 * Provides secure token generation and validation for one-click unsubscribe.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface UnsubscribeTokenService {

    /**
     * Generates a secure unsubscribe token for a user.
     *
     * @param userId the user ID
     * @return the generated token
     */
    String generateToken(UUID userId);

    /**
     * Validates an unsubscribe token.
     *
     * @param token the token to validate
     * @return the user ID if valid, empty if invalid or expired
     */
    Optional<UUID> validateToken(String token);

    /**
     * Gets or creates an unsubscribe token for a user.
     *
     * @param userId the user ID
     * @return the token
     */
    String getOrCreateToken(UUID userId);
}
