package com.lexia.backend.service.impl;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.exception.InvalidInputException;
import com.lexia.backend.exception.UserNotFoundException;
import com.lexia.backend.mapper.UserProfileMapper;
import com.lexia.backend.repository.UserProfileRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.AuditLogService;
import com.lexia.backend.service.UserProfileService;
import com.lexia.backend.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of UserProfileService for managing user profiles.
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuditLogService auditLogService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(UUID userId) {
        LOG.debug("Fetching profile for user ID: {}", userId);

        if (userId == null) {
            throw new InvalidInputException("User ID cannot be null");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    LOG.warn("User profile not found for user ID: {}", userId);
                    return new UserNotFoundException("User profile not found for user ID: " + userId);
                });

        LOG.info("Successfully retrieved profile for user ID: {}", userId);
        return UserProfileMapper.toDTO(profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        UUID userId = getCurrentUserId();
        LOG.debug("Fetching current user profile for user ID: {}", userId);
        return getProfile(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserProfileDTO updateProfile(UUID userId, UpdateProfileDTO dto) {
        LOG.debug("Updating profile for user ID: {}", userId);

        if (userId == null) {
            throw new InvalidInputException("User ID cannot be null");
        }

        if (dto == null) {
            throw new InvalidInputException("Update profile data cannot be null");
        }

        // Validate business rules
        validateUpdateProfileDTO(dto);

        // Fetch user profile
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    LOG.warn("User profile not found for user ID: {}", userId);
                    return new UserNotFoundException("User profile not found for user ID: " + userId);
                });

        // Update profile fields using mapper
        UserProfileMapper.updateEntityFromDTO(profile, dto);

        // Save updated profile
        UserProfile updatedProfile = userProfileRepository.save(profile);

        // Log the profile update for audit trail
        try {
            String changes = buildChangesJson(dto);
            auditLogService.logProfileUpdate(userId, profile.getUserId(), changes, null, null);
        } catch (Exception e) {
            LOG.warn("Failed to log profile update audit for user ID: {}", userId, e);
        }

        LOG.info("Successfully updated profile for user ID: {}", userId);
        return UserProfileMapper.toDTO(updatedProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateAvatar(UUID userId, String avatarUrl) {
        LOG.debug("Updating avatar for user ID: {}", userId);

        if (userId == null) {
            throw new InvalidInputException("User ID cannot be null");
        }

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            throw new InvalidInputException("Avatar URL cannot be null or empty");
        }

        // Basic URL validation
        if (!avatarUrl.matches("^https?://.*")) {
            throw new InvalidInputException("Avatar URL must be a valid HTTP or HTTPS URL");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    LOG.warn("User profile not found for user ID: {}", userId);
                    return new UserNotFoundException("User profile not found for user ID: " + userId);
                });

        profile.setAvatarUrl(avatarUrl);
        userProfileRepository.save(profile);

        // Log the avatar update
        try {
            auditLogService.logAvatarUpdate(userId, profile.getUserId(), avatarUrl, null, null);
        } catch (Exception e) {
            LOG.warn("Failed to log avatar update audit for user ID: {}", userId, e);
        }

        LOG.info("Successfully updated avatar for user ID: {}", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAvatar(UUID userId) {
        LOG.debug("Deleting avatar for user ID: {}", userId);

        if (userId == null) {
            throw new InvalidInputException("User ID cannot be null");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    LOG.warn("User profile not found for user ID: {}", userId);
                    return new UserNotFoundException("User profile not found for user ID: " + userId);
                });

        profile.setAvatarUrl(null);
        userProfileRepository.save(profile);

        // Log the avatar deletion
        try {
            auditLogService.logAvatarDelete(userId, profile.getUserId(), null, null);
        } catch (Exception e) {
            LOG.warn("Failed to log avatar deletion audit for user ID: {}", userId, e);
        }

        LOG.info("Successfully deleted avatar for user ID: {}", userId);
    }

    /**
     * Validates the UpdateProfileDTO fields using business rules.
     *
     * @param dto the update profile data
     * @throws InvalidInputException if validation fails
     */
    private void validateUpdateProfileDTO(UpdateProfileDTO dto) {
        // Validate timezone
        ValidationUtils.validateTimezone(dto.getTimezone());

        // Validate language
        ValidationUtils.validateLanguage(dto.getLanguage());

        // Validate phone number (optional field)
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty()) {
            ValidationUtils.validatePhoneNumber(dto.getPhoneNumber());
        }

        // Validate bio length
        ValidationUtils.validateBio(dto.getBio());
    }

    /**
     * Gets the current authenticated user's ID from SecurityContext.
     *
     * @return the current user's UUID
     * @throws UserNotFoundException if no authenticated user found
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            LOG.warn("No authenticated user found in SecurityContext");
            throw new UserNotFoundException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warn("User not found for email: {}", email);
                    return new UserNotFoundException("User not found for email: " + email);
                });

        return user.getId();
    }

    /**
     * Builds a JSON string representing the changes made to a profile.
     *
     * @param dto the update profile data
     * @return JSON string with field changes
     */
    private String buildChangesJson(UpdateProfileDTO dto) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        if (dto.getFirstName() != null) {
            json.append("\"firstName\":\"").append(escapeJson(dto.getFirstName())).append("\"");
            first = false;
        }

        if (dto.getLastName() != null) {
            if (!first)
                json.append(",");
            json.append("\"lastName\":\"").append(escapeJson(dto.getLastName())).append("\"");
            first = false;
        }

        if (dto.getBio() != null) {
            if (!first)
                json.append(",");
            json.append("\"bio\":\"").append(escapeJson(dto.getBio())).append("\"");
            first = false;
        }

        if (dto.getPhoneNumber() != null) {
            if (!first)
                json.append(",");
            json.append("\"phoneNumber\":\"").append(escapeJson(dto.getPhoneNumber())).append("\"");
            first = false;
        }

        if (dto.getTimezone() != null) {
            if (!first)
                json.append(",");
            json.append("\"timezone\":\"").append(escapeJson(dto.getTimezone())).append("\"");
            first = false;
        }

        if (dto.getLanguage() != null) {
            if (!first)
                json.append(",");
            json.append("\"language\":\"").append(escapeJson(dto.getLanguage())).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings.
     *
     * @param str the string to escape
     * @return escaped string
     */
    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
