package com.lexia.backend.dto;

import com.lexia.backend.validation.PasswordConfirmation;
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
public class RegisterDTO {

    /**
     * User's email address.
     * Must be a valid email format and unique in the system.
     */
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User's password.
     * Must be at least 8 characters long and contain at least one letter and one
     * number.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    private String password;

    /**
     * Password confirmation.
     * Must match the password field.
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    /**
     * User's full name.
     * Required for profile creation.
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;
}