package com.lexia.backend.service;

import com.lexia.backend.entity.UserSession;
import com.lexia.backend.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing user login sessions.
 * Used to track active users for admin monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    /**
     * Create a new session when user logs in
     */
    @Transactional
    public UserSession createSession(UUID userId, String ipAddress, String userAgent) {
        log.info("Creating session for user: {} from IP: {}", userId, ipAddress);

        UserSession session = UserSession.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(detectDeviceType(userAgent))
                .loginTime(Instant.now())
                .lastActivityTime(Instant.now())
                .isActive(true)
                .build();

        UserSession saved = userSessionRepository.save(session);
        log.debug("Session created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update last activity time for a session
     */
    @Transactional
    public void updateLastActivity(UUID userId) {
        userSessionRepository.findFirstByUserIdAndIsActiveTrueOrderByLoginTimeDesc(userId)
                .ifPresent(session -> {
                    session.setLastActivityTime(Instant.now());
                    userSessionRepository.save(session);
                });
    }

    /**
     * Deactivate session when user logs out
     */
    @Transactional
    public void deactivateSession(UUID userId) {
        log.info("Deactivating sessions for user: {}", userId);
        userSessionRepository.deactivateAllUserSessions(userId, Instant.now());
    }

    /**
     * Deactivate all sessions for a user (force logout)
     */
    @Transactional
    public void deactivateAllUserSessions(UUID userId) {
        log.info("Force logout: Deactivating all sessions for user: {}", userId);
        userSessionRepository.deactivateAllUserSessions(userId, Instant.now());
    }

    /**
     * Detect device type from User-Agent string
     */
    private UserSession.DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UserSession.DeviceType.UNKNOWN;
        }

        String lowerUserAgent = userAgent.toLowerCase();

        // Check for mobile devices
        if (lowerUserAgent.contains("mobile") ||
                lowerUserAgent.contains("android") ||
                lowerUserAgent.contains("iphone") ||
                lowerUserAgent.contains("ipod")) {
            return UserSession.DeviceType.MOBILE;
        }

        // Check for tablets
        if (lowerUserAgent.contains("tablet") ||
                lowerUserAgent.contains("ipad") ||
                (lowerUserAgent.contains("android") && !lowerUserAgent.contains("mobile"))) {
            return UserSession.DeviceType.TABLET;
        }

        // Default to desktop for browsers on desktop OS
        if (lowerUserAgent.contains("windows") ||
                lowerUserAgent.contains("macintosh") ||
                lowerUserAgent.contains("linux") ||
                lowerUserAgent.contains("x11")) {
            return UserSession.DeviceType.DESKTOP;
        }

        return UserSession.DeviceType.UNKNOWN;
    }
}
