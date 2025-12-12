package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for submitting grammar exercise answers.
 * Contains user responses and timing information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Grammar exercise answer submission")
public class GrammarAnswerDTO {

    @Schema(description = "Exercise set ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Exercise set ID is required")
    private UUID exerciseSetId;

    @Schema(description = "List of answers")
    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<AnswerItem> answers;

    @Schema(description = "Total time spent in seconds", example = "180")
    @PositiveOrZero(message = "Time spent must be non-negative")
    private Integer totalTimeSeconds;

    // ========== Nested DTO for Individual Answer ==========

    /**
     * A single answer item with question index and response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual answer item")
    public static class AnswerItem {

        @Schema(description = "Question index (0-based)", example = "0")
        @Min(value = 0, message = "Question index must be non-negative")
        private Integer questionIndex;

        @Schema(description = "User's answer", example = "goes")
        @NotNull(message = "Answer is required")
        private Object answer;

        @Schema(description = "Time spent on this question in milliseconds", example = "5000")
        @PositiveOrZero(message = "Time must be non-negative")
        private Long timeMs;
    }

    // ========== Helper Methods ==========

    /**
     * Gets the number of answers submitted.
     */
    public int getAnswerCount() {
        return answers != null ? answers.size() : 0;
    }

    /**
     * Gets time spent in minutes.
     */
    public int getTotalTimeMinutes() {
        return totalTimeSeconds != null ? totalTimeSeconds / 60 : 0;
    }
}
