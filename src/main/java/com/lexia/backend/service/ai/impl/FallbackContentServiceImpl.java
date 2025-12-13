package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import com.lexia.backend.entity.RolePlayScenario;
import com.lexia.backend.mapper.RolePlayScenarioMapper;
import com.lexia.backend.repository.RolePlayScenarioRepository;
import com.lexia.backend.service.ai.FallbackContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Implementation of FallbackContentService for providing pre-generated content
 * when AI services are unavailable.
 * 
 * <p>Fallback scenarios are seeded in V25 migration with is_fallback=true.
 * This service provides random selection to ensure variety.</p>
 * 
 * @author LEXIA Team
 * @since Sprint 5 - Task B7
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FallbackContentServiceImpl implements FallbackContentService {

    private final RolePlayScenarioRepository scenarioRepository;
    private final Random random = new Random();

    // Pre-defined fallback responses by CEFR level
    private static final List<String> A1_A2_RESPONSES = List.of(
            "I understand. Can you tell me more?",
            "That's interesting. What do you think?",
            "I see. Please continue.",
            "Good point. Let me think about that.",
            "Thank you for sharing. What else?",
            "Yes, I agree. And you?",
            "That makes sense. Go on."
    );

    private static final List<String> B1_B2_RESPONSES = List.of(
            "That's a great observation. Could you elaborate on that?",
            "I appreciate your input. How would you suggest we proceed?",
            "Interesting perspective. What are the key factors here?",
            "Thank you for sharing that. What's your recommendation?",
            "I see your point. What would be the next step?",
            "That's helpful information. How does this affect our timeline?",
            "Good analysis. What alternatives should we consider?"
    );

    private static final List<String> C1_C2_RESPONSES = List.of(
            "That's an insightful point. I'd like to explore this further with you.",
            "Your analysis raises some important considerations. What alternatives do you see?",
            "I value your perspective on this matter. How would you prioritize these factors?",
            "That's a nuanced observation. Could you walk me through your reasoning?",
            "An excellent point that merits further discussion. What implications do you foresee?",
            "Your expertise in this area is evident. How might we leverage this insight?",
            "That raises an interesting strategic consideration. What would be your approach?"
    );

    @Override
    @Transactional(readOnly = true)
    public Optional<RolePlayScenarioDTO> getRandomFallbackScenario(String cefrLevel, String domain) {
        log.debug("Fetching random fallback scenario for CEFR={}, domain={}", cefrLevel, domain);
        
        Optional<RolePlayScenario> scenario = scenarioRepository
                .findRandomFallbackByCefrLevelAndDomain(cefrLevel, domain);
        
        if (scenario.isPresent()) {
            log.info("Found fallback scenario '{}' for CEFR={}, domain={}", 
                    scenario.get().getTitle(), cefrLevel, domain);
        } else {
            log.warn("No fallback scenario found for CEFR={}, domain={}", cefrLevel, domain);
        }
        
        return scenario.map(RolePlayScenarioMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RolePlayScenarioDTO> getRandomFallbackScenarioFlexible(String cefrLevel, String domain) {
        log.debug("Fetching flexible fallback scenario for CEFR={}, domain={}", cefrLevel, domain);
        
        // Try exact match first
        Optional<RolePlayScenario> scenario = scenarioRepository
                .findRandomFallbackByCefrLevelAndDomain(cefrLevel, domain);
        
        if (scenario.isPresent()) {
            log.info("Found exact match fallback scenario '{}'", scenario.get().getTitle());
            return scenario.map(RolePlayScenarioMapper::toDTO);
        }
        
        // Try same CEFR level, any domain
        scenario = scenarioRepository.findRandomFallbackByCefrLevel(cefrLevel);
        if (scenario.isPresent()) {
            log.info("Found CEFR-level fallback scenario '{}' (any domain)", scenario.get().getTitle());
            return scenario.map(RolePlayScenarioMapper::toDTO);
        }
        
        // Try same domain, any CEFR level
        scenario = scenarioRepository.findRandomFallbackByDomain(domain);
        if (scenario.isPresent()) {
            log.info("Found domain fallback scenario '{}' (any CEFR level)", scenario.get().getTitle());
            return scenario.map(RolePlayScenarioMapper::toDTO);
        }
        
        // Try any fallback
        scenario = scenarioRepository.findRandomFallback();
        if (scenario.isPresent()) {
            log.info("Found any fallback scenario '{}' (no criteria match)", scenario.get().getTitle());
            return scenario.map(RolePlayScenarioMapper::toDTO);
        }
        
        log.error("No fallback scenarios available in database!");
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePlayScenarioDTO> getFallbackScenarios(String cefrLevel, String domain) {
        log.debug("Fetching all fallback scenarios for CEFR={}, domain={}", cefrLevel, domain);
        
        List<RolePlayScenario> scenarios;
        
        if (cefrLevel != null && domain != null) {
            scenarios = scenarioRepository.findByIsFallbackTrueAndCefrLevelAndDomain(cefrLevel, domain);
        } else if (cefrLevel != null) {
            scenarios = scenarioRepository.findByIsFallbackTrueAndCefrLevel(cefrLevel);
        } else if (domain != null) {
            scenarios = scenarioRepository.findByIsFallbackTrueAndDomain(domain);
        } else {
            scenarios = scenarioRepository.findByIsFallbackTrue();
        }
        
        log.info("Found {} fallback scenarios for CEFR={}, domain={}", 
                scenarios.size(), cefrLevel, domain);
        
        return scenarios.stream()
                .map(RolePlayScenarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePlayScenarioDTO> getFallbackScenariosByCefrLevel(String cefrLevel) {
        return getFallbackScenarios(cefrLevel, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePlayScenarioDTO> getFallbackScenariosByDomain(String domain) {
        return getFallbackScenarios(null, domain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFallbackScenarios(String cefrLevel, String domain) {
        if (cefrLevel != null && domain != null) {
            return scenarioRepository.countByIsFallbackTrueAndCefrLevelAndDomain(cefrLevel, domain);
        } else if (cefrLevel != null) {
            return scenarioRepository.countByIsFallbackTrueAndCefrLevel(cefrLevel);
        } else if (domain != null) {
            return scenarioRepository.countByIsFallbackTrueAndDomain(domain);
        } else {
            return scenarioRepository.countByIsFallbackTrue();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFallbackScenarios(String cefrLevel, String domain) {
        if (cefrLevel != null && domain != null) {
            return scenarioRepository.existsByIsFallbackTrueAndCefrLevelAndDomain(cefrLevel, domain);
        }
        return countFallbackScenarios(cefrLevel, domain) > 0;
    }

    @Override
    public String generateFallbackResponse(String cefrLevel, String aiRole, String userMessage) {
        log.debug("Generating fallback response for CEFR={}, role={}", cefrLevel, aiRole);
        
        List<String> responses = getResponsesForLevel(cefrLevel);
        String response = responses.get(random.nextInt(responses.size()));
        
        log.info("Generated fallback response for CEFR={}: '{}'", cefrLevel, 
                response.substring(0, Math.min(50, response.length())) + "...");
        
        return response;
    }

    @Override
    public String generateFallbackLearningResponse(String cefrLevel, String aiRole, String userMessage) {
        log.debug("Generating fallback learning response for CEFR={}", cefrLevel);
        
        String aiMessage = generateFallbackResponse(cefrLevel, aiRole, userMessage);
        
        // Generate a simple JSON response with placeholder feedback
        String jsonResponse = String.format("""
                {
                  "aiMessage": "%s",
                  "feedback": {
                    "grammarFeedback": "Keep practicing! Your message was understood.",
                    "vocabularyFeedback": "Good vocabulary choice for this context.",
                    "fluencyScore": 70,
                    "suggestions": ["Try using more complex sentences", "Consider adding transition words"]
                  }
                }
                """, escapeJsonString(aiMessage));
        
        log.info("Generated fallback learning response for CEFR={}", cefrLevel);
        return jsonResponse;
    }

    // ========== Private Helper Methods ==========

    private List<String> getResponsesForLevel(String cefrLevel) {
        if (cefrLevel == null) {
            return B1_B2_RESPONSES; // Default to intermediate level
        }
        
        return switch (cefrLevel.toUpperCase()) {
            case "A1", "A2" -> A1_A2_RESPONSES;
            case "B1", "B2" -> B1_B2_RESPONSES;
            case "C1", "C2" -> C1_C2_RESPONSES;
            default -> B1_B2_RESPONSES;
        };
    }

    private String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
