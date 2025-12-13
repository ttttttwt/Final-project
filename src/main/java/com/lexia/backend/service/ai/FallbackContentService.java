package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.RolePlayScenarioDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing fallback content when AI services are unavailable.
 * 
 * <p>Provides pre-generated content from the database to ensure graceful degradation
 * when the Gemini AI API fails or is rate-limited. Fallback scenarios are seeded
 * in V25 migration with is_fallback=true.</p>
 * 
 * <p>Usage with Resilience4j:</p>
 * <pre>
 * {@code
 * @Retry(name = "geminiApi", fallbackMethod = "getFallbackScenario")
 * public RolePlayScenarioDTO generateScenario(RolePlayRequestDTO request) {
 *     // Gemini call
 * }
 * 
 * public RolePlayScenarioDTO getFallbackScenario(RolePlayRequestDTO request, Exception e) {
 *     return fallbackContentService.getRandomFallbackScenario(
 *         request.getCefrLevel(), request.getDomain()
 *     ).orElseThrow(() -> new AiServiceException("No fallback available"));
 * }
 * }
 * </pre>
 * 
 * @author LEXIA Team
 * @since Sprint 5 - Task B7
 */
public interface FallbackContentService {

    /**
     * Retrieves a random fallback scenario matching the given criteria.
     * 
     * <p>Selects randomly from available fallback scenarios to provide variety
     * when the same user triggers multiple fallbacks.</p>
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @param domain the domain (e.g., "meetings", "negotiations", "presentations")
     * @return Optional containing a random fallback scenario, or empty if none available
     */
    Optional<RolePlayScenarioDTO> getRandomFallbackScenario(String cefrLevel, String domain);

    /**
     * Retrieves a random fallback scenario with flexible matching.
     * 
     * <p>If no exact match is found for CEFR level + domain, falls back to:
     * 1. Same CEFR level, any domain
     * 2. Same domain, any CEFR level
     * 3. Any fallback scenario</p>
     * 
     * @param cefrLevel the preferred CEFR level
     * @param domain the preferred domain
     * @return Optional containing a fallback scenario with flexible matching
     */
    Optional<RolePlayScenarioDTO> getRandomFallbackScenarioFlexible(String cefrLevel, String domain);

    /**
     * Retrieves all fallback scenarios matching the given criteria.
     * 
     * @param cefrLevel the CEFR level (null for all levels)
     * @param domain the domain (null for all domains)
     * @return list of matching fallback scenarios
     */
    List<RolePlayScenarioDTO> getFallbackScenarios(String cefrLevel, String domain);

    /**
     * Retrieves all fallback scenarios for a CEFR level.
     * 
     * @param cefrLevel the CEFR level
     * @return list of fallback scenarios for the level
     */
    List<RolePlayScenarioDTO> getFallbackScenariosByCefrLevel(String cefrLevel);

    /**
     * Retrieves all fallback scenarios for a domain.
     * 
     * @param domain the domain
     * @return list of fallback scenarios for the domain
     */
    List<RolePlayScenarioDTO> getFallbackScenariosByDomain(String domain);

    /**
     * Counts available fallback scenarios matching the criteria.
     * 
     * @param cefrLevel the CEFR level (null for all levels)
     * @param domain the domain (null for all domains)
     * @return count of matching fallback scenarios
     */
    long countFallbackScenarios(String cefrLevel, String domain);

    /**
     * Checks if any fallback scenarios are available for the given criteria.
     * 
     * @param cefrLevel the CEFR level (null for all levels)
     * @param domain the domain (null for all domains)
     * @return true if at least one fallback scenario is available
     */
    boolean hasFallbackScenarios(String cefrLevel, String domain);

    /**
     * Generates a contextual fallback response for an ongoing conversation.
     * 
     * <p>Provides level-appropriate responses when AI fails during a conversation.
     * Responses are generic but contextually appropriate for the CEFR level.</p>
     * 
     * @param cefrLevel the CEFR level for language complexity
     * @param aiRole the AI's role in the conversation
     * @param userMessage the user's most recent message (for context)
     * @return a fallback response appropriate for the conversation
     */
    String generateFallbackResponse(String cefrLevel, String aiRole, String userMessage);

    /**
     * Generates a fallback learning mode response with placeholder feedback.
     * 
     * <p>Provides a response with minimal feedback when AI fails in learning mode.
     * Includes placeholder grammar/vocabulary feedback.</p>
     * 
     * @param cefrLevel the CEFR level
     * @param aiRole the AI's role
     * @param userMessage the user's message
     * @return JSON string with aiMessage and feedback structure
     */
    String generateFallbackLearningResponse(String cefrLevel, String aiRole, String userMessage);
}
