package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a complete grammar exercise set.
 * Contains explanation, exercises, and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete grammar exercise set with explanation and exercises")
public class GrammarExerciseSetDTO {

    @Schema(description = "Exercise set ID")
    private UUID id;

    @Schema(description = "Grammar point being tested", example = "Present Simple")
    private String grammarPoint;

    @Schema(description = "CEFR level", example = "B1")
    private String cefrLevel;

    @Schema(description = "Contextual theme", example = "workplace")
    private String theme;

    @Schema(description = "Grammar rule explanation")
    private GrammarExplanationDTO explanation;

    @Schema(description = "List of exercises")
    private List<GrammarExerciseDTO> exercises;

    @Schema(description = "Number of exercises")
    private Integer exerciseCount;

    @Schema(description = "Time limit in seconds", example = "600")
    private Integer timeLimitSeconds;

    @Schema(description = "Whether this is fallback content")
    private Boolean isFallback;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    // ========== Nested DTO for Explanation ==========

    /**
     * Grammar rule explanation with examples and common mistakes.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Grammar rule explanation")
    public static class GrammarExplanationDTO {

        @Schema(description = "Grammar rule description", 
                example = "The Present Simple is used for habits, routines, and general truths.")
        private String rule;

        @Schema(description = "Example sentences demonstrating the rule")
        private List<String> examples;

        @Schema(description = "Common mistakes learners make")
        private List<String> commonMistakes;

        @Schema(description = "Tips for remembering the rule")
        private List<String> tips;
    }

    // ========== Helper Methods ==========

    /**
     * Checks if time limit is enabled.
     */
    public boolean hasTimeLimit() {
        return timeLimitSeconds != null && timeLimitSeconds > 0;
    }

    /**
     * Gets time limit in minutes.
     */
    public int getTimeLimitMinutes() {
        return hasTimeLimit() ? timeLimitSeconds / 60 : 0;
    }

    /**
     * Checks if the set has explanation.
     */
    public boolean hasExplanation() {
        return explanation != null;
    }

    /**
     * Gets the number of exercises.
     */
    public int getActualExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }
}
