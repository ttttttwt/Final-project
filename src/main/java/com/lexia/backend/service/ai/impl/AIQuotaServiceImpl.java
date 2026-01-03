package com.lexia.backend.service.ai.impl;

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.dto.ai.QuotaSummaryStatsDTO;
import com.lexia.backend.dto.ai.UpdateQuotaRequest;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.ai.AIQuotaService;
import com.lexia.backend.specification.UserAiQuotaSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIQuotaServiceImpl implements AIQuotaService {

    private final UserAiQuotaRepository quotaRepository;
    private final QuotaLimitsConfig quotaLimitsConfig;

    @Override
    public Page<UserAiQuota> getAllQuotas(Pageable pageable) {
        return quotaRepository.findAll(pageable);
    }
    
    @Override
    public Page<UserAiQuota> getAllQuotas(String search, String planType, Pageable pageable) {
        Specification<UserAiQuota> spec = UserAiQuotaSpecification.withSearchAndPlan(search, planType);
        return quotaRepository.findAll(spec, pageable);
    }

    @Override
    public QuotaSummaryStatsDTO getQuotaSummaryStats() {
        long total = quotaRepository.countTotal();
        long proUsers = quotaRepository.countProUsers();
        long freeUsers = quotaRepository.countFreeUsers();
        long unlimitedUsers = quotaRepository.countUnlimitedUsers();
        
        // Count users who have exceeded critical threshold
        // This requires iterating through all quotas which may be slow for large datasets
        // For better performance, consider adding a database column or scheduled job
        long quotaExceeded = countQuotaExceededUsers();
        
        return QuotaSummaryStatsDTO.builder()
                .totalUsers(total)
                .proUsers(proUsers)
                .freeUsers(freeUsers)
                .quotaExceeded(quotaExceeded)
                .unlimitedUsers(unlimitedUsers)
                .build();
    }
    
    private long countQuotaExceededUsers() {
        double criticalThreshold = quotaLimitsConfig.getCriticalThreshold();
        
        // Count users who have exceeded critical threshold for any quota type
        return quotaRepository.findAll().stream()
                .filter(quota -> {
                    if (quota.getDailyLimit() == Integer.MAX_VALUE) {
                        return false; // Unlimited users can't exceed
                    }
                    QuotaLimitsConfig.QuotaLimits limits = quotaLimitsConfig.getForPlan(quota.getPlanType());
                    
                    return (limits.getRoleplaySessions() > 0 && 
                            (double) quota.getRoleplaySessionsUsed() / limits.getRoleplaySessions() >= criticalThreshold) ||
                           (limits.getFlashcardDecks() > 0 && 
                            (double) quota.getFlashcardDecksUsed() / limits.getFlashcardDecks() >= criticalThreshold) ||
                           (limits.getGrammarExercises() > 0 && 
                            (double) quota.getGrammarExercisesUsed() / limits.getGrammarExercises() >= criticalThreshold) ||
                           (limits.getCustomMaterialsLimit() != null && limits.getCustomMaterialsLimit() > 0 &&
                            (double) quota.getCustomMaterialsUsed() / limits.getCustomMaterialsLimit() >= criticalThreshold);
                })
                .count();
    }

    @Override
    public UserAiQuota getQuotaByUserId(UUID userId) {
        return quotaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quota not found for user: " + userId));
    }

    @Override
    @Transactional
    public UserAiQuota getOrCreateQuota(UUID userId) {
        return quotaRepository.findById(userId)
                .orElseGet(() -> createDefaultQuota(userId));
    }

    /**
     * Create a default quota record for a new user with Free plan limits.
     */
    private UserAiQuota createDefaultQuota(UUID userId) {
        UserAiQuota quota = new UserAiQuota();
        quota.setUserId(userId);
        quota.setPlanType(PlanType.FREE);
        quota.setQuotaResetDate(LocalDate.now().plusMonths(1));

        // Default limits for Free plan
        quota.setDailyLimit(50);
        quota.setDailyUsed(0);
        quota.setMonthlyLimit(100);
        quota.setMonthlyUsed(0);

        // Session counters
        quota.setRoleplaySessionsUsed(0);
        quota.setFlashcardDecksUsed(0);
        quota.setGrammarExercisesUsed(0);

        quota.setIsPremium(false);
        quota.setSuspended(false);
        quota.setLastResetDaily(java.time.Instant.now());
        quota.setLastResetMonthly(java.time.Instant.now());

        return quotaRepository.save(quota);
    }

    @Override
    @Transactional
    public UserAiQuota updateQuota(UUID userId, UpdateQuotaRequest request) {
        UserAiQuota quota = getQuotaByUserId(userId);
        if (request.getDailyLimit() != null)
            quota.setDailyLimit(request.getDailyLimit());
        if (request.getMonthlyLimit() != null)
            quota.setMonthlyLimit(request.getMonthlyLimit());
        if (request.getSuspended() != null)
            quota.setSuspended(request.getSuspended());
        if (request.getIsPremium() != null)
            quota.setIsPremium(request.getIsPremium());

        updateFeatureLimits(quota, request);

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
            if (request.getDailyLimit() != null)
                quota.setDailyLimit(request.getDailyLimit());
            if (request.getMonthlyLimit() != null)
                quota.setMonthlyLimit(request.getMonthlyLimit());
            if (request.getSuspended() != null)
                quota.setSuspended(request.getSuspended());
            if (request.getIsPremium() != null)
                quota.setIsPremium(request.getIsPremium());

            updateFeatureLimits(quota, request);
        });
        quotaRepository.saveAll(quotas);
    }

    private void updateFeatureLimits(UserAiQuota quota, UpdateQuotaRequest request) {
        if (request.getRolePlayDailyLimit() != null) {
            updateFeatureLimit(quota, "roleplay", request.getRolePlayDailyLimit());
        }
        if (request.getGrammarDailyLimit() != null) {
            updateFeatureLimit(quota, "grammar", request.getGrammarDailyLimit());
        }
        if (request.getFlashcardDailyLimit() != null) {
            updateFeatureLimit(quota, "flashcard", request.getFlashcardDailyLimit());
        }
        if (request.getCustomMaterialsLimit() != null) {
            quota.setCustomMaterialsMonthlyLimit(request.getCustomMaterialsLimit());
        }
    }

    private void updateFeatureLimit(UserAiQuota quota, String feature, Integer limit) {
        java.util.Map<String, java.util.Map<String, Integer>> limits = quota.getFeatureLimits();
        if (limits == null) {
            limits = new java.util.HashMap<>();
            quota.setFeatureLimits(limits);
        }

        java.util.Map<String, Integer> featureLimit = limits.get(feature);
        if (featureLimit == null) {
            featureLimit = new java.util.HashMap<>();
            limits.put(feature, featureLimit);
        } else if (!(featureLimit instanceof java.util.HashMap)) {
            // Convert immutable map to mutable
            featureLimit = new java.util.HashMap<>(featureLimit);
            limits.put(feature, featureLimit);
        }

        featureLimit.put("daily", limit);
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
