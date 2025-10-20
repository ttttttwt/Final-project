package com.lexia.backend.validation;

import com.lexia.backend.dto.RegisterDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for password confirmation validation.
 * Ensures that the password and confirmPassword fields match.
 */
public class PasswordConfirmationValidator implements ConstraintValidator<PasswordConfirmation, RegisterDTO> {

    @Override
    public void initialize(PasswordConfirmation constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(RegisterDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // Let other validators handle null checks
        }

        String password = dto.getPassword();
        String confirmPassword = dto.getConfirmPassword();

        // If either field is null, let @NotBlank handle it
        if (password == null || confirmPassword == null) {
            return true;
        }

        return password.equals(confirmPassword);
    }
}