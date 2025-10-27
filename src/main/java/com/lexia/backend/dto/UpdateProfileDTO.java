package com.lexia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating user profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    /**
     * User's first name
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * User's last name
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * User's bio (optional, max 500 characters)
     */
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    /**
     * User's phone number (optional, must be valid format)
     */
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be 10-20 digits, optionally starting with +")
    private String phoneNumber;

    /**
     * User's timezone (IANA format, e.g., Asia/Ho_Chi_Minh)
     */
    @NotBlank(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    /**
     * User's language preference (ISO 639-1 code, e.g., en, vi)
     */
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (e.g., en, vi)")
    private String language;
}
