package com.lexia.backend.dto.ai;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for user AI quota information.
 * Used to display quota usage and limits in frontend dashboards.
 */
@Data
public class UserAiQuotaDTO {
    private UUID userId;
    private String userEmail;
    private String userFullName;

    private Integer dailyLimit;
    private Integer dailyUsed;
    private Instant lastResetDaily;

    private Integer monthlyLimit;
    private Integer monthlyUsed;
    private Instant lastResetMonthly;

    private Boolean isPremium;
    private Boolean suspended;
    private Boolean isUnlimited;

    // ========== Subscription-Based Quota ==========

    /** User's subscription plan: FREE, MONTHLY, YEARLY */
    private String planType;

    /** Date when monthly quota resets */
    private LocalDate quotaResetDate;

    /** Days until next quota reset */
    private Integer daysUntilReset;

    // ========== Session/Deck/Exercise Counters ==========

    /** Role play sessions used/limit for current month */
    private Integer roleplaySessionsUsed;
    private Integer roleplaySessionsLimit;

    /** Flashcard decks generated used/limit for current month */
    private Integer flashcardDecksUsed;
    private Integer flashcardDecksLimit;

    /** Grammar exercises generated used/limit for current month */
    private Integer grammarExercisesUsed;
    private Integer grammarExercisesLimit;

    /** Total AI requests used/limit for current month */
    private Integer totalRequestsUsed;
    private Integer totalRequestsLimit;

    // ========== Warning Flags ==========

    /** True if any quota is above 80% usage */
    private Boolean quotaWarning;

    /** True if any quota is above 95% usage */
    private Boolean quotaCritical;

    // ========== Legacy Feature-Specific Fields ==========

    // Feature specific (daily)
    private Integer rolePlayDailyLimit;
    private Integer rolePlayUsedToday;
    private Integer rolePlayMonthlyLimit;
    private Integer rolePlayUsedMonth;

    private Integer grammarDailyLimit;
    private Integer grammarUsedToday;
    private Integer grammarMonthlyLimit;
    private Integer grammarUsedMonth;

    private Integer flashcardDailyLimit;
    private Integer flashcardUsedToday;
    private Integer flashcardMonthlyLimit;
    private Integer flashcardUsedMonth;

    // Total used today (for the badge logic in frontend)
    private Integer totalUsedToday;
    private Integer totalDailyLimit;
}
