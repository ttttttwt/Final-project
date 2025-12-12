package com.lexia.backend.dto.ai;

/**
 * DTO for SSE (Server-Sent Events) stream events.
 * Used for streaming AI responses token-by-token.
 */
public record SseEventDTO(
    /**
     * Event type: "token", "complete", "error"
     */
    String event,
    
    /**
     * The content/data of the event
     */
    String data,
    
    /**
     * Index of this token in the stream (for "token" events)
     */
    int index,
    
    /**
     * Message ID (populated on "complete" event)
     */
    String messageId,
    
    /**
     * Total tokens used (populated on "complete" event)
     */
    Integer totalTokens,
    
    /**
     * Error message (for "error" events)
     */
    String error
) {
    /**
     * Creates a token event.
     * 
     * @param content the token content
     * @param index token index in stream
     * @return SseEventDTO for token event
     */
    public static SseEventDTO token(String content, int index) {
        return new SseEventDTO("token", content, index, null, null, null);
    }

    /**
     * Creates a completion event.
     * 
     * @param messageId the message ID
     * @param totalTokens total tokens used
     * @return SseEventDTO for complete event
     */
    public static SseEventDTO complete(String messageId, int totalTokens) {
        return new SseEventDTO("complete", null, 0, messageId, totalTokens, null);
    }

    /**
     * Creates an error event.
     * 
     * @param errorMessage the error message
     * @return SseEventDTO for error event
     */
    public static SseEventDTO error(String errorMessage) {
        return new SseEventDTO("error", null, 0, null, null, errorMessage);
    }
}
