package com.lexia.backend.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for admin view of custom material processing jobs.
 * 
 * @since Sprint 6
 */
@Data
@Builder
public class AdminJobDTO {
    private UUID id;
    private UUID materialId;
    private String materialTitle;
    private UUID userId;
    private String userEmail;
    private String status;
    private Integer progress;
    private Integer retryCount;
    private Integer maxRetries;
    private String lastError;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Job is considered stuck if PROCESSING for more than 10 minutes.
     */
    private Boolean isStuck;
}
