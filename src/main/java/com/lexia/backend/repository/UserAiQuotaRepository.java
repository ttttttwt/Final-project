package com.lexia.backend.repository;

import com.lexia.backend.entity.UserAiQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserAiQuota entity.
 * Provides methods for managing user AI quotas and usage tracking.
 */
@Repository
public interface UserAiQuotaRepository extends JpaRepository<UserAiQuota, UUID> {

    /**
     * Finds quota by user ID.
     */
    Optional<UserAiQuota> findByUserId(UUID userId);

    /**
     * Checks if a quota record exists for a user.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Finds all suspended users.
     */
    List<UserAiQuota> findBySuspendedTrue();

    /**
     * Finds all premium users.
     */
    List<UserAiQuota> findByIsPremiumTrue();

    /**
     * Finds quotas that need daily reset (last reset before given time).
     */
    @Query("SELECT q FROM UserAiQuota q WHERE q.lastResetDaily < :resetBefore")
    List<UserAiQuota> findQuotasNeedingDailyReset(@Param("resetBefore") Instant resetBefore);

    /**
     * Finds quotas that need monthly reset (last reset before given time).
     */
    @Query("SELECT q FROM UserAiQuota q WHERE q.lastResetMonthly < :resetBefore")
    List<UserAiQuota> findQuotasNeedingMonthlyReset(@Param("resetBefore") Instant resetBefore);

    /**
     * Resets daily usage for all quotas that need it.
     */
    @Modifying
    @Query(value = """
        UPDATE user_ai_quotas 
        SET daily_used = 0, 
            last_reset_daily = NOW(),
            feature_usage = jsonb_set(
                jsonb_set(
                    jsonb_set(feature_usage, '{roleplay,daily}', '0'),
                    '{grammar,daily}', '0'
                ),
                '{flashcard,daily}', '0'
            ),
            updated_at = NOW()
        WHERE last_reset_daily < :resetBefore
        """, nativeQuery = true)
    int resetDailyUsageForAll(@Param("resetBefore") Instant resetBefore);

    /**
     * Resets monthly usage for all quotas that need it.
     */
    @Modifying
    @Query(value = """
        UPDATE user_ai_quotas 
        SET monthly_used = 0, 
            last_reset_monthly = NOW(),
            feature_usage = jsonb_set(
                jsonb_set(
                    jsonb_set(feature_usage, '{roleplay,monthly}', '0'),
                    '{grammar,monthly}', '0'
                ),
                '{flashcard,monthly}', '0'
            ),
            updated_at = NOW()
        WHERE last_reset_monthly < :resetBefore
        """, nativeQuery = true)
    int resetMonthlyUsageForAll(@Param("resetBefore") Instant resetBefore);

    /**
     * Increments usage for a specific user and feature.
     * Uses native query for atomic update of JSONB fields.
     */
    @Modifying
    @Query(value = """
        UPDATE user_ai_quotas 
        SET daily_used = daily_used + 1,
            monthly_used = monthly_used + 1,
            feature_usage = jsonb_set(
                jsonb_set(
                    feature_usage,
                    ARRAY[:feature, 'daily'],
                    to_jsonb((feature_usage -> :feature ->> 'daily')::int + 1)
                ),
                ARRAY[:feature, 'monthly'],
                to_jsonb((feature_usage -> :feature ->> 'monthly')::int + 1)
            ),
            updated_at = NOW()
        WHERE user_id = :userId
        """, nativeQuery = true)
    int incrementUsage(@Param("userId") UUID userId, @Param("feature") String feature);

    /**
     * Updates suspension status for a user.
     */
    @Modifying
    @Query("UPDATE UserAiQuota q SET q.suspended = :suspended, q.suspensionReason = :reason, q.updatedAt = CURRENT_TIMESTAMP WHERE q.userId = :userId")
    int updateSuspensionStatus(@Param("userId") UUID userId, @Param("suspended") boolean suspended, @Param("reason") String reason);

    /**
     * Updates premium status for a user.
     */
    @Modifying
    @Query("UPDATE UserAiQuota q SET q.isPremium = :isPremium, q.premiumMultiplier = :multiplier, q.updatedAt = CURRENT_TIMESTAMP WHERE q.userId = :userId")
    int updatePremiumStatus(@Param("userId") UUID userId, @Param("isPremium") boolean isPremium, @Param("multiplier") java.math.BigDecimal multiplier);
}
