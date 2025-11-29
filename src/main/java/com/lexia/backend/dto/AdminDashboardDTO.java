package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Admin Dashboard statistics.
 * Contains aggregated data for dashboard overview.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    /**
     * Total number of registered users
     */
    private long totalUsers;

    /**
     * Total number of courses
     */
    private long totalCourses;

    /**
     * Number of published courses
     */
    private long publishedCourses;

    /**
     * Total number of lessons
     */
    private long totalLessons;

    /**
     * Recent admin activities (course/lesson CRUD operations)
     */
    private List<ActivityDTO> recentActivities;

    /**
     * Course count by CEFR level for overview chart
     * Key: CEFR level (A1, A2, B1, B2, C1, C2)
     * Value: count of courses
     */
    private Map<String, Long> coursesByLevel;
}
