package com.lexia.backend.dto;

import com.lexia.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for User responses.
 * Excludes sensitive information like password hashes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * User's unique identifier
     */
    private UUID id;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's authentication provider
     */
    private User.AuthProvider authProvider;

    /**
     * Whether the user account is active
     */
    private Boolean isActive;

    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Account last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * User's full name from profile
     */
    private String fullName;

    /**
     * User's avatar URL from profile
     */
    private String avatarUrl;

    /**
     * User's current learning level
     */
    private String currentLevel;

    /**
     * User's learning goal
     */
    private String learningGoal;

    /**
     * Converts a User entity to UserDTO.
     * Excludes sensitive information and includes profile data.
     *
     * @param user the user entity
     * @return UserDTO representation
     */
    public static UserDTO fromEntity(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .authProvider(user.getAuthProvider())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        // Include profile information if available
        if (user.getProfile() != null) {
            builder.fullName(user.getProfile().getFullName())
                    .avatarUrl(user.getProfile().getAvatarUrl())
                    .currentLevel(user.getProfile().getCurrentLevel())
                    .learningGoal(user.getProfile().getLearningGoal());
        }

        return builder.build();
    }
}