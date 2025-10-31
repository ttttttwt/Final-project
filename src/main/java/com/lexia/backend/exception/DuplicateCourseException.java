package com.lexia.backend.exception;

/**
 * Exception thrown when attempting to create a course with a duplicate title.
 * 
 * <p>
 * This exception enforces the business rule that course titles must be unique
 * within the system to avoid confusion and maintain data integrity.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class DuplicateCourseException extends RuntimeException {

    /**
     * Constructs a new duplicate course exception with the specified detail
     * message.
     * 
     * @param message the detail message describing the duplicate course issue
     */
    public DuplicateCourseException(String message) {
        super(message);
    }

    /**
     * Constructs a new duplicate course exception with the specified detail message
     * and cause.
     * 
     * @param message the detail message describing the duplicate course issue
     * @param cause   the cause of the exception
     */
    public DuplicateCourseException(String message, Throwable cause) {
        super(message, cause);
    }
}
