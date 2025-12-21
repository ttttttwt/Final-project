package com.lexia.backend.dto.admin;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for job processing statistics.
 * 
 * @since Sprint 6
 */
@Data
@Builder
public class JobStatsDTO {
    private Long totalJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Long processingJobs;
    private Long queuedJobs;
    private Long stuckJobs;
    private Double successRate;
}
