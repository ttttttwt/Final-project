package com.lexia.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for quota summary statistics.
 * Used by admin dashboard to display aggregate quota information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaSummaryStatsDTO {
    
    /** Total number of users with quota records */
    private long totalUsers;
    
    /** Number of Pro users (MONTHLY or YEARLY subscription) */
    private long proUsers;
    
    /** Number of Free users */
    private long freeUsers;
    
    /** Number of users who have exceeded their quota (critical) */
    private long quotaExceeded;
    
    /** Number of users with unlimited quota */
    private long unlimitedUsers;
}
