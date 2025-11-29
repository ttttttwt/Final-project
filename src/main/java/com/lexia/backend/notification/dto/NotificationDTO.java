package com.lexia.backend.notification.dto;

import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Notification entity.
 * Used for API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification data transfer object")
public class NotificationDTO {

    @Schema(description = "Unique identifier of the notification", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Type of notification", example = "COURSE_COMPLETED")
    private NotificationType type;

    @Schema(description = "Notification title", example = "Congratulations! 🎉")
    private String title;

    @Schema(description = "Notification message", example = "You completed Business English Basics")
    private String message;

    @Schema(description = "Additional data payload")
    private Map<String, Object> data;

    @Schema(description = "Priority level", example = "HIGH")
    private NotificationPriority priority;

    @Schema(description = "Whether the notification has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "When the notification was read")
    private OffsetDateTime readAt;

    @Schema(description = "When the notification was created")
    private OffsetDateTime createdAt;
}
