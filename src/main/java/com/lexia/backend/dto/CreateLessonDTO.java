package com.lexia.backend.dto;

import com.lexia.backend.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new lesson.
 * Contains validation rules for lesson input data.
 * JSONB content will be validated against lesson type schemas by
 * LessonContentValidator.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new lesson with type-specific JSONB content")
public class CreateLessonDTO {

    /**
     * Lesson title (required, max 255 characters).
     */
    @NotBlank(message = "Lesson title is required")
    @Size(max = 255, message = "Lesson title must not exceed 255 characters")
    @Schema(description = "Lesson title", example = "Basic Greetings and Introductions", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String title;

    /**
     * Lesson type (required: READING, LISTENING, QUIZ, or SPEAKING).
     */
    @NotNull(message = "Lesson type is required")
    @Schema(description = "Lesson type", example = "READING", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
            "READING", "LISTENING", "QUIZ", "SPEAKING" })
    private Lesson.LessonType lessonType;

    /**
     * JSONB content (required).
     * Must be valid JSON string.
     * Will be validated against lesson type schema by LessonContentValidator.
     * See DATABASE-SCHEMA.md section 2.3 for schemas.
     */
    @NotBlank(message = "Lesson content is required")
    @Schema(description = "JSONB content (must be valid JSON matching lesson type schema - see DATABASE-SCHEMA.md section 2.3)", example = "{\"passages\": [{\"title\": \"Meeting People\", \"text\": \"When you meet someone new...\"}], \"questions\": [{\"question\": \"What should you say first?\", \"type\": \"multiple_choice\", \"options\": [\"Hello\", \"Goodbye\", \"Thank you\"], \"correctAnswer\": 0}]}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * Order position within the section (required, 0-based).
     */
    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be 0 or greater")
    @Schema(description = "Order position within the section (0-based)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer orderIndex;

    /**
     * Estimated time to complete the lesson (required, 1-240 minutes).
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    @Schema(description = "Estimated time to complete the lesson (in minutes)", example = "15", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "240")
    private Integer durationMinutes;
}
