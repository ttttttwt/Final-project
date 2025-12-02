package com.lexia.backend.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for email statistics and analytics.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatsDTO {

    /**
     * Time period for statistics.
     */
    private String period;

    /**
     * Summary totals.
     */
    private EmailStatsSummary summary;

    /**
     * Calculated rates.
     */
    private EmailRates rates;

    /**
     * Statistics broken down by email type.
     */
    private List<EmailTypeStats> byType;

    /**
     * Daily trend data.
     */
    private EmailTrend trend;

    /**
     * Summary counts for emails.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailStatsSummary {
        private long totalSent;
        private long delivered;
        private long opened;
        private long clicked;
        private long bounced;
        private long complained;
        private long failed;
    }

    /**
     * Calculated delivery and engagement rates.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRates {
        private double deliveryRate;
        private double openRate;
        private double clickRate;
        private double bounceRate;
        private double complaintRate;
    }

    /**
     * Statistics for a specific email type.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailTypeStats {
        private String type;
        private long sent;
        private long opened;
        private double openRate;
        private long clicked;
        private double clickRate;
    }

    /**
     * Daily trend data for charting.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailTrend {
        private List<String> dates;
        private List<Long> sent;
        private List<Long> opened;
        private List<Long> clicked;
    }

    /**
     * Available time periods for statistics.
     */
    public enum StatsPeriod {
        LAST_24_HOURS("Last 24 Hours"),
        LAST_7_DAYS("Last 7 Days"),
        LAST_30_DAYS("Last 30 Days"),
        LAST_90_DAYS("Last 90 Days");

        private final String displayName;

        StatsPeriod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
