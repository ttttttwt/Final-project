package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for detailed course progress information.
 * Includes overall progress and per-lesson status.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed course progress with lesson-by-lesson breakdown")
public class CourseProgressDTO {

    @Schema(description = "Course ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long courseId;

    @Schema(description = "Course title", example = "English Basics (A1)", accessMode = Schema.AccessMode.READ_ONLY)
    private String courseTitle;

    @Schema(description = "Course CEFR level", example = "A1", allowableValues = { "A1", "A2", "B1", "B2", "C1",
            "C2" }, accessMode = Schema.AccessMode.READ_ONLY)
    private String cefrLevel;

    @Schema(description = "Total number of lessons in the course", example = "18", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer totalLessons;

    @Schema(description = "Number of completed lessons", example = "8", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer completedLessons;

    @Schema(description = "Overall course completion percentage (0-100)", example = "44", minimum = "0", maximum = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer progressPercentage;

    @Schema(description = "List of lesson progress details", accessMode = Schema.AccessMode.READ_ONLY)
    private List<LessonProgressSummary> lessonProgress;

    /**
     * Nested DTO for individual lesson progress summary.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual lesson progress summary")
    public static class LessonProgressSummary {

        @Schema(description = "Lesson ID", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
        private Long lessonId;

        @Schema(description = "Lesson title", example = "Greetings and Introductions", accessMode = Schema.AccessMode.READ_ONLY)
        private String lessonTitle;

        @Schema(description = "Lesson type", example = "READING", allowableValues = { "READING", "LISTENING", "QUIZ",
                "SPEAKING" }, accessMode = Schema.AccessMode.READ_ONLY)
        private String lessonType;

        @Schema(description = "Section title", example = "Getting Started", accessMode = Schema.AccessMode.READ_ONLY)
        private String sectionTitle;

        @Schema(description = "Lesson status", example = "COMPLETED", allowableValues = { "NOT_STARTED", "IN_PROGRESS",
                "COMPLETED" }, accessMode = Schema.AccessMode.READ_ONLY)
        private String status;

        @Schema(description = "Score achieved (0-100, null if not applicable)", example = "85", minimum = "0", maximum = "100", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
        private Integer score;

        @Schema(description = "Number of attempts", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
        private Integer attempts;
    }
}
