package com.lexia.backend.exception;

/**
 * Exception thrown when a user already exists in the system.
 * Used for duplicate email registration attempts.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}