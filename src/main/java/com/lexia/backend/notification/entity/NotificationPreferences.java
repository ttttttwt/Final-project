package com.lexia.backend.notification.entity;

import com.lexia.backend.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Entity representing user notification preferences.
 * Stores channel preferences, type preferences, and quiet hours settings.
 */
@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences implements Persistable<UUID> {

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Channel preferences
    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;

    // Type preferences (which notifications to receive)
    @Column(name = "learning_enabled", nullable = false)
    @Builder.Default
    private Boolean learningEnabled = true;

    @Column(name = "achievements_enabled", nullable = false)
    @Builder.Default
    private Boolean achievementsEnabled = true;

    @Column(name = "reminders_enabled", nullable = false)
    @Builder.Default
    private Boolean remindersEnabled = true;

    @Column(name = "system_enabled", nullable = false)
    @Builder.Default
    private Boolean systemEnabled = true;

    // Quiet hours (optional)
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Size(max = 50)
    @Column(name = "quiet_hours_timezone", length = 50)
    @Builder.Default
    private String quietHoursTimezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }

    /**
     * Check if notifications are enabled for a specific category.
     *
     * @param category the notification category
     * @return true if enabled, false otherwise
     */
    public boolean isCategoryEnabled(Notification.NotificationCategory category) {
        return switch (category) {
            case LEARNING -> learningEnabled;
            case ACHIEVEMENT -> achievementsEnabled;
            case ENGAGEMENT -> remindersEnabled;
            case SYSTEM -> systemEnabled;
        };
    }

    /**
     * Check if we're currently in quiet hours.
     *
     * @return true if in quiet hours, false otherwise
     */
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }

        LocalTime now = LocalTime.now();

        // Handle overnight quiet hours (e.g., 22:00 - 07:00)
        if (quietHoursStart.isAfter(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }

        return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
    }

    /**
     * Create default preferences for a user.
     *
     * @param user the user
     * @return default notification preferences
     */
    public static NotificationPreferences createDefault(User user) {
        return NotificationPreferences.builder()
                .user(user)
                .userId(user.getId())
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
}
