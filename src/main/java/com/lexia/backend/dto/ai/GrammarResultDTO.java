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
 * DTO for grammar exercise results.
 * Contains score, feedback, and detailed answer analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Grammar exercise results with score and feedback")
public class GrammarResultDTO {

    @Schema(description = "Exercise set ID")
    private UUID exerciseSetId;

    @Schema(description = "Grammar point tested", example = "Present Simple")
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

    @Schema(description = "Detailed feedback for each question")
    private List<QuestionFeedback> feedback;

    @Schema(description = "Summary of areas to improve")
    private List<String> areasToImprove;

    @Schema(description = "Positive feedback and encouragement")
    private String encouragement;

    @Schema(description = "Completion timestamp")
    private Instant completedAt;

    // ========== Nested DTO for Question Feedback ==========

    /**
     * Detailed feedback for a single question.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Feedback for a single question")
    public static class QuestionFeedback {

        @Schema(description = "Question index (0-based)", example = "0")
        private Integer questionIndex;

        @Schema(description = "User's answer", example = "go")
        private Object userAnswer;

        @Schema(description = "Correct answer", example = "goes")
        private Object correctAnswer;

        @Schema(description = "Whether the answer was correct", example = "false")
        private Boolean correct;

        @Schema(description = "Explanation of the correct answer",
                example = "We use 'goes' because 'she' is third person singular.")
        private String explanation;

        @Schema(description = "Time spent on this question in milliseconds", example = "5000")
        private Long timeMs;
    }

    // ========== Helper Methods ==========

    /**
     * Checks if the user passed (default threshold 70%).
     */
    public boolean isPassed() {
        return passed != null && passed;
    }

    /**
     * Gets the number of incorrect answers.
     */
    public int getIncorrectCount() {
        return maxScore != null && score != null ? maxScore - score : 0;
    }

    /**
     * Gets time spent in minutes.
     */
    public int getTimeSpentMinutes() {
        return timeSpentSeconds != null ? timeSpentSeconds / 60 : 0;
    }

    /**
     * Gets the count of feedback items.
     */
    public int getFeedbackCount() {
        return feedback != null ? feedback.size() : 0;
    }

    /**
     * Calculates pass status based on custom threshold.
     * 
     * @param threshold passing percentage (e.g., 70.0)
     * @return true if percentage >= threshold
     */
    public boolean isPassed(double threshold) {
        return percentage != null && percentage.doubleValue() >= threshold;
    }

    /**
     * Creates a result indicating a pass.
     */
    public static GrammarResultDTO pass(UUID exerciseSetId, String grammarPoint, String cefrLevel,
            int score, int maxScore, BigDecimal percentage, int timeSpentSeconds,
            List<QuestionFeedback> feedback) {
        return GrammarResultDTO.builder()
                .exerciseSetId(exerciseSetId)
                .grammarPoint(grammarPoint)
                .cefrLevel(cefrLevel)
                .score(score)
                .maxScore(maxScore)
                .percentage(percentage)
                .passed(true)
                .timeSpentSeconds(timeSpentSeconds)
                .feedback(feedback)
                .encouragement("Great job! You've demonstrated a solid understanding of " + grammarPoint + "!")
                .completedAt(Instant.now())
                .build();
    }

    /**
     * Creates a result indicating a fail.
     */
    public static GrammarResultDTO fail(UUID exerciseSetId, String grammarPoint, String cefrLevel,
            int score, int maxScore, BigDecimal percentage, int timeSpentSeconds,
            List<QuestionFeedback> feedback, List<String> areasToImprove) {
        return GrammarResultDTO.builder()
                .exerciseSetId(exerciseSetId)
                .grammarPoint(grammarPoint)
                .cefrLevel(cefrLevel)
                .score(score)
                .maxScore(maxScore)
                .percentage(percentage)
                .passed(false)
                .timeSpentSeconds(timeSpentSeconds)
                .feedback(feedback)
                .areasToImprove(areasToImprove)
                .encouragement("Keep practicing! Review the explanations and try again.")
                .completedAt(Instant.now())
                .build();
    }
}
