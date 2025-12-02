package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.service.UnsubscribeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UnsubscribeTokenService.
 * Generates cryptographically signed tokens for secure unsubscribe links.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class UnsubscribeTokenServiceImpl implements UnsubscribeTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int TOKEN_VALIDITY_DAYS = 365;

    private final String secretKey;

    public UnsubscribeTokenServiceImpl(EmailConfig emailConfig) {
        this.secretKey = emailConfig.getUnsubscribeSecret();
    }

    @Override
    public String generateToken(UUID userId) {
        try {
            long expiry = Instant.now().plus(TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS).toEpochMilli();
            String payload = userId.toString() + ":" + expiry;

            String signature = sign(payload);
            String combined = payload + ":" + signature;

            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    combined.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to generate unsubscribe token for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate unsubscribe token", e);
        }
    }

    @Override
    public Optional<UUID> validateToken(String token) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String combined = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = combined.split(":");

            if (parts.length != 3) {
                log.warn("Invalid token format: wrong number of parts");
                return Optional.empty();
            }

            UUID userId = UUID.fromString(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            String providedSignature = parts[2];

            // Check expiry
            if (Instant.now().toEpochMilli() > expiry) {
                log.debug("Token expired for user {}", userId);
                return Optional.empty();
            }

            // Verify signature
            String payload = parts[0] + ":" + parts[1];
            String expectedSignature = sign(payload);

            if (MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.of(userId);
            } else {
                log.warn("Invalid token signature for user {}", userId);
                return Optional.empty();
            }

        } catch (IllegalArgumentException e) {
            log.warn("Invalid token format: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getOrCreateToken(UUID userId) {
        // For now, just generate a new token each time
        // In production, you might want to cache or store tokens
        return generateToken(userId);
    }

    /**
     * Signs a payload using HMAC-SHA256.
     *
     * @param payload the payload to sign
     * @return the Base64-encoded signature
     */
    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM);
        mac.init(keySpec);
        byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }
}
