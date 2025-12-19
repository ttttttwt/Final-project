package com.lexia.backend.repository;

import com.lexia.backend.entity.Subscription;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserId(UUID userId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    // ==================== Analytics Methods ====================

    /**
     * Count subscriptions by status (ignore planType for now as it's an enum)
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);

    /**
     * Count subscriptions by status and plan type
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status AND s.planType = :planType")
    long countByStatusAndPlanType(@Param("status") SubscriptionStatus status, @Param("planType") PlanType planType);

    /**
     * Sum total revenue - returns count as placeholder since no amount field
     * In real scenario, would join with payment/invoice table
     */
    default BigDecimal sumTotalRevenue() {
        return BigDecimal.ZERO; // Placeholder - no amount field in Subscription
    }

    /**
     * Sum revenue after a specific date - placeholder
     */
    default BigDecimal sumRevenueAfter(LocalDateTime date) {
        return BigDecimal.ZERO; // Placeholder
    }

    /**
     * Sum revenue between dates - placeholder
     */
    default BigDecimal sumRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return BigDecimal.ZERO; // Placeholder
    }
}
