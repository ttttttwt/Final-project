package com.lexia.backend.exception;

/**
 * Exception thrown when a course is not found.
 * 
 * <p>
 * This exception is typically thrown when trying to retrieve, update, or delete
 * a course that doesn't exist in the database.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class CourseNotFoundException extends RuntimeException {

    /**
     * Constructs a new course not found exception with the specified detail
     * message.
     * 
     * @param message the detail message describing which course was not found
     */
    public CourseNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new course not found exception with the specified detail message
     * and cause.
     * 
     * @param message the detail message describing which course was not found
     * @param cause   the cause of the exception
     */
    public CourseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
