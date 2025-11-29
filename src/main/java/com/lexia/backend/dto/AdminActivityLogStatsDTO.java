package com.lexia.backend.dto;

import lombok.*;

import java.util.Map;

/**
 * DTO for admin activity log statistics response.
 * Provides aggregated statistics about admin activities.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLogStatsDTO {

    private String period;
    private ActivityStatsSummary stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityStatsSummary {
        /**
         * Total number of activities in the period.
         */
        private long totalActivities;

        /**
         * Number of activities by action type.
         * Key: ActionType name (e.g., "COURSE_CREATED")
         * Value: count
         */
        private Map<String, Long> activitiesByAction;

        /**
         * Number of activities by entity type.
         * Key: EntityType name (e.g., "COURSE")
         * Value: count
         */
        private Map<String, Long> activitiesByEntityType;

        /**
         * Number of activities by user.
         * Key: user name
         * Value: count
         */
        private Map<String, Long> activitiesByUser;

        /**
         * Most active users (top 5).
         */
        private Map<String, Long> topActiveUsers;
    }
}
