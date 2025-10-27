package com.lexia.backend.service;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.exception.InvalidInputException;
import com.lexia.backend.exception.UserNotFoundException;

import java.util.UUID;

/**
 * Service interface for user profile management operations.
 * Handles profile retrieval, updates, and avatar management.
 */
public interface UserProfileService {

    /**
     * Retrieves user profile by user ID.
     *
     * @param userId the user ID
     * @return UserProfileDTO containing profile data
     * @throws UserNotFoundException if user not found
     */
    UserProfileDTO getProfile(UUID userId);

    /**
     * Retrieves the current authenticated user's profile.
     * Gets user ID from Spring Security context.
     *
     * @return UserProfileDTO containing profile data
     * @throws UserNotFoundException if user not found
     */
    UserProfileDTO getCurrentUserProfile();

    /**
     * Updates user profile fields.
     *
     * @param userId the user ID
     * @param dto    the update profile data
     * @return UserProfileDTO containing updated profile
     * @throws UserNotFoundException if user not found
     * @throws InvalidInputException if validation fails
     */
    UserProfileDTO updateProfile(UUID userId, UpdateProfileDTO dto);

    /**
     * Updates user avatar URL.
     *
     * @param userId    the user ID
     * @param avatarUrl the new avatar URL
     * @throws UserNotFoundException if user not found
     * @throws InvalidInputException if avatar URL is invalid
     */
    void updateAvatar(UUID userId, String avatarUrl);

    /**
     * Removes user avatar by setting it to null.
     *
     * @param userId the user ID
     * @throws UserNotFoundException if user not found
     */
    void deleteAvatar(UUID userId);
}
