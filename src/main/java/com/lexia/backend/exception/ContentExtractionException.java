package com.lexia.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when content extraction from a source fails.
 * 
 * @since Sprint 5
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ContentExtractionException extends RuntimeException {

    private final String sourceType;
    private final String reason;

    public ContentExtractionException(String sourceType, String reason) {
        super(String.format("Failed to extract content from %s: %s", sourceType, reason));
        this.sourceType = sourceType;
        this.reason = reason;
    }

    public ContentExtractionException(String sourceType, String reason, Throwable cause) {
        super(String.format("Failed to extract content from %s: %s", sourceType, reason), cause);
        this.sourceType = sourceType;
        this.reason = reason;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getReason() {
        return reason;
    }
}
