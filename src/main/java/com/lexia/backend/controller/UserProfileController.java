package com.lexia.backend.controller;

import com.lexia.backend.common.ErrorResponse;
import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Profile API", description = "User profile management operations including viewing and updating profile details, managing avatar images. All endpoints require JWT authentication.")
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
    @Operation(summary = "Get current user's profile", description = "Retrieves the complete profile information for the currently authenticated user. "
            +
            "User identity is derived from the JWT token in the Authorization header. " +
            "Returns comprehensive profile data including personal information, preferences, and learning details.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "userId": "123e4567-e89b-12d3-a456-426614174000",
                      "email": "john.doe@lexia.com",
                      "firstName": "John",
                      "lastName": "Doe",
                      "bio": "English learner passionate about business communication",
                      "phoneNumber": "+84901234567",
                      "avatarUrl": "https://cdn.lexia.com/avatars/john-doe.jpg",
                      "timezone": "Asia/Ho_Chi_Minh",
                      "language": "en",
                      "currentLevel": "B2",
                      "learningGoal": "Business English proficiency",
                      "createdAt": "2025-10-20T10:15:30",
                      "updatedAt": "2025-10-28T14:22:45"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid or expired token",
                      "path": "/api/v1/users/profile"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Profile not found for authenticated user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Not Found", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 404,
                      "error": "Not Found",
                      "message": "User profile not found",
                      "path": "/api/v1/users/profile"
                    }
                    """)))
    })
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
    @Operation(summary = "Update current user's profile", description = "Updates the profile information for the currently authenticated user. "
            +
            "Users can update their personal details including name, bio, contact information, and preferences. " +
            "All fields are validated before updating. Audit logging tracks all profile changes.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "userId": "123e4567-e89b-12d3-a456-426614174000",
                      "email": "john.doe@lexia.com",
                      "firstName": "John",
                      "lastName": "Doe",
                      "bio": "Updated bio: Senior business analyst learning English",
                      "phoneNumber": "+84901234567",
                      "avatarUrl": "https://cdn.lexia.com/avatars/john-doe.jpg",
                      "timezone": "Asia/Bangkok",
                      "language": "en",
                      "currentLevel": "B2",
                      "learningGoal": "Business English proficiency",
                      "createdAt": "2025-10-20T10:15:30",
                      "updatedAt": "2025-10-28T15:30:22"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - Validation failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Validation Error", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Validation failed",
                      "path": "/api/v1/users/profile",
                      "validationErrors": [
                        {
                          "field": "language",
                          "rejectedValue": "english",
                          "message": "Language must be a valid ISO 639-1 code (e.g., en, vi)"
                        },
                        {
                          "field": "phoneNumber",
                          "rejectedValue": "123",
                          "message": "Phone number must be 10-20 digits, optionally starting with +"
                        }
                      ]
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid or expired token",
                      "path": "/api/v1/users/profile"
                    }
                    """)))
    })
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
    @Operation(summary = "Upload or update user avatar", description = "Updates the avatar URL for the currently authenticated user. "
            +
            "Currently accepts avatar URL as JSON. Future implementation will support " +
            "multipart/form-data for direct image upload. Avatar changes are tracked in audit logs.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar updated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "message": "Avatar updated successfully"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid avatar URL", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Invalid URL", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Invalid avatar URL format",
                      "path": "/api/v1/users/profile/avatar"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid or expired token",
                      "path": "/api/v1/users/profile/avatar"
                    }
                    """)))
    })
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
    @Operation(summary = "Delete user avatar", description = "Removes the avatar for the currently authenticated user by setting the avatar URL to null. "
            +
            "This operation is tracked in audit logs for security and compliance purposes.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar deleted successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "message": "Avatar deleted successfully"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid or expired token",
                      "path": "/api/v1/users/profile/avatar"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User profile not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Not Found", value = """
                    {
                      "timestamp": "2025-10-28T10:15:30",
                      "status": 404,
                      "error": "Not Found",
                      "message": "User profile not found",
                      "path": "/api/v1/users/profile/avatar"
                    }
                    """)))
    })
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
