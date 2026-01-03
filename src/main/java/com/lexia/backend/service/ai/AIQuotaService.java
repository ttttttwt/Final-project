package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.QuotaSummaryStatsDTO;
import com.lexia.backend.entity.UserAiQuota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AIQuotaService {
    Page<UserAiQuota> getAllQuotas(Pageable pageable);
    
    /**
     * Get all quotas with search and plan type filtering.
     *
     * @param search   search term for user email or full name
     * @param planType filter by plan type (FREE, PRO, ALL, or null)
     * @param pageable pagination and sorting parameters
     * @return page of filtered quotas
     */
    Page<UserAiQuota> getAllQuotas(String search, String planType, Pageable pageable);

    /**
     * Get summary statistics for all quotas.
     * Returns counts of total, pro, free, exceeded, and unlimited users.
     * 
     * @return QuotaSummaryStatsDTO with aggregate statistics
     */
    QuotaSummaryStatsDTO getQuotaSummaryStats();

    UserAiQuota getQuotaByUserId(UUID userId);

    /**
     * Get or create a quota record for a user.
     * If no quota exists, creates a new one with Free plan defaults.
     * 
     * @param userId the user ID
     * @return the user's quota (existing or newly created)
     */
    UserAiQuota getOrCreateQuota(UUID userId);

    UserAiQuota updateQuota(UUID userId, com.lexia.backend.dto.ai.UpdateQuotaRequest request);

    void resetQuota(UUID userId);

    void bulkUpdateQuotas(java.util.List<UUID> userIds, com.lexia.backend.dto.ai.UpdateQuotaRequest request);

    UserAiQuota setUnlimited(UUID userId, boolean isUnlimited);
}
