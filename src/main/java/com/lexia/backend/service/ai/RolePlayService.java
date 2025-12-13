package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.RolePlayConversationDTO;
import com.lexia.backend.dto.ai.RolePlayMessageDTO;
import com.lexia.backend.dto.ai.RolePlayRequestDTO;
import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Service interface for AI Role-Play operations.
 * 
 * <p>Provides methods for:</p>
 * <ul>
 *   <li>AI-generated scenario creation</li>
 *   <li>Conversation management with two modes:
 *     <ul>
 *       <li><b>Immersive Mode</b>: Fast chat-only responses for fluency practice</li>
 *       <li><b>Learning Mode</b>: Chat with grammar/vocabulary feedback</li>
 *     </ul>
 *   </li>
 *   <li>SSE streaming for real-time AI responses</li>
 *   <li>Conversation completion and statistics</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
public interface RolePlayService {

    /**
     * Generates a new role-play scenario based on user preferences.
     *
     * @param request the scenario generation request
     * @return the generated scenario
     */
    RolePlayScenarioDTO generateScenario(RolePlayRequestDTO request);

    /**
     * Retrieves a scenario by ID.
     *
     * @param id the scenario ID
     * @return the scenario DTO
     */
    RolePlayScenarioDTO getScenario(UUID id);

    /**
     * Lists all available scenarios (with pagination).
     *
     * @param pageable pagination information
     * @return page of scenarios
     */
    Page<RolePlayScenarioDTO> getAllScenarios(Pageable pageable);

    /**
     * Starts a new conversation for a given scenario.
     *
     * @param scenarioId the scenario ID
     * @param userId the user ID
     * @param mode the conversation mode (immersive or learning)
     * @return the initialized conversation
     */
    RolePlayConversationDTO startConversation(UUID scenarioId, UUID userId, String mode);

    /**
     * Sends a user message to the conversation and gets the AI response.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @param message the user message content
     * @return the AI response message
     */
    RolePlayMessageDTO sendMessage(UUID conversationId, UUID userId, String message);

    /**
     * Retrieves a conversation by ID (ADMIN/INTERNAL USE ONLY).
     * 
     * <p>WARNING: This method does not verify ownership. Use 
     * {@link #getConversation(UUID, UUID)} for user-facing endpoints.
     *
     * @param conversationId the conversation ID
     * @return the conversation DTO
     */
    RolePlayConversationDTO getConversation(UUID conversationId);

    /**
     * Lists conversations for a user with optional status filter.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @param status optional status filter (in_progress, completed, abandoned)
     * @return page of conversations
     */
    Page<RolePlayConversationDTO> getUserConversations(UUID userId, Pageable pageable, String status);

    // ========== Immersive Mode (B5a) ==========

    /**
     * Sends a message in immersive mode (chat-only, fast response).
     * 
     * <p>Immersive mode provides quick AI responses without grammar/vocabulary feedback.
     * Optimized for fluency practice with target latency &lt;1s (p95).</p>
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @param message the user message content (max 500 characters)
     * @return the AI response message (role="ai", no feedback)
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     * @throws IllegalStateException if conversation is not in_progress
     */
    RolePlayMessageDTO sendImmersiveMessage(UUID conversationId, UUID userId, String message);

    // ========== Learning Mode (B5b) ==========

    /**
     * Sends a message in learning mode (chat + feedback).
     * 
     * <p>Learning mode provides AI responses with grammar, vocabulary, and fluency feedback.
     * Target latency &lt;3s (p95). Recommended for A1-B1 users.</p>
     * 
     * <p>Response includes feedback map with:</p>
     * <ul>
     *   <li>grammarFeedback: corrections and suggestions</li>
     *   <li>vocabularyFeedback: alternative expressions</li>
     *   <li>fluencyScore: 1-100 rating</li>
     *   <li>suggestions: improvement tips</li>
     * </ul>
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @param message the user message content (max 500 characters)
     * @return the AI response message with feedback map
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     * @throws IllegalStateException if conversation is not in_progress
     */
    RolePlayMessageDTO sendLearningMessage(UUID conversationId, UUID userId, String message);

    // ========== SSE Streaming (B5c) ==========

    /**
     * Streams AI response via Server-Sent Events.
     * 
     * <p>Provides real-time token-by-token streaming for responsive UI.
     * Target first token latency &lt;500ms, chunk latency &lt;100ms.</p>
     * 
     * <p>SSE Event types:</p>
     * <ul>
     *   <li><code>event: token</code> - data: {"content": "...", "index": n}</li>
     *   <li><code>event: complete</code> - data: {"messageId": "uuid", "totalTokens": n}</li>
     *   <li><code>event: error</code> - data: {"error": "message"}</li>
     * </ul>
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @param message the user message content (max 500 characters)
     * @return SseEmitter for streaming response (30s timeout)
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     * @throws IllegalStateException if conversation is not in_progress
     */
    SseEmitter streamMessage(UUID conversationId, UUID userId, String message);

    // ========== Fallback Mode (B5d) ==========

    /**
     * Sends a message with fallback support (non-streaming, offline-friendly).
     * 
     * <p>Uses synchronous AI call with fallback to pre-seeded responses if AI fails.
     * Suitable for offline-ready scenarios or when SSE is not supported.</p>
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @param message the user message content (max 500 characters)
     * @return the AI response message (may be from fallback content)
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     * @throws IllegalStateException if conversation is not in_progress
     */
    RolePlayMessageDTO sendFallbackMessage(UUID conversationId, UUID userId, String message);

    // ========== Conversation Completion ==========

    /**
     * Marks a conversation as complete and calculates metrics.
     * 
     * <p>Sets status to "completed" and calculates:</p>
     * <ul>
     *   <li>messageCount: total messages exchanged</li>
     *   <li>userWordCount: total words from user</li>
     *   <li>aiWordCount: total words from AI</li>
     *   <li>durationMinutes: conversation duration</li>
     *   <li>averageResponseTimeMs: average AI response time</li>
     * </ul>
     * 
     * <p>For learning mode, also generates feedback summary.</p>
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID (for ownership check)
     * @return the updated conversation with final metrics
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     * @throws IllegalStateException if conversation is already completed or abandoned
     */
    RolePlayConversationDTO completeConversation(UUID conversationId, UUID userId);

    /**
     * Retrieves a conversation by ID with ownership check.
     * 
     * @param conversationId the conversation ID
     * @param userId the user ID for ownership verification
     * @return the conversation DTO
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own conversation
     */
    RolePlayConversationDTO getConversation(UUID conversationId, UUID userId);
}
