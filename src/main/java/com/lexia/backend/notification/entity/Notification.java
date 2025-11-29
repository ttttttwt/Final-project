package com.lexia.backend.notification.entity;

import com.lexia.backend.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a user notification.
 * Stores notifications for learning events, achievements, and system alerts.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_unread", columnList = "user_id, is_read"),
        @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_notifications_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    /**
     * Notification types supported by the system.
     */
    public enum NotificationType {
        // Learning notifications
        COURSE_PUBLISHED,
        LESSON_ADDED,
        ENROLLMENT_CONFIRMED,

        // Achievement notifications
        LESSON_COMPLETED,
        COURSE_COMPLETED,
        ACHIEVEMENT_UNLOCKED,

        // Engagement notifications
        STREAK_REMINDER,
        STREAK_LOST,
        STREAK_MILESTONE,
        LEVEL_UP,

        // System notifications
        SYSTEM_ANNOUNCEMENT,
        MAINTENANCE_NOTICE
    }

    /**
     * Priority levels for notifications.
     */
    public enum NotificationPriority {
        HIGH, // Real-time + Persist 30 days
        NORMAL, // Persist only 14 days
        LOW // Persist only 7 days
    }

    /**
     * Mark this notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }

    /**
     * Get the category of this notification based on its type.
     */
    public NotificationCategory getCategory() {
        return switch (type) {
            case COURSE_PUBLISHED, LESSON_ADDED, ENROLLMENT_CONFIRMED -> NotificationCategory.LEARNING;
            case LESSON_COMPLETED, COURSE_COMPLETED, ACHIEVEMENT_UNLOCKED, STREAK_MILESTONE, LEVEL_UP ->
                NotificationCategory.ACHIEVEMENT;
            case STREAK_REMINDER, STREAK_LOST -> NotificationCategory.ENGAGEMENT;
            case SYSTEM_ANNOUNCEMENT, MAINTENANCE_NOTICE -> NotificationCategory.SYSTEM;
        };
    }

    /**
     * Categories for notification types.
     */
    public enum NotificationCategory {
        LEARNING,
        ACHIEVEMENT,
        ENGAGEMENT,
        SYSTEM
    }
}
