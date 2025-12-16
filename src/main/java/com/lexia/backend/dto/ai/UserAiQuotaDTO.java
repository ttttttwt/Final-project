package com.lexia.backend.dto.ai;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class UserAiQuotaDTO {
    private UUID userId;
    private Integer dailyLimit;
    private Integer dailyUsed;
    private Instant lastResetDaily;
    private Integer monthlyLimit;
    private Integer monthlyUsed;
    private Instant lastResetMonthly;
    private Boolean isPremium;
    private Boolean suspended;
}
