package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user's learning path progress.
 * Used to return progress information about a user's enrollment in a learning
 * path.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's progress in a learning path")
public class UserPathProgressDTO {

    /**
     * User learning path enrollment ID.
     */
    @Schema(description = "User learning path enrollment ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long enrollmentId;

    /**
     * Learning path ID.
     */
    @Schema(description = "Learning path ID", example = "1")
    private Long pathId;

    /**
     * Learning path name.
     */
    @Schema(description = "Learning path name", example = "Beginner Path (A1)")
    private String pathName;

    /**
     * Learning path CEFR level.
     */
    @Schema(description = "Learning path CEFR level", example = "A1")
    private String pathCefrLevel;

    /**
     * Current course ID the user is working on.
     */
    @Schema(description = "Current course ID user is working on", example = "1", nullable = true)
    private Long currentCourseId;

    /**
     * Current course title the user is working on.
     */
    @Schema(description = "Current course title user is working on", example = "English Basics (A1)", nullable = true)
    private String currentCourseTitle;

    /**
     * Number of courses completed in this path.
     */
    @Schema(description = "Number of courses completed", example = "2")
    private Integer coursesCompleted;

    /**
     * Total number of courses in this path.
     */
    @Schema(description = "Total number of courses in the path", example = "3")
    private Integer totalCourses;

    /**
     * Progress percentage (0-100).
     */
    @Schema(description = "Overall progress percentage in the path", example = "66", minimum = "0", maximum = "100")
    private Integer progressPercentage;

    /**
     * Timestamp when user started this learning path.
     */
    @Schema(description = "Enrollment start timestamp", example = "2025-11-01T10:00:00")
    private LocalDateTime startedAt;

    /**
     * Timestamp when user completed this learning path.
     */
    @Schema(description = "Completion timestamp", example = "2025-11-15T16:30:00", nullable = true)
    private LocalDateTime completedAt;

    /**
     * Whether the learning path is completed.
     */
    @Schema(description = "Whether the learning path is completed", example = "false")
    private Boolean isCompleted;
}
