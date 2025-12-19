package com.lexia.backend.service;

import com.lexia.backend.dto.admin.AbnormalActivityAlertDTO;
import com.lexia.backend.dto.admin.ActiveUsersDTO;
import com.lexia.backend.dto.admin.UserActivityDTO;
import com.lexia.backend.dto.admin.UserSessionDTO;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserSession;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin user monitoring operations.
 * Tracks sessions, activity, and detects abnormal behavior.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserMonitoringService {

        private final UserSessionRepository userSessionRepository;
        private final UserRepository userRepository;
        private final AIUsageLogRepository aiUsageLogRepository;

        // Thresholds for abnormal activity detection
        private static final int HIGH_REQUEST_THRESHOLD = 100; // requests per hour
        private static final int MULTIPLE_IP_THRESHOLD = 3; // different IPs

        /**
         * Get overview of active users with device breakdown.
         * Auto-cleans stale sessions (inactive for 30+ minutes) before counting.
         */
        @Transactional
        public ActiveUsersDTO getActiveUsers(int limit) {
                // First, cleanup stale sessions (inactive for 30+ minutes)
                Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
                int deactivated = userSessionRepository.deactivateStaleSessions(threshold, Instant.now());
                if (deactivated > 0) {
                        log.info("Auto-deactivated {} stale sessions", deactivated);
                }

                // Count unique active users (not sessions)
                long totalActiveUsers = userSessionRepository.countDistinctActiveUsers();

                // Get only the latest session per active user
                Page<UserSession> recentSessions = userSessionRepository
                                .findLatestSessionPerActiveUser(PageRequest.of(0, limit));

                // Get user details for sessions
                Set<UUID> userIds = recentSessions.getContent().stream()
                                .map(UserSession::getUserId)
                                .collect(Collectors.toSet());

                Map<UUID, User> userMap = getUserMap(userIds);

                List<UserSessionDTO> sessionDTOs = recentSessions.getContent().stream()
                                .<UserSessionDTO>map(session -> {
                                        User user = userMap.get(session.getUserId());
                                        return UserSessionDTO.fromEntityWithUser(
                                                        session,
                                                        user != null ? user.getEmail() : "Unknown",
                                                        user != null && user.getProfile() != null
                                                                        ? user.getProfile().getFullName()
                                                                        : "Unknown");
                                })
                                .collect(Collectors.toList());

                // Count by device type (from unique sessions only)
                long desktop = sessionDTOs.stream()
                                .filter(s -> "DESKTOP".equals(s.getDeviceType()))
                                .count();
                long mobile = sessionDTOs.stream()
                                .filter(s -> "MOBILE".equals(s.getDeviceType()))
                                .count();
                long tablet = sessionDTOs.stream()
                                .filter(s -> "TABLET".equals(s.getDeviceType()))
                                .count();

                return ActiveUsersDTO.builder()
                                .totalActiveUsers(totalActiveUsers)
                                .desktopUsers(desktop)
                                .mobileUsers(mobile)
                                .tabletUsers(tablet)
                                .recentSessions(sessionDTOs)
                                .build();
        }

        /**
         * Get session history for a specific user.
         */
        public Page<UserSessionDTO> getUserSessions(UUID userId, Pageable pageable) {
                User user = userRepository.findById(userId).orElse(null);

                return userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId, pageable)
                                .map(session -> UserSessionDTO.fromEntityWithUser(
                                                session,
                                                user != null ? user.getEmail() : "Unknown",
                                                user != null && user.getProfile() != null
                                                                ? user.getProfile().getFullName()
                                                                : "Unknown"));
        }

        /**
         * Get AI activity history for a specific user.
         */
        public UserActivityDTO getUserActivity(UUID userId, int limit) {
                User user = userRepository.findById(userId).orElse(null);

                // Get recent AI usage logs
                Pageable pageable = PageRequest.of(0, limit);
                List<AIUsageLog> logs = aiUsageLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

                List<UserActivityDTO.ActivityItem> activities = logs.stream()
                                .map(log -> UserActivityDTO.ActivityItem.builder()
                                                .id(log.getId())
                                                .contentType(log.getContentType())
                                                .description(buildActivityDescription(log))
                                                .timestamp(log.getCreatedAt())
                                                .tokensUsed(log.calculateTotalTokens())
                                                .success(log.getSuccess())
                                                .build())
                                .collect(Collectors.toList());

                // Calculate summary
                int totalRequests = logs.size();
                int roleplayRequests = (int) logs.stream()
                                .filter(l -> "roleplay".equalsIgnoreCase(l.getContentType()))
                                .count();
                int grammarRequests = (int) logs.stream()
                                .filter(l -> "grammar".equalsIgnoreCase(l.getContentType()))
                                .count();
                int flashcardRequests = (int) logs.stream()
                                .filter(l -> "flashcard".equalsIgnoreCase(l.getContentType()))
                                .count();
                int successfulRequests = (int) logs.stream()
                                .filter(log -> Boolean.TRUE.equals(log.getSuccess()))
                                .count();

                UserActivityDTO.ActivitySummary summary = UserActivityDTO.ActivitySummary.builder()
                                .totalRequests(totalRequests)
                                .roleplayRequests(roleplayRequests)
                                .grammarRequests(grammarRequests)
                                .flashcardRequests(flashcardRequests)
                                .successfulRequests(successfulRequests)
                                .failedRequests(totalRequests - successfulRequests)
                                .build();

                return UserActivityDTO.builder()
                                .userId(userId)
                                .userEmail(user != null ? user.getEmail() : "Unknown")
                                .activities(activities)
                                .summary(summary)
                                .build();
        }

        /**
         * Detect and return abnormal activity alerts.
         */
        public List<AbnormalActivityAlertDTO> getAbnormalActivityAlerts() {
                List<AbnormalActivityAlertDTO> alerts = new ArrayList<>();

                // Check for users with multiple IP sessions
                List<UUID> multipleIpUsers = userSessionRepository
                                .findUsersWithMultipleIpSessions(MULTIPLE_IP_THRESHOLD);

                for (UUID userId : multipleIpUsers) {
                        User user = userRepository.findById(userId).orElse(null);
                        alerts.add(AbnormalActivityAlertDTO.builder()
                                        .alertId(UUID.randomUUID().toString())
                                        .alertType(AbnormalActivityAlertDTO.AlertType.MULTIPLE_IP_SESSIONS)
                                        .severity(AbnormalActivityAlertDTO.AlertSeverity.MEDIUM)
                                        .userId(userId)
                                        .userEmail(user != null ? user.getEmail() : "Unknown")
                                        .description("User has active sessions from multiple IP addresses")
                                        .details("More than " + MULTIPLE_IP_THRESHOLD + " different IPs detected")
                                        .detectedAt(Instant.now())
                                        .isResolved(false)
                                        .build());
                }

                // Check for high request volume (last hour)
                Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
                List<Object[]> highVolumeUsers = aiUsageLogRepository
                                .findUsersWithHighRequestVolume(oneHourAgo, HIGH_REQUEST_THRESHOLD);

                for (Object[] row : highVolumeUsers) {
                        UUID userId = (UUID) row[0];
                        Long requestCount = (Long) row[1];
                        User user = userRepository.findById(userId).orElse(null);

                        alerts.add(AbnormalActivityAlertDTO.builder()
                                        .alertId(UUID.randomUUID().toString())
                                        .alertType(AbnormalActivityAlertDTO.AlertType.HIGH_REQUEST_VOLUME)
                                        .severity(requestCount > HIGH_REQUEST_THRESHOLD * 2
                                                        ? AbnormalActivityAlertDTO.AlertSeverity.HIGH
                                                        : AbnormalActivityAlertDTO.AlertSeverity.MEDIUM)
                                        .userId(userId)
                                        .userEmail(user != null ? user.getEmail() : "Unknown")
                                        .description("Unusually high AI request volume")
                                        .details(requestCount + " requests in the last hour (threshold: "
                                                        + HIGH_REQUEST_THRESHOLD + ")")
                                        .detectedAt(Instant.now())
                                        .isResolved(false)
                                        .build());
                }

                // Sort by severity (critical first)
                alerts.sort((a, b) -> b.getSeverity().ordinal() - a.getSeverity().ordinal());

                return alerts;
        }

        /**
         * Create a new session when user logs in.
         */
        @Transactional
        public UserSession createSession(UUID userId, String ipAddress, String userAgent) {
                UserSession session = UserSession.builder()
                                .userId(userId)
                                .ipAddress(ipAddress)
                                .userAgent(userAgent)
                                .deviceType(UserSession.detectDeviceType(userAgent))
                                .isActive(true)
                                .build();

                log.info("Creating session for user {} from IP {}", userId, ipAddress);
                return userSessionRepository.save(session);
        }

        /**
         * Update session activity timestamp.
         */
        @Transactional
        public void updateSessionActivity(UUID sessionId) {
                userSessionRepository.findById(sessionId).ifPresent(session -> {
                        session.setLastActivityTime(Instant.now());
                        userSessionRepository.save(session);
                });
        }

        /**
         * Deactivate session(s) on logout.
         */
        @Transactional
        public void deactivateSession(UUID sessionId) {
                userSessionRepository.findById(sessionId).ifPresent(session -> {
                        session.deactivate();
                        userSessionRepository.save(session);
                });
        }

        /**
         * Deactivate all sessions for a user.
         */
        @Transactional
        public int deactivateAllUserSessions(UUID userId) {
                return userSessionRepository.deactivateAllUserSessions(userId, Instant.now());
        }

        /**
         * Cleanup stale sessions (inactive for more than 30 minutes).
         */
        @Transactional
        public void cleanupStaleSessions() {
                Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
                List<UserSession> staleSessions = userSessionRepository.findInactiveSessions(threshold);

                for (UserSession session : staleSessions) {
                        session.deactivate();
                }

                if (!staleSessions.isEmpty()) {
                        userSessionRepository.saveAll(staleSessions);
                        log.info("Deactivated {} stale sessions", staleSessions.size());
                }
        }

        // Helper methods

        private Map<UUID, User> getUserMap(Set<UUID> userIds) {
                if (userIds.isEmpty()) {
                        return Collections.emptyMap();
                }
                return userRepository.findAllById(userIds).stream()
                                .collect(Collectors.toMap(User::getId, u -> u));
        }

        private String buildActivityDescription(AIUsageLog log) {
                String contentType = log.getContentType();
                if (contentType == null) {
                        return "AI request";
                }
                return switch (contentType.toLowerCase()) {
                        case "roleplay" -> "Role Play conversation";
                        case "grammar" -> "Grammar exercise generation";
                        case "flashcard" -> "Flashcard deck generation";
                        default -> "AI " + contentType + " request";
                };
        }
}
