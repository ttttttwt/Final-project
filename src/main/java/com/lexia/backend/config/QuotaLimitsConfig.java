package com.lexia.backend.config;

import com.lexia.backend.enums.PlanType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for subscription-based AI quota limits.
 * Configurable via application.yml with prefix "lexia.quota".
 * 
 * <p>
 * Quota Limits by Plan:
 * </p>
 * <ul>
 * <li>Free: 10 roleplay sessions, 10 flashcard decks, 75 exercises, 100
 * total</li>
 * <li>Pro: 50 roleplay sessions, 30 flashcard decks, 300 exercises, 300
 * total</li>
 * </ul>
 */
@Configuration
@ConfigurationProperties(prefix = "lexia.quota")
@Data
public class QuotaLimitsConfig {

    // ========== Free Tier Limits ==========

    /** Maximum role play sessions per month for Free users */
    private int freeRoleplaySessions = 10;

    /** Maximum flashcard decks generated per month for Free users */
    private int freeFlashcardDecks = 10;

    /** Maximum grammar exercises generated per month for Free users */
    private int freeGrammarExercises = 75;

    /** Maximum total AI requests per month for Free users */
    private int freeTotalRequests = 100;

    /** Maximum custom materials per month for Free users (Premium only feature) */
    private int freeCustomMaterials = 0;

    // ========== Pro Tier Limits ==========

    /** Maximum role play sessions per month for Pro users */
    private int proRoleplaySessions = 50;

    /** Maximum flashcard decks generated per month for Pro users */
    private int proFlashcardDecks = 30;

    /** Maximum grammar exercises generated per month for Pro users */
    private int proGrammarExercises = 300;

    /** Maximum total AI requests per month for Pro users */
    private int proTotalRequests = 300;

    /** Maximum custom materials per month for Pro users */
    private int proCustomMaterials = 10;

    // ========== Warning Thresholds ==========

    /** Percentage threshold for showing quota warning (0.0-1.0) */
    private double warningThreshold = 0.80;

    /** Percentage threshold for critical warning (0.0-1.0) */
    private double criticalThreshold = 0.95;

    /**
     * Gets the quota limits for a specific plan type.
     * 
     * @param planType The user's subscription plan type
     * @return QuotaLimits containing the limits for that plan
     */
    public QuotaLimits getForPlan(PlanType planType) {
        if (planType != null && planType.isPro()) {
            return new QuotaLimits(
                    proRoleplaySessions,
                    proFlashcardDecks,
                    proGrammarExercises,
                    proTotalRequests,
                    proCustomMaterials);
        }
        return new QuotaLimits(
                freeRoleplaySessions,
                freeFlashcardDecks,
                freeGrammarExercises,
                freeTotalRequests,
                freeCustomMaterials);
    }

    /**
     * Inner class representing quota limits for a plan.
     */
    @Data
    public static class QuotaLimits {
        private final int roleplaySessions;
        private final int flashcardDecks;
        private final int grammarExercises;
        private final int totalRequests;
        private final Integer customMaterialsLimit;

        public QuotaLimits(int roleplaySessions, int flashcardDecks,
                int grammarExercises, int totalRequests, int customMaterials) {
            this.roleplaySessions = roleplaySessions;
            this.flashcardDecks = flashcardDecks;
            this.grammarExercises = grammarExercises;
            this.totalRequests = totalRequests;
            this.customMaterialsLimit = customMaterials;
        }
    }
}
