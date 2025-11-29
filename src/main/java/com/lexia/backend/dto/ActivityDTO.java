package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for recent activity items displayed on dashboard.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    /**
     * Unique identifier for the activity (can be String or numeric)
     */
    private String id;

    /**
     * Type of activity (COURSE_CREATED, COURSE_PUBLISHED, USER_REGISTERED,
     * ENROLLMENT_CREATED, LESSON_COMPLETED)
     */
    private String type;

    /**
     * Human-readable description of the activity
     */
    private String description;

    /**
     * When the activity occurred
     */
    private LocalDateTime timestamp;

    /**
     * URL to view details (optional)
     */
    private String link;

    /**
     * Score if applicable (for quiz completions)
     */
    private Integer score;

    /**
     * Name of the user involved (if applicable)
     */
    private String userName;

    /**
     * Name of the course involved (if applicable)
     */
    private String courseName;
}
