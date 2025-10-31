package com.lexia.backend.exception;

/**
 * Exception thrown when lesson content validation fails.
 * 
 * <p>
 * This exception is thrown by {@code LessonContentValidator} when JSONB content
 * does not conform to the required schema for a specific lesson type.
 * </p>
 * 
 * <p>
 * The exception message should clearly indicate which validation rule failed
 * to help content creators fix the issue.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see com.lexia.backend.service.LessonContentValidator
 */
public class InvalidLessonContentException extends RuntimeException {

    /**
     * Constructs a new invalid lesson content exception with the specified detail
     * message.
     * 
     * @param message the detail message describing the validation failure
     */
    public InvalidLessonContentException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid lesson content exception with the specified detail
     * message
     * and cause.
     * 
     * @param message the detail message describing the validation failure
     * @param cause   the cause of the exception (e.g., JSON parsing error)
     */
    public InvalidLessonContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
