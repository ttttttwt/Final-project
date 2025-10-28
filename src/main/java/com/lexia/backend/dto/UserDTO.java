package com.lexia.backend.dto;

import com.lexia.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User account information (excludes sensitive data like passwords)")
public class UserDTO {

    /**
     * User's unique identifier
     */
    @Schema(description = "User's unique identifier (UUID format)", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    /**
     * User's email address
     */
    @Schema(description = "User's registered email address", example = "john.doe@lexia.com", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    /**
     * User's authentication provider
     */
    @Schema(description = "Authentication provider used for account creation", example = "LOCAL", accessMode = Schema.AccessMode.READ_ONLY)
    private User.AuthProvider authProvider;

    /**
     * Whether the user account is active
     */
    @Schema(description = "Indicates if the user account is active", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isActive;

    /**
     * Account creation timestamp
     */
    @Schema(description = "Account creation timestamp", example = "2025-10-20T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Account last update timestamp
     */
    @Schema(description = "Account last update timestamp", example = "2025-10-28T14:22:45", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * User's full name from profile
     */
    @Schema(description = "User's full name from profile", example = "John Doe", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private String fullName;

    /**
     * User's avatar URL from profile
     */
    @Schema(description = "URL to user's avatar image", example = "https://cdn.lexia.com/avatars/john-doe.jpg", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private String avatarUrl;

    /**
     * User's current learning level
     */
    @Schema(description = "User's current English proficiency level", example = "B2", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private String currentLevel;

    /**
     * User's learning goal
     */
    @Schema(description = "User's learning objective or goal", example = "Business English proficiency", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
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