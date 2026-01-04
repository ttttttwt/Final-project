package com.lexia.backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for vocabulary analysis feedback in Speaking assessment.
 * Focuses on target vocabulary defined in the lesson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyFeedbackDTO {

    /**
     * Target vocabulary words that were successfully used.
     */
    private List<String> usedTargetWords;

    /**
     * Target vocabulary words that were not used.
     */
    private List<String> missedWords;

    /**
     * Advanced/bonus vocabulary used (not in target list).
     */
    private List<String> advancedWords;

    /**
     * Overall suggestion for vocabulary improvement.
     */
    private String suggestion;

    /**
     * Vocabulary score from 0-100.
     */
    private int score;
}
