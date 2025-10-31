package com.lexia.backend.exception;

/**
 * Exception thrown when a lesson is not found.
 * 
 * <p>
 * This exception is typically thrown when trying to retrieve, update, or delete
 * a lesson that doesn't exist in the database.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class LessonNotFoundException extends RuntimeException {

    /**
     * Constructs a new lesson not found exception with the specified detail
     * message.
     * 
     * @param message the detail message describing which lesson was not found
     */
    public LessonNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new lesson not found exception with the specified detail message
     * and cause.
     * 
     * @param message the detail message describing which lesson was not found
     * @param cause   the cause of the exception
     */
    public LessonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
