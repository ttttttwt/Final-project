package com.lexia.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for AI prompts.
 * Ensures user input is safe from prompt injection, SQL injection, and XSS attacks.
 * 
 * <p>Usage example:</p>
 * <pre>
 * public class RolePlayRequestDTO {
 *     &#64;ValidPrompt(maxLength = 500)
 *     private String userMessage;
 * }
 * </pre>
 * 
 * @see ValidPromptValidator
 * @see PromptSanitizer
 */
@Documented
@Constraint(validatedBy = ValidPromptValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrompt {

    /**
     * Default error message when validation fails.
     * @return the error message
     */
    String message() default "Invalid prompt: contains disallowed content or exceeds length limit";

    /**
     * Validation groups for conditional validation.
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for extensibility purposes.
     * @return the payload classes
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum allowed length for the prompt.
     * Default is 500 characters (suitable for role-play messages).
     * Use 200 for grammar prompts.
     * 
     * @return maximum character length
     */
    int maxLength() default 500;

    /**
     * Whether to allow empty/blank input.
     * Default is false (empty strings fail validation).
     * 
     * @return true if empty input is allowed
     */
    boolean allowEmpty() default false;
}
