package com.lexia.backend.notification.dto;

import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for sending notifications to specific users (Admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send notification to specific users")
public class SendNotificationRequest {

    @NotEmpty(message = "At least one user ID is required")
    @Schema(description = "List of user IDs to send notification to", required = true)
    private List<UUID> userIds;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Type of notification", example = "SYSTEM_ANNOUNCEMENT", required = true)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Notification title", example = "New Feature Available!", required = true)
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification message", example = "Check out our new interactive lessons!", required = true)
    private String message;

    @Schema(description = "Additional data payload")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Schema(description = "Priority level", example = "NORMAL")
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
}
