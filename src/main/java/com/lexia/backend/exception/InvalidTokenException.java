package com.lexia.backend.exception;

/**
 * Exception thrown when a JWT token is invalid, expired, or revoked.
 * Results in HTTP 401 Unauthorized response.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
