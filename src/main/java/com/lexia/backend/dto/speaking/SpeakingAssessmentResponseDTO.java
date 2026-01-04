package com.lexia.backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Complete response DTO for Speaking assessment.
 * Combines pronunciation scoring, grammar feedback, and vocabulary feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAssessmentResponseDTO {

    /**
     * Whether no speech was detected in the audio.
     * If true, all scores will be 0.
     */
    @Builder.Default
    private boolean noSpeechDetected = false;

    /**
     * Transcription of user's speech (Speech-to-Text result).
     * Empty string if no speech detected.
     */
    private String transcription;

    /**
     * Pronunciation score from 0-100.
     * Weighted: Accuracy (40%), Stress/Intonation (30%), Fluency (30%).
     */
    private int pronunciationScore;

    /**
     * Human-readable feedback on pronunciation.
     */
    private String pronunciationFeedback;

    /**
     * Word-level pronunciation breakdown.
     * Key: word identifier, Value: WordScore with score and notes.
     */
    private Map<String, WordScore> wordBreakdown;

    /**
     * Grammar analysis feedback.
     */
    private GrammarFeedbackDTO grammarFeedback;

    /**
     * Vocabulary analysis feedback.
     */
    private VocabularyFeedbackDTO vocabularyFeedback;

    /**
     * Combined overall score from 0-100.
     * Weighted average of pronunciation, grammar, and vocabulary.
     */
    private int overallScore;

    /**
     * The prompt ID this assessment is for.
     */
    private String promptId;

    /**
     * Represents word-level pronunciation score.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordScore {
        private String word;
        private int score;
        private String note;
    }
}
