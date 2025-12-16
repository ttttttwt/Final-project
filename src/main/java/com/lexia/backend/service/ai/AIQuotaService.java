package com.lexia.backend.service.ai;

import com.lexia.backend.entity.UserAiQuota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AIQuotaService {
    Page<UserAiQuota> getAllQuotas(Pageable pageable);
    UserAiQuota getQuotaByUserId(UUID userId);
    UserAiQuota updateQuota(UUID userId, com.lexia.backend.dto.ai.UpdateQuotaRequest request);
    void resetQuota(UUID userId);
    void bulkUpdateQuotas(java.util.List<UUID> userIds, com.lexia.backend.dto.ai.UpdateQuotaRequest request);
    UserAiQuota setUnlimited(UUID userId, boolean isUnlimited);
}
