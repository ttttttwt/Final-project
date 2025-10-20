package com.lexia.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard error response DTO for API errors.
 * Provides consistent error formatting across all endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type/category.
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * API endpoint path where the error occurred.
     */
    private String path;

    /**
     * Field-specific validation errors (for validation failures).
     */
    private List<ValidationError> validationErrors;

    /**
     * Additional error details/context.
     */
    private Map<String, Object> details;
}