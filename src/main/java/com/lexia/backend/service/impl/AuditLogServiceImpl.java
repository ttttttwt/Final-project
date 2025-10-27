package com.lexia.backend.service.impl;

import com.lexia.backend.entity.AuditLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.AuditLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of AuditLogService for recording user actions.
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void logProfileUpdate(UUID userId, UUID profileId, String changes, String ipAddress, String userAgent) {
        LOG.debug("Logging profile update for user ID: {}, profile ID: {}", userId, profileId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                LOG.warn("User not found for audit log: {}", userId);
                return;
            }

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action("PROFILE_UPDATE")
                    .entityType("UserProfile")
                    .entityId(profileId)
                    .changes(changes)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            LOG.info("Profile update logged for user ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Failed to log profile update for user ID: {}", userId, e);
            // Don't throw exception - audit logging should not break the main flow
        }
    }

    @Override
    @Transactional
    public void logAvatarUpdate(UUID userId, UUID profileId, String avatarUrl, String ipAddress, String userAgent) {
        LOG.debug("Logging avatar update for user ID: {}, profile ID: {}", userId, profileId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                LOG.warn("User not found for audit log: {}", userId);
                return;
            }

            String changes = String.format("{\"avatarUrl\": \"%s\"}", avatarUrl);

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action("AVATAR_UPDATE")
                    .entityType("UserProfile")
                    .entityId(profileId)
                    .changes(changes)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            LOG.info("Avatar update logged for user ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Failed to log avatar update for user ID: {}", userId, e);
        }
    }

    @Override
    @Transactional
    public void logAvatarDelete(UUID userId, UUID profileId, String ipAddress, String userAgent) {
        LOG.debug("Logging avatar deletion for user ID: {}, profile ID: {}", userId, profileId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                LOG.warn("User not found for audit log: {}", userId);
                return;
            }

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action("AVATAR_DELETE")
                    .entityType("UserProfile")
                    .entityId(profileId)
                    .changes("{\"avatarUrl\": null}")
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            LOG.info("Avatar deletion logged for user ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Failed to log avatar deletion for user ID: {}", userId, e);
        }
    }
}
