package com.lexia.backend.notification.repository;

import com.lexia.backend.notification.entity.Notification;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Notification entity.
 * Provides custom queries for notification management.
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    /**
     * Find all notifications for a user, ordered by creation date descending.
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user.
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find a notification by ID and user ID (for ownership verification).
     */
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndIsReadFalse(UUID userId);

    /**
     * Count high priority unread notifications for a user.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.priority = 'HIGH'")
    long countHighPriorityUnreadByUserId(@Param("userId") UUID userId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") OffsetDateTime readAt);

    /**
     * Delete all read notifications for a user.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.isRead = true")
    int deleteAllReadByUserId(@Param("userId") UUID userId);

    /**
     * Delete expired notifications.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") OffsetDateTime now);

    /**
     * Find notifications by type for a user.
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type);

    /**
     * Check if a notification exists for a user with specific type and data.
     * Useful for preventing duplicate notifications.
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n " +
            "WHERE n.user.id = :userId AND n.type = :type AND n.createdAt > :since")
    boolean existsByUserIdAndTypeSince(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type,
            @Param("since") OffsetDateTime since);

    /**
     * Find all user IDs (for broadcast notifications).
     */
    @Query("SELECT DISTINCT u.id FROM User u WHERE u.isActive = true")
    List<UUID> findAllActiveUserIds();
}
