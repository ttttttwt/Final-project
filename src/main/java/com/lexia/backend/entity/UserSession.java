package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing user login sessions for monitoring.
 * Tracks IP addresses, devices, and session activity.
 */
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_user_sessions_is_active", columnList = "is_active"),
        @Index(name = "idx_user_sessions_last_activity", columnList = "last_activity_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Device type derived from user agent: DESKTOP, MOBILE, TABLET
     */
    @Column(name = "device_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @CreationTimestamp
    @Column(name = "login_time", nullable = false, updatable = false)
    private Instant loginTime;

    @Column(name = "last_activity_time", nullable = false)
    @Builder.Default
    private Instant lastActivityTime = Instant.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "logout_time")
    private Instant logoutTime;

    /**
     * Session token hash for validation (not the actual token)
     */
    @Column(name = "session_token_hash", length = 64)
    private String sessionTokenHash;

    @PreUpdate
    protected void onUpdate() {
        lastActivityTime = Instant.now();
    }

    /**
     * Mark session as inactive (logged out)
     */
    public void deactivate() {
        this.isActive = false;
        this.logoutTime = Instant.now();
    }

    public enum DeviceType {
        DESKTOP,
        MOBILE,
        TABLET,
        UNKNOWN
    }

    /**
     * Detect device type from user agent string
     */
    public static DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return DeviceType.UNKNOWN;
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") && !ua.contains("tablet")) {
            return DeviceType.MOBILE;
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return DeviceType.TABLET;
        } else if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) {
            return DeviceType.DESKTOP;
        }
        return DeviceType.UNKNOWN;
    }
}
