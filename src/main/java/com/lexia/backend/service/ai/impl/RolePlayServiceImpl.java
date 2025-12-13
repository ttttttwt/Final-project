package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.entity.RolePlayScenario;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.mapper.RolePlayConversationMapper;
import com.lexia.backend.mapper.RolePlayScenarioMapper;
import com.lexia.backend.repository.RolePlayConversationRepository;
import com.lexia.backend.repository.RolePlayScenarioRepository;
import com.lexia.backend.service.ai.AiUsageTracker;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.service.ai.RolePlayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the Role-Play Service for AI-powered conversation practice.
 * 
 * <p>Supports two conversation modes:</p>
 * <ul>
 *   <li><b>Immersive Mode</b>: Fast chat-only responses (~500 tokens, &lt;1s p95)</li>
 *   <li><b>Learning Mode</b>: Chat with feedback (~1500 tokens, &lt;3s p95)</li>
 * </ul>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>SSE streaming for real-time responses</li>
 *   <li>Context window management (last 10 messages)</li>
 *   <li>Fallback content when AI fails</li>
 *   <li>Usage tracking and quota management</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolePlayServiceImpl implements RolePlayService {

    private final RolePlayScenarioRepository scenarioRepository;
    private final RolePlayConversationRepository conversationRepository;
    private final GeminiClientService geminiClientService;
    private final AiUsageTracker aiUsageTracker;
    private final ObjectMapper objectMapper;

    private static final int CONTEXT_WINDOW_SIZE = 10;
    private static final long SSE_TIMEOUT_MS = 30_000L;
    private static final int MAX_MESSAGE_LENGTH = 500;
    
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    private static final String SCENARIO_GENERATION_PROMPT = """
            Generate a role-play scenario for learning English.
            CEFR Level: %s
            Domain: %s
            Industry: %s
            User Context: %s
            
            Output strictly valid JSON matching this schema:
            {
              "title": "string",
              "context": "string",
              "yourRole": "string",
              "aiRole": "string",
              "objectives": ["string"],
              "keyVocabulary": [{"term": "string", "definition": "string", "ipa": "string"}],
              "openingLine": "string",
              "suggestedDuration": 10
            }
            """;

    @Override
    @Transactional
    public RolePlayScenarioDTO generateScenario(RolePlayRequestDTO request) {
        String prompt = String.format(SCENARIO_GENERATION_PROMPT,
                request.getCefrLevel(),
                request.getDomain(),
                request.getIndustry() != null ? request.getIndustry() : "General",
                request.getUserContext() != null ? request.getUserContext() : "None");

        GeminiResponseDTO response = geminiClientService.generateContent(prompt);
        
        try {
            String jsonContent = cleanJson(response.content());
            RolePlayScenarioDTO generatedDto = objectMapper.readValue(jsonContent, RolePlayScenarioDTO.class);
            
            generatedDto.setCefrLevel(request.getCefrLevel());
            generatedDto.setDomain(request.getDomain());
            generatedDto.setIndustry(request.getIndustry());
            generatedDto.setIsFallback(false);
            
            RolePlayScenario entity = RolePlayScenarioMapper.toEntity(generatedDto);
            entity = scenarioRepository.save(entity);
            
            return RolePlayScenarioMapper.toDTO(entity);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI generated scenario", e);
            throw new AiServiceException("Failed to parse AI-generated scenario", e);
        }
    }

    @Override
    public RolePlayScenarioDTO getScenario(UUID id) {
        return scenarioRepository.findById(id)
                .map(RolePlayScenarioMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayScenario", id));
    }

    @Override
    public Page<RolePlayScenarioDTO> getAllScenarios(Pageable pageable) {
        return scenarioRepository.findAll(pageable)
                .map(RolePlayScenarioMapper::toDTO);
    }

    @Override
    @Transactional
    public RolePlayConversationDTO startConversation(UUID scenarioId, UUID userId, String mode) {
        RolePlayScenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayScenario", scenarioId));

        RolePlayConversation conversation = RolePlayConversation.builder()
                .userId(userId)
                .scenario(scenario)
                .mode(mode)
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .messages(new ArrayList<>())
                .build();

        if (scenario.getOpeningLine() != null && !scenario.getOpeningLine().isBlank()) {
            Map<String, Object> openingMessage = new HashMap<>();
            openingMessage.put("role", "ai");
            openingMessage.put("content", scenario.getOpeningLine());
            openingMessage.put("timestamp", Instant.now().toString());
            conversation.getMessages().add(openingMessage);
        }

        conversation = conversationRepository.save(conversation);
        return RolePlayConversationMapper.toDTO(conversation);
    }

    @Override
    @Transactional
    public RolePlayMessageDTO sendMessage(UUID conversationId, UUID userId, String message) {
        RolePlayConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayConversation", conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this conversation");
        }

        if (!RolePlayConversation.STATUS_IN_PROGRESS.equals(conversation.getStatus())) {
            throw new IllegalStateException("Cannot send message to " + conversation.getStatus() + " conversation");
        }

        String sanitizedMessage = sanitizeUserInput(message);

        // Add user message
        Map<String, Object> userMsgMap = new HashMap<>();
        userMsgMap.put("role", "user");
        userMsgMap.put("content", sanitizedMessage);
        userMsgMap.put("timestamp", Instant.now().toString());
        conversation.getMessages().add(userMsgMap);

        // Construct prompt with context window (last 10 messages)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are ").append(conversation.getScenario().getAiRole())
                .append(". The user is ").append(conversation.getScenario().getYourRole())
                .append(". Context: ").append(conversation.getScenario().getContext())
                .append("\n\nConversation History:\n");

        List<Map<String, Object>> messages = conversation.getMessages();
        int start = Math.max(0, messages.size() - 10);
        for (int i = start; i < messages.size(); i++) {
            Map<String, Object> msg = messages.get(i);
            promptBuilder.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }
        promptBuilder.append("ai: ");

        // Get AI response
        GeminiResponseDTO response = geminiClientService.generateContent(promptBuilder.toString());
        String aiContent = response.content();

        // Track usage
        AiUsageTrackingRequest trackingRequest = AiUsageTrackingRequest.builder()
                .userId(userId)
                .contentType(AiUsageTracker.CONTENT_TYPE_ROLEPLAY)
                .modelId(response.model())
                .inputTokens(0) 
                .outputTokens(response.tokenUsage() != null ? response.tokenUsage().outputTokens() : 0)
                .responseTimeMs((int) response.responseTimeMs())
                .success(true)
                .build();
        aiUsageTracker.trackUsage(trackingRequest);

        // Add AI message
        Map<String, Object> aiMsgMap = new HashMap<>();
        aiMsgMap.put("role", "ai");
        aiMsgMap.put("content", aiContent);
        aiMsgMap.put("timestamp", Instant.now().toString());
        conversation.getMessages().add(aiMsgMap);

        conversationRepository.save(conversation);

        return RolePlayMessageDTO.builder()
                .role("ai")
                .content(aiContent)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public RolePlayConversationDTO getConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .map(RolePlayConversationMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayConversation", conversationId));
    }

    @Override
    public Page<RolePlayConversationDTO> getUserConversations(UUID userId, Pageable pageable, String status) {
        Page<RolePlayConversation> conversations;
        if (status != null && !status.isBlank()) {
            conversations = conversationRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            conversations = conversationRepository.findByUserId(userId, pageable);
        }
        return conversations.map(RolePlayConversationMapper::toDTO);
    }

    // ========== Immersive Mode (B5a) ==========

    @Override
    @Transactional
    public RolePlayMessageDTO sendImmersiveMessage(UUID conversationId, UUID userId, String message) {
        log.debug("Sending immersive message for conversation {}", conversationId);
        
        RolePlayConversation conversation = validateAndGetConversation(conversationId, userId);
        String sanitizedMessage = sanitizeUserInput(message);
        
        // Add user message
        addUserMessage(conversation, sanitizedMessage);
        
        // Build immersive prompt (concise, no feedback)
        String prompt = buildImmersivePrompt(conversation, sanitizedMessage);
        
        long startTime = System.currentTimeMillis();
        GeminiResponseDTO response = geminiClientService.generateContent(prompt);
        long responseTime = System.currentTimeMillis() - startTime;
        
        String aiContent = response.content();
        
        // Track usage
        trackUsage(userId, response, responseTime);
        
        // Add AI message (no feedback for immersive mode)
        Map<String, Object> aiMsgMap = createAiMessage(aiContent, null);
        conversation.getMessages().add(aiMsgMap);
        
        conversationRepository.save(conversation);
        
        log.info("Immersive message sent in {}ms for conversation {}", responseTime, conversationId);
        
        return RolePlayMessageDTO.builder()
                .role("ai")
                .content(aiContent)
                .timestamp(Instant.now())
                .build();
    }

    // ========== Learning Mode (B5b) ==========

    @Override
    @Transactional
    public RolePlayMessageDTO sendLearningMessage(UUID conversationId, UUID userId, String message) {
        log.debug("Sending learning message for conversation {}", conversationId);
        
        RolePlayConversation conversation = validateAndGetConversation(conversationId, userId);
        String sanitizedMessage = sanitizeUserInput(message);
        
        // Add user message
        addUserMessage(conversation, sanitizedMessage);
        
        // Build learning prompt (includes feedback request)
        String prompt = buildLearningPrompt(conversation, sanitizedMessage);
        
        long startTime = System.currentTimeMillis();
        GeminiResponseDTO response = geminiClientService.generateContent(prompt);
        long responseTime = System.currentTimeMillis() - startTime;
        
        // Parse learning mode response (AI message + feedback)
        LearningModeResponse parsedResponse = parseLearningResponse(response.content());
        
        // Track usage
        trackUsage(userId, response, responseTime);
        
        // Add AI message with feedback
        Map<String, Object> aiMsgMap = createAiMessage(parsedResponse.aiMessage, parsedResponse.feedback);
        conversation.getMessages().add(aiMsgMap);
        
        conversationRepository.save(conversation);
        
        log.info("Learning message sent in {}ms for conversation {}", responseTime, conversationId);
        
        return RolePlayMessageDTO.builder()
                .role("ai")
                .content(parsedResponse.aiMessage)
                .timestamp(Instant.now())
                .feedback(parsedResponse.feedback)
                .build();
    }

    // ========== SSE Streaming (B5c) ==========

    @Override
    @Transactional
    public SseEmitter streamMessage(UUID conversationId, UUID userId, String message) {
        log.debug("Starting SSE stream for conversation {}", conversationId);
        
        RolePlayConversation conversation = validateAndGetConversation(conversationId, userId);
        String sanitizedMessage = sanitizeUserInput(message);
        
        // Add user message immediately
        addUserMessage(conversation, sanitizedMessage);
        conversationRepository.save(conversation);
        
        // Build prompt
        String prompt = buildImmersivePrompt(conversation, sanitizedMessage);
        
        // Create emitter with timeout for the client
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        StringBuilder fullContent = new StringBuilder();
        UUID convId = conversationId;
        
        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out for conversation {}", convId);
            // Persist whatever content we have before timeout
            if (!fullContent.isEmpty()) {
                persistStreamedMessage(convId, userId, fullContent.toString());
            }
        });
        
        emitter.onError(ex -> {
            log.error("SSE stream error for conversation {}: {}", convId, ex.getMessage());
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\": \"Stream error occurred\"}"));
            } catch (IOException ignored) {
                // Client disconnected
            }
        });
        
        // Stream content in background thread
        sseExecutor.submit(() -> {
            try {
                // Use streaming generation from Gemini
                SseEmitter geminiEmitter = geminiClientService.streamContent(prompt);
                
                // For now, since we can't easily relay SSE, fall back to non-streaming
                // and send the response in chunks to simulate streaming
                GeminiResponseDTO response = geminiClientService.generateContent(prompt);
                String content = response.content();
                fullContent.append(content);
                
                // Send content in chunks for streaming effect
                int chunkSize = 50;
                for (int i = 0; i < content.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, content.length());
                    String chunk = content.substring(i, end);
                    emitter.send(SseEmitter.event()
                            .name("token")
                            .data("{\"token\": \"" + escapeJsonString(chunk) + "\"}"));
                    Thread.sleep(50); // Small delay between chunks
                }
                
                // Send completion event
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("{\"complete\": true}"));
                
                // Persist the AI message
                persistStreamedMessage(convId, userId, fullContent.toString());
                
                emitter.complete();
                log.debug("SSE stream completed for conversation {}", convId);
            } catch (Exception e) {
                log.error("Error in SSE stream: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"" + escapeJsonString(e.getMessage()) + "\"}"));
                    emitter.completeWithError(e);
                } catch (IOException ignored) {
                    // Client disconnected
                }
            }
        });
        
        return emitter; // Return our emitter that we control
    }
    
    private String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // ========== Fallback Mode (B5d) ==========

    @Override
    @Transactional
    public RolePlayMessageDTO sendFallbackMessage(UUID conversationId, UUID userId, String message) {
        log.debug("Sending fallback message for conversation {}", conversationId);
        
        RolePlayConversation conversation = validateAndGetConversation(conversationId, userId);
        String sanitizedMessage = sanitizeUserInput(message);
        
        // Add user message
        addUserMessage(conversation, sanitizedMessage);
        
        String aiContent;
        boolean usedFallback = false;
        
        try {
            // Try AI first
            String prompt = buildImmersivePrompt(conversation, sanitizedMessage);
            GeminiResponseDTO response = geminiClientService.generateContent(prompt);
            aiContent = response.content();
            
            // Track AI usage
            trackUsage(userId, response, response.responseTimeMs());
        } catch (Exception e) {
            log.warn("AI failed, using fallback for conversation {}: {}", conversationId, e.getMessage());
            aiContent = generateFallbackResponse(conversation, sanitizedMessage);
            usedFallback = true;
        }
        
        // Add AI message
        Map<String, Object> aiMsgMap = createAiMessage(aiContent, null);
        if (usedFallback) {
            aiMsgMap.put("isFallback", true);
        }
        conversation.getMessages().add(aiMsgMap);
        
        conversationRepository.save(conversation);
        
        return RolePlayMessageDTO.builder()
                .role("ai")
                .content(aiContent)
                .timestamp(Instant.now())
                .build();
    }

    // ========== Conversation Completion ==========

    @Override
    @Transactional
    public RolePlayConversationDTO completeConversation(UUID conversationId, UUID userId) {
        log.debug("Completing conversation {}", conversationId);
        
        RolePlayConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayConversation", conversationId));
        
        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this conversation");
        }
        
        if (RolePlayConversation.STATUS_COMPLETED.equals(conversation.getStatus())) {
            throw new IllegalStateException("Conversation is already completed");
        }
        
        if (RolePlayConversation.STATUS_ABANDONED.equals(conversation.getStatus())) {
            throw new IllegalStateException("Cannot complete an abandoned conversation");
        }
        
        // Calculate metrics
        Map<String, Object> metrics = calculateConversationMetrics(conversation);
        conversation.setMetrics(metrics);
        conversation.setStatus(RolePlayConversation.STATUS_COMPLETED);
        
        // Generate feedback summary for learning mode
        if ("learning".equals(conversation.getMode())) {
            Map<String, Object> feedbackSummary = generateFeedbackSummary(conversation);
            conversation.setFeedbackSummary(feedbackSummary);
        }
        
        conversation = conversationRepository.save(conversation);
        
        log.info("Conversation {} completed with {} messages", conversationId, 
                conversation.getMessages().size());
        
        return RolePlayConversationMapper.toDTO(conversation);
    }

    @Override
    public RolePlayConversationDTO getConversation(UUID conversationId, UUID userId) {
        RolePlayConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayConversation", conversationId));
        
        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this conversation");
        }
        
        return RolePlayConversationMapper.toDTO(conversation);
    }

    // ========== Private Helper Methods ==========
    
    private RolePlayConversation validateAndGetConversation(UUID conversationId, UUID userId) {
        RolePlayConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("RolePlayConversation", conversationId));
        
        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this conversation");
        }
        
        if (!RolePlayConversation.STATUS_IN_PROGRESS.equals(conversation.getStatus())) {
            throw new IllegalStateException("Cannot send message to " + conversation.getStatus() + " conversation");
        }
        
        return conversation;
    }
    
    private void addUserMessage(RolePlayConversation conversation, String content) {
        Map<String, Object> userMsgMap = new HashMap<>();
        userMsgMap.put("role", "user");
        userMsgMap.put("content", content);
        userMsgMap.put("timestamp", Instant.now().toString());
        conversation.getMessages().add(userMsgMap);
    }
    
    private Map<String, Object> createAiMessage(String content, Map<String, Object> feedback) {
        Map<String, Object> aiMsgMap = new HashMap<>();
        aiMsgMap.put("role", "ai");
        aiMsgMap.put("content", content);
        aiMsgMap.put("timestamp", Instant.now().toString());
        if (feedback != null && !feedback.isEmpty()) {
            aiMsgMap.put("feedback", feedback);
        }
        return aiMsgMap;
    }
    
    private String buildImmersivePrompt(RolePlayConversation conversation, String userMessage) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are ").append(conversation.getScenario().getAiRole())
                .append(". The user is ").append(conversation.getScenario().getYourRole())
                .append(". Context: ").append(conversation.getScenario().getContext())
                .append("\n\n");
        
        promptBuilder.append("INSTRUCTIONS: Respond naturally in your role. Keep response concise (2-4 sentences). ")
                .append("Match the CEFR level: ").append(conversation.getScenario().getCefrLevel())
                .append("\n\nConversation History:\n");
        
        // Context window: last N messages
        List<Map<String, Object>> messages = conversation.getMessages();
        int start = Math.max(0, messages.size() - CONTEXT_WINDOW_SIZE);
        for (int i = start; i < messages.size(); i++) {
            Map<String, Object> msg = messages.get(i);
            promptBuilder.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }
        
        return promptBuilder.toString();
    }
    
    private String buildLearningPrompt(RolePlayConversation conversation, String userMessage) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are ").append(conversation.getScenario().getAiRole())
                .append(". The user is ").append(conversation.getScenario().getYourRole())
                .append(". Context: ").append(conversation.getScenario().getContext())
                .append("\nCEFR Level: ").append(conversation.getScenario().getCefrLevel())
                .append("\n\n");
        
        promptBuilder.append("""
                INSTRUCTIONS: 
                1. First, respond naturally in your role (2-4 sentences)
                2. Then, provide feedback on the user's message
                
                OUTPUT FORMAT (strict JSON):
                {
                  "aiMessage": "your response here",
                  "feedback": {
                    "grammarFeedback": "any grammar corrections or 'Excellent grammar!'",
                    "vocabularyFeedback": "alternative expressions or 'Great vocabulary choice!'",
                    "fluencyScore": 85,
                    "suggestions": ["suggestion 1", "suggestion 2"]
                  }
                }
                
                Conversation History:
                """);
        
        // Context window: last N messages
        List<Map<String, Object>> messages = conversation.getMessages();
        int start = Math.max(0, messages.size() - CONTEXT_WINDOW_SIZE);
        for (int i = start; i < messages.size(); i++) {
            Map<String, Object> msg = messages.get(i);
            promptBuilder.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }
        
        return promptBuilder.toString();
    }
    
    private LearningModeResponse parseLearningResponse(String content) {
        try {
            String jsonContent = cleanJson(content);
            var node = objectMapper.readTree(jsonContent);
            
            String aiMessage = node.has("aiMessage") ? node.get("aiMessage").asText() : content;
            Map<String, Object> feedback = new HashMap<>();
            
            if (node.has("feedback")) {
                var feedbackNode = node.get("feedback");
                if (feedbackNode.has("grammarFeedback")) {
                    feedback.put("grammarFeedback", feedbackNode.get("grammarFeedback").asText());
                }
                if (feedbackNode.has("vocabularyFeedback")) {
                    feedback.put("vocabularyFeedback", feedbackNode.get("vocabularyFeedback").asText());
                }
                if (feedbackNode.has("fluencyScore")) {
                    feedback.put("fluencyScore", feedbackNode.get("fluencyScore").asInt());
                }
                if (feedbackNode.has("suggestions")) {
                    List<String> suggestions = new ArrayList<>();
                    feedbackNode.get("suggestions").forEach(s -> suggestions.add(s.asText()));
                    feedback.put("suggestions", suggestions);
                }
            }
            
            return new LearningModeResponse(aiMessage, feedback);
        } catch (Exception e) {
            log.warn("Failed to parse learning response JSON, using raw content: {}", e.getMessage());
            return new LearningModeResponse(content, Map.of());
        }
    }
    
    private record LearningModeResponse(String aiMessage, Map<String, Object> feedback) {}
    
    private void trackUsage(UUID userId, GeminiResponseDTO response, long responseTimeMs) {
        AiUsageTrackingRequest trackingRequest = AiUsageTrackingRequest.builder()
                .userId(userId)
                .contentType(AiUsageTracker.CONTENT_TYPE_ROLEPLAY)
                .modelId(response.model())
                .inputTokens(response.tokenUsage() != null ? response.tokenUsage().inputTokens() : 0)
                .outputTokens(response.tokenUsage() != null ? response.tokenUsage().outputTokens() : 0)
                .responseTimeMs((int) responseTimeMs)
                .success(true)
                .build();
        aiUsageTracker.trackUsage(trackingRequest);
    }
    
    private void persistStreamedMessage(UUID conversationId, UUID userId, String content) {
        try {
            RolePlayConversation conversation = conversationRepository.findById(conversationId)
                    .orElse(null);
            if (conversation != null && content != null && !content.isBlank()) {
                Map<String, Object> aiMsgMap = createAiMessage(content, null);
                conversation.getMessages().add(aiMsgMap);
                conversationRepository.save(conversation);
                log.debug("Persisted streamed message for conversation {}", conversationId);
            }
        } catch (Exception e) {
            log.error("Failed to persist streamed message: {}", e.getMessage());
        }
    }
    
    private String generateFallbackResponse(RolePlayConversation conversation, String userMessage) {
        // Generate a context-aware fallback response
        String aiRole = conversation.getScenario().getAiRole();
        String cefrLevel = conversation.getScenario().getCefrLevel();
        
        // Simple fallback responses based on CEFR level
        List<String> fallbacks = switch (cefrLevel) {
            case "A1", "A2" -> List.of(
                "I understand. Can you tell me more?",
                "That's interesting. What do you think?",
                "I see. Please continue.",
                "Good point. Let me think about that."
            );
            case "B1", "B2" -> List.of(
                "That's a great observation. Could you elaborate on that?",
                "I appreciate your input. How would you suggest we proceed?",
                "Interesting perspective. What are the key factors here?",
                "Thank you for sharing that. What's your recommendation?"
            );
            default -> List.of(
                "That's an insightful point. I'd like to explore this further with you.",
                "Your analysis raises some important considerations. What alternatives do you see?",
                "I value your perspective on this matter. How would you prioritize these factors?",
                "That's a nuanced observation. Could you walk me through your reasoning?"
            );
        };
        
        return fallbacks.get(new Random().nextInt(fallbacks.size()));
    }
    
    private Map<String, Object> calculateConversationMetrics(RolePlayConversation conversation) {
        Map<String, Object> metrics = new HashMap<>();
        List<Map<String, Object>> messages = conversation.getMessages();
        
        int messageCount = messages.size();
        int userWordCount = 0;
        int aiWordCount = 0;
        
        for (Map<String, Object> msg : messages) {
            String content = (String) msg.get("content");
            if (content != null) {
                int wordCount = content.split("\\s+").length;
                if ("user".equals(msg.get("role"))) {
                    userWordCount += wordCount;
                } else {
                    aiWordCount += wordCount;
                }
            }
        }
        
        // Calculate duration
        long durationMinutes = 0;
        if (messageCount >= 2) {
            try {
                String firstTs = (String) messages.get(0).get("timestamp");
                String lastTs = (String) messages.get(messageCount - 1).get("timestamp");
                if (firstTs != null && lastTs != null) {
                    Instant first = Instant.parse(firstTs);
                    Instant last = Instant.parse(lastTs);
                    durationMinutes = Duration.between(first, last).toMinutes();
                }
            } catch (Exception e) {
                log.warn("Could not calculate duration: {}", e.getMessage());
            }
        }
        
        metrics.put("messageCount", messageCount);
        metrics.put("userWordCount", userWordCount);
        metrics.put("aiWordCount", aiWordCount);
        metrics.put("durationMinutes", durationMinutes);
        
        return metrics;
    }
    
    private Map<String, Object> generateFeedbackSummary(RolePlayConversation conversation) {
        Map<String, Object> summary = new HashMap<>();
        
        List<String> allGrammarFeedback = new ArrayList<>();
        List<String> allVocabFeedback = new ArrayList<>();
        List<Integer> fluencyScores = new ArrayList<>();
        
        for (Map<String, Object> msg : conversation.getMessages()) {
            if (msg.containsKey("feedback")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> feedback = (Map<String, Object>) msg.get("feedback");
                if (feedback.containsKey("grammarFeedback")) {
                    allGrammarFeedback.add((String) feedback.get("grammarFeedback"));
                }
                if (feedback.containsKey("vocabularyFeedback")) {
                    allVocabFeedback.add((String) feedback.get("vocabularyFeedback"));
                }
                if (feedback.containsKey("fluencyScore")) {
                    fluencyScores.add((Integer) feedback.get("fluencyScore"));
                }
            }
        }
        
        summary.put("grammarPoints", allGrammarFeedback);
        summary.put("vocabularyPoints", allVocabFeedback);
        
        if (!fluencyScores.isEmpty()) {
            double avgFluency = fluencyScores.stream().mapToInt(i -> i).average().orElse(0);
            summary.put("averageFluencyScore", Math.round(avgFluency));
        }
        
        return summary;
    }
    
    private String cleanJson(String content) {
        Pattern pattern = Pattern.compile("\\{.*\\}|\\[.*\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return content.trim();
    }

    private String sanitizeUserInput(String input) {
        if (input == null) return "";
        String processed = input.replaceAll("(?i)(ignore|disregard)\\s+(previous|all)\\s+(instructions|prompts)", "[filtered]");
        return processed.substring(0, Math.min(processed.length(), 500));
    }
}
