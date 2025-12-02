package com.lexia.backend.email.exception;

/**
 * Exception thrown when email sending fails.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public class EmailSendException extends RuntimeException {

    private final String errorCode;

    /**
     * Creates an EmailSendException with a message.
     *
     * @param message the error message
     */
    public EmailSendException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * Creates an EmailSendException with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * Creates an EmailSendException with a code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @param cause     the underlying cause
     */
    public EmailSendException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code if available.
     *
     * @return the error code or null
     */
    public String getErrorCode() {
        return errorCode;
    }
}
