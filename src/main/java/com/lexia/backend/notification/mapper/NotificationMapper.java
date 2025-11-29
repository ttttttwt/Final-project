package com.lexia.backend.notification.mapper;

import com.lexia.backend.entity.User;
import com.lexia.backend.notification.dto.CreateNotificationRequest;
import com.lexia.backend.notification.dto.NotificationDTO;
import com.lexia.backend.notification.dto.NotificationPreferencesDTO;
import com.lexia.backend.notification.entity.Notification;
import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.NotificationPreferences;

import java.time.OffsetDateTime;

/**
 * Mapper utility class for Notification entities and DTOs.
 */
public final class NotificationMapper {

    private NotificationMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert Notification entity to NotificationDTO.
     *
     * @param notification the entity
     * @return the DTO
     */
    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Convert CreateNotificationRequest to Notification entity.
     *
     * @param request the request DTO
     * @param user    the user to associate with the notification
     * @return the entity
     */
    public static Notification toEntity(CreateNotificationRequest request, User user) {
        if (request == null) {
            return null;
        }

        OffsetDateTime expiresAt = calculateExpirationDate(request.getPriority());

        return Notification.builder()
                .user(user)
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData() != null ? request.getData() : new java.util.HashMap<>())
                .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL)
                .isRead(false)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Convert NotificationPreferences entity to NotificationPreferencesDTO.
     *
     * @param preferences the entity
     * @return the DTO
     */
    public static NotificationPreferencesDTO toPreferencesDTO(NotificationPreferences preferences) {
        if (preferences == null) {
            return null;
        }

        return NotificationPreferencesDTO.builder()
                .inAppEnabled(preferences.getInAppEnabled())
                .emailEnabled(preferences.getEmailEnabled())
                .pushEnabled(preferences.getPushEnabled())
                .learningEnabled(preferences.getLearningEnabled())
                .achievementsEnabled(preferences.getAchievementsEnabled())
                .remindersEnabled(preferences.getRemindersEnabled())
                .systemEnabled(preferences.getSystemEnabled())
                .quietHoursStart(preferences.getQuietHoursStart())
                .quietHoursEnd(preferences.getQuietHoursEnd())
                .quietHoursTimezone(preferences.getQuietHoursTimezone())
                .build();
    }

    /**
     * Update NotificationPreferences entity from DTO.
     *
     * @param preferences the entity to update
     * @param dto         the DTO with new values
     */
    public static void updatePreferencesFromDTO(NotificationPreferences preferences, NotificationPreferencesDTO dto) {
        if (preferences == null || dto == null) {
            return;
        }

        if (dto.getInAppEnabled() != null) {
            preferences.setInAppEnabled(dto.getInAppEnabled());
        }
        if (dto.getEmailEnabled() != null) {
            preferences.setEmailEnabled(dto.getEmailEnabled());
        }
        if (dto.getPushEnabled() != null) {
            preferences.setPushEnabled(dto.getPushEnabled());
        }
        if (dto.getLearningEnabled() != null) {
            preferences.setLearningEnabled(dto.getLearningEnabled());
        }
        if (dto.getAchievementsEnabled() != null) {
            preferences.setAchievementsEnabled(dto.getAchievementsEnabled());
        }
        if (dto.getRemindersEnabled() != null) {
            preferences.setRemindersEnabled(dto.getRemindersEnabled());
        }
        if (dto.getSystemEnabled() != null) {
            preferences.setSystemEnabled(dto.getSystemEnabled());
        }
        // Quiet hours can be set to null
        preferences.setQuietHoursStart(dto.getQuietHoursStart());
        preferences.setQuietHoursEnd(dto.getQuietHoursEnd());
        if (dto.getQuietHoursTimezone() != null) {
            preferences.setQuietHoursTimezone(dto.getQuietHoursTimezone());
        }
    }

    /**
     * Calculate expiration date based on priority.
     *
     * @param priority the notification priority
     * @return the expiration date
     */
    private static OffsetDateTime calculateExpirationDate(NotificationPriority priority) {
        NotificationPriority p = priority != null ? priority : NotificationPriority.NORMAL;
        return switch (p) {
            case HIGH -> OffsetDateTime.now().plusDays(30);
            case NORMAL -> OffsetDateTime.now().plusDays(14);
            case LOW -> OffsetDateTime.now().plusDays(7);
        };
    }
}
