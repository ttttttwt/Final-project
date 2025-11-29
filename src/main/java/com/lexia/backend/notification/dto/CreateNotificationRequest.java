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
 * Request DTO for creating a new notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new notification")
public class CreateNotificationRequest {

    @NotNull(message = "Notification type is required")
    @Schema(description = "Type of notification", example = "COURSE_COMPLETED", required = true)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Notification title", example = "Congratulations! 🎉", required = true)
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification message", example = "You completed Business English Basics", required = true)
    private String message;

    @Schema(description = "Additional data payload")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Schema(description = "Priority level", example = "HIGH")
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
}
