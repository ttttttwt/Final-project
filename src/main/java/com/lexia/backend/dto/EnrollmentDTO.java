package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for enrollment information.
 * Represents a user's enrollment in a course with progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User enrollment in a course with progress tracking")
public class EnrollmentDTO {

    @Schema(description = "Enrollment ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Course ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long courseId;

    @Schema(description = "Course title", example = "English Basics (A1)", accessMode = Schema.AccessMode.READ_ONLY)
    private String courseTitle;

    @Schema(description = "Course thumbnail URL", example = "https://example.com/thumbnails/english-basics.jpg", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private String thumbnailUrl;

    @Schema(description = "Course CEFR level", example = "A1", allowableValues = { "A1", "A2", "B1", "B2", "C1",
            "C2" }, accessMode = Schema.AccessMode.READ_ONLY)
    private String cefrLevel;

    @Schema(description = "Date and time when user enrolled", example = "2025-11-05T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime enrolledAt;

    @Schema(description = "Overall course completion percentage (0-100)", example = "45", minimum = "0", maximum = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer progressPercentage;

    @Schema(description = "Date and time when course was completed (null if incomplete)", example = "2025-12-15T18:45:00", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime completedAt;

    @Schema(description = "Whether the course is completed", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isCompleted;
}
