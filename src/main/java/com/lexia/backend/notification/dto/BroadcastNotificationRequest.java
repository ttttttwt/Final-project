package com.lexia.backend.notification.dto;

import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for broadcasting notification to all users (Admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to broadcast notification to all users")
public class BroadcastNotificationRequest {

    @NotNull(message = "Notification type is required")
    @Schema(description = "Type of notification", example = "SYSTEM_ANNOUNCEMENT", required = true)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Notification title", example = "Platform Update", required = true)
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification message", example = "We've released new features!", required = true)
    private String message;

    @Schema(description = "Additional data payload")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Schema(description = "Priority level", example = "HIGH")
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.HIGH;
}
