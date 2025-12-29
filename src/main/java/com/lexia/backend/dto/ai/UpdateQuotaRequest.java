package com.lexia.backend.dto.ai;

import lombok.Data;

@Data
public class UpdateQuotaRequest {
    private Integer dailyLimit;
    private Integer monthlyLimit;
    private Boolean isPremium;
    private Boolean suspended;

    // Feature specific
    private Integer rolePlayDailyLimit;
    private Integer grammarDailyLimit;
    private Integer flashcardDailyLimit;
    private Integer customMaterialsLimit;
}
