package com.lexia.backend.dto.custommaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for chat message containing AI response.
 *
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDTO {

    /**
     * The chat session ID.
     */
    private UUID sessionId;

    /**
     * The AI's response message.
     */
    private String aiResponse;

    /**
     * Corrections (only in STRICT mode).
     * Each correction contains: original, suggestion, explanation.
     */
    private List<Map<String, String>> corrections;

    /**
     * Number of messages in the session so far.
     */
    private int messageCount;
}
