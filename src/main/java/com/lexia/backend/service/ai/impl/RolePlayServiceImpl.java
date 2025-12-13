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

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePlayServiceImpl implements RolePlayService {

    private final RolePlayScenarioRepository scenarioRepository;
    private final RolePlayConversationRepository conversationRepository;
    private final GeminiClientService geminiClientService;
    private final AiUsageTracker aiUsageTracker;
    private final ObjectMapper objectMapper;

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
    public Page<RolePlayConversationDTO> getUserConversations(UUID userId, Pageable pageable) {
        return conversationRepository.findByUserId(userId, pageable)
                .map(RolePlayConversationMapper::toDTO);
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
        return input.replaceAll("(?i)(ignore|disregard)\\s+(previous|all)\\s+(instructions|prompts)", "[filtered]")
                    .substring(0, Math.min(input.length(), 500));
    }
}
