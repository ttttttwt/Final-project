package com.lexia.backend.exception;

/**
 * Exception thrown when a learning path is not found.
 * 
 * <p>
 * This exception is typically thrown when trying to retrieve or start
 * a learning path that doesn't exist in the database.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class LearningPathNotFoundException extends RuntimeException {

    /**
     * Constructs a new learning path not found exception with the specified detail
     * message.
     * 
     * @param message the detail message describing which learning path was not found
     */
    public LearningPathNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new learning path not found exception with the specified detail
     * message and cause.
     * 
     * @param message the detail message describing which learning path was not found
     * @param cause   the cause of the exception
     */
    public LearningPathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
