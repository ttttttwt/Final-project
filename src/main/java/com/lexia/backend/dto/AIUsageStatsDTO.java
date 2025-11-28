package com.lexia.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for AI usage statistics response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIUsageStatsDTO {

    private String period;
    private AIUsageSummary stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIUsageSummary {
        private long totalCalls;
        private long totalInputTokens;
        private long totalOutputTokens;
        private BigDecimal totalCost;
        private BigDecimal averageCostPerCall;
        private Map<String, Long> callsByFeature;
    }
}
