package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.ai.UpdateQuotaRequest;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.ai.AIQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIQuotaServiceImpl implements AIQuotaService {

    private final UserAiQuotaRepository quotaRepository;

    @Override
    public Page<UserAiQuota> getAllQuotas(Pageable pageable) {
        return quotaRepository.findAll(pageable);
    }

    @Override
    public UserAiQuota getQuotaByUserId(UUID userId) {
        return quotaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quota not found for user: " + userId));
    }

    @Override
    @Transactional
    public UserAiQuota updateQuota(UUID userId, UpdateQuotaRequest request) {
        UserAiQuota quota = getQuotaByUserId(userId);
        if (request.getDailyLimit() != null) quota.setDailyLimit(request.getDailyLimit());
        if (request.getMonthlyLimit() != null) quota.setMonthlyLimit(request.getMonthlyLimit());
        if (request.getSuspended() != null) quota.setSuspended(request.getSuspended());
        if (request.getIsPremium() != null) quota.setIsPremium(request.getIsPremium());
        return quotaRepository.save(quota);
    }

    @Override
    @Transactional
    public void resetQuota(UUID userId) {
        UserAiQuota quota = getQuotaByUserId(userId);
        quota.setDailyUsed(0);
        quota.setMonthlyUsed(0);
        quotaRepository.save(quota);
    }

    @Override
    @Transactional
    public void bulkUpdateQuotas(java.util.List<UUID> userIds, UpdateQuotaRequest request) {
        java.util.List<UserAiQuota> quotas = quotaRepository.findAllById(userIds);
        quotas.forEach(quota -> {
            if (request.getDailyLimit() != null) quota.setDailyLimit(request.getDailyLimit());
            if (request.getMonthlyLimit() != null) quota.setMonthlyLimit(request.getMonthlyLimit());
            if (request.getSuspended() != null) quota.setSuspended(request.getSuspended());
            if (request.getIsPremium() != null) quota.setIsPremium(request.getIsPremium());
        });
        quotaRepository.saveAll(quotas);
    }

    @Override
    @Transactional
    public UserAiQuota setUnlimited(UUID userId, boolean isUnlimited) {
        UserAiQuota quota = getQuotaByUserId(userId);
        if (isUnlimited) {
            quota.setDailyLimit(Integer.MAX_VALUE);
            quota.setMonthlyLimit(Integer.MAX_VALUE);
        } else {
            quota.setDailyLimit(100); 
            quota.setMonthlyLimit(3000);
        }
        return quotaRepository.save(quota);
    }
}
