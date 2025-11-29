package com.lexia.backend.notification.service;

import com.lexia.backend.notification.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for notification operations.
 */
public interface NotificationService {

    // ==================== CRUD Operations ====================

    /**
     * Get paginated notifications for a user.
     *
     * @param userId   the user ID
     * @param pageable pagination parameters
     * @return paginated notifications
     */
    Page<NotificationDTO> getNotifications(UUID userId, Pageable pageable);

    /**
     * Get a single notification by ID.
     *
     * @param userId         the user ID
     * @param notificationId the notification ID
     * @return the notification
     */
    NotificationDTO getNotificationById(UUID userId, UUID notificationId);

    /**
     * Mark a notification as read.
     *
     * @param userId         the user ID
     * @param notificationId the notification ID
     */
    void markAsRead(UUID userId, UUID notificationId);

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId the user ID
     * @return number of notifications marked as read
     */
    int markAllAsRead(UUID userId);

    /**
     * Delete a notification.
     *
     * @param userId         the user ID
     * @param notificationId the notification ID
     */
    void deleteNotification(UUID userId, UUID notificationId);

    /**
     * Delete all read notifications for a user.
     *
     * @param userId the user ID
     * @return number of notifications deleted
     */
    int deleteAllReadNotifications(UUID userId);

    // ==================== Unread Count ====================

    /**
     * Get unread notification count for a user.
     *
     * @param userId the user ID
     * @return unread count DTO
     */
    UnreadCountDTO getUnreadCount(UUID userId);

    // ==================== Create & Send ====================

    /**
     * Create a notification for a user.
     *
     * @param userId  the user ID
     * @param request the notification request
     * @return the created notification
     */
    NotificationDTO createNotification(UUID userId, CreateNotificationRequest request);

    /**
     * Send a notification via WebSocket (real-time delivery).
     *
     * @param userId       the user ID
     * @param notification the notification to send
     */
    void sendRealTimeNotification(UUID userId, NotificationDTO notification);

    /**
     * Broadcast a notification to all active users.
     *
     * @param request the broadcast request
     * @return number of users notified
     */
    int broadcastNotification(BroadcastNotificationRequest request);

    /**
     * Send notification to specific users (admin function).
     *
     * @param request the send request
     * @return number of users notified
     */
    int sendToUsers(SendNotificationRequest request);

    // ==================== Preferences ====================

    /**
     * Get notification preferences for a user.
     *
     * @param userId the user ID
     * @return the preferences DTO
     */
    NotificationPreferencesDTO getPreferences(UUID userId);

    /**
     * Update notification preferences for a user.
     *
     * @param userId      the user ID
     * @param preferences the preferences to update
     * @return the updated preferences
     */
    NotificationPreferencesDTO updatePreferences(UUID userId, NotificationPreferencesDTO preferences);

    // ==================== Cleanup ====================

    /**
     * Delete expired notifications.
     *
     * @return number of notifications deleted
     */
    int deleteExpiredNotifications();

    // ==================== Helper Methods ====================

    /**
     * Check if a user should receive a notification based on preferences.
     *
     * @param userId  the user ID
     * @param request the notification request
     * @return true if the user should receive the notification
     */
    boolean shouldNotify(UUID userId, CreateNotificationRequest request);
}
