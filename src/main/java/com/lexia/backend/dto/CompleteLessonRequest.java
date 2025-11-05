package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for marking a lesson as complete.
 * Contains optional result details as JSON string.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to mark a lesson as complete with optional result details")
public class CompleteLessonRequest {

    @NotNull(message = "Result details JSON is required")
    @Schema(description = "Result details as JSON string. Structure varies by lesson type. Can be empty JSON object \"{}\" if no details.", example = "{\"score\": 85, \"correctAnswers\": 17, \"totalQuestions\": 20, \"timeSpent\": 420, \"aiNarrative\": \"Great job on pronunciation!\"}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resultDetailsJson;
}
