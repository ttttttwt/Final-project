package com.lexia.backend.controller;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.service.UserProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Profile Controller for LEXIA.
 * Handles user profile retrieval, updates, and avatar management endpoints.
 * All endpoints require authentication.
 *
 * Base path: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Get current authenticated user's profile.
     * Retrieves user ID from Spring Security context.
     *
     * @return ResponseEntity with user profile data
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        LOG.info("Received request to get current user profile");

        UserProfileDTO profile = userProfileService.getCurrentUserProfile();

        LOG.info("Successfully retrieved profile for user: {}", profile.getUserId());

        return ResponseEntity.ok(profile);
    }

    /**
     * Update current authenticated user's profile.
     * Users can only update their own profile.
     *
     * @param updateProfileDTO the profile update data
     * @return ResponseEntity with updated profile data
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        LOG.info("Received request to update current user profile");

        // Get current user's profile and update it
        UserProfileDTO currentProfile = userProfileService.getCurrentUserProfile();
        UserProfileDTO updatedProfile = userProfileService.updateProfile(
                currentProfile.getUserId(), updateProfileDTO);

        LOG.info("Successfully updated profile for user: {}", updatedProfile.getUserId());

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Upload or update user avatar.
     * This is a placeholder for future multipart/form-data implementation.
     * Currently accepts avatar URL as JSON.
     *
     * @param avatarRequest the avatar URL request
     * @return ResponseEntity with success status
     */
    @PostMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateAvatar(@RequestBody AvatarRequest avatarRequest) {
        LOG.info("Received request to update avatar");

        UserProfileDTO currentProfile = userProfileService.getCurrentUserProfile();
        userProfileService.updateAvatar(currentProfile.getUserId(), avatarRequest.getAvatarUrl());

        LOG.info("Successfully updated avatar for user: {}", currentProfile.getUserId());

        return ResponseEntity.ok().build();
    }

    /**
     * Remove user avatar.
     * Sets avatar URL to null.
     *
     * @return ResponseEntity with success status
     */
    @DeleteMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAvatar() {
        LOG.info("Received request to delete avatar");

        UserProfileDTO currentProfile = userProfileService.getCurrentUserProfile();
        userProfileService.deleteAvatar(currentProfile.getUserId());

        LOG.info("Successfully deleted avatar for user: {}", currentProfile.getUserId());

        return ResponseEntity.ok().build();
    }

    /**
     * Inner class for avatar URL request.
     * Used for JSON request body in avatar upload endpoint.
     */
    public static class AvatarRequest {
        private String avatarUrl;

        public AvatarRequest() {
        }

        public AvatarRequest(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
