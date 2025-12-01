package com.lexia.backend.file.service;

import com.lexia.backend.entity.User;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for centralizing file access control logic.
 * Provides methods to check file ownership and access permissions.
 */
@Service
@RequiredArgsConstructor
public class FileSecurityService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSecurityService.class);
    private static final String ROLE_ADMIN = "ADMIN";

    private final FileRepository fileRepository;

    /**
     * Check if the given user is the owner of the file.
     *
     * @param fileId the file ID
     * @param userId the user ID
     * @return true if user owns the file
     */
    @Transactional(readOnly = true)
    public boolean isOwner(UUID fileId, UUID userId) {
        if (fileId == null || userId == null) {
            return false;
        }

        return fileRepository.findById(fileId)
                .map(file -> file.getUploadedBy() != null &&
                        file.getUploadedBy().getId().equals(userId))
                .orElse(false);
    }

    /**
     * Check if the given user has permission to access the file.
     * Access is granted if:
     * - File is public
     * - User is the file owner
     * - User has ADMIN role
     *
     * @param file the file entity
     * @param user the authenticated user (can be null for public access check)
     * @return true if access is permitted
     */
    public boolean canAccess(FileEntity file, User user) {
        if (file == null) {
            return false;
        }

        // Public files can be accessed by anyone
        if (Boolean.TRUE.equals(file.getIsPublic())) {
            LOG.debug("File {} is public, access granted", file.getId());
            return true;
        }

        // Non-public files require authentication
        if (user == null) {
            LOG.debug("File {} is not public and user is not authenticated", file.getId());
            return false;
        }

        // Check if user is owner
        if (file.getUploadedBy() != null && file.getUploadedBy().getId().equals(user.getId())) {
            LOG.debug("User {} is owner of file {}", user.getId(), file.getId());
            return true;
        }

        // Check if user is admin
        if (hasAdminRole(user)) {
            LOG.debug("User {} has ADMIN role, access granted to file {}", user.getId(), file.getId());
            return true;
        }

        LOG.debug("User {} denied access to file {}", user.getId(), file.getId());
        return false;
    }

    /**
     * Check if the given user can delete the file.
     * Deletion is permitted if:
     * - User is the file owner
     * - User has ADMIN role
     *
     * @param file the file entity
     * @param user the authenticated user
     * @return true if deletion is permitted
     */
    public boolean canDelete(FileEntity file, User user) {
        if (file == null || user == null) {
            return false;
        }

        // Check if user is owner
        if (file.getUploadedBy() != null && file.getUploadedBy().getId().equals(user.getId())) {
            return true;
        }

        // Check if user is admin
        return hasAdminRole(user);
    }

    /**
     * Check if user has ADMIN role.
     */
    private boolean hasAdminRole(User user) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }

        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole() != null &&
                        ROLE_ADMIN.equals(userRole.getRole().getName()));
    }
}
