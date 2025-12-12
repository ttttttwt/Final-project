package com.lexia.backend.exception.ai;

/**
 * Exception thrown when AI service operations timeout.
 * 
 * <p>Timeouts can occur during:</p>
 * <ul>
 *   <li>Initial API connection</li>
 *   <li>Waiting for response generation</li>
 *   <li>Streaming response transmission</li>
 * </ul>
 * 
 * <p>This exception is retryable - the request can be attempted again.</p>
 * 
 * @see AiServiceException
 */
public class AiTimeoutException extends AiServiceException {

    private final long timeoutMs;
    private final String operation;

    /**
     * Creates an AI timeout exception.
     * 
     * @param message the error message
     */
    public AiTimeoutException(String message) {
        super(message, "AI_TIMEOUT", true);
        this.timeoutMs = 0;
        this.operation = "UNKNOWN";
    }

    /**
     * Creates an AI timeout exception with details.
     * 
     * @param message the error message
     * @param timeoutMs the timeout value in milliseconds
     * @param operation the operation that timed out
     */
    public AiTimeoutException(String message, long timeoutMs, String operation) {
        super(message, "AI_TIMEOUT", true);
        this.timeoutMs = timeoutMs;
        this.operation = operation;
    }

    /**
     * Creates an AI timeout exception with cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param timeoutMs the timeout value in milliseconds
     * @param operation the operation that timed out
     */
    public AiTimeoutException(String message, Throwable cause, long timeoutMs, String operation) {
        super(message, cause, "AI_TIMEOUT", true);
        this.timeoutMs = timeoutMs;
        this.operation = operation;
    }

    /**
     * Gets the timeout value that was exceeded.
     * 
     * @return timeout in milliseconds
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Gets the operation that timed out.
     * 
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }
}
