package com.lexia.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Analytics Dashboard
 */
public class AnalyticsDTO {

    /**
     * Overview statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private long totalUsers;
        private long activeUsers; // users active in last 30 days
        private long newUsersThisMonth;
        private long freeUsers;
        private long proUsers;
        private BigDecimal totalRevenue;
        private BigDecimal revenueThisMonth;
        private long totalAIRequests;
        private long aiRequestsThisMonth;
    }

    /**
     * Monthly statistics for charts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private String month; // "2024-01"
        private long newUsers;
        private long activeUsers;
        private BigDecimal revenue;
        private long aiRequests;
    }

    /**
     * User distribution stats
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDistribution {
        private long freeUsers;
        private long monthlyProUsers;
        private long yearlyProUsers;
        private Map<String, Long> usersByLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    }

    /**
     * AI Usage breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIUsageStats {
        private long totalRequests;
        private long roleplayRequests;
        private long grammarRequests;
        private long flashcardRequests;
        private long translationRequests;
        private long successRate; // percentage
        private long averageResponseTimeMs;
        private List<DailyAIUsage> dailyUsage;
    }

    /**
     * Daily AI usage for chart
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAIUsage {
        private String date; // "2024-01-15"
        private long requests;
        private long tokensUsed;
    }

    /**
     * Full analytics response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsResponse {
        private OverviewStats overview;
        private List<MonthlyStats> monthlyStats;
        private UserDistribution userDistribution;
        private AIUsageStats aiUsage;
    }
}
