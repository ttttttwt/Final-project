package com.lexia.backend.dto;

import com.lexia.backend.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing lesson.
 * All fields are optional - only provided fields will be updated.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing lesson (all fields optional)")
public class UpdateLessonDTO {

    /**
     * Lesson title (optional, max 255 characters).
     */
    @Size(max = 255, message = "Lesson title must not exceed 255 characters")
    @Schema(description = "Lesson title (optional)", example = "Advanced Greetings and Introductions", maxLength = 255)
    private String title;

    /**
     * Lesson type (optional: READING, LISTENING, QUIZ, or SPEAKING).
     */
    @Schema(description = "Lesson type (optional)", example = "READING", allowableValues = {
            "READING", "LISTENING", "QUIZ", "SPEAKING" })
    private Lesson.LessonType lessonType;

    /**
     * JSONB content (optional).
     * If provided, must be valid JSON matching the lesson type schema.
     * Will be validated by LessonContentValidator.
     */
    @Schema(description = "JSONB content (optional, must match lesson type schema if provided)", example = "{\"passages\": [{\"title\": \"Meeting People\", \"text\": \"When you meet someone new...\"}], \"questions\": [{\"question\": \"What should you say first?\", \"type\": \"multiple_choice\", \"options\": [\"Hello\", \"Goodbye\", \"Thank you\"], \"correctAnswer\": 0}]}")
    private String content;

    /**
     * Order position within the section (optional, 0-based).
     */
    @Min(value = 0, message = "Order index must be 0 or greater")
    @Schema(description = "Order position within the section (optional, 0-based)", example = "1", minimum = "0")
    private Integer orderIndex;

    /**
     * Estimated time to complete the lesson (optional, 1-240 minutes).
     */
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    @Schema(description = "Estimated time to complete the lesson in minutes (optional)", example = "20", minimum = "1", maximum = "240")
    private Integer durationMinutes;
}
