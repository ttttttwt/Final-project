package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.custommaterial.ChatMessageResponseDTO;
import com.lexia.backend.dto.custommaterial.EndChatResponseDTO;
import com.lexia.backend.entity.CustomMaterialChatSession;
import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.AiCorrectionMode;
import com.lexia.backend.exception.AccessDeniedException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.repository.CustomMaterialChatSessionRepository;
import com.lexia.backend.repository.UserCustomMaterialRepository;
import com.lexia.backend.service.ai.CustomMaterialChatService;
import com.lexia.backend.service.ai.GeminiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of CustomMaterialChatService for role-play conversations.
 *
 * <p>
 * Manages chat sessions where users practice English based on their
 * custom materials. Supports STRICT and POLITE correction modes.
 * </p>
 *
 * @since Sprint 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomMaterialChatServiceImpl implements CustomMaterialChatService {

    private static final String CHAT_SYSTEM_PROMPT = """
            You are an AI conversation partner for English language learning.

            <context>
            {{context}}
            </context>

            <scenario>
            {{scenario}}
            </scenario>

            <user_role>{{user_role}}</user_role>
            <ai_role>{{ai_role}}</ai_role>

            <correction_mode>{{correction_mode}}</correction_mode>

            <instructions>
            - Stay in character as {{ai_role}}
            - Respond naturally to the user's message
            - Keep responses concise (2-4 sentences typically)
            - **Ongoing Interaction**: The conversation is an ongoing interaction or a continuation of a story based on the material. Act as if the interaction is already in progress.
            - **Material Priority**: You MUST prioritize using vocabulary, sentence patterns, and specific content from the provided material. If the material is a list of vocabulary, use as many of those words as possible in your responses.
            {{#strict_mode}}
            - If the user makes grammar or vocabulary errors, correct them IMMEDIATELY before continuing:
              Format: "[Correction: 'X' should be 'Y' because...]" then continue the conversation.
            {{/strict_mode}}
            {{#polite_mode}}
            - Do NOT correct errors during the conversation
            - Respond naturally and encouragingly
            {{/polite_mode}}
            - Use vocabulary appropriate for {{cefr_level}} level
            </instructions>

            <chat_history>
            {{chat_history}}
            </chat_history>

            User (as {{user_role}}): {{user_message}}

            Respond as {{ai_role}}:
            """;

    private static final String REPORT_PROMPT = """
            Analyze this English conversation and provide a learning report.

            <chat_history>
            {{chat_history}}
            </chat_history>

            <instructions>
            Provide a JSON response with this structure:
            {
              "overallScore": 85,
              "grammarErrors": [
                {"original": "...", "suggestion": "...", "explanation": "..."}
              ],
              "vocabularySuggestions": ["word1", "word2"],
              "strengths": ["Clear arguments", "Good vocabulary usage"],
              "improvements": ["Use more formal phrases", "Watch verb tenses"]
            }

            Score fairly (0-100) based on grammar, vocabulary, and communication effectiveness.
            Be encouraging but honest about areas for improvement.
            </instructions>

            Return ONLY valid JSON, no markdown.
            """;

    private static final String DYNAMIC_PROMPTS_TEMPLATE = """
            Based on this conversation and the provided material, generate 4-5 contextually relevant response prompts for the learner.

            Scenario: %s
            Learner's Role: %s
            AI's Role: %s
            CEFR Level: %s
            
            <material_content>
            %s
            </material_content>

            AI's Last Message: "%s"

            INSTRUCTIONS:
            1. Generate 4-5 complete sentences the learner could say next
            2. Prompts should be direct responses to what the AI just said
            3. Include a variety: questions, answers, requests, statements
            4. Match the CEFR level vocabulary and grammar complexity
            5. **Material Priority**: You MUST prioritize using vocabulary, sentence patterns, and specific content from the <material_content>.

            Output as a JSON array of strings ONLY, no explanation:
            ["prompt1", "prompt2", "prompt3", "prompt4", "prompt5"]
            """;

    private final UserCustomMaterialRepository materialRepository;
    private final CustomMaterialChatSessionRepository sessionRepository;
    private final GeminiClientService geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChatMessageResponseDTO sendMessage(UUID materialId, String message, UUID sessionId, UUID userId) {
        // Get and validate material
        UserCustomMaterial material = getMaterialWithAccess(materialId, userId);

        if (!material.isReady()) {
            throw new IllegalStateException("Material is not ready for chat. Current status: " + material.getStatus());
        }

        // Get or create session
        CustomMaterialChatSession session;
        if (sessionId != null) {
            session = sessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

            if (!session.isActive()) {
                throw new IllegalStateException("Chat session is no longer active");
            }
        } else {
            // If sessionId is null, we create a new session
            // But we should check if the AI should have started it
            session = createNewSession(material, userId);
        }

        // Add user message
        session.addMessage("user", message);

        // Generate AI response
        String aiResponse = generateAiResponse(material, session, message);
        session.addMessage("ai", aiResponse);

        // Parse any corrections from STRICT mode
        List<Map<String, String>> corrections = extractCorrections(aiResponse, getCorrectionMode(material));

        // Save session
        sessionRepository.save(session);

        log.info("Chat message processed for material {} session {}", materialId, session.getId());

        return ChatMessageResponseDTO.builder()
                .sessionId(session.getId())
                .aiResponse(cleanAiResponse(aiResponse))
                .corrections(corrections)
                .messageCount(session.getMessageCount())
                .build();
    }

    @Override
    @Transactional
    public ChatMessageResponseDTO startSession(UUID materialId, UUID userId) {
        // Get and validate material
        UserCustomMaterial material = getMaterialWithAccess(materialId, userId);

        if (!material.isReady()) {
            throw new IllegalStateException("Material is not ready for chat. Current status: " + material.getStatus());
        }

        // Create new session
        CustomMaterialChatSession session = createNewSession(material, userId);

        // Get opening line from material
        Map<String, Object> roleplay = extractRoleplayData(material.getGeneratedContent());
        String openingLine = (String) roleplay.get("openingLine");

        if (openingLine == null || openingLine.isBlank()) {
            openingLine = (String) roleplay.get("suggestedOpening");
        }

        // If no opening line, generate one
        if (openingLine == null || openingLine.isBlank()) {
            openingLine = generateOpeningLine(material);
        }

        // Add AI opening message
        session.addMessage("ai", openingLine);
        sessionRepository.save(session);

        log.info("Chat session started for material {} session {}", materialId, session.getId());

        return ChatMessageResponseDTO.builder()
                .sessionId(session.getId())
                .aiResponse(openingLine)
                .corrections(new ArrayList<>())
                .messageCount(session.getMessageCount())
                .build();
    }

    private String generateOpeningLine(UserCustomMaterial material) {
        Map<String, Object> roleplay = extractRoleplayData(material.getGeneratedContent());
        String aiRole = getOrDefault(roleplay, "aiRole", "Assistant");
        String scenario = getOrDefault(roleplay, "description", "Business conversation");
        String content = material.getContentText() != null ? truncateContent(material.getContentText(), 1000) : "";

        String prompt = String.format(
                "You are an AI conversation partner playing the role of %s in this scenario: %s.\n\n" +
                        "<material_content>\n%s\n</material_content>\n\n" +
                        "The conversation is a continuation of a story based on the provided material. " +
                        "Provide a short opening line (1-2 sentences) to start the conversation as %s. " +
                        "You MUST prioritize using vocabulary, sentence patterns, and specific content from the <material_content>. " +
                        "Return ONLY the opening line.",
                aiRole, scenario, content, aiRole);

        try {
            var response = geminiClient.generateContent(prompt);
            return response.content();
        } catch (Exception e) {
            return "Hello! I'm ready to start our conversation. Shall we begin?";
        }
    }

    @Override
    @Transactional
    public EndChatResponseDTO endSession(UUID materialId, UUID sessionId, UUID userId) {
        // Validate material access
        getMaterialWithAccess(materialId, userId);

        // Get session
        CustomMaterialChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Chat session is already ended");
        }

        // Generate performance report
        Map<String, Object> report = generatePerformanceReport(session);
        session.endSession(report);
        sessionRepository.save(session);

        log.info("Chat session {} ended for material {}", sessionId, materialId);

        return buildEndChatResponse(session, report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> generateDynamicPrompts(UUID materialId, UUID sessionId, UUID userId) {
        log.debug("Generating dynamic prompts for material {} session {}", materialId, sessionId);

        // Validate material access
        UserCustomMaterial material = getMaterialWithAccess(materialId, userId);

        // Get session
        CustomMaterialChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

        Map<String, Object> roleplay = extractRoleplayData(material.getGeneratedContent());
        List<Map<String, Object>> messages = session.getChatHistory();

        // Find the last AI message
        String lastAiMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = messages.get(i);
            if ("ai".equals(msg.get("role"))) {
                lastAiMessage = (String) msg.get("content");
                break;
            }
        }

        // If no AI message found, return default prompts from material
        if (lastAiMessage.isEmpty()) {
            log.info("No AI message found, returning material default prompts");
            return extractSuggestedPrompts(roleplay);
        }

        try {
            String content = material.getContentText() != null ? truncateContent(material.getContentText(), 1000) : "";
            String prompt = String.format(DYNAMIC_PROMPTS_TEMPLATE,
                    getOrDefault(roleplay, "description", "Business conversation"),
                    getOrDefault(roleplay, "yourRole", "User"),
                    getOrDefault(roleplay, "aiRole", "Assistant"),
                    "B2", // Default level
                    content,
                    lastAiMessage);

            var response = geminiClient.generateContent(prompt);
            String jsonContent = cleanJsonResponse(response.content());

            // Parse JSON array
            List<String> prompts = objectMapper.readValue(jsonContent,
                    new TypeReference<List<String>>() {
                    });

            log.info("Generated {} dynamic prompts for session {}", prompts.size(), sessionId);
            return prompts;
        } catch (Exception e) {
            log.warn("Failed to generate dynamic prompts, falling back to defaults: {}", e.getMessage());
            return extractSuggestedPrompts(roleplay);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractSuggestedPrompts(Map<String, Object> roleplay) {
        Object prompts = roleplay.get("suggestedPrompts");
        if (prompts instanceof List) {
            return (List<String>) prompts;
        }
        return List.of(
                "Could you tell me more about that?",
                "I understand. What should I do next?",
                "Thank you for the information.",
                "Can you explain that in more detail?",
                "I have a question about what you mentioned.");
    }

    // ===== Private Helper Methods =====

    private UserCustomMaterial getMaterialWithAccess(UUID materialId, UUID userId) {
        UserCustomMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomMaterial", materialId));

        if (!material.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have access to this material");
        }

        return material;
    }

    private CustomMaterialChatSession createNewSession(UserCustomMaterial material, UUID userId) {
        CustomMaterialChatSession session = CustomMaterialChatSession.builder()
                .material(material)
                .userId(userId)
                .build();
        return sessionRepository.save(session);
    }

    private AiCorrectionMode getCorrectionMode(UserCustomMaterial material) {
        if (material.getSettings() != null) {
            return material.getSettings().getAiCorrectionMode();
        }
        return AiCorrectionMode.POLITE; // Default
    }

    private String generateAiResponse(UserCustomMaterial material, CustomMaterialChatSession session,
            String userMessage) {
        String prompt = buildChatPrompt(material, session, userMessage);

        try {
            var response = geminiClient.generateContent(prompt);
            return response.content();
        } catch (Exception e) {
            log.error("Failed to generate AI response: {}", e.getMessage(), e);
            return "I apologize, but I'm having trouble responding right now. Could you try again?";
        }
    }

    private String buildChatPrompt(UserCustomMaterial material, CustomMaterialChatSession session, String userMessage) {
        Map<String, Object> generatedContent = material.getGeneratedContent();
        Map<String, Object> roleplay = extractRoleplayData(generatedContent);
        AiCorrectionMode mode = getCorrectionMode(material);

        String prompt = CHAT_SYSTEM_PROMPT
                .replace("{{context}}",
                        material.getContentText() != null ? truncateContent(material.getContentText(), 1000) : "")
                .replace("{{scenario}}", getOrDefault(roleplay, "description", "Business conversation"))
                .replace("{{user_role}}", getOrDefault(roleplay, "userRole", "User"))
                .replace("{{ai_role}}", getOrDefault(roleplay, "aiRole", "Assistant"))
                .replace("{{correction_mode}}", mode.name())
                .replace("{{cefr_level}}", "B2") // Could be dynamic based on user profile
                .replace("{{chat_history}}", formatChatHistory(session.getChatHistory()))
                .replace("{{user_message}}", userMessage);

        // Handle conditional sections
        if (mode == AiCorrectionMode.STRICT) {
            prompt = prompt.replace("{{#strict_mode}}", "").replace("{{/strict_mode}}", "");
            prompt = prompt.replaceAll("\\{\\{#polite_mode\\}\\}.*?\\{\\{/polite_mode\\}\\}", "");
        } else {
            prompt = prompt.replace("{{#polite_mode}}", "").replace("{{/polite_mode}}", "");
            prompt = prompt.replaceAll("\\{\\{#strict_mode\\}\\}.*?\\{\\{/strict_mode\\}\\}", "");
        }

        return prompt;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractRoleplayData(Map<String, Object> generatedContent) {
        if (generatedContent == null)
            return new HashMap<>();
        Object roleplay = generatedContent.get("roleplay");
        if (roleplay instanceof Map) {
            return (Map<String, Object>) roleplay;
        }
        return new HashMap<>();
    }

    private String getOrDefault(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null)
            return "";
        if (content.length() <= maxLength)
            return content;
        return content.substring(0, maxLength) + "...";
    }

    private String formatChatHistory(List<Map<String, Object>> history) {
        if (history == null || history.isEmpty())
            return "(No previous messages)";

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> msg : history) {
            String role = (String) msg.get("role");
            String content = (String) msg.get("content");
            sb.append(role.equals("user") ? "User: " : "AI: ");
            sb.append(content).append("\n");
        }
        return sb.toString().trim();
    }

    private List<Map<String, String>> extractCorrections(String aiResponse, AiCorrectionMode mode) {
        if (mode != AiCorrectionMode.STRICT)
            return Collections.emptyList();

        List<Map<String, String>> corrections = new ArrayList<>();
        // Simple pattern matching for [Correction: ...] format
        String pattern = "\\[Correction: ([^\\]]+)\\]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(aiResponse);

        while (m.find()) {
            String correctionText = m.group(1);
            corrections.add(Map.of("correction", correctionText));
        }

        return corrections;
    }

    private String cleanAiResponse(String response) {
        // Remove correction annotations if present
        return response.replaceAll("\\[Correction: [^\\]]+\\]\\s*", "").trim();
    }

    private String cleanJsonResponse(String json) {
        if (json == null)
            return "";
        // Remove markdown code blocks if present
        json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
        if (json.startsWith("```")) {
            json = json.substring(3).trim();
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3).trim();
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> generatePerformanceReport(CustomMaterialChatSession session) {
        String prompt = REPORT_PROMPT.replace("{{chat_history}}", formatChatHistory(session.getChatHistory()));

        try {
            var response = geminiClient.generateContent(prompt);
            String content = response.content();

            // Parse JSON response
            // In production, use Jackson ObjectMapper
            // For now, create a basic structure
            return parseReportJson(content);
        } catch (Exception e) {
            log.error("Failed to generate performance report: {}", e.getMessage(), e);
            return createDefaultReport(session);
        }
    }

    private Map<String, Object> parseReportJson(String json) {
        try {
            // Remove any markdown formatting
            json = cleanJsonResponse(json);

            // Parse using Jackson
            Map<String, Object> parsed = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {
                    });

            // Build normalized report with expected structure
            Map<String, Object> report = new HashMap<>();

            // Extract overallScore
            if (parsed.containsKey("overallScore")) {
                Object score = parsed.get("overallScore");
                report.put("overallScore", score instanceof Number ? ((Number) score).intValue() : 70);
            } else {
                report.put("overallScore", 70);
            }

            // Extract grammarErrors (array of objects with original/suggestion/explanation)
            if (parsed.containsKey("grammarErrors") && parsed.get("grammarErrors") instanceof List) {
                report.put("grammarErrors", parsed.get("grammarErrors"));
            } else {
                report.put("grammarErrors", new ArrayList<>());
            }

            // Extract vocabularySuggestions (array of strings)
            if (parsed.containsKey("vocabularySuggestions") && parsed.get("vocabularySuggestions") instanceof List) {
                report.put("vocabularySuggestions", parsed.get("vocabularySuggestions"));
            } else {
                report.put("vocabularySuggestions", new ArrayList<>());
            }

            // Extract strengths (array of strings)
            if (parsed.containsKey("strengths") && parsed.get("strengths") instanceof List) {
                report.put("strengths", parsed.get("strengths"));
            } else {
                report.put("strengths", List.of("Good communication effort"));
            }

            // Extract improvements (array of strings)
            if (parsed.containsKey("improvements") && parsed.get("improvements") instanceof List) {
                report.put("improvements", parsed.get("improvements"));
            } else {
                report.put("improvements", List.of("Continue practicing"));
            }

            return report;

        } catch (Exception e) {
            log.warn("Failed to parse report JSON: {}", e.getMessage());
            return createDefaultReport(null);
        }
    }

    private Map<String, Object> createDefaultReport(CustomMaterialChatSession session) {
        Map<String, Object> report = new HashMap<>();
        report.put("overallScore", 70);
        report.put("grammarErrors", new ArrayList<>());
        report.put("vocabularySuggestions", new ArrayList<>());
        report.put("strengths", List.of("Good participation", "Engaged in conversation"));
        report.put("improvements", List.of("Keep practicing regularly"));
        return report;
    }

    @SuppressWarnings("unchecked")
    private EndChatResponseDTO buildEndChatResponse(CustomMaterialChatSession session, Map<String, Object> report) {
        Instant startedAt = session.getStartedAt();
        Instant endedAt = session.getEndedAt() != null ? session.getEndedAt() : Instant.now();
        long durationSeconds = Duration.between(startedAt, endedAt).getSeconds();

        return EndChatResponseDTO.builder()
                .sessionId(session.getId())
                .overallScore((Integer) report.getOrDefault("overallScore", 70))
                .grammarErrors((List<Map<String, String>>) report.getOrDefault("grammarErrors", new ArrayList<>()))
                .vocabularySuggestions((List<String>) report.getOrDefault("vocabularySuggestions", new ArrayList<>()))
                .strengths((List<String>) report.getOrDefault("strengths", new ArrayList<>()))
                .improvements((List<String>) report.getOrDefault("improvements", new ArrayList<>()))
                .totalMessages(session.getMessageCount())
                .durationSeconds(durationSeconds)
                .build();
    }
}
