package com.lexia.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for subscription plan quota limits.
 * Used by admin to view and edit Free/Pro plan quotas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanLimitsDTO {

    // ========== Free Tier Limits ==========

    /** Maximum role play sessions per month for Free users */
    private int freeRoleplaySessions;

    /** Maximum flashcard decks generated per month for Free users */
    private int freeFlashcardDecks;

    /** Maximum grammar exercises generated per month for Free users */
    private int freeGrammarExercises;

    /** Maximum total AI requests per month for Free users */
    private int freeTotalRequests;

    // ========== Pro Tier Limits ==========

    /** Maximum role play sessions per month for Pro users */
    private int proRoleplaySessions;

    /** Maximum flashcard decks generated per month for Pro users */
    private int proFlashcardDecks;

    /** Maximum grammar exercises generated per month for Pro users */
    private int proGrammarExercises;

    /** Maximum total AI requests per month for Pro users */
    private int proTotalRequests;

    // ========== Warning Thresholds ==========

    /** Percentage threshold for showing quota warning (0-100) */
    private int warningThresholdPercent;

    /** Percentage threshold for critical warning (0-100) */
    private int criticalThresholdPercent;
}
