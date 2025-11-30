package com.lexia.backend.file.exception;

/**
 * Exception thrown when file validation fails.
 * This includes invalid MIME type, file too large, empty file, etc.
 */
public class FileValidationException extends RuntimeException {

    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
