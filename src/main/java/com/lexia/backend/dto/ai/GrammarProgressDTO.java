package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user grammar progress information.
 * Returns progress details for display and tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User progress on grammar exercises")
public class GrammarProgressDTO {

    @Schema(description = "Progress record ID")
    private Long id;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Exercise set ID")
    private UUID exerciseSetId;

    @Schema(description = "Grammar point", example = "Present Simple")
    private String grammarPoint;

    @Schema(description = "CEFR level", example = "B1")
    private String cefrLevel;

    @Schema(description = "Number of correct answers", example = "4")
    private Integer score;

    @Schema(description = "Maximum possible score", example = "5")
    private Integer maxScore;

    @Schema(description = "Score as percentage (0-100)", example = "80.00")
    private BigDecimal percentage;

    @Schema(description = "Whether the user passed (>= 70%)", example = "true")
    private Boolean passed;

    @Schema(description = "Total time spent in seconds", example = "180")
    private Integer timeSpentSeconds;

    @Schema(description = "Whether the exercise is completed", example = "true")
    private Boolean completed;

    @Schema(description = "Completion timestamp")
    private Instant completedAt;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    // ========== Helper Methods ==========

    /**
     * Gets time spent in minutes.
     */
    public int getTimeSpentMinutes() {
        return timeSpentSeconds != null ? timeSpentSeconds / 60 : 0;
    }

    /**
     * Checks if passed with default threshold (70%).
     */
    public boolean isPassed() {
        return passed != null && passed;
    }

    // ========== Statistics DTO ==========

    /**
     * Summary statistics for user grammar progress.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Grammar progress statistics summary")
    public static class GrammarStatsSummaryDTO {

        @Schema(description = "Total exercises attempted")
        private Long totalAttempted;

        @Schema(description = "Total exercises completed")
        private Long totalCompleted;

        @Schema(description = "Total exercises passed")
        private Long totalPassed;

        @Schema(description = "Average score percentage")
        private BigDecimal averagePercentage;

        @Schema(description = "Total score points earned")
        private Integer totalScore;

        @Schema(description = "Total time spent in seconds")
        private Integer totalTimeSeconds;

        @Schema(description = "Best scores by grammar point")
        private List<GrammarPointScore> bestScores;

        @Schema(description = "Completed count by CEFR level")
        private List<CefrLevelProgress> progressByLevel;
    }

    /**
     * Best score for a grammar point.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Best score for a grammar point")
    public static class GrammarPointScore {

        @Schema(description = "Grammar point name")
        private String grammarPoint;

        @Schema(description = "Best percentage achieved")
        private BigDecimal bestPercentage;
    }

    /**
     * Progress count by CEFR level.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Progress count by CEFR level")
    public static class CefrLevelProgress {

        @Schema(description = "CEFR level")
        private String cefrLevel;

        @Schema(description = "Number of completed exercises")
        private Long completedCount;
    }
}
