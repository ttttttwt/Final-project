package com.lexia.backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for grammar analysis feedback in Speaking assessment.
 * Focuses on target grammar structures defined in the lesson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarFeedbackDTO {

    /**
     * List of grammar issues found in the user's response.
     * Only includes issues related to target grammar structures.
     */
    private List<GrammarIssue> issues;

    /**
     * Target grammar structures that were successfully used.
     */
    private List<String> usedTargetStructures;

    /**
     * Target grammar structures that were not used.
     */
    private List<String> missedStructures;

    /**
     * Overall suggestion for grammar improvement.
     */
    private String suggestion;

    /**
     * Grammar score from 0-100.
     */
    private int score;

    /**
     * Represents a single grammar issue found in the response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarIssue {
        /**
         * The original text containing the issue.
         */
        private String originalText;

        /**
         * The corrected version.
         */
        private String correction;

        /**
         * The grammar rule that was violated.
         */
        private String rule;

        /**
         * Explanation of why this is incorrect.
         */
        private String explanation;
    }
}
