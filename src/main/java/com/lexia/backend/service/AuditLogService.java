package com.lexia.backend.service;

import java.util.UUID;

/**
 * Service interface for audit logging.
 * Records user actions for compliance, security, and debugging purposes.
 */
public interface AuditLogService {

    /**
     * Log a profile update action.
     *
     * @param userId    the user who performed the update
     * @param profileId the profile that was updated
     * @param changes   description of changes (JSON format)
     * @param ipAddress IP address of the user (optional)
     * @param userAgent user agent string (optional)
     */
    void logProfileUpdate(UUID userId, UUID profileId, String changes, String ipAddress, String userAgent);

    /**
     * Log an avatar update action.
     *
     * @param userId    the user who performed the update
     * @param profileId the profile whose avatar was updated
     * @param avatarUrl the new avatar URL
     * @param ipAddress IP address of the user (optional)
     * @param userAgent user agent string (optional)
     */
    void logAvatarUpdate(UUID userId, UUID profileId, String avatarUrl, String ipAddress, String userAgent);

    /**
     * Log an avatar deletion action.
     *
     * @param userId    the user who performed the deletion
     * @param profileId the profile whose avatar was deleted
     * @param ipAddress IP address of the user (optional)
     * @param userAgent user agent string (optional)
     */
    void logAvatarDelete(UUID userId, UUID profileId, String ipAddress, String userAgent);
}
