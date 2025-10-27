package com.lexia.backend.dto;

import com.lexia.backend.entity.UserProfile;
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
public class UserProfileDTO {

    /**
     * User's unique identifier
     */
    private UUID userId;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's first name
     */
    private String firstName;

    /**
     * User's last name
     */
    private String lastName;

    /**
     * User's bio (max 500 characters)
     */
    private String bio;

    /**
     * User's phone number (optional)
     */
    private String phoneNumber;

    /**
     * User's avatar URL
     */
    private String avatarUrl;

    /**
     * User's timezone (IANA format)
     */
    private String timezone;

    /**
     * User's language preference (ISO 639-1)
     */
    private String language;

    /**
     * User's current learning level
     */
    private String currentLevel;

    /**
     * User's learning goal
     */
    private String learningGoal;

    /**
     * Profile creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Profile last update timestamp
     */
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
