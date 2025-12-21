package com.lexia.backend.dto.custommaterial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for sending a chat message in custom material role-play.
 *
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {

    /**
     * Existing session ID to continue, or null to start a new session.
     */
    private UUID sessionId;

    /**
     * The user's message.
     */
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    private String message;
}
