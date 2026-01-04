package com.lexia.backend.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.config.TestSecurityConfig;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.service.ai.RolePlayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RolePlayController.
 * Tests all REST endpoints for role-play conversation operations.
 * 
 * <p><b>KNOWN LIMITATION:</b> Tests for endpoints using {@code @AuthenticationPrincipal User user}
 * fail with 400 Bad Request because Spring Security's argument resolver doesn't work properly
 * with {@code @WebMvcTest} and {@code addFilters=false}. The controller receives null for the user
 * parameter.</p>
 * 
 * <p>Tests that pass (10/38):
 * <ul>
 *   <li>generateScenario_* - These endpoints don't use @AuthenticationPrincipal</li>
 *   <li>startConversation_Success - Works when mock returns expected value</li>
 *   <li>Some validation tests (400 responses)</li>
 * </ul>
 * </p>
 * 
 * <p><b>To fully test this controller:</b> Use integration tests with {@code @SpringBootTest}
 * or migrate to MockMvc with security filters enabled and proper JWT token setup.</p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@WebMvcTest(RolePlayController.class)
@ContextConfiguration(classes = { RolePlayController.class, GlobalExceptionHandler.class })
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class RolePlayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolePlayService rolePlayService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private UUID testUserId;
    private UUID testScenarioId;
    private UUID testConversationId;
    private RolePlayScenarioDTO testScenario;
    private RolePlayConversationDTO testConversation;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testScenarioId = UUID.randomUUID();
        testConversationId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");

        // Set up test scenario DTO
        testScenario = RolePlayScenarioDTO.builder()
                .id(testScenarioId)
                .title("Project Status Meeting")
                .context("You are in a weekly team meeting to discuss project progress.")
                .yourRole("Team Member")
                .aiRole("Project Manager (Sarah)")
                .cefrLevel("B1")
                .domain("meetings")
                .industry("technology")
                .objectives(List.of("Practice reporting progress", "Ask clarifying questions"))
                .keyVocabulary(List.of(
                        RolePlayVocabularyItemDTO.builder()
                                .term("deadline")
                                .definition("the time by which something must be finished")
                                .example("The deadline is next Friday.")
                                .build()
                ))
                .openingLine("Good morning everyone! Let's start with project updates.")
                .suggestedDuration(10)
                .isFallback(false)
                .createdAt(Instant.now())
                .build();

        // Set up test conversation DTO
        testConversation = RolePlayConversationDTO.builder()
                .id(testConversationId)
                .userId(testUserId)
                .scenarioId(testScenarioId)
                .messages(List.of(
                        RolePlayMessageDTO.builder()
                                .role("ai")
                                .content("Good morning everyone! Let's start with project updates.")
                                .timestamp(Instant.now())
                                .build()
                ))
                .status("in_progress")
                .mode("immersive")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Set up Spring Security context
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ========== Scenario Endpoint Tests ==========

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/scenarios")
    class GenerateScenarioTests {

        @Test
        @DisplayName("Should generate scenario successfully")
        void generateScenario_Success() throws Exception {
            RolePlayRequestDTO request = RolePlayRequestDTO.builder()
                    .cefrLevel("B1")
                    .domain("meetings")
                    .industry("technology")
                    .build();

            when(rolePlayService.generateScenario(any(RolePlayRequestDTO.class), any(UUID.class)))
                    .thenReturn(testScenario);

            mockMvc.perform(post("/api/v1/ai/roleplay/scenarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testScenarioId.toString()))
                    .andExpect(jsonPath("$.title").value("Project Status Meeting"))
                    .andExpect(jsonPath("$.cefrLevel").value("B1"))
                    .andExpect(jsonPath("$.domain").value("meetings"))
                    .andExpect(jsonPath("$.isFallback").value(false));

            verify(rolePlayService).generateScenario(any(RolePlayRequestDTO.class), any(UUID.class));
        }

        @Test
        @DisplayName("Should return 400 when CEFR level is missing")
        void generateScenario_MissingCefrLevel() throws Exception {
            String invalidRequest = "{\"domain\": \"meetings\"}";

            mockMvc.perform(post("/api/v1/ai/roleplay/scenarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/roleplay/scenarios")
    class GetAllScenariosTests {

        @Test
        @DisplayName("Should return paginated scenarios")
        void getAllScenarios_Success() throws Exception {
            Page<RolePlayScenarioDTO> scenarioPage = new PageImpl<>(
                    List.of(testScenario), 
                    Pageable.unpaged(), 
                    1
            );

            when(rolePlayService.getAllScenarios(any(Pageable.class)))
                    .thenReturn(scenarioPage);

            mockMvc.perform(get("/api/v1/ai/roleplay/scenarios")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(testScenarioId.toString()))
                    .andExpect(jsonPath("$.content[0].title").value("Project Status Meeting"));

            verify(rolePlayService).getAllScenarios(any(Pageable.class));
        }

        @Test
        @DisplayName("Should respect max page size limit")
        void getAllScenarios_MaxPageSize() throws Exception {
            Page<RolePlayScenarioDTO> emptyPage = new PageImpl<>(
                    Collections.emptyList(), 
                    Pageable.unpaged(), 
                    0
            );

            when(rolePlayService.getAllScenarios(any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/ai/roleplay/scenarios")
                            .param("size", "200")) // Over max (100)
                    .andExpect(status().isOk());

            // Verify the service was called (page size capped internally)
            verify(rolePlayService).getAllScenarios(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/roleplay/scenarios/{scenarioId}")
    class GetScenarioTests {

        @Test
        @DisplayName("Should return scenario by ID")
        void getScenario_Success() throws Exception {
            when(rolePlayService.getScenario(testScenarioId))
                    .thenReturn(testScenario);

            mockMvc.perform(get("/api/v1/ai/roleplay/scenarios/{scenarioId}", testScenarioId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testScenarioId.toString()))
                    .andExpect(jsonPath("$.title").value("Project Status Meeting"));

            verify(rolePlayService).getScenario(testScenarioId);
        }

        @Test
        @DisplayName("Should return 404 when scenario not found")
        void getScenario_NotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(rolePlayService.getScenario(unknownId))
                    .thenThrow(new ResourceNotFoundException("RolePlayScenario", unknownId));

            mockMvc.perform(get("/api/v1/ai/roleplay/scenarios/{scenarioId}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== Conversation Endpoint Tests ==========

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/conversations")
    class StartConversationTests {

        @Test
        @DisplayName("Should start conversation successfully")
        void startConversation_Success() throws Exception {
            RolePlayStartConversationDTO request = RolePlayStartConversationDTO.builder()
                    .scenarioId(testScenarioId)
                    .mode("immersive")
                    .build();

            when(rolePlayService.startConversation(eq(testScenarioId), eq(testUserId), eq("immersive")))
                    .thenReturn(testConversation);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testConversationId.toString()))
                    .andExpect(jsonPath("$.status").value("in_progress"))
                    .andExpect(jsonPath("$.mode").value("immersive"))
                    .andExpect(jsonPath("$.messages", hasSize(1)));

            verify(rolePlayService).startConversation(testScenarioId, testUserId, "immersive");
        }

        @Test
        @DisplayName("Should start conversation in learning mode")
        void startConversation_LearningMode() throws Exception {
            RolePlayStartConversationDTO request = RolePlayStartConversationDTO.builder()
                    .scenarioId(testScenarioId)
                    .mode("learning")
                    .build();

            RolePlayConversationDTO learningConv = RolePlayConversationDTO.builder()
                    .id(testConversationId)
                    .userId(testUserId)
                    .scenarioId(testScenarioId)
                    .status("in_progress")
                    .mode("learning")
                    .messages(List.of())
                    .build();

            when(rolePlayService.startConversation(eq(testScenarioId), eq(testUserId), eq("learning")))
                    .thenReturn(learningConv);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mode").value("learning"));
        }

        @Test
        @DisplayName("Should return 404 when scenario not found")
        void startConversation_ScenarioNotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            RolePlayStartConversationDTO request = RolePlayStartConversationDTO.builder()
                    .scenarioId(unknownId)
                    .mode("immersive")
                    .build();

            when(rolePlayService.startConversation(eq(unknownId), eq(testUserId), anyString()))
                    .thenThrow(new ResourceNotFoundException("RolePlayScenario", unknownId));

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/roleplay/conversations")
    class GetUserConversationsTests {

        @Test
        @DisplayName("Should return user's conversations")
        void getUserConversations_Success() throws Exception {
            Page<RolePlayConversationDTO> convPage = new PageImpl<>(
                    List.of(testConversation), 
                    Pageable.unpaged(), 
                    1
            );

            when(rolePlayService.getUserConversations(eq(testUserId), any(Pageable.class), isNull()))
                    .thenReturn(convPage);

            mockMvc.perform(get("/api/v1/ai/roleplay/conversations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(testConversationId.toString()));

            verify(rolePlayService).getUserConversations(eq(testUserId), any(Pageable.class), isNull());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/roleplay/conversations/{conversationId}")
    class GetConversationTests {

        @Test
        @DisplayName("Should return conversation by ID")
        void getConversation_Success() throws Exception {
            when(rolePlayService.getConversation(testConversationId, testUserId))
                    .thenReturn(testConversation);

            mockMvc.perform(get("/api/v1/ai/roleplay/conversations/{conversationId}", testConversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testConversationId.toString()))
                    .andExpect(jsonPath("$.status").value("in_progress"));

            verify(rolePlayService).getConversation(testConversationId, testUserId);
        }

        @Test
        @DisplayName("Should return 404 when conversation not found")
        void getConversation_NotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(rolePlayService.getConversation(unknownId, testUserId))
                    .thenThrow(new ResourceNotFoundException("RolePlayConversation", unknownId));

            mockMvc.perform(get("/api/v1/ai/roleplay/conversations/{conversationId}", unknownId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't own conversation")
        void getConversation_Forbidden() throws Exception {
            when(rolePlayService.getConversation(testConversationId, testUserId))
                    .thenThrow(new AccessDeniedException("User does not own this conversation"));

            mockMvc.perform(get("/api/v1/ai/roleplay/conversations/{conversationId}", testConversationId))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== Message Endpoint Tests ==========

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/conversations/{id}/messages/immersive")
    class SendImmersiveMessageTests {

        @Test
        @DisplayName("Should send immersive message successfully")
        void sendImmersiveMessage_Success() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("Good morning! I'd like to report on the project status.")
                    .build();

            RolePlayMessageDTO response = RolePlayMessageDTO.builder()
                    .role("ai")
                    .content("Great! Please go ahead and share your update.")
                    .timestamp(Instant.now())
                    .build();

            when(rolePlayService.sendImmersiveMessage(eq(testConversationId), eq(testUserId), anyString()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/immersive", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ai"))
                    .andExpect(jsonPath("$.content").value("Great! Please go ahead and share your update."));

            verify(rolePlayService).sendImmersiveMessage(eq(testConversationId), eq(testUserId), anyString());
        }

        @Test
        @DisplayName("Should return 400 when message is blank")
        void sendImmersiveMessage_BlankContent() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("")
                    .build();

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/immersive", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when message exceeds 500 characters")
        void sendImmersiveMessage_TooLong() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("a".repeat(501))
                    .build();

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/immersive", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when conversation not found")
        void sendImmersiveMessage_ConversationNotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("Hello")
                    .build();

            when(rolePlayService.sendImmersiveMessage(eq(unknownId), eq(testUserId), anyString()))
                    .thenThrow(new ResourceNotFoundException("RolePlayConversation", unknownId));

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/immersive", unknownId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't own conversation")
        void sendImmersiveMessage_Forbidden() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("Hello")
                    .build();

            when(rolePlayService.sendImmersiveMessage(eq(testConversationId), eq(testUserId), anyString()))
                    .thenThrow(new AccessDeniedException("User does not own this conversation"));

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/immersive", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/conversations/{id}/messages/learning")
    class SendLearningMessageTests {

        @Test
        @DisplayName("Should send learning message with feedback")
        void sendLearningMessage_Success() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("I finish the report yesterday.")
                    .build();

            RolePlayMessageDTO response = RolePlayMessageDTO.builder()
                    .role("ai")
                    .content("Thank you for the update! Can you tell me more about the findings?")
                    .timestamp(Instant.now())
                    .feedback(Map.of(
                            "grammarFeedback", "Use 'finished' instead of 'finish' for past tense.",
                            "vocabularyFeedback", "Great use of 'report'. You could also say 'analysis'.",
                            "fluencyScore", 78,
                            "suggestions", List.of("Practice past tense verbs")
                    ))
                    .build();

            when(rolePlayService.sendLearningMessage(eq(testConversationId), eq(testUserId), anyString()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/learning", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ai"))
                    .andExpect(jsonPath("$.feedback.grammarFeedback").exists())
                    .andExpect(jsonPath("$.feedback.fluencyScore").value(78));

            verify(rolePlayService).sendLearningMessage(eq(testConversationId), eq(testUserId), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/conversations/{id}/messages/stream")
    class StreamMessageTests {

        @Test
        @DisplayName("Should return SSE emitter")
        void streamMessage_Success() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("Tell me about the project.")
                    .build();

            SseEmitter emitter = new SseEmitter(30000L);

            when(rolePlayService.streamMessage(eq(testConversationId), eq(testUserId), anyString()))
                    .thenReturn(emitter);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/stream", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(rolePlayService).streamMessage(eq(testConversationId), eq(testUserId), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/roleplay/conversations/{id}/messages/fallback")
    class SendFallbackMessageTests {

        @Test
        @DisplayName("Should send fallback message successfully")
        void sendFallbackMessage_Success() throws Exception {
            RolePlaySendMessageDTO request = RolePlaySendMessageDTO.builder()
                    .content("What's the next step?")
                    .build();

            RolePlayMessageDTO response = RolePlayMessageDTO.builder()
                    .role("ai")
                    .content("Let's discuss that in our next meeting.")
                    .timestamp(Instant.now())
                    .build();

            when(rolePlayService.sendFallbackMessage(eq(testConversationId), eq(testUserId), anyString()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/ai/roleplay/conversations/{id}/messages/fallback", testConversationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ai"));

            verify(rolePlayService).sendFallbackMessage(eq(testConversationId), eq(testUserId), anyString());
        }
    }

    // ========== Conversation Lifecycle Tests ==========

    @Nested
    @DisplayName("PATCH /api/v1/ai/roleplay/conversations/{id}/complete")
    class CompleteConversationTests {

        @Test
        @DisplayName("Should complete conversation successfully")
        void completeConversation_Success() throws Exception {
            RolePlayConversationDTO completedConv = RolePlayConversationDTO.builder()
                    .id(testConversationId)
                    .userId(testUserId)
                    .status("completed")
                    .mode("learning")
                    .metrics(Map.of(
                            "messageCount", 12,
                            "userWordCount", 150,
                            "aiWordCount", 200,
                            "durationMinutes", 8L
                    ))
                    .feedbackSummary(Map.of(
                            "grammarPoints", List.of("Use present perfect for recent actions"),
                            "averageFluencyScore", 85
                    ))
                    .build();

            when(rolePlayService.completeConversation(testConversationId, testUserId))
                    .thenReturn(completedConv);

            mockMvc.perform(patch("/api/v1/ai/roleplay/conversations/{id}/complete", testConversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("completed"))
                    .andExpect(jsonPath("$.metrics.messageCount").value(12))
                    .andExpect(jsonPath("$.feedbackSummary.averageFluencyScore").value(85));

            verify(rolePlayService).completeConversation(testConversationId, testUserId);
        }

        @Test
        @DisplayName("Should return 404 when conversation not found")
        void completeConversation_NotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();

            when(rolePlayService.completeConversation(unknownId, testUserId))
                    .thenThrow(new ResourceNotFoundException("RolePlayConversation", unknownId));

            mockMvc.perform(patch("/api/v1/ai/roleplay/conversations/{id}/complete", unknownId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't own conversation")
        void completeConversation_Forbidden() throws Exception {
            when(rolePlayService.completeConversation(testConversationId, testUserId))
                    .thenThrow(new AccessDeniedException("User does not own this conversation"));

            mockMvc.perform(patch("/api/v1/ai/roleplay/conversations/{id}/complete", testConversationId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when conversation already completed")
        void completeConversation_AlreadyCompleted() throws Exception {
            when(rolePlayService.completeConversation(testConversationId, testUserId))
                    .thenThrow(new IllegalStateException("Conversation is already completed"));

            mockMvc.perform(patch("/api/v1/ai/roleplay/conversations/{id}/complete", testConversationId))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== Error Handling Tests ==========

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle internal server error")
        void handleInternalServerError() throws Exception {
            when(rolePlayService.getScenario(testScenarioId))
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(get("/api/v1/ai/roleplay/scenarios/{scenarioId}", testScenarioId))
                    .andExpect(status().isInternalServerError());
        }
    }
}
