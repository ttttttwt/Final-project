package com.lexia.backend.exception.ai;

/**
 * Exception thrown when AI service operations fail.
 * This is the base exception for all AI-related errors.
 * 
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Gemini API returns an error response</li>
 *   <li>Request processing fails</li>
 *   <li>Response parsing errors</li>
 * </ul>
 * 
 * @see AiConfigurationException
 * @see AiRateLimitException
 */
public class AiServiceException extends RuntimeException {

    private final String errorCode;
    private final boolean retryable;

    /**
     * Creates an AI service exception with a message.
     * 
     * @param message the error message
     */
    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_ERROR";
        this.retryable = true;
    }

    /**
     * Creates an AI service exception with a message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_ERROR";
        this.retryable = true;
    }

    /**
     * Creates an AI service exception with full details.
     * 
     * @param message the error message
     * @param errorCode specific error code for categorization
     * @param retryable whether the operation can be retried
     */
    public AiServiceException(String message, String errorCode, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Creates an AI service exception with full details and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param errorCode specific error code for categorization
     * @param retryable whether the operation can be retried
     */
    public AiServiceException(String message, Throwable cause, String errorCode, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Checks if this error is retryable.
     * 
     * @return true if the operation can be retried
     */
    public boolean isRetryable() {
        return retryable;
    }
}
