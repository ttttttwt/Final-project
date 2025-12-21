package com.lexia.backend.service.ai;

import com.lexia.backend.dto.custommaterial.ChatMessageResponseDTO;
import com.lexia.backend.dto.custommaterial.EndChatResponseDTO;

import java.util.UUID;

/**
 * Service interface for Custom Material Chat (Role-Play) operations.
 *
 * <p>
 * Manages chat sessions where users practice English conversations
 * based on their custom materials.
 * </p>
 *
 * @since Sprint 5
 */
public interface CustomMaterialChatService {

    /**
     * Sends a message in a chat session.
     *
     * <p>
     * If sessionId is null, creates a new session.
     * Returns AI response based on the role-play scenario.
     * </p>
     *
     * @param materialId the custom material ID
     * @param message    the user's message
     * @param sessionId  existing session ID or null for new session
     * @param userId     the authenticated user ID
     * @return chat response with AI message
     * @throws com.lexia.backend.exception.ResourceNotFoundException if material not
     *                                                               found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     * @throws IllegalStateException                                 if material not
     *                                                               ready
     */
    ChatMessageResponseDTO sendMessage(UUID materialId, String message, UUID sessionId, UUID userId);

    /**
     * Ends a chat session and generates performance report.
     *
     * @param materialId the custom material ID
     * @param sessionId  the session ID to end
     * @param userId     the authenticated user ID
     * @return end chat response with performance report
     * @throws com.lexia.backend.exception.ResourceNotFoundException if session not
     *                                                               found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     */
    EndChatResponseDTO endSession(UUID materialId, UUID sessionId, UUID userId);
}
