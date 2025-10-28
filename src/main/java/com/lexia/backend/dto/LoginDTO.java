package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login requests.
 * Contains validation rules for authentication credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login request containing email and password credentials")
public class LoginDTO {

    /**
     * User's email address.
     * Must be a valid email format and registered in the system.
     */
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User's registered email address", example = "john.doe@lexia.com", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String email;

    /**
     * User's password.
     * Must match the registered password in the system.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Schema(description = "User's password for authentication", example = "SecurePass123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 255, format = "password")
    private String password;
}
