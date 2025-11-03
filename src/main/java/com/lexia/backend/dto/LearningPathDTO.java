package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for LearningPath responses.
 * Used to return learning path information with associated courses to clients.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Learning path information with courses, progress metrics, and metadata")
public class LearningPathDTO {

    /**
     * Learning path unique identifier (auto-generated).
     */
    @Schema(description = "Learning path unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * Display name of the learning path.
     */
    @Schema(description = "Learning path name", example = "Beginner Path (A1)", maxLength = 100)
    private String name;

    /**
     * Detailed description of the learning path objectives.
     */
    @Schema(description = "Learning path description", example = "A comprehensive path for beginners starting their English learning journey. Master basic grammar, vocabulary, and conversational skills.", nullable = true)
    private String description;

    /**
     * Target Common European Framework of Reference level.
     */
    @Schema(description = "Target CEFR level for this learning path", example = "A1", allowableValues = { "A1", "A2",
            "B1", "B2", "C1", "C2" })
    private String cefrLevel;

    /**
     * System-provided default path (true) vs user-created custom path (false).
     */
    @Schema(description = "Whether this is a system-provided default path", example = "true")
    private Boolean isDefault;

    /**
     * List of courses in this learning path, ordered by sequence.
     */
    @Schema(description = "Ordered list of courses in this learning path")
    private List<LearningPathCourseDTO> courses;

    /**
     * Total number of courses in this learning path.
     */
    @Schema(description = "Total number of courses in the path", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer totalCourses;

    /**
     * Estimated hours to complete all courses in this path.
     */
    @Schema(description = "Estimated total hours to complete the learning path", example = "45", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer estimatedHours;

    /**
     * Timestamp when learning path was created.
     */
    @Schema(description = "Learning path creation timestamp", example = "2025-11-03T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Timestamp when learning path was last updated.
     */
    @Schema(description = "Learning path last update timestamp", example = "2025-11-03T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * Nested DTO for course information within a learning path.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Course information within a learning path")
    public static class LearningPathCourseDTO {

        /**
         * Course unique identifier.
         */
        @Schema(description = "Course ID", example = "1")
        private Long courseId;

        /**
         * Course title.
         */
        @Schema(description = "Course title", example = "English Basics (A1)")
        private String courseTitle;

        /**
         * Course thumbnail URL.
         */
        @Schema(description = "Course thumbnail URL", example = "https://cdn.lexia.com/courses/english-basics-a1.jpg", nullable = true)
        private String courseThumbnailUrl;

        /**
         * Course CEFR level.
         */
        @Schema(description = "Course CEFR level", example = "A1")
        private String courseCefrLevel;

        /**
         * Position of this course in the learning path (0-based).
         */
        @Schema(description = "Sequential position in the learning path (0-based)", example = "0")
        private Integer orderIndex;

        /**
         * Number of sections in this course.
         */
        @Schema(description = "Number of sections in the course", example = "5")
        private Integer sectionCount;
    }
}
