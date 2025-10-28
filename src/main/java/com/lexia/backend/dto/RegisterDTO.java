package com.lexia.backend.dto;

import com.lexia.backend.validation.PasswordConfirmation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests.
 * Contains validation rules for user input data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordConfirmation
@Schema(description = "User registration request containing email, password, and personal information")
public class RegisterDTO {

    /**
     * User's email address.
     * Must be a valid email format and unique in the system.
     */
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User's email address (must be unique in the system)", example = "john.doe@lexia.com", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String email;

    /**
     * User's password.
     * Must be at least 8 characters long and contain at least one letter and one
     * number.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    @Schema(description = "User's password (minimum 8 characters, must contain at least one letter and one number)", example = "SecurePass123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 255, format = "password")
    private String password;

    /**
     * Password confirmation.
     * Must match the password field.
     */
    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Password confirmation (must match the password field)", example = "SecurePass123", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String confirmPassword;

    /**
     * User's full name.
     * Required for profile creation.
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    @Schema(description = "User's full name for profile creation", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String fullName;
}