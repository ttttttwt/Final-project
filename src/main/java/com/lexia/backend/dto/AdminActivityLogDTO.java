package com.lexia.backend.dto;

import com.lexia.backend.entity.AdminActivityLog;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for admin activity log response.
 * Used by the Log Management feature for ADMIN users.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLogDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private AdminActivityLog.ActionType action;
    private AdminActivityLog.EntityType entityType;
    private String entityId;
    private String entityName;
    private String description;
    private String details;
    private LocalDateTime createdAt;

    /**
     * Create DTO from entity.
     * 
     * @param entity the AdminActivityLog entity
     * @return the DTO
     */
    public static AdminActivityLogDTO fromEntity(AdminActivityLog entity) {
        return AdminActivityLogDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .entityName(entity.getEntityName())
                .description(entity.getDescription())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
