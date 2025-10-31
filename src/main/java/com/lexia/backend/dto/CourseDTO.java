package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Course responses.
 * Used to return course information to clients.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course information with metadata and section count")
public class CourseDTO {

    /**
     * Course unique identifier (auto-generated).
     */
    @Schema(description = "Course unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * Course title displayed to users.
     */
    @Schema(description = "Course title", example = "English Basics (A1)", maxLength = 255)
    private String title;

    /**
     * Detailed course description.
     */
    @Schema(description = "Course description", example = "Foundation course for beginners starting to learn English", nullable = true)
    private String description;

    /**
     * URL to course thumbnail image.
     */
    @Schema(description = "URL to course thumbnail image", example = "https://cdn.lexia.com/courses/english-basics-a1.jpg", nullable = true)
    private String thumbnailUrl;

    /**
     * Common European Framework of Reference level (A1, A2, B1, B2, C1, C2).
     */
    @Schema(description = "CEFR level of the course", example = "A1", allowableValues = { "A1", "A2", "B1", "B2", "C1",
            "C2" })
    private String cefrLevel;

    /**
     * Publication status - only published courses are visible to learners.
     */
    @Schema(description = "Whether the course is published and visible to learners", example = "true")
    private Boolean isPublished;

    /**
     * Number of sections in this course (derived field).
     */
    @Schema(description = "Number of sections in the course", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer sectionCount;

    /**
     * Timestamp when course was created.
     */
    @Schema(description = "Course creation timestamp", example = "2025-10-30T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Timestamp when course was last updated.
     */
    @Schema(description = "Course last update timestamp", example = "2025-10-31T14:22:45", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
