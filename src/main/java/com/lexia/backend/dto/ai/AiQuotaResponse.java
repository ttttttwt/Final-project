package com.lexia.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuotaResponse {
    private Integer dailyUsed;
    private Integer dailyLimit;
    private Integer monthlyUsed;
    private Integer monthlyLimit;
    private String dailyResetAt;
    private String monthlyResetAt;
}
