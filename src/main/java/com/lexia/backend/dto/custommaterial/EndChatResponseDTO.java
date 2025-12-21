package com.lexia.backend.dto.custommaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for ending a chat session with performance report.
 *
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndChatResponseDTO {

    /**
     * The chat session ID.
     */
    private UUID sessionId;

    /**
     * Overall performance score (0-100).
     */
    private Integer overallScore;

    /**
     * List of grammar errors found.
     * Each error: { original, suggestion, explanation }
     */
    private List<Map<String, String>> grammarErrors;

    /**
     * Vocabulary suggestions for improvement.
     */
    private List<String> vocabularySuggestions;

    /**
     * Things the user did well.
     */
    private List<String> strengths;

    /**
     * Areas for improvement.
     */
    private List<String> improvements;

    /**
     * Total messages in the session.
     */
    private int totalMessages;

    /**
     * Session duration in seconds.
     */
    private long durationSeconds;
}
