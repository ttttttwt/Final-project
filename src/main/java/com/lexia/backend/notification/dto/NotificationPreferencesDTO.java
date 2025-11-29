package com.lexia.backend.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for NotificationPreferences entity.
 * Used for API requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User notification preferences")
public class NotificationPreferencesDTO {

    // Channel preferences
    @Schema(description = "Enable in-app notifications", example = "true")
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Schema(description = "Enable email notifications", example = "true")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Schema(description = "Enable push notifications", example = "false")
    @Builder.Default
    private Boolean pushEnabled = true;

    // Type preferences
    @Schema(description = "Receive learning-related notifications", example = "true")
    @Builder.Default
    private Boolean learningEnabled = true;

    @Schema(description = "Receive achievement notifications", example = "true")
    @Builder.Default
    private Boolean achievementsEnabled = true;

    @Schema(description = "Receive reminder notifications", example = "true")
    @Builder.Default
    private Boolean remindersEnabled = true;

    @Schema(description = "Receive system announcements", example = "true")
    @Builder.Default
    private Boolean systemEnabled = true;

    // Quiet hours
    @Schema(description = "Start time for quiet hours", example = "22:00")
    private LocalTime quietHoursStart;

    @Schema(description = "End time for quiet hours", example = "07:00")
    private LocalTime quietHoursEnd;

    @Schema(description = "Timezone for quiet hours", example = "Asia/Ho_Chi_Minh")
    @Size(max = 50)
    @Builder.Default
    private String quietHoursTimezone = "UTC";
}
