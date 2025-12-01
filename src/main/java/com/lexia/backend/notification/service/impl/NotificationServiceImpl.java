package com.lexia.backend.notification.service.impl;

import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.notification.dto.*;
import com.lexia.backend.notification.entity.Notification;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.mapper.NotificationMapper;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.notification.repository.NotificationRepository;
import com.lexia.backend.notification.service.NotificationService;
import com.lexia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of NotificationService.
 * Handles all notification operations including CRUD, real-time delivery, and
 * preferences.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ==================== CRUD Operations ====================

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(UUID userId, Pageable pageable) {
        log.debug("Getting notifications for user: {}, page: {}", userId, pageable);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(UUID userId, UUID notificationId) {
        log.debug("Getting notification: {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        return NotificationMapper.toDTO(notification);
    }

    @Override
    public void markAsRead(UUID userId, UUID notificationId) {
        log.debug("Marking notification: {} as read for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("Notification {} marked as read", notificationId);
        }
    }

    @Override
    public int markAllAsRead(UUID userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        int count = notificationRepository.markAllAsReadByUserId(userId, OffsetDateTime.now());
        log.info("Marked {} notifications as read for user: {}", count, userId);
        return count;
    }

    @Override
    public void deleteNotification(UUID userId, UUID notificationId) {
        log.debug("Deleting notification: {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        notificationRepository.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }

    @Override
    public int deleteAllReadNotifications(UUID userId) {
        log.debug("Deleting all read notifications for user: {}", userId);
        int count = notificationRepository.deleteAllReadByUserId(userId);
        log.info("Deleted {} read notifications for user: {}", count, userId);
        return count;
    }

    // ==================== Unread Count ====================

    @Override
    @Transactional(readOnly = true)
    public UnreadCountDTO getUnreadCount(UUID userId) {
        log.debug("Getting unread count for user: {}", userId);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        long highPriorityCount = notificationRepository.countHighPriorityUnreadByUserId(userId);

        return UnreadCountDTO.builder()
                .unreadCount(unreadCount)
                .highPriorityCount(highPriorityCount)
                .build();
    }

    // ==================== Create & Send ====================

    @Override
    public NotificationDTO createNotification(UUID userId, CreateNotificationRequest request) {
        log.debug("Creating notification for user: {}, type: {}", userId, request.getType());

        // Always save notification to DB regardless of preferences
        // Preferences only affect real-time delivery, not persistence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Notification notification = NotificationMapper.toEntity(request, user);
        notification = notificationRepository.save(notification);

        log.info("Created notification {} for user: {}", notification.getId(), userId);
        return NotificationMapper.toDTO(notification);
    }

    @Override
    @Async
    public void sendRealTimeNotification(UUID userId, NotificationDTO notification) {
        if (notification == null) {
            return;
        }

        log.debug("Sending real-time notification to user: {}", userId);
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification);
            log.debug("Real-time notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user: {}", userId, e);
        }
    }

    @Override
    @Async
    public int broadcastNotification(BroadcastNotificationRequest request) {
        log.info("Broadcasting notification: {}", request.getTitle());

        // 1. Send to topic for real-time delivery to all connected users
        NotificationDTO broadcastDTO = NotificationDTO.builder()
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData())
                .priority(request.getPriority())
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            messagingTemplate.convertAndSend("/topic/announcements", broadcastDTO);
            log.debug("Broadcast sent to /topic/announcements");
        } catch (Exception e) {
            log.error("Failed to broadcast to topic", e);
        }

        // 2. Persist notifications for all active users using paginated batch
        // processing
        // This approach avoids loading all user IDs into memory at once (OOM
        // prevention)
        int count = 0;
        int pageSize = 500; // Fetch user IDs in pages of 500
        int batchSize = 100; // Save notifications in batches of 100
        int pageNumber = 0;

        // Calculate expiration date once for all notifications
        OffsetDateTime expiresAt = calculateExpirationDate(request.getPriority());

        Page<UUID> userPage;
        do {
            // Fetch a page of active user IDs from database
            userPage = notificationRepository.findActiveUserIds(
                    org.springframework.data.domain.PageRequest.of(pageNumber, pageSize));

            List<Notification> batch = new ArrayList<>(batchSize);

            for (UUID userId : userPage.getContent()) {
                try {
                    // Use getReferenceById to get a JPA proxy reference without fetching the full
                    // User entity
                    // This is more efficient and JPA-compliant than creating a new User object
                    User userRef = userRepository.getReferenceById(userId);

                    Notification notification = Notification.builder()
                            .user(userRef)
                            .type(request.getType())
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .data(request.getData() != null ? request.getData() : new java.util.HashMap<>())
                            .priority(request.getPriority() != null ? request.getPriority()
                                    : Notification.NotificationPriority.NORMAL)
                            .isRead(false)
                            .expiresAt(expiresAt)
                            .build();

                    batch.add(notification);

                    // Save batch when it reaches the batch size
                    if (batch.size() >= batchSize) {
                        notificationRepository.saveAll(batch);
                        count += batch.size();
                        batch.clear();
                    }
                } catch (Exception e) {
                    log.warn("Failed to create notification for user: {}", userId, e);
                }
            }

            // Save remaining notifications in this page's batch
            if (!batch.isEmpty()) {
                notificationRepository.saveAll(batch);
                count += batch.size();
            }

            pageNumber++;
            log.debug("Processed page {} with {} users, total notifications: {}",
                    pageNumber, userPage.getNumberOfElements(), count);

        } while (userPage.hasNext());

        log.info("Broadcast notification persisted for {} users", count);
        return count;
    }

    /**
     * Calculate expiration date based on priority.
     *
     * @param priority the notification priority
     * @return the expiration date
     */
    private OffsetDateTime calculateExpirationDate(Notification.NotificationPriority priority) {
        Notification.NotificationPriority p = priority != null ? priority : Notification.NotificationPriority.NORMAL;
        return switch (p) {
            case HIGH -> OffsetDateTime.now().plusDays(30);
            case NORMAL -> OffsetDateTime.now().plusDays(14);
            case LOW -> OffsetDateTime.now().plusDays(7);
        };
    }

    @Override
    public int sendToUsers(SendNotificationRequest request) {
        log.info("Sending notification to {} users", request.getUserIds().size());

        List<UUID> userIds = request.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("No user IDs provided for sendToUsers");
            return 0;
        }

        // Calculate expiration date once for all notifications
        OffsetDateTime expiresAt = calculateExpirationDate(request.getPriority());

        // Batch processing configuration
        int batchSize = 100;
        List<Notification> batch = new ArrayList<>(batchSize);
        List<UUID> successfulUserIds = new ArrayList<>();
        int count = 0;

        for (UUID userId : userIds) {
            try {
                // Use getReferenceById for efficient JPA proxy without DB query
                User userRef = userRepository.getReferenceById(userId);

                Notification notification = Notification.builder()
                        .user(userRef)
                        .type(request.getType())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .data(request.getData() != null ? request.getData() : new java.util.HashMap<>())
                        .priority(request.getPriority() != null ? request.getPriority()
                                : Notification.NotificationPriority.NORMAL)
                        .isRead(false)
                        .expiresAt(expiresAt)
                        .build();

                batch.add(notification);
                successfulUserIds.add(userId);

                // Save batch when it reaches the batch size
                if (batch.size() >= batchSize) {
                    List<Notification> savedBatch = notificationRepository.saveAll(batch);
                    count += savedBatch.size();

                    // Send real-time notifications for this batch
                    sendRealTimeNotificationsAsync(successfulUserIds, savedBatch);

                    batch.clear();
                    successfulUserIds.clear();
                }
            } catch (Exception e) {
                log.warn("Failed to create notification for user: {}", userId, e);
            }
        }

        // Save remaining notifications in the last batch
        if (!batch.isEmpty()) {
            List<Notification> savedBatch = notificationRepository.saveAll(batch);
            count += savedBatch.size();

            // Send real-time notifications for remaining batch
            sendRealTimeNotificationsAsync(successfulUserIds, savedBatch);
        }

        log.info("Notification sent to {} users", count);
        return count;
    }

    /**
     * Send real-time notifications asynchronously for a batch of notifications.
     * This method is called after batch save to ensure notifications have IDs.
     *
     * @param userIds       list of user IDs corresponding to notifications
     * @param notifications list of saved notifications with IDs
     */
    @Async
    protected void sendRealTimeNotificationsAsync(List<UUID> userIds, List<Notification> notifications) {
        for (int i = 0; i < Math.min(userIds.size(), notifications.size()); i++) {
            UUID userId = userIds.get(i);
            Notification notification = notifications.get(i);

            try {
                NotificationDTO dto = NotificationMapper.toDTO(notification);
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/notifications",
                        dto);
            } catch (Exception e) {
                log.warn("Failed to send real-time notification to user: {}", userId, e);
            }
        }
    }

    // ==================== Preferences ====================

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesDTO getPreferences(UUID userId) {
        log.debug("Getting preferences for user: {}", userId);

        NotificationPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElse(null);

        if (preferences == null) {
            // Return default preferences
            return NotificationPreferencesDTO.builder()
                    .inAppEnabled(true)
                    .emailEnabled(true)
                    .pushEnabled(true)
                    .learningEnabled(true)
                    .achievementsEnabled(true)
                    .remindersEnabled(true)
                    .systemEnabled(true)
                    .quietHoursTimezone("UTC")
                    .build();
        }

        return NotificationMapper.toPreferencesDTO(preferences);
    }

    @Override
    public NotificationPreferencesDTO updatePreferences(UUID userId, NotificationPreferencesDTO dto) {
        log.debug("Updating preferences for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        NotificationPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreferences.createDefault(user));

        NotificationMapper.updatePreferencesFromDTO(preferences, dto);
        preferences = preferencesRepository.save(preferences);

        log.info("Updated preferences for user: {}", userId);
        return NotificationMapper.toPreferencesDTO(preferences);
    }

    // ==================== Cleanup ====================

    @Override
    @Scheduled(cron = "0 0 3 * * *") // Run daily at 3:00 AM
    public int deleteExpiredNotifications() {
        log.info("Starting expired notification cleanup");
        int count = notificationRepository.deleteExpiredNotifications(OffsetDateTime.now());
        log.info("Deleted {} expired notifications", count);
        return count;
    }

    // ==================== Helper Methods ====================

    @Override
    @Transactional(readOnly = true)
    public boolean shouldNotify(UUID userId, CreateNotificationRequest request) {
        NotificationPreferences preferences = preferencesRepository.findByUserId(userId).orElse(null);

        // If no preferences, default to sending real-time notifications
        if (preferences == null) {
            log.debug("No preferences found for user {}, defaulting to send", userId);
            return true;
        }

        // Check if in-app notifications are enabled (for real-time delivery)
        if (!preferences.getInAppEnabled()) {
            log.debug("User {} has in-app notifications disabled", userId);
            return false;
        }

        // Check if in quiet hours (only affects real-time, not persistence)
        if (preferences.isInQuietHours()) {
            log.debug("User {} is in quiet hours - skipping real-time delivery", userId);
            return false;
        }

        // Check category preferences using NotificationType's getCategory method
        // This eliminates code duplication - single source of truth in Entity
        Notification.NotificationCategory category = request.getType().getCategory();
        boolean enabled = preferences.isCategoryEnabled(category);

        if (!enabled) {
            log.debug("User {} has category {} disabled", userId, category);
        }

        return enabled;
    }
}
