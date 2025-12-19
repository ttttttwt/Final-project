package com.lexia.backend.dto.admin;

import com.lexia.backend.entity.UserSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user session information in admin monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private Instant loginTime;
    private Instant lastActivityTime;
    private Boolean isActive;
    private Instant logoutTime;

    /**
     * Convert entity to DTO (without user details)
     */
    public static UserSessionDTO fromEntity(UserSession session) {
        return UserSessionDTO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceType(session.getDeviceType() != null ? session.getDeviceType().name() : null)
                .loginTime(session.getLoginTime())
                .lastActivityTime(session.getLastActivityTime())
                .isActive(session.getIsActive())
                .logoutTime(session.getLogoutTime())
                .build();
    }

    /**
     * Convert entity to DTO with user details
     */
    public static UserSessionDTO fromEntityWithUser(UserSession session, String email, String name) {
        UserSessionDTO dto = fromEntity(session);
        dto.setUserEmail(email);
        dto.setUserName(name);
        return dto;
    }
}
