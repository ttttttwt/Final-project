package com.lexia.backend.validation;

import com.lexia.backend.dto.RegisterDTO;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordConfirmationValidator.
 * Tests password confirmation validation logic.
 */
class PasswordConfirmationValidatorTest {

    private PasswordConfirmationValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordConfirmationValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_WithMatchingPasswords_ReturnsTrue() {
        // Given
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WithNonMatchingPasswords_ReturnsFalse() {
        // Given
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123!")
                .confirmPassword("DifferentPassword123!")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_WithNullPassword_ReturnsTrue() {
        // Given - Let @NotBlank handle null checks
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password(null)
                .confirmPassword("Password123!")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertTrue(result); // Validator defers null checking to @NotBlank
    }

    @Test
    void isValid_WithNullConfirmPassword_ReturnsTrue() {
        // Given - Let @NotBlank handle null checks
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123!")
                .confirmPassword(null)
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertTrue(result); // Validator defers null checking to @NotBlank
    }

    @Test
    void isValid_WithBothPasswordsNull_ReturnsTrue() {
        // Given - Let @NotBlank handle null checks
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password(null)
                .confirmPassword(null)
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertTrue(result); // Validator defers null checking to @NotBlank
    }

    @Test
    void isValid_WithEmptyPasswords_ReturnsTrue() {
        // Given - Empty passwords match, but other validators will catch this
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("")
                .confirmPassword("")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertTrue(result); // Validation passes (passwords match), but @NotBlank will fail
    }

    @Test
    void isValid_WithCaseSensitivePasswords_ReturnsFalse() {
        // Given
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123!")
                .confirmPassword("password123!")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertFalse(result); // Should be case-sensitive
    }

    @Test
    void isValid_WithWhitespaceInPasswords_ReturnsFalseIfDifferent() {
        // Given
        RegisterDTO dto = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123! ")
                .confirmPassword("Password123!")
                .fullName("Test User")
                .build();

        // When
        boolean result = validator.isValid(dto, context);

        // Then
        assertFalse(result); // Trailing whitespace should make them different
    }
}
