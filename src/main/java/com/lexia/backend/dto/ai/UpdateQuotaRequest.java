package com.lexia.backend.dto.ai;

import lombok.Data;

@Data
public class UpdateQuotaRequest {
    private Integer dailyLimit;
    private Integer monthlyLimit;
    private Boolean isPremium;
    private Boolean suspended;
}
