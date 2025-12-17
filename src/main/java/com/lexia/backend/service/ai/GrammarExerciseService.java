package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for AI-powered grammar exercise operations.
 * Provides methods for generating, retrieving, and evaluating grammar
 * exercises.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>AI-generated exercises via Google Gemini</li>
 * <li>CEFR-level appropriate difficulty</li>
 * <li>Multiple exercise types: MCQ, fill-in-blank, transformation,
 * error-correction</li>
 * <li>Fallback content support when AI is unavailable</li>
 * <li>Progress tracking and scoring</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
public interface GrammarExerciseService {

    /**
     * Generates a new set of grammar exercises using AI.
     * 
     * <p>
     * The exercises are generated based on the specified grammar topic, CEFR level,
     * and optional theme. If AI generation fails, fallback content is used.
     * </p>
     * 
     * @param request the exercise generation request containing grammar topic, CEFR
     *                level, etc.
     * @param userId  the ID of the user requesting the exercises
     * @return the generated exercise set DTO
     * @throws com.lexia.backend.exception.ai.AiRateLimitException   if user has
     *                                                               exceeded their
     *                                                               quota
     * @throws com.lexia.backend.exception.ResourceNotFoundException if grammar
     *                                                               topic is not
     *                                                               found
     */
    GrammarExerciseSetDTO generateExercises(GrammarRequestDTO request, UUID userId);

    /**
     * Lists all available grammar topics.
     * 
     * @return list of all active grammar topics
     */
    List<GrammarTopicDTO> getAllTopics();

    /**
     * Lists grammar topics filtered by CEFR level.
     * 
     * @param cefrLevel the CEFR level to filter by (A1, A2, B1, B2, C1, C2)
     * @return list of topics applicable to the specified level
     */
    List<GrammarTopicDTO> getTopicsByLevel(String cefrLevel);

    /**
     * Lists grammar topics filtered by category.
     * 
     * @param category the category to filter by (Tenses, Modals, etc.)
     * @return list of topics in the specified category
     */
    List<GrammarTopicDTO> getTopicsByCategory(String category);

    /**
     * Gets all distinct topic categories.
     * 
     * @return list of category names
     */
    List<String> getCategories();

    /**
     * Retrieves an exercise set by its ID.
     * 
     * @param exerciseSetId the exercise set UUID
     * @return the exercise set DTO
     * @throws com.lexia.backend.exception.ResourceNotFoundException if not found
     */
    GrammarExerciseSetDTO getExerciseSet(UUID exerciseSetId);

    /**
     * Retrieves exercise sets for a user with pagination.
     * 
     * @param userId   the user's UUID
     * @param pageable pagination information
     * @return page of exercise sets created for/by the user
     */
    Page<GrammarExerciseSetDTO> getUserExerciseSets(UUID userId, Pageable pageable);

    /**
     * Submits answers for an exercise set and calculates the score.
     * 
     * <p>
     * Validates each answer, calculates the score, and saves the progress.
     * Generates detailed feedback for each question.
     * </p>
     * 
     * @param exerciseSetId the exercise set UUID
     * @param userId        the user's UUID
     * @param submission    the answer submission containing list of answers
     * @return the result DTO containing score, feedback, and analysis
     * @throws com.lexia.backend.exception.ResourceNotFoundException if exercise set
     *                                                               not found
     * @throws IllegalStateException                                 if already
     *                                                               submitted by
     *                                                               this user
     */
    GrammarResultDTO submitAnswers(UUID exerciseSetId, UUID userId, GrammarAnswerDTO submission);

    /**
     * Gets the submission history for a user.
     * 
     * @param userId   the user's UUID
     * @param pageable pagination information
     * @return page of progress records
     */
    Page<GrammarProgressDTO> getHistory(UUID userId, Pageable pageable);

    /**
     * Gets progress statistics for a user.
     * 
     * @param userId the user's UUID
     * @return progress statistics including averages and counts
     */
    GrammarStatsDTO getStatistics(UUID userId);

    /**
     * Gets progress for a specific exercise set and user.
     * 
     * @param exerciseSetId the exercise set UUID
     * @param userId        the user's UUID
     * @return the progress DTO if exists, null otherwise
     */
    GrammarProgressDTO getProgress(UUID exerciseSetId, UUID userId);

    /**
     * Checks if a user has already submitted answers for an exercise set.
     * 
     * @param exerciseSetId the exercise set UUID
     * @param userId        the user's UUID
     * @return true if already submitted
     */
    boolean hasSubmitted(UUID exerciseSetId, UUID userId);

    /**
     * Gets random fallback exercises for a grammar topic and level.
     * Used when AI generation fails or is explicitly requested.
     * 
     * @param grammarTopic the grammar topic
     * @param cefrLevel    the CEFR level
     * @return the fallback exercise set, or null if none available
     */
    GrammarExerciseSetDTO getFallbackExercises(String grammarTopic, String cefrLevel);

    /**
     * Resets user progress for an exercise set, allowing retry.
     * Deletes the user's progress record so they can retake the exercise.
     * 
     * @param exerciseSetId the exercise set UUID
     * @param userId        the user's UUID
     * @throws com.lexia.backend.exception.ResourceNotFoundException if exercise set
     *                                                               or progress not
     *                                                               found
     */
    void resetProgress(UUID exerciseSetId, UUID userId);
}
