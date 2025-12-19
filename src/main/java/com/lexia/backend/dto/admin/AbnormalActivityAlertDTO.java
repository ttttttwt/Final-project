package com.lexia.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for abnormal activity alerts in admin monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalActivityAlertDTO {
    private String alertId;
    private AlertType alertType;
    private AlertSeverity severity;
    private UUID userId;
    private String userEmail;
    private String description;
    private String details;
    private Instant detectedAt;
    private Boolean isResolved;

    public enum AlertType {
        HIGH_REQUEST_VOLUME, // Too many AI requests in short time
        MULTIPLE_IP_SESSIONS, // Same user from many different IPs
        UNUSUAL_LOCATION, // Login from unusual location
        QUOTA_ABUSE, // Attempting to bypass quota limits
        SUSPICIOUS_PATTERN // General suspicious activity pattern
    }

    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
