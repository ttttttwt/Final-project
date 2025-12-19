package com.lexia.backend.repository;

import com.lexia.backend.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SubscriptionPlan entity.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    /**
     * Find all active plans ordered by display order
     */
    List<SubscriptionPlan> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find active plans by plan type
     */
    List<SubscriptionPlan> findByPlanTypeAndIsActiveTrue(String planType);

    /**
     * Find currently effective plans
     */
    @Query("""
            SELECT p FROM SubscriptionPlan p
            WHERE p.isActive = true
            AND (p.effectiveFrom IS NULL OR p.effectiveFrom <= :now)
            AND (p.effectiveUntil IS NULL OR p.effectiveUntil >= :now)
            ORDER BY p.displayOrder ASC
            """)
    List<SubscriptionPlan> findEffectivePlans(LocalDateTime now);

    /**
     * Find featured plan
     */
    Optional<SubscriptionPlan> findByIsFeaturedTrueAndIsActiveTrue();

    /**
     * Find by Stripe price ID
     */
    Optional<SubscriptionPlan> findByStripePriceId(String stripePriceId);

    /**
     * Find plan by type for current period
     */
    @Query("""
            SELECT p FROM SubscriptionPlan p
            WHERE p.planType = :planType
            AND p.isActive = true
            AND (p.effectiveFrom IS NULL OR p.effectiveFrom <= :now)
            AND (p.effectiveUntil IS NULL OR p.effectiveUntil >= :now)
            ORDER BY p.createdAt DESC
            """)
    Optional<SubscriptionPlan> findCurrentPlanByType(String planType, LocalDateTime now);
}
