package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.RolePlayConversationDTO;
import com.lexia.backend.dto.ai.RolePlayMessageDTO;
import com.lexia.backend.dto.ai.RolePlayRequestDTO;
import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for AI Role-Play operations.
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
     * Retrieves a conversation by ID.
     *
     * @param conversationId the conversation ID
     * @return the conversation DTO
     */
    RolePlayConversationDTO getConversation(UUID conversationId);

    /**
     * Lists conversations for a user.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of conversations
     */
    Page<RolePlayConversationDTO> getUserConversations(UUID userId, Pageable pageable);
}
