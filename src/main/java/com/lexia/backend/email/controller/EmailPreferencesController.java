package com.lexia.backend.email.controller;

import com.lexia.backend.email.dto.EmailPreferencesDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST Controller for managing user email preferences.
 * Allows users to configure which types of emails they want to receive.
 *
 * <p>
 * Base path: /api/v1/users/me/email-preferences
 * </p>
 *
 * <p>
 * Security: Requires authentication. Users can only manage their own
 * preferences.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@RestController
@RequestMapping("/api/v1/users/me/email-preferences")
@Tag(name = "Email Preferences API", description = "Endpoints for managing user email preferences")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class EmailPreferencesController {

    private final NotificationPreferencesRepository preferencesRepository;

    /**
     * Get current email preferences for the authenticated user.
     *
     * @param user the authenticated user
     * @return the user's email preferences
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get email preferences", description = "Retrieves email preferences for the authenticated user. Returns default values if not yet customized.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved email preferences", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailPreferencesDTO.class), examples = @ExampleObject(value = """
                    {
                      "userId": "550e8400-e29b-41d4-a716-446655440000",
                      "emailVerificationEnabled": true,
                      "securityAlertsEnabled": true,
                      "weeklyDigestEnabled": true,
                      "streakRemindersEnabled": true,
                      "achievementsEnabled": true,
                      "courseUpdatesEnabled": true,
                      "announcementsEnabled": true,
                      "updatedAt": "2025-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<EmailPreferencesDTO> getEmailPreferences(@AuthenticationPrincipal User user) {
        log.info("Getting email preferences for user: {}", user.getEmail());

        NotificationPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        EmailPreferencesDTO dto = mapToEmailPreferencesDTO(prefs);
        return ResponseEntity.ok(dto);
    }

    /**
     * Update email preferences for the authenticated user.
     *
     * @param user    the authenticated user
     * @param request the updated preferences
     * @return the updated email preferences
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update email preferences", description = "Updates email preferences for the authenticated user. Only provided fields will be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated email preferences", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailPreferencesDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EmailPreferencesDTO> updateEmailPreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EmailPreferencesDTO request) {
        log.info("Updating email preferences for user: {}", user.getEmail());

        NotificationPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        // Update preferences based on request
        updatePreferencesFromDTO(prefs, request);

        NotificationPreferences savedPrefs = preferencesRepository.save(prefs);
        log.info("Email preferences updated for user: {}", user.getEmail());

        EmailPreferencesDTO dto = mapToEmailPreferencesDTO(savedPrefs);
        return ResponseEntity.ok(dto);
    }

    /**
     * Reset email preferences to default values.
     *
     * @param user the authenticated user
     * @return the reset email preferences
     */
    @PostMapping(value = "/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reset email preferences", description = "Resets email preferences to default values for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully reset email preferences", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailPreferencesDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EmailPreferencesDTO> resetEmailPreferences(@AuthenticationPrincipal User user) {
        log.info("Resetting email preferences for user: {}", user.getEmail());

        NotificationPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        // Reset to defaults
        prefs.setEmailEnabled(true);
        prefs.setLearningEnabled(true);
        prefs.setAchievementsEnabled(true);
        prefs.setRemindersEnabled(true);
        prefs.setSystemEnabled(true);

        NotificationPreferences savedPrefs = preferencesRepository.save(prefs);
        log.info("Email preferences reset for user: {}", user.getEmail());

        EmailPreferencesDTO dto = mapToEmailPreferencesDTO(savedPrefs);
        return ResponseEntity.ok(dto);
    }

    /**
     * Unsubscribe from all non-mandatory emails.
     *
     * @param user the authenticated user
     * @return the updated email preferences
     */
    @PostMapping(value = "/unsubscribe-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Unsubscribe from all emails", description = "Disables all non-mandatory email notifications. Security alerts and verification emails will still be sent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unsubscribed from all emails", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailPreferencesDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EmailPreferencesDTO> unsubscribeAll(@AuthenticationPrincipal User user) {
        log.info("Unsubscribing from all emails for user: {}", user.getEmail());

        NotificationPreferences prefs = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        // Disable all email categories (mandatory emails still sent)
        prefs.setEmailEnabled(false);
        prefs.setLearningEnabled(false);
        prefs.setAchievementsEnabled(false);
        prefs.setRemindersEnabled(false);
        prefs.setSystemEnabled(false);

        NotificationPreferences savedPrefs = preferencesRepository.save(prefs);
        log.info("User unsubscribed from all emails: {}", user.getEmail());

        EmailPreferencesDTO dto = mapToEmailPreferencesDTO(savedPrefs);
        return ResponseEntity.ok(dto);
    }

    /**
     * Create default notification preferences for a new user.
     */
    private NotificationPreferences createDefaultPreferences(User user) {
        NotificationPreferences prefs = NotificationPreferences.createDefault(user);
        return preferencesRepository.save(prefs);
    }

    /**
     * Map NotificationPreferences to EmailPreferencesDTO.
     * Maps general notification categories to email-specific preferences.
     */
    private EmailPreferencesDTO mapToEmailPreferencesDTO(NotificationPreferences prefs) {
        return EmailPreferencesDTO.builder()
                .userId(prefs.getUserId())
                // Email verification is always enabled (mandatory)
                .emailVerificationEnabled(true)
                // Security alerts are always enabled (mandatory)
                .securityAlertsEnabled(true)
                // Map reminders category to weekly digest and streak reminders
                .weeklyDigestEnabled(Boolean.TRUE.equals(prefs.getRemindersEnabled())
                        && Boolean.TRUE.equals(prefs.getEmailEnabled()))
                .streakRemindersEnabled(Boolean.TRUE.equals(prefs.getRemindersEnabled())
                        && Boolean.TRUE.equals(prefs.getEmailEnabled()))
                // Map achievements category
                .achievementsEnabled(Boolean.TRUE.equals(prefs.getAchievementsEnabled())
                        && Boolean.TRUE.equals(prefs.getEmailEnabled()))
                // Map learning category to course updates
                .courseUpdatesEnabled(Boolean.TRUE.equals(prefs.getLearningEnabled())
                        && Boolean.TRUE.equals(prefs.getEmailEnabled()))
                // Map system category to announcements
                .announcementsEnabled(Boolean.TRUE.equals(prefs.getSystemEnabled())
                        && Boolean.TRUE.equals(prefs.getEmailEnabled()))
                .updatedAt(prefs.getUpdatedAt() != null ? prefs.getUpdatedAt().toInstant() : Instant.now())
                .build();
    }

    /**
     * Update NotificationPreferences from EmailPreferencesDTO.
     * Maps email-specific preferences back to general notification categories.
     */
    private void updatePreferencesFromDTO(NotificationPreferences prefs, EmailPreferencesDTO dto) {
        // Enable email if any non-mandatory email is enabled
        boolean anyEnabled = Boolean.TRUE.equals(dto.isWeeklyDigestEnabled())
                || Boolean.TRUE.equals(dto.isStreakRemindersEnabled())
                || Boolean.TRUE.equals(dto.isAchievementsEnabled())
                || Boolean.TRUE.equals(dto.isCourseUpdatesEnabled())
                || Boolean.TRUE.equals(dto.isAnnouncementsEnabled());

        prefs.setEmailEnabled(anyEnabled);

        // Update category-level preferences
        // Reminders = weekly digest OR streak reminders
        prefs.setRemindersEnabled(Boolean.TRUE.equals(dto.isWeeklyDigestEnabled())
                || Boolean.TRUE.equals(dto.isStreakRemindersEnabled()));

        // Achievements
        prefs.setAchievementsEnabled(dto.isAchievementsEnabled());

        // Learning = course updates
        prefs.setLearningEnabled(dto.isCourseUpdatesEnabled());

        // System = announcements
        prefs.setSystemEnabled(dto.isAnnouncementsEnabled());
    }
}
