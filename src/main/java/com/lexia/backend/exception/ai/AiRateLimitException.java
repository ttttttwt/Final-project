package com.lexia.backend.exception.ai;

import java.time.Instant;

/**
 * Exception thrown when AI service rate limits are exceeded.
 * 
 * <p>Rate limits can be exceeded at multiple levels:</p>
 * <ul>
 *   <li>User daily/monthly quota</li>
 *   <li>Application-wide rate limits</li>
 *   <li>Gemini API rate limits</li>
 * </ul>
 * 
 * <p>This exception includes retry timing information.</p>
 * 
 * @see AiServiceException
 */
public class AiRateLimitException extends AiServiceException {

    private final Instant retryAfter;
    private final String limitType;
    private final int currentUsage;
    private final int maxAllowed;

    /**
     * Creates an AI rate limit exception with basic message.
     * 
     * @param message the error message
     */
    public AiRateLimitException(String message) {
        super(message, "AI_RATE_LIMIT", true);
        this.retryAfter = null;
        this.limitType = "UNKNOWN";
        this.currentUsage = 0;
        this.maxAllowed = 0;
    }

    /**
     * Creates an AI rate limit exception with retry information.
     * 
     * @param message the error message
     * @param retryAfter when the request can be retried
     */
    public AiRateLimitException(String message, Instant retryAfter) {
        super(message, "AI_RATE_LIMIT", true);
        this.retryAfter = retryAfter;
        this.limitType = "UNKNOWN";
        this.currentUsage = 0;
        this.maxAllowed = 0;
    }

    /**
     * Creates an AI rate limit exception with full quota details.
     * 
     * @param message the error message
     * @param retryAfter when the request can be retried
     * @param limitType type of limit exceeded (DAILY, MONTHLY, API)
     * @param currentUsage current usage count
     * @param maxAllowed maximum allowed count
     */
    public AiRateLimitException(String message, Instant retryAfter, String limitType, 
                                 int currentUsage, int maxAllowed) {
        super(message, "AI_RATE_LIMIT", true);
        this.retryAfter = retryAfter;
        this.limitType = limitType;
        this.currentUsage = currentUsage;
        this.maxAllowed = maxAllowed;
    }

    /**
     * Gets when the request can be retried.
     * 
     * @return the retry timestamp, or null if unknown
     */
    public Instant getRetryAfter() {
        return retryAfter;
    }

    /**
     * Gets the type of rate limit exceeded.
     * 
     * @return the limit type (DAILY, MONTHLY, API, UNKNOWN)
     */
    public String getLimitType() {
        return limitType;
    }

    /**
     * Gets the current usage count.
     * 
     * @return current usage
     */
    public int getCurrentUsage() {
        return currentUsage;
    }

    /**
     * Gets the maximum allowed count.
     * 
     * @return max allowed
     */
    public int getMaxAllowed() {
        return maxAllowed;
    }
}
