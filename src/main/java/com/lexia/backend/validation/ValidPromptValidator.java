package com.lexia.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for {@link ValidPrompt} annotation.
 * Uses {@link PromptSanitizer} to detect malicious content in AI prompts.
 * 
 * <p>Validation rules:</p>
 * <ul>
 *   <li>Checks for prompt injection patterns</li>
 *   <li>Checks for SQL injection patterns</li>
 *   <li>Checks for XSS patterns</li>
 *   <li>Enforces maximum length</li>
 *   <li>Handles empty input based on allowEmpty setting</li>
 * </ul>
 * 
 * @see ValidPrompt
 * @see PromptSanitizer
 */
@Component
public class ValidPromptValidator implements ConstraintValidator<ValidPrompt, String> {

    private final PromptSanitizer promptSanitizer;

    private int maxLength;
    private boolean allowEmpty;

    @Autowired
    public ValidPromptValidator(PromptSanitizer promptSanitizer) {
        this.promptSanitizer = promptSanitizer;
    }

    /**
     * Default constructor for CDI/Hibernate Validator.
     * Uses a new PromptSanitizer instance if not injected.
     */
    public ValidPromptValidator() {
        this.promptSanitizer = new PromptSanitizer();
    }

    @Override
    public void initialize(ValidPrompt constraintAnnotation) {
        this.maxLength = constraintAnnotation.maxLength();
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null/empty cases
        if (value == null || value.isBlank()) {
            return allowEmpty;
        }

        // Get validation error from sanitizer
        String errorMessage = promptSanitizer.getValidationError(value, maxLength);

        if (errorMessage != null) {
            // Disable default constraint violation message
            context.disableDefaultConstraintViolation();
            // Add custom error message from sanitizer
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
