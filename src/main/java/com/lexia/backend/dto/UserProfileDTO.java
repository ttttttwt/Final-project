package com.lexia.backend.dto;

import com.lexia.backend.entity.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for User Profile responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete user profile information including personal details, preferences, and learning status")
public class UserProfileDTO {

    /**
     * User's unique identifier
     */
    @Schema(description = "User's unique identifier (UUID format)", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    /**
     * User's email address
     */
    @Schema(description = "User's registered email address", example = "john.doe@lexia.com", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    /**
     * User's first name
     */
    @Schema(description = "User's first name", example = "John", maxLength = 100)
    private String firstName;

    /**
     * User's last name
     */
    @Schema(description = "User's last name", example = "Doe", maxLength = 100)
    private String lastName;

    /**
     * User's bio (max 500 characters)
     */
    @Schema(description = "User's biography or personal description", example = "English learner passionate about business communication", maxLength = 500, nullable = true)
    private String bio;

    /**
     * User's phone number (optional)
     */
    @Schema(description = "User's phone number in international format", example = "+84901234567", nullable = true, pattern = "^[+]?[0-9]{10,20}$")
    private String phoneNumber;

    /**
     * User's avatar URL
     */
    @Schema(description = "URL to user's avatar image", example = "https://cdn.lexia.com/avatars/john-doe.jpg", nullable = true)
    private String avatarUrl;

    /**
     * User's timezone (IANA format)
     */
    @Schema(description = "User's timezone in IANA format", example = "Asia/Ho_Chi_Minh", maxLength = 50)
    private String timezone;

    /**
     * User's language preference (ISO 639-1)
     */
    @Schema(description = "User's preferred language (ISO 639-1 code)", example = "en", pattern = "^[a-z]{2}$")
    private String language;

    /**
     * User's current learning level
     */
    @Schema(description = "User's current English proficiency level (A1, A2, B1, B2, C1, C2)", example = "B2", nullable = true)
    private String currentLevel;

    /**
     * User's learning goal
     */
    @Schema(description = "User's learning objective or goal", example = "Business English proficiency", nullable = true)
    private String learningGoal;

    /**
     * Profile creation timestamp
     */
    @Schema(description = "Profile creation timestamp", example = "2025-10-20T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Profile last update timestamp
     */
    @Schema(description = "Profile last update timestamp", example = "2025-10-28T14:22:45", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * Converts a UserProfile entity to UserProfileDTO.
     *
     * @param profile the user profile entity
     * @return UserProfileDTO representation
     */
    public static UserProfileDTO fromEntity(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return UserProfileDTO.builder()
                .userId(profile.getUserId())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .avatarUrl(profile.getAvatarUrl())
                .timezone(profile.getTimezone())
                .language(profile.getLanguage())
                .currentLevel(profile.getCurrentLevel())
                .learningGoal(profile.getLearningGoal())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
