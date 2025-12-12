package com.lexia.backend.dto.ai;

import java.time.Instant;

/**
 * DTO for Gemini API response containing generated content and metadata.
 */
public record GeminiResponseDTO(
    /**
     * The generated text content
     */
    String content,
    
    /**
     * Model used for generation (e.g., "gemini-2.0-flash-exp")
     */
    String model,
    
    /**
     * Token usage information
     */
    TokenUsageDTO tokenUsage,
    
    /**
     * Response generation timestamp
     */
    Instant timestamp,
    
    /**
     * Response time in milliseconds
     */
    long responseTimeMs,
    
    /**
     * Whether this is fallback content (due to API failure)
     */
    boolean isFallback,
    
    /**
     * Finish reason (e.g., "STOP", "MAX_TOKENS", "SAFETY")
     */
    String finishReason
) {
    /**
     * Creates a successful response.
     * 
     * @param content generated content
     * @param model model used
     * @param tokenUsage token usage details
     * @param responseTimeMs response time in ms
     * @param finishReason reason for completion
     * @return GeminiResponseDTO instance
     */
    public static GeminiResponseDTO success(String content, String model, TokenUsageDTO tokenUsage,
                                            long responseTimeMs, String finishReason) {
        return new GeminiResponseDTO(content, model, tokenUsage, Instant.now(), 
                                     responseTimeMs, false, finishReason);
    }

    /**
     * Creates a fallback response.
     * 
     * @param content fallback content
     * @param model intended model
     * @return GeminiResponseDTO instance marked as fallback
     */
    public static GeminiResponseDTO fallback(String content, String model) {
        return new GeminiResponseDTO(content, model, TokenUsageDTO.empty(), 
                                     Instant.now(), 0, true, "FALLBACK");
    }

    /**
     * Creates an empty response (for errors).
     * 
     * @param model intended model
     * @return empty GeminiResponseDTO
     */
    public static GeminiResponseDTO empty(String model) {
        return new GeminiResponseDTO("", model, TokenUsageDTO.empty(), 
                                     Instant.now(), 0, false, "ERROR");
    }
}
