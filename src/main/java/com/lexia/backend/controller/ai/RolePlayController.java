package com.lexia.backend.controller.ai;

import com.lexia.backend.annotation.QuotaCheck;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ai.RolePlayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * REST Controller for AI-Powered Role-Play Conversation Operations.
 * Provides endpoints for scenario generation, conversation management,
 * and message exchange in both immersive and learning modes.
 * 
 * <p>
 * Base path: /api/v1/ai/roleplay
 * </p>
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>AI-generated role-play scenarios via Google Gemini</li>
 * <li>Two conversation modes:
 * <ul>
 * <li><b>Immersive</b>: Fast chat-only for fluency practice (&lt;1s
 * response)</li>
 * <li><b>Learning</b>: Chat with grammar/vocabulary feedback (&lt;3s
 * response)</li>
 * </ul>
 * </li>
 * <li>SSE streaming for real-time AI responses</li>
 * <li>Fallback content when AI is unavailable</li>
 * <li>Conversation metrics and completion tracking</li>
 * </ul>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require JWT authentication</li>
 * <li>Users can only access their own conversations (IDOR protection)</li>
 * <li>AI operations are rate-limited per user (20 roleplay/day)</li>
 * <li>Input validation (max 500 characters per message)</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/ai/roleplay")
@Tag(name = "Role-Play API", description = "AI-powered conversation practice for English learners")
@SecurityRequirement(name = "bearerAuth")
public class RolePlayController {

        private static final Logger LOG = LoggerFactory.getLogger(RolePlayController.class);

        private static final int MAX_PAGE_SIZE = 100;

        private final RolePlayService rolePlayService;

        public RolePlayController(RolePlayService rolePlayService) {
                this.rolePlayService = rolePlayService;
        }

        // ========== Scenario Endpoints ==========

        /**
         * Generate a new role-play scenario using AI.
         * 
         * <p>
         * Uses Google Gemini AI to generate a unique scenario based on CEFR level,
         * domain, and optional industry. Falls back to pre-seeded content if AI fails.
         * </p>
         * 
         * @param user    the authenticated user
         * @param request the scenario generation request
         * @return 201 Created with generated scenario
         */
        @PostMapping(value = "/scenarios", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Generate role-play scenario", description = "Uses AI to generate a unique role-play scenario based on CEFR level, domain, and industry. "
                        +
                        "Includes context, roles, objectives, and key vocabulary. " +
                        "Falls back to pre-seeded content if AI is unavailable.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Scenario generated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePlayScenarioDTO.class), examples = @ExampleObject(name = "Generated Scenario", value = """
                                        {
                                          "id": "550e8400-e29b-41d4-a716-446655440000",
                                          "title": "Project Status Meeting",
                                          "context": "You are in a weekly team meeting to discuss project progress...",
                                          "yourRole": "Team Member",
                                          "aiRole": "Project Manager (Sarah)",
                                          "cefrLevel": "B1",
                                          "domain": "meetings",
                                          "industry": "technology",
                                          "objectives": ["Practice reporting progress", "Ask clarifying questions"],
                                          "keyVocabulary": [{"term": "deadline", "definition": "the time by which something must be finished", "ipa": "/ˈdedlaɪn/"}],
                                          "openingLine": "Good morning everyone! Let's start with project updates.",
                                          "suggestedDuration": 10,
                                          "isFallback": false,
                                          "createdAt": "2025-12-13T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid request - validation failed"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                        @ApiResponse(responseCode = "429", description = "Rate limit exceeded - daily roleplay quota reached (20/day)")
        })
        public ResponseEntity<RolePlayScenarioDTO> generateScenario(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody RolePlayRequestDTO request) {

                LOG.info("User {} generating roleplay scenario for level {} domain {}",
                                user.getEmail(), request.getCefrLevel(), request.getDomain());

                RolePlayScenarioDTO scenario = rolePlayService.generateScenario(request);

                LOG.info("User {} generated scenario '{}' (fallback: {})",
                                user.getEmail(), scenario.getTitle(), scenario.getIsFallback());

                return ResponseEntity.status(HttpStatus.CREATED).body(scenario);
        }

        /**
         * List all available scenarios with pagination.
         * 
         * @param page page number (0-based)
         * @param size page size (default 20, max 100)
         * @return 200 OK with paginated scenario list
         */
        @GetMapping(value = "/scenarios", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "List role-play scenarios", description = "Returns all available role-play scenarios with pagination. "
                        +
                        "Includes both AI-generated and pre-seeded fallback scenarios.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Scenarios retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Page<RolePlayScenarioDTO>> getAllScenarios(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {

                LOG.debug("Fetching scenarios (page={}, size={})", page, size);

                size = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                Page<RolePlayScenarioDTO> scenarios = rolePlayService.getAllScenarios(pageable);

                return ResponseEntity.ok(scenarios);
        }

        /**
         * Get a specific scenario by ID.
         * 
         * @param scenarioId the scenario UUID
         * @return 200 OK with scenario details
         */
        @GetMapping(value = "/scenarios/{scenarioId}", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get scenario details", description = "Retrieves a specific role-play scenario by its ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Scenario retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Scenario not found")
        })
        public ResponseEntity<RolePlayScenarioDTO> getScenario(
                        @Parameter(description = "Scenario UUID") @PathVariable UUID scenarioId) {

                LOG.debug("Fetching scenario {}", scenarioId);

                RolePlayScenarioDTO scenario = rolePlayService.getScenario(scenarioId);

                return ResponseEntity.ok(scenario);
        }

        // ========== Conversation Endpoints ==========

        /**
         * Start a new conversation for a scenario.
         * 
         * @param user    the authenticated user
         * @param request the conversation start request
         * @return 201 Created with initialized conversation (includes AI opening line)
         */
        @PostMapping(value = "/conversations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Start new conversation", description = "Initiates a new role-play conversation for the specified scenario. "
                        +
                        "Returns the conversation with the AI's opening line. " +
                        "Choose 'immersive' mode for fast practice or 'learning' mode for feedback.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Conversation started successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePlayConversationDTO.class), examples = @ExampleObject(name = "New Conversation", value = """
                                        {
                                          "id": "660e8400-e29b-41d4-a716-446655440001",
                                          "userId": "123e4567-e89b-12d3-a456-426614174000",
                                          "scenarioId": "550e8400-e29b-41d4-a716-446655440000",
                                          "messages": [
                                            {
                                              "role": "ai",
                                              "content": "Good morning everyone! Let's start with project updates.",
                                              "timestamp": "2025-12-13T10:30:00Z"
                                            }
                                          ],
                                          "status": "in_progress",
                                          "mode": "immersive",
                                          "createdAt": "2025-12-13T10:30:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Scenario not found")
        })
        @QuotaCheck(contentType = "roleplay", incrementSession = true, sessionType = "roleplay")
        public ResponseEntity<RolePlayConversationDTO> startConversation(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody RolePlayStartConversationDTO request) {

                LOG.info("User {} starting conversation for scenario {} in {} mode",
                                user.getEmail(), request.getScenarioId(), request.getMode());

                String mode = request.getMode() != null ? request.getMode() : "immersive";
                RolePlayConversationDTO conversation = rolePlayService.startConversation(
                                request.getScenarioId(), user.getId(), mode);

                LOG.info("User {} started conversation {} with {} initial messages",
                                user.getEmail(), conversation.getId(),
                                conversation.getMessages() != null ? conversation.getMessages().size() : 0);

                return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        }

        /**
         * List all conversations for the authenticated user.
         * 
         * @param user   the authenticated user
         * @param page   page number (0-based)
         * @param size   page size (default 20, max 100)
         * @param status optional status filter
         * @return 200 OK with paginated conversation list
         */
        @GetMapping(value = "/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "List user's conversations", description = "Retrieves all role-play conversations for the authenticated user with pagination.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Page<RolePlayConversationDTO>> getUserConversations(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Filter by status (in_progress, completed, abandoned)") @RequestParam(required = false) String status) {

                LOG.debug("User {} fetching conversations (page={}, size={}, status={})",
                                user.getEmail(), page, size, status);

                size = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

                Page<RolePlayConversationDTO> conversations = rolePlayService.getUserConversations(user.getId(),
                                pageable,
                                status);

                return ResponseEntity.ok(conversations);
        }

        /**
         * Get a specific conversation by ID.
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @return 200 OK with conversation details
         */
        @GetMapping(value = "/conversations/{conversationId}", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get conversation details", description = "Retrieves a specific conversation with full message history.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Conversation retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found")
        })
        public ResponseEntity<RolePlayConversationDTO> getConversation(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId) {

                LOG.debug("User {} fetching conversation {}", user.getEmail(), conversationId);

                RolePlayConversationDTO conversation = rolePlayService.getConversation(conversationId, user.getId());

                return ResponseEntity.ok(conversation);
        }

        // ========== Message Endpoints ==========

        /**
         * Send a message in immersive mode (fast, chat-only).
         * 
         * <p>
         * Optimized for fluency practice with target latency &lt;1s (p95).
         * Returns AI response without grammar/vocabulary feedback.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @param request        the message content
         * @return 200 OK with AI response
         */
        @PostMapping(value = "/conversations/{conversationId}/messages/immersive", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Send immersive message", description = "Sends a message in immersive mode for fast conversation practice. "
                        +
                        "AI responds quickly without detailed feedback. " +
                        "Best for fluency practice and natural conversation flow.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "AI response received", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePlayMessageDTO.class), examples = @ExampleObject(name = "Immersive Response", value = """
                                        {
                                          "role": "ai",
                                          "content": "That's great progress! Can you tell me about the timeline for the next milestone?",
                                          "timestamp": "2025-12-13T10:31:00Z"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid message content"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found"),
                        @ApiResponse(responseCode = "400", description = "Bad request - conversation not in progress"),
                        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
        })
        @QuotaCheck(contentType = "roleplay")
        public ResponseEntity<RolePlayMessageDTO> sendImmersiveMessage(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId,
                        @Valid @RequestBody RolePlaySendMessageDTO request) {

                LOG.debug("User {} sending immersive message to conversation {}", user.getEmail(), conversationId);

                RolePlayMessageDTO response = rolePlayService.sendImmersiveMessage(
                                conversationId, user.getId(), request.getContent());

                return ResponseEntity.ok(response);
        }

        /**
         * Send a message in learning mode (with feedback).
         * 
         * <p>
         * Provides AI response with grammar, vocabulary, and fluency feedback.
         * Target latency &lt;3s (p95). Recommended for A1-B1 learners.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @param request        the message content
         * @return 200 OK with AI response and feedback
         */
        @PostMapping(value = "/conversations/{conversationId}/messages/learning", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Send learning message", description = "Sends a message in learning mode with detailed feedback. "
                        +
                        "AI provides grammar corrections, vocabulary suggestions, and fluency scores. " +
                        "Recommended for error correction and language improvement.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "AI response with feedback received", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePlayMessageDTO.class), examples = @ExampleObject(name = "Learning Response", value = """
                                        {
                                          "role": "ai",
                                          "content": "That's great progress! Can you tell me about the timeline?",
                                          "timestamp": "2025-12-13T10:31:00Z",
                                          "feedback": {
                                            "grammarFeedback": "Good sentence structure! Consider using 'We have completed' instead of 'We completed'.",
                                            "vocabularyFeedback": "Great use of 'milestone'. You could also say 'deliverable' or 'target'.",
                                            "fluencyScore": 82,
                                            "suggestions": ["Try using more complex sentence structures", "Add transition words"]
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid message content or conversation not in progress"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found"),
                        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
        })
        @QuotaCheck(contentType = "roleplay")
        public ResponseEntity<RolePlayMessageDTO> sendLearningMessage(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId,
                        @Valid @RequestBody RolePlaySendMessageDTO request) {

                LOG.debug("User {} sending learning message to conversation {}", user.getEmail(), conversationId);

                RolePlayMessageDTO response = rolePlayService.sendLearningMessage(
                                conversationId, user.getId(), request.getContent());

                return ResponseEntity.ok(response);
        }

        /**
         * Stream AI response via Server-Sent Events.
         * 
         * <p>
         * Provides real-time token-by-token streaming for responsive UI.
         * Target first token latency &lt;500ms.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @param request        the message content
         * @return SSE emitter for streaming response
         */
        @PostMapping(value = "/conversations/{conversationId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Stream AI response", description = "Sends a message and streams the AI response in real-time via Server-Sent Events. "
                        +
                        "Events: 'token' (partial response), 'complete' (finished), 'error' (failed).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "SSE stream started", content = @Content(mediaType = "text/event-stream")),
                        @ApiResponse(responseCode = "400", description = "Invalid message content"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found")
        })
        public SseEmitter streamMessage(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId,
                        @Valid @RequestBody RolePlaySendMessageDTO request) {

                LOG.debug("User {} starting stream for conversation {}", user.getEmail(), conversationId);

                return rolePlayService.streamMessage(conversationId, user.getId(), request.getContent());
        }

        /**
         * Send a message with fallback support (non-streaming).
         * 
         * <p>
         * Uses synchronous AI call with automatic fallback to pre-seeded responses
         * if AI fails. Suitable for offline-ready scenarios.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @param request        the message content
         * @return 200 OK with AI response (may be fallback)
         */
        @PostMapping(value = "/conversations/{conversationId}/messages/fallback", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Send message with fallback", description = "Sends a message with automatic fallback to pre-generated responses if AI fails. "
                        +
                        "Ensures conversation can continue even when AI service is unavailable.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "AI response received (may be fallback)"),
                        @ApiResponse(responseCode = "400", description = "Invalid message content"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found")
        })
        public ResponseEntity<RolePlayMessageDTO> sendFallbackMessage(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId,
                        @Valid @RequestBody RolePlaySendMessageDTO request) {

                LOG.debug("User {} sending fallback message to conversation {}", user.getEmail(), conversationId);

                RolePlayMessageDTO response = rolePlayService.sendFallbackMessage(
                                conversationId, user.getId(), request.getContent());

                return ResponseEntity.ok(response);
        }

        // ========== Conversation Lifecycle ==========

        /**
         * Mark a conversation as complete.
         * 
         * <p>
         * Calculates metrics (message count, word counts, duration) and
         * generates feedback summary for learning mode conversations.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @return 200 OK with completed conversation and metrics
         */
        @PatchMapping(value = "/conversations/{conversationId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Complete conversation", description = "Marks a conversation as complete and calculates final metrics. "
                        +
                        "For learning mode, generates a summary of all feedback received.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Conversation completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolePlayConversationDTO.class), examples = @ExampleObject(name = "Completed Conversation", value = """
                                        {
                                          "id": "660e8400-e29b-41d4-a716-446655440001",
                                          "status": "completed",
                                          "mode": "learning",
                                          "metrics": {
                                            "messageCount": 12,
                                            "userWordCount": 150,
                                            "aiWordCount": 200,
                                            "durationMinutes": 8
                                          },
                                          "feedbackSummary": {
                                            "grammarPoints": ["Use present perfect for recent actions"],
                                            "vocabularyPoints": ["business terminology used correctly"],
                                            "averageFluencyScore": 85
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found"),
                        @ApiResponse(responseCode = "400", description = "Bad request - conversation already completed or abandoned")
        })
        public ResponseEntity<RolePlayConversationDTO> completeConversation(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId) {

                LOG.info("User {} completing conversation {}", user.getEmail(), conversationId);

                RolePlayConversationDTO conversation = rolePlayService.completeConversation(conversationId,
                                user.getId());

                LOG.info("User {} completed conversation {} with {} messages",
                                user.getEmail(), conversationId,
                                conversation.getMetrics() != null ? conversation.getMetrics().get("messageCount") : 0);

                return ResponseEntity.ok(conversation);
        }

        /**
         * Delete a conversation.
         * 
         * <p>
         * Only completed or abandoned conversations can be deleted.
         * In-progress conversations must be completed first.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @return 204 No Content on successful deletion
         */
        @DeleteMapping(value = "/conversations/{conversationId}")
        @Operation(summary = "Delete conversation", description = "Permanently deletes a conversation. Only completed or abandoned "
                        +
                        "conversations can be deleted. In-progress conversations must be " +
                        "completed or abandoned first.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Conversation deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found"),
                        @ApiResponse(responseCode = "400", description = "Bad request - cannot delete in-progress conversation")
        })
        public ResponseEntity<Void> deleteConversation(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId) {

                LOG.info("User {} deleting conversation {}", user.getEmail(), conversationId);

                rolePlayService.deleteConversation(conversationId, user.getId());

                LOG.info("User {} successfully deleted conversation {}", user.getEmail(), conversationId);

                return ResponseEntity.noContent().build();
        }

        // ========== Dynamic Prompts (Feature: Context-aware suggestions) ==========

        /**
         * Generate dynamic suggested prompts based on conversation context.
         * 
         * <p>
         * Analyzes the AI's last response and scenario context to generate
         * contextually relevant prompts for the user to consider.
         * </p>
         * 
         * @param user           the authenticated user
         * @param conversationId the conversation UUID
         * @return 200 OK with list of suggested prompts
         */
        @PostMapping(value = "/conversations/{conversationId}/suggest-prompts", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Generate dynamic prompts", description = "Generates contextually relevant prompts based on the AI's last response "
                        +
                        "and scenario context. Returns 4-5 prompts the user can use as responses.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Prompts generated successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own conversation"),
                        @ApiResponse(responseCode = "404", description = "Conversation not found")
        })
        public ResponseEntity<java.util.List<String>> generateDynamicPrompts(
                        @AuthenticationPrincipal User user,
                        @Parameter(description = "Conversation UUID") @PathVariable UUID conversationId) {

                LOG.debug("User {} requesting dynamic prompts for conversation {}", user.getEmail(), conversationId);

                java.util.List<String> prompts = rolePlayService.generateDynamicPrompts(conversationId, user.getId());

                LOG.info("Generated {} dynamic prompts for conversation {}", prompts.size(), conversationId);

                return ResponseEntity.ok(prompts);
        }
}
