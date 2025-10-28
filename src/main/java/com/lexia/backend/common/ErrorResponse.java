package com.lexia.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Standard error response returned for all API errors with consistent structure")
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    @Builder.Default
    @Schema(description = "Timestamp when the error occurred", example = "2025-10-28T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * HTTP status code.
     */
    @Schema(description = "HTTP status code", example = "400", accessMode = Schema.AccessMode.READ_ONLY)
    private int status;

    /**
     * Error type/category.
     */
    @Schema(description = "Error type or category (e.g., Bad Request, Unauthorized, Not Found)", example = "Bad Request", accessMode = Schema.AccessMode.READ_ONLY)
    private String error;

    /**
     * Human-readable error message.
     */
    @Schema(description = "Human-readable error message describing what went wrong", example = "Validation failed for one or more fields", accessMode = Schema.AccessMode.READ_ONLY)
    private String message;

    /**
     * API endpoint path where the error occurred.
     */
    @Schema(description = "API endpoint path where the error occurred", example = "/api/v1/users/profile", accessMode = Schema.AccessMode.READ_ONLY)
    private String path;

    /**
     * Field-specific validation errors (for validation failures).
     */
    @Schema(description = "List of field-specific validation errors (only present for validation failures)", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private List<ValidationError> validationErrors;

    /**
     * Additional error details/context.
     */
    @Schema(description = "Additional error details or context information", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private Map<String, Object> details;
}