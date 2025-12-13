package com.lexia.backend.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.entity.RolePlayScenario;
import com.lexia.backend.repository.RolePlayConversationRepository;
import com.lexia.backend.repository.RolePlayScenarioRepository;
import com.lexia.backend.service.ai.impl.RolePlayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePlayServiceImplTest {

    @Mock
    private RolePlayScenarioRepository scenarioRepository;

    @Mock
    private RolePlayConversationRepository conversationRepository;

    @Mock
    private GeminiClientService geminiClientService;

    @Mock
    private AiUsageTracker aiUsageTracker;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RolePlayServiceImpl rolePlayService;

    private RolePlayScenario scenario;
    private RolePlayConversation conversation;

    @BeforeEach
    void setUp() {
        scenario = RolePlayScenario.builder()
                .id(UUID.randomUUID())
                .title("Test Scenario")
                .context("Test Context")
                .yourRole("User")
                .aiRole("AI")
                .cefrLevel("B1")
                .domain("Business")
                .openingLine("Hello")
                .build();

        conversation = RolePlayConversation.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .scenario(scenario)
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .messages(new ArrayList<>())
                .build();
    }

    @Test
    void generateScenario_Success() throws Exception {
        RolePlayRequestDTO request = RolePlayRequestDTO.builder()
                .cefrLevel("B1")
                .domain("Business")
                .build();

        String jsonResponse = "{\"title\":\"Test Scenario\"}";
        GeminiResponseDTO geminiResponse = new GeminiResponseDTO(jsonResponse, "model", null, Instant.now(), 100, false, "STOP");

        when(geminiClientService.generateContent(anyString())).thenReturn(geminiResponse);
        when(objectMapper.readValue(anyString(), eq(RolePlayScenarioDTO.class))).thenReturn(RolePlayScenarioDTO.builder().title("Test Scenario").build());
        when(scenarioRepository.save(any(RolePlayScenario.class))).thenReturn(scenario);

        RolePlayScenarioDTO result = rolePlayService.generateScenario(request);

        assertNotNull(result);
        assertEquals("Test Scenario", result.getTitle());
        verify(geminiClientService).generateContent(anyString());
        verify(scenarioRepository).save(any(RolePlayScenario.class));
    }

    @Test
    void startConversation_Success() {
        when(scenarioRepository.findById(scenario.getId())).thenReturn(Optional.of(scenario));
        when(conversationRepository.save(any(RolePlayConversation.class))).thenReturn(conversation);

        RolePlayConversationDTO result = rolePlayService.startConversation(scenario.getId(), conversation.getUserId(), "immersive");

        assertNotNull(result);
        verify(conversationRepository).save(any(RolePlayConversation.class));
    }

    @Test
    void sendMessage_Success() {
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        
        GeminiResponseDTO geminiResponse = new GeminiResponseDTO("AI Response", "model", null, Instant.now(), 100, false, "STOP");
        when(geminiClientService.generateContent(anyString())).thenReturn(geminiResponse);

        RolePlayMessageDTO result = rolePlayService.sendMessage(conversation.getId(), conversation.getUserId(), "Hello AI");

        assertNotNull(result);
        assertEquals("AI Response", result.getContent());
        assertEquals("ai", result.getRole());
        verify(conversationRepository).save(any(RolePlayConversation.class));
        verify(aiUsageTracker).trackUsage(any());
    }
}
