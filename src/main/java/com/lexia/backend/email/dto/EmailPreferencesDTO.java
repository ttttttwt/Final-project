package com.lexia.backend.email.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user email preferences.
 * Controls which types of emails a user wants to receive.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailPreferencesDTO {

    /**
     * User ID these preferences belong to.
     */
    private UUID userId;

    /**
     * Email verification emails (always true, cannot be disabled).
     */
    @Builder.Default
    private boolean emailVerificationEnabled = true;

    /**
     * Security alert emails (password changes, suspicious login).
     */
    @NotNull
    @Builder.Default
    private boolean securityAlertsEnabled = true;

    /**
     * Weekly progress digest emails.
     */
    @NotNull
    @Builder.Default
    private boolean weeklyDigestEnabled = true;

    /**
     * Streak reminder emails when streak is at risk.
     */
    @NotNull
    @Builder.Default
    private boolean streakRemindersEnabled = true;

    /**
     * Achievement emails (level up, milestones).
     */
    @NotNull
    @Builder.Default
    private boolean achievementsEnabled = true;

    /**
     * Course-related emails (enrollment, completion).
     */
    @NotNull
    @Builder.Default
    private boolean courseUpdatesEnabled = true;

    /**
     * System announcement emails.
     */
    @NotNull
    @Builder.Default
    private boolean announcementsEnabled = true;

    /**
     * Last update timestamp.
     */
    private Instant updatedAt;
}
