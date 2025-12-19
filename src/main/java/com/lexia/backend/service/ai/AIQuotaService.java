package com.lexia.backend.service.ai;

import com.lexia.backend.entity.UserAiQuota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AIQuotaService {
    Page<UserAiQuota> getAllQuotas(Pageable pageable);

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
