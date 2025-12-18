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
 * Data Transfer Object for change password requests.
 * Requires current password verification and validates new password strength.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request for authenticated users")
public class ChangePasswordDTO {

    /**
     * User's current password for verification.
     */
    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password for verification", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String currentPassword;

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
    private String confirmNewPassword;
}
