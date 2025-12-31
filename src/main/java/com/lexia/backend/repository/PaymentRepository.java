package com.lexia.backend.repository;

import com.lexia.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    /**
     * Find payments by user ID
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find by Stripe payment ID
     */
    Optional<Payment> findByStripePaymentId(String stripePaymentId);

    /**
     * Find by Stripe invoice ID
     */
    Optional<Payment> findByStripeInvoiceId(String stripeInvoiceId);

    // ==================== Analytics Methods ====================

    /**
     * Sum total revenue from successful payments
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.lexia.backend.entity.Payment.PaymentStatus.SUCCEEDED")
    BigDecimal sumTotalRevenue();

    /**
     * Sum revenue after a specific date
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.lexia.backend.entity.Payment.PaymentStatus.SUCCEEDED AND p.paidAt >= :date")
    BigDecimal sumRevenueAfter(@Param("date") LocalDateTime date);

    /**
     * Sum revenue between dates
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.lexia.backend.entity.Payment.PaymentStatus.SUCCEEDED AND p.paidAt BETWEEN :start AND :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count payments by status
     */
    long countByStatus(Payment.PaymentStatus status);

    /**
     * Count successful payments after date
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = com.lexia.backend.entity.Payment.PaymentStatus.SUCCEEDED AND p.paidAt >= :date")
    long countSucceededAfter(@Param("date") LocalDateTime date);
}
