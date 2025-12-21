package com.lexia.backend.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for admin view of custom materials.
 * 
 * @since Sprint 6
 */
@Data
@Builder
public class AdminCustomMaterialDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String title;
    private String sourceType;
    private String status;
    private String errorMessage;
    private Long contentLength;
    private Instant createdAt;
    private Instant updatedAt;
}
