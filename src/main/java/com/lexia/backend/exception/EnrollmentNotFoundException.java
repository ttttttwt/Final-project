package com.lexia.backend.exception;

/**
 * Exception thrown when an enrollment is not found.
 * Results in HTTP 404 Not Found response.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class EnrollmentNotFoundException extends RuntimeException {

    /**
     * Constructs a new EnrollmentNotFoundException with a default message.
     * 
     * @param enrollmentId the enrollment ID that was not found
     */
    public EnrollmentNotFoundException(Long enrollmentId) {
        super("Enrollment not found with id: " + enrollmentId);
    }

    /**
     * Constructs a new EnrollmentNotFoundException with a custom message.
     * 
     * @param message the custom error message
     */
    public EnrollmentNotFoundException(String message) {
        super(message);
    }
}
