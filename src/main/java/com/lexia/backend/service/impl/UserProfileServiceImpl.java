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

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warn("User not found for email: {}", email);
                    return new UserNotFoundException("User not found for email: " + email);
                });

        return user.getId();
    }
}
