package com.lexia.backend.exception;

/**
 * Exception thrown when a section is not found.
 * 
 * <p>
 * This exception is typically thrown when trying to retrieve, update, or delete
 * a section that doesn't exist in the database.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class SectionNotFoundException extends RuntimeException {

    /**
     * Constructs a new section not found exception with the specified detail
     * message.
     * 
     * @param message the detail message describing which section was not found
     */
    public SectionNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new section not found exception with the specified detail
     * message
     * and cause.
     * 
     * @param message the detail message describing which section was not found
     * @param cause   the cause of the exception
     */
    public SectionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
