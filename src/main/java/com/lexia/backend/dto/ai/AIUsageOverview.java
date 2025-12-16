package com.lexia.backend.dto.ai;

import com.lexia.backend.entity.AIAlert;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AIUsageOverview {
    private long totalRequestsToday;
    private BigDecimal totalCostToday;
    private long activeUsersToday;
    private List<TopAIUser> topUsers;
    private List<AIAlert> recentAlerts;
    private FeatureUsageChart featureUsageChart;

    @Data
    public static class TopAIUser {
        private java.util.UUID userId;
        private String userEmail;
        private String userFullName;
        private long totalRequests;
        private BigDecimal totalCost;
        private java.time.Instant lastUsedAt;
    }

    @Data
    public static class FeatureUsageChart {
        private List<String> labels;
        private List<Long> data;
    }
}
