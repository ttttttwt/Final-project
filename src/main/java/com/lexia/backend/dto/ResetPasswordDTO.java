package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reset password requests.
 * Contains the reset token and new password.
 * 
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reset password with token")
public class ResetPasswordDTO {

    /**
     * The reset token received via email.
     */
    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token from email link", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    /**
     * New password to set.
     * Must be at least 8 characters and contain uppercase, lowercase, and number.
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    @Schema(description = "New password (min 8 chars, must contain uppercase, lowercase, and number)", example = "NewSecurePass123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 255, format = "password")
    private String newPassword;

    /**
     * Confirmation of new password.
     * Must match the newPassword field.
     */
    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirm new password (must match newPassword)", example = "NewSecurePass123", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String confirmPassword;
}
