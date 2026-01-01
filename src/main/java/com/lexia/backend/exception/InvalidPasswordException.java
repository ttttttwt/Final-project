package com.lexia.backend.exception;

/**
 * Exception thrown when password validation fails.
 * This includes incorrect current password or password policy violations.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
