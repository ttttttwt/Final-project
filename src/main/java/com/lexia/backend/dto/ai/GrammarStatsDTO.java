package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for user grammar exercise statistics.
 * Contains aggregated progress data and performance metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User grammar exercise statistics and progress summary")
public class GrammarStatsDTO {

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Total number of exercise sets attempted", example = "25")
    private Integer totalAttempted;

    @Schema(description = "Number of completed exercise sets", example = "20")
    private Integer totalCompleted;

    @Schema(description = "Number of passed exercise sets (>=70%)", example = "18")
    private Integer totalPassed;

    @Schema(description = "Average score percentage across all attempts", example = "78.50")
    private BigDecimal averageScore;

    @Schema(description = "Total time spent on exercises in seconds", example = "7200")
    private Integer totalTimeSpentSeconds;

    @Schema(description = "Average time per exercise set in seconds", example = "360")
    private Integer averageTimePerSet;

    @Schema(description = "Total number of questions answered", example = "125")
    private Integer totalQuestionsAnswered;

    @Schema(description = "Total correct answers", example = "98")
    private Integer totalCorrectAnswers;

    @Schema(description = "Breakdown by grammar topic")
    private List<TopicStats> topicBreakdown;

    @Schema(description = "Breakdown by CEFR level")
    private Map<String, LevelStats> levelBreakdown;

    @Schema(description = "Current streak (consecutive days with practice)", example = "5")
    private Integer currentStreak;

    @Schema(description = "Longest streak ever", example = "14")
    private Integer longestStreak;

    // ========== Nested DTOs ==========

    /**
     * Statistics for a specific grammar topic.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistics for a grammar topic")
    public static class TopicStats {

        @Schema(description = "Grammar topic name", example = "Present Simple")
        private String topicName;

        @Schema(description = "Number of attempts", example = "5")
        private Integer attempts;

        @Schema(description = "Average score for this topic", example = "82.00")
        private BigDecimal averageScore;

        @Schema(description = "Best score for this topic", example = "100.00")
        private BigDecimal bestScore;

        @Schema(description = "Whether mastered (best score >= 90%)", example = "true")
        private Boolean mastered;
    }

    /**
     * Statistics for a specific CEFR level.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistics for a CEFR level")
    public static class LevelStats {

        @Schema(description = "CEFR level", example = "B1")
        private String cefrLevel;

        @Schema(description = "Number of exercises completed", example = "10")
        private Integer completed;

        @Schema(description = "Average score", example = "75.00")
        private BigDecimal averageScore;

        @Schema(description = "Pass rate percentage", example = "80.00")
        private BigDecimal passRate;
    }

    // ========== Helper Methods ==========

    /**
     * Calculates pass rate as percentage.
     */
    public BigDecimal getPassRate() {
        if (totalCompleted == null || totalCompleted == 0) {
            return BigDecimal.ZERO;
        }
        int passed = totalPassed != null ? totalPassed : 0;
        return BigDecimal.valueOf((passed * 100.0) / totalCompleted)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gets accuracy rate (correct answers / total answers).
     */
    public BigDecimal getAccuracyRate() {
        if (totalQuestionsAnswered == null || totalQuestionsAnswered == 0) {
            return BigDecimal.ZERO;
        }
        int correct = totalCorrectAnswers != null ? totalCorrectAnswers : 0;
        return BigDecimal.valueOf((correct * 100.0) / totalQuestionsAnswered)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gets total time spent in minutes.
     */
    public Integer getTotalTimeSpentMinutes() {
        return totalTimeSpentSeconds != null ? totalTimeSpentSeconds / 60 : 0;
    }

    /**
     * Creates an empty stats object for a user with no progress.
     */
    public static GrammarStatsDTO empty(UUID userId) {
        return GrammarStatsDTO.builder()
                .userId(userId)
                .totalAttempted(0)
                .totalCompleted(0)
                .totalPassed(0)
                .averageScore(BigDecimal.ZERO)
                .totalTimeSpentSeconds(0)
                .averageTimePerSet(0)
                .totalQuestionsAnswered(0)
                .totalCorrectAnswers(0)
                .currentStreak(0)
                .longestStreak(0)
                .build();
    }
}
