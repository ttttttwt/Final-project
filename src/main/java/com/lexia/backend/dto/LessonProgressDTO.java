package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for lesson progress response.
 * Contains details about a user's progress on a specific lesson.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed lesson progress information")
public class LessonProgressDTO {

    @Schema(description = "Progress record ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Lesson ID", example = "1")
    private Long lessonId;

    @Schema(description = "Lesson title", example = "Present Simple Tense")
    private String lessonTitle;

    @Schema(description = "Lesson type", example = "GRAMMAR")
    private String lessonType;

    @Schema(description = "Progress status", example = "COMPLETED", allowableValues = { "NOT_STARTED", "IN_PROGRESS",
            "COMPLETED" })
    private String status;

    @Schema(description = "Completion score (0-100)", example = "85", minimum = "0", maximum = "100")
    private Integer score;

    @Schema(description = "Number of attempts", example = "2")
    private Integer attempts;

    @Schema(description = "Date when started", example = "2025-01-15T10:30:00")
    private LocalDateTime startedAt;

    @Schema(description = "Date when completed", example = "2025-01-15T11:15:00")
    private LocalDateTime completedAt;

    @Schema(description = "Result details (JSONB)", example = "{\"correctAnswers\": 17, \"totalQuestions\": 20}")
    private Map<String, Object> resultDetails;

    @Schema(description = "Last update timestamp", example = "2025-01-15T11:15:00")
    private LocalDateTime updatedAt;
}
