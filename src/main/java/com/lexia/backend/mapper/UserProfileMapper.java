package com.lexia.backend.mapper;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.entity.UserProfile;

/**
 * Mapper utility class for converting between UserProfile entities and DTOs.
 * Provides centralized mapping logic for user profile data transformations.
 */
public class UserProfileMapper {

    private UserProfileMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a UserProfile entity to UserProfileDTO.
     *
     * @param profile the user profile entity
     * @return UserProfileDTO representation, or null if profile is null
     */
    public static UserProfileDTO toDTO(UserProfile profile) {
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

    /**
     * Updates a UserProfile entity from UpdateProfileDTO.
     * Only updates fields that are present in the DTO (non-null values).
     *
     * @param profile the user profile entity to update
     * @param dto     the update profile data
     * @throws IllegalArgumentException if profile or dto is null
     */
    public static void updateEntityFromDTO(UserProfile profile, UpdateProfileDTO dto) {
        if (profile == null) {
            throw new IllegalArgumentException("UserProfile entity cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateProfileDTO cannot be null");
        }

        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setTimezone(dto.getTimezone());
        profile.setLanguage(dto.getLanguage());

        // Update full_name for backward compatibility
        if (dto.getFirstName() != null && dto.getLastName() != null) {
            profile.setFullName(dto.getFirstName() + " " + dto.getLastName());
        }
    }

    /**
     * Creates a UserProfile entity from UpdateProfileDTO.
     * This is useful when creating a new profile with initial data.
     *
     * @param dto the update profile data
     * @return a new UserProfile entity with data from DTO
     * @throws IllegalArgumentException if dto is null
     */
    public static UserProfile toEntity(UpdateProfileDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UpdateProfileDTO cannot be null");
        }

        return UserProfile.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .bio(dto.getBio())
                .phoneNumber(dto.getPhoneNumber())
                .timezone(dto.getTimezone())
                .language(dto.getLanguage())
                .build();
    }
}
