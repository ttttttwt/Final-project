package com.lexia.backend.notification.service.impl;

import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.notification.dto.*;
import com.lexia.backend.notification.entity.Notification;
import com.lexia.backend.notification.entity.Notification.NotificationCategory;
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

        // Check if user should receive this notification
        if (!shouldNotify(userId, request)) {
            log.debug("User {} opted out of notification type: {}", userId, request.getType());
            return null;
        }

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
    public int broadcastNotification(BroadcastNotificationRequest request) {
        log.info("Broadcasting notification: {}", request.getTitle());

        List<UUID> activeUserIds = notificationRepository.findAllActiveUserIds();
        int count = 0;

        CreateNotificationRequest createRequest = CreateNotificationRequest.builder()
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData())
                .priority(request.getPriority())
                .build();

        for (UUID userId : activeUserIds) {
            NotificationDTO notification = createNotification(userId, createRequest);
            if (notification != null) {
                sendRealTimeNotification(userId, notification);
                count++;
            }
        }

        // Also broadcast to topic for all connected users
        try {
            messagingTemplate.convertAndSend("/topic/announcements", NotificationDTO.builder()
                    .type(request.getType())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .data(request.getData())
                    .priority(request.getPriority())
                    .createdAt(OffsetDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to broadcast to topic", e);
        }

        log.info("Broadcast notification sent to {} users", count);
        return count;
    }

    @Override
    public int sendToUsers(SendNotificationRequest request) {
        log.info("Sending notification to {} users", request.getUserIds().size());

        CreateNotificationRequest createRequest = CreateNotificationRequest.builder()
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData())
                .priority(request.getPriority())
                .build();

        int count = 0;
        for (UUID userId : request.getUserIds()) {
            try {
                NotificationDTO notification = createNotification(userId, createRequest);
                if (notification != null) {
                    sendRealTimeNotification(userId, notification);
                    count++;
                }
            } catch (ResourceNotFoundException e) {
                log.warn("User not found: {}", userId);
            }
        }

        log.info("Notification sent to {} users", count);
        return count;
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

        // If no preferences, default to sending notifications
        if (preferences == null) {
            return true;
        }

        // Check if in-app notifications are enabled
        if (!preferences.getInAppEnabled()) {
            return false;
        }

        // Check if in quiet hours
        if (preferences.isInQuietHours()) {
            log.debug("User {} is in quiet hours", userId);
            return false;
        }

        // Check category preferences
        Notification tempNotification = Notification.builder()
                .type(request.getType())
                .build();
        NotificationCategory category = tempNotification.getCategory();

        return preferences.isCategoryEnabled(category);
    }
}
