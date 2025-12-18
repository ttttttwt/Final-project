package com.lexia.backend.dto.ai;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

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

    // Feature specific
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
