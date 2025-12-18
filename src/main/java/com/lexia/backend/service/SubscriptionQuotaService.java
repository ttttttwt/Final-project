package com.lexia.backend.service;

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for synchronizing subscription status with AI quota limits.
 * Handles quota reset cycles and plan-based limit enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionQuotaService {

    private final UserAiQuotaRepository quotaRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuotaLimitsConfig quotaLimitsConfig;

    /**
     * Syncs user's quota limits based on their current subscription.
     * Called when subscription changes (upgrade, downgrade, expire).
     * 
     * @param user         The user whose quota to sync
     * @param subscription The user's current subscription (may be null)
     */
    @Transactional
    public void syncQuotaWithSubscription(User user, Subscription subscription) {
        UserAiQuota quota = quotaRepository.findById(user.getId())
                .orElseGet(() -> createDefaultQuota(user.getId()));

        PlanType newPlanType = determinePlanType(subscription);
        PlanType oldPlanType = quota.getPlanType();

        quota.setPlanType(newPlanType);
        quota.setIsPremium(newPlanType.isPro());

        // Update quota reset date if upgrading
        if (!oldPlanType.isPro() && newPlanType.isPro()) {
            quota.setQuotaResetDate(LocalDate.now().plusMonths(1));
            log.info("User {} upgraded to Pro, quota reset date set to {}",
                    user.getId(), quota.getQuotaResetDate());
        }

        quotaRepository.save(quota);
        log.debug("Synced quota for user {} to plan type {}", user.getId(), newPlanType);
    }

    /**
     * Checks if quota reset is needed and performs reset if required.
     * Should be called before any quota check.
     * 
     * @param userId The user ID to check
     */
    @Transactional
    public void checkAndResetQuotaIfNeeded(UUID userId) {
        UserAiQuota quota = quotaRepository.findById(userId).orElse(null);
        if (quota == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate resetDate = quota.getQuotaResetDate();

        // If no reset date set, initialize it
        if (resetDate == null) {
            quota.setQuotaResetDate(today.plusMonths(1));
            quotaRepository.save(quota);
            return;
        }

        // Check if reset is due
        if (!today.isBefore(resetDate)) {
            resetMonthlyQuota(quota);
            quota.setQuotaResetDate(today.plusMonths(1));
            quotaRepository.save(quota);
            log.info("Reset monthly quota for user {}, next reset: {}",
                    userId, quota.getQuotaResetDate());
        }
    }

    /**
     * Gets quota limits for a user based on their plan.
     * 
     * @param userId The user ID
     * @return QuotaLimits for the user's plan
     */
    public QuotaLimitsConfig.QuotaLimits getQuotaLimitsForUser(UUID userId) {
        UserAiQuota quota = quotaRepository.findById(userId).orElse(null);
        PlanType planType = quota != null ? quota.getPlanType() : PlanType.FREE;
        return quotaLimitsConfig.getForPlan(planType);
    }

    /**
     * Syncs all users' quota with their subscription status.
     * Useful for batch updates or data consistency checks.
     */
    @Transactional
    public void syncAllUserQuotas() {
        log.info("Starting batch quota sync for all users");
        // This would typically be implemented with pagination for large datasets
        // For now, left as a placeholder for future implementation
    }

    // ========== Private Helper Methods ==========

    private UserAiQuota createDefaultQuota(UUID userId) {
        UserAiQuota quota = UserAiQuota.builder()
                .userId(userId)
                .planType(PlanType.FREE)
                .quotaResetDate(LocalDate.now().plusMonths(1))
                .build();
        return quotaRepository.save(quota);
    }

    private PlanType determinePlanType(Subscription subscription) {
        if (subscription == null) {
            return PlanType.FREE;
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            return PlanType.FREE;
        }

        PlanType planType = subscription.getPlanType();
        return planType != null ? planType : PlanType.FREE;
    }

    private void resetMonthlyQuota(UserAiQuota quota) {
        quota.setMonthlyUsed(0);
        quota.setRoleplaySessionsUsed(0);
        quota.setFlashcardDecksUsed(0);
        quota.setGrammarExercisesUsed(0);

        // Also reset feature usage counters
        if (quota.getFeatureUsage() != null) {
            quota.getFeatureUsage().forEach((feature, usage) -> {
                if (usage instanceof java.util.HashMap) {
                    usage.put("monthly", 0);
                }
            });
        }
    }
}
