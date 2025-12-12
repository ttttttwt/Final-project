package com.lexia.backend.exception.ai;

/**
 * Exception thrown when AI service configuration is invalid or missing.
 * 
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>API key not configured</li>
 *   <li>Invalid model name</li>
 *   <li>Missing required configuration properties</li>
 * </ul>
 * 
 * <p>This exception is non-retryable - configuration must be fixed before retrying.</p>
 * 
 * @see AiServiceException
 */
public class AiConfigurationException extends AiServiceException {

    /**
     * Creates an AI configuration exception.
     * 
     * @param message the error message describing the configuration issue
     */
    public AiConfigurationException(String message) {
        super(message, "AI_CONFIG_ERROR", false);
    }

    /**
     * Creates an AI configuration exception with cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public AiConfigurationException(String message, Throwable cause) {
        super(message, cause, "AI_CONFIG_ERROR", false);
    }
}
