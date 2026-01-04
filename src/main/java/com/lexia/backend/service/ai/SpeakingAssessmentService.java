package com.lexia.backend.service.ai;

import com.lexia.backend.dto.speaking.SpeakingAssessmentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service for AI-powered Speaking lesson assessment.
 * Provides speech recognition, pronunciation scoring, and grammar/vocabulary
 * feedback.
 * 
 * @author LEXIA Team
 * @since Sprint 6
 */
public interface SpeakingAssessmentService {

    /**
     * Assesses a user's speaking attempt for a given lesson prompt.
     * 
     * <p>
     * This method performs the following:
     * </p>
     * <ol>
     * <li>Transcribes the audio (Speech-to-Text)</li>
     * <li>Scores pronunciation (Accuracy 40%, Stress 30%, Fluency 30%)</li>
     * <li>Analyzes grammar usage against target structures</li>
     * <li>Analyzes vocabulary usage against target words</li>
     * </ol>
     *
     * @param lessonId The ID of the Speaking lesson
     * @param promptId The ID of the specific prompt being assessed
     * @param audio    The audio recording of the user's attempt (MP3, WAV, WebM)
     * @param userId   The ID of the user submitting the attempt
     * @return Complete assessment including transcription, scores, and feedback
     * @throws com.lexia.backend.exception.ResourceNotFoundException if lesson or
     *                                                               prompt not
     *                                                               found
     * @throws com.lexia.backend.exception.AiServiceException        if AI
     *                                                               processing
     *                                                               fails
     */
    SpeakingAssessmentResponseDTO assessSpeaking(
            Long lessonId,
            String promptId,
            MultipartFile audio,
            UUID userId);
}
