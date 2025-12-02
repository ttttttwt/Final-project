package com.lexia.backend.email.controller;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.repository.EmailLogRepository;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Email Tracking.
 * Handles email open/click tracking and unsubscribe requests.
 *
 * <p>
 * Base paths:
 * </p>
 * <ul>
 * <li>/api/v1/email/track - Tracking endpoints (no auth required)</li>
 * <li>/api/v1/email/unsubscribe - Unsubscribe endpoints</li>
 * </ul>
 *
 * <p>
 * Security: Tracking endpoints use signed tokens instead of authentication.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@RestController
@RequestMapping("/api/v1/email")
@Tag(name = "Email Tracking API", description = "Endpoints for email tracking and unsubscribe")
@RequiredArgsConstructor
@Slf4j
public class EmailTrackingController {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // 1x1 transparent GIF pixel for open tracking
    private static final byte[] TRACKING_PIXEL = Base64.getDecoder().decode(
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    private final EmailLogRepository emailLogRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final EmailConfig emailConfig;

    /**
     * Track email open (pixel tracking).
     * Returns a 1x1 transparent GIF image.
     *
     * @param token signed tracking token containing email log ID
     * @return transparent GIF pixel
     */
    @GetMapping(value = "/track/open/{token}", produces = "image/gif")
    @Operation(summary = "Track email open", description = "Records that an email was opened. Used by tracking pixel in email templates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking pixel returned"),
            @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    public ResponseEntity<byte[]> trackOpen(
            @Parameter(description = "Signed tracking token") @PathVariable String token) {

        try {
            UUID emailLogId = validateAndExtractToken(token);
            if (emailLogId == null) {
                log.warn("Invalid open tracking token");
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("image/gif"))
                        .body(TRACKING_PIXEL);
            }

            // Record open event
            emailLogRepository.findById(emailLogId).ifPresent(log -> {
                if (log.getOpenedAt() == null) {
                    log.setOpenedAt(java.time.Instant.now());
                    emailLogRepository.save(log);
                    EmailTrackingController.log.debug("Email open tracked: {}", emailLogId);
                }
            });

        } catch (Exception e) {
            log.warn("Error processing open tracking: {}", e.getMessage());
        }

        // Always return the pixel regardless of tracking success
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/gif"))
                .cacheControl(org.springframework.http.CacheControl.noCache())
                .body(TRACKING_PIXEL);
    }

    /**
     * Track email link click.
     * Redirects to the target URL after recording the click.
     *
     * @param token     signed tracking token containing email log ID
     * @param targetUrl the destination URL to redirect to
     * @return redirect response
     */
    @GetMapping("/track/click/{token}")
    @Operation(summary = "Track email link click", description = "Records link clicks and redirects to the target URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to target URL"),
            @ApiResponse(responseCode = "400", description = "Invalid token or missing target URL")
    })
    public ResponseEntity<Void> trackClick(
            @Parameter(description = "Signed tracking token") @PathVariable String token,
            @Parameter(description = "Target URL to redirect to") @RequestParam String targetUrl) {

        try {
            UUID emailLogId = validateAndExtractToken(token);
            if (emailLogId != null) {
                // Record click event
                emailLogRepository.findById(emailLogId).ifPresent(log -> {
                    if (log.getClickedAt() == null) {
                        log.setClickedAt(java.time.Instant.now());
                        // Also mark as opened if not already
                        if (log.getOpenedAt() == null) {
                            log.setOpenedAt(java.time.Instant.now());
                        }
                        emailLogRepository.save(log);
                        EmailTrackingController.log.debug("Email click tracked: {}", emailLogId);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Error processing click tracking: {}", e.getMessage());
        }

        // Redirect to target URL
        if (targetUrl == null || targetUrl.isBlank()) {
            targetUrl = emailConfig.getBaseUrl();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, targetUrl)
                .build();
    }

    /**
     * Handle unsubscribe request from email link.
     * Disables email notifications for the user.
     *
     * @param token signed unsubscribe token containing user ID
     * @return redirect to unsubscribe confirmation page
     */
    @GetMapping("/unsubscribe/{token}")
    @Operation(summary = "Process unsubscribe request", description = "Unsubscribes user from email notifications using signed token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to confirmation page"),
            @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "Signed unsubscribe token") @PathVariable String token) {

        try {
            UUID userId = validateAndExtractUnsubscribeToken(token);
            if (userId == null) {
                log.warn("Invalid unsubscribe token");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, emailConfig.getBaseUrl() + "/unsubscribe?error=invalid")
                        .build();
            }

            // Disable email notifications
            Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
            if (prefsOpt.isPresent()) {
                NotificationPreferences prefs = prefsOpt.get();
                prefs.setEmailEnabled(false);
                preferencesRepository.save(prefs);
                log.info("User unsubscribed from emails: {}", userId);
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, emailConfig.getBaseUrl() + "/unsubscribe?success=true")
                    .build();

        } catch (Exception e) {
            log.error("Error processing unsubscribe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, emailConfig.getBaseUrl() + "/unsubscribe?error=processing")
                    .build();
        }
    }

    /**
     * Unsubscribe from specific email category.
     *
     * @param token    signed unsubscribe token
     * @param category the category to unsubscribe from
     * @return redirect to confirmation page
     */
    @GetMapping("/unsubscribe/{token}/category/{category}")
    @Operation(summary = "Unsubscribe from category", description = "Unsubscribes user from a specific email category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to confirmation page"),
            @ApiResponse(responseCode = "400", description = "Invalid token or category")
    })
    public ResponseEntity<Void> unsubscribeFromCategory(
            @Parameter(description = "Signed unsubscribe token") @PathVariable String token,
            @Parameter(description = "Category to unsubscribe from") @PathVariable String category) {

        try {
            UUID userId = validateAndExtractUnsubscribeToken(token);
            if (userId == null) {
                log.warn("Invalid unsubscribe token for category");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, emailConfig.getBaseUrl() + "/unsubscribe?error=invalid")
                        .build();
            }

            // Update category preference
            Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
            if (prefsOpt.isPresent()) {
                NotificationPreferences prefs = prefsOpt.get();
                updateCategoryPreference(prefs, category, false);
                preferencesRepository.save(prefs);
                log.info("User unsubscribed from category {}: {}", category, userId);
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION,
                            emailConfig.getBaseUrl() + "/unsubscribe?success=true&category=" + category)
                    .build();

        } catch (Exception e) {
            log.error("Error processing category unsubscribe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, emailConfig.getBaseUrl() + "/unsubscribe?error=processing")
                    .build();
        }
    }

    /**
     * Generate tracking token for an email log.
     * Used internally when sending emails.
     *
     * @param emailLogId the email log ID
     * @return signed tracking token
     */
    public String generateTrackingToken(UUID emailLogId) {
        return signToken(emailLogId.toString());
    }

    /**
     * Generate unsubscribe token for a user.
     * Used internally when sending emails.
     *
     * @param userId the user ID
     * @return signed unsubscribe token
     */
    public String generateUnsubscribeToken(UUID userId) {
        return signToken("unsub:" + userId.toString());
    }

    /**
     * Sign a payload using HMAC-SHA256.
     */
    private String signToken(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    emailConfig.getUnsubscribeSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
            String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            return payloadBase64 + "." + signatureBase64;
        } catch (Exception e) {
            log.error("Error signing token", e);
            return null;
        }
    }

    /**
     * Validate and extract email log ID from tracking token.
     */
    private UUID validateAndExtractToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            String payloadBase64 = parts[0];
            String signatureBase64 = parts[1];

            String payload = new String(Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);

            // Verify signature
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    emailConfig.getUnsubscribeSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] expectedSignature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSignature);

            if (!signatureBase64.equals(expectedSignatureBase64)) {
                return null;
            }

            return UUID.fromString(payload);
        } catch (Exception e) {
            log.debug("Invalid tracking token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate and extract user ID from unsubscribe token.
     */
    private UUID validateAndExtractUnsubscribeToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            String payloadBase64 = parts[0];
            String signatureBase64 = parts[1];

            String payload = new String(Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);

            // Verify signature
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    emailConfig.getUnsubscribeSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] expectedSignature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSignature);

            if (!signatureBase64.equals(expectedSignatureBase64)) {
                return null;
            }

            // Extract user ID from payload
            if (!payload.startsWith("unsub:")) {
                return null;
            }
            return UUID.fromString(payload.substring(6));
        } catch (Exception e) {
            log.debug("Invalid unsubscribe token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Update notification preference for a specific category.
     */
    private void updateCategoryPreference(NotificationPreferences prefs, String category, boolean enabled) {
        switch (category.toLowerCase()) {
            case "learning" -> prefs.setLearningEnabled(enabled);
            case "achievements" -> prefs.setAchievementsEnabled(enabled);
            case "reminders", "engagement" -> prefs.setRemindersEnabled(enabled);
            case "system" -> prefs.setSystemEnabled(enabled);
            default -> log.warn("Unknown email category: {}", category);
        }
    }
}
