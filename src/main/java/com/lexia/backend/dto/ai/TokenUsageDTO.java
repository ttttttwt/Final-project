package com.lexia.backend.dto.ai;

/**
 * DTO for token usage tracking in AI responses.
 * Used for cost estimation and quota management.
 */
public record TokenUsageDTO(
    /**
     * Number of tokens in the input/prompt
     */
    int inputTokens,
    
    /**
     * Number of tokens in the generated output
     */
    int outputTokens,
    
    /**
     * Total tokens (input + output)
     */
    int totalTokens,
    
    /**
     * Estimated cost in USD (based on model pricing)
     */
    double estimatedCostUsd
) {
    /**
     * Creates TokenUsageDTO with auto-calculated total.
     * 
     * @param inputTokens input token count
     * @param outputTokens output token count
     * @param estimatedCostUsd estimated cost in USD
     * @return TokenUsageDTO instance
     */
    public static TokenUsageDTO of(int inputTokens, int outputTokens, double estimatedCostUsd) {
        return new TokenUsageDTO(inputTokens, outputTokens, inputTokens + outputTokens, estimatedCostUsd);
    }

    /**
     * Creates TokenUsageDTO with zero values.
     * 
     * @return empty TokenUsageDTO
     */
    public static TokenUsageDTO empty() {
        return new TokenUsageDTO(0, 0, 0, 0.0);
    }
}
