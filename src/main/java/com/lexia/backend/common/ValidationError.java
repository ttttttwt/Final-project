package com.lexia.backend.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for field-specific validation errors.
 * Used in ErrorResponse to provide detailed validation failure information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * The field name that failed validation.
     */
    private String field;

    /**
     * The rejected value that caused the validation failure.
     */
    private Object rejectedValue;

    /**
     * The validation error message.
     */
    private String message;
}