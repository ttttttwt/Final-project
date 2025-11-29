package com.lexia.backend.dto;

import com.lexia.backend.entity.AuditLog;
import com.lexia.backend.entity.UserProfile;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for audit log response.
 * Used by the Log Management feature for ADMIN users.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String action;
    private String entityType;
    private UUID entityId;
    private String changes;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    /**
     * Create DTO from entity.
     * 
     * @param entity the AuditLog entity
     * @return the DTO
     */
    public static AuditLogDTO fromEntity(AuditLog entity) {
        String userEmail = entity.getUser() != null ? entity.getUser().getEmail() : null;
        String userName = null;
        if (entity.getUser() != null && entity.getUser().getProfile() != null) {
            UserProfile profile = entity.getUser().getProfile();
            if (profile.getFirstName() != null || profile.getLastName() != null) {
                userName = (profile.getFirstName() != null ? profile.getFirstName() : "")
                        + " "
                        + (profile.getLastName() != null ? profile.getLastName() : "");
                userName = userName.trim();
            } else if (profile.getFullName() != null) {
                userName = profile.getFullName();
            }
        }

        return AuditLogDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(userEmail)
                .userName(userName)
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .changes(entity.getChanges())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
