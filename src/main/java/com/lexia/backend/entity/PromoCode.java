package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing promotional discount codes.
 * Allows admin to create and manage discount campaigns.
 */
@Entity
@Table(name = "promo_codes", indexes = {
        @Index(name = "idx_promo_codes_code", columnList = "code", unique = true),
        @Index(name = "idx_promo_codes_active", columnList = "is_active"),
        @Index(name = "idx_promo_codes_validity", columnList = "valid_from, valid_until")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique promo code string (e.g., "NEWYEAR2024", "SAVE20")
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Description of the promo (for admin reference)
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Discount type: PERCENTAGE, FIXED_AMOUNT
     */
    @Column(name = "discount_type", nullable = false, length = 20)
    @Builder.Default
    private String discountType = "PERCENTAGE";

    /**
     * Discount value (percentage 0-100 or fixed amount in USD)
     */
    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;

    /**
     * Maximum discount amount (for percentage discounts)
     */
    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    /**
     * Only applicable to specific plan types (null = all plans)
     */
    @Column(name = "applicable_plan_type", length = 20)
    private String applicablePlanType;

    /**
     * Start date of validity
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /**
     * End date of validity
     */
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    /**
     * Maximum number of times this code can be used (null = unlimited)
     */
    @Column(name = "max_uses")
    private Integer maxUses;

    /**
     * How many times this code has been used
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * Maximum uses per user (null = unlimited per user)
     */
    @Column(name = "max_uses_per_user")
    @Builder.Default
    private Integer maxUsesPerUser = 1;

    /**
     * Whether the promo code is active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Stripe Coupon ID (if integrated with Stripe)
     */
    @Column(name = "stripe_coupon_id", length = 100)
    private String stripeCouponId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if promo code is currently valid
     */
    public boolean isCurrentlyValid() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        if (maxUses != null && usedCount >= maxUses) {
            return false;
        }

        return true;
    }

    /**
     * Check if code can be used for a specific plan
     */
    public boolean isApplicableForPlan(String planType) {
        if (applicablePlanType == null || applicablePlanType.isEmpty()) {
            return true; // Applicable to all plans
        }
        return applicablePlanType.equalsIgnoreCase(planType);
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.usedCount = (this.usedCount != null ? this.usedCount : 0) + 1;
    }

    /**
     * Calculate discount amount for a given price
     */
    public double calculateDiscount(double originalPrice) {
        double discount;

        if ("PERCENTAGE".equals(discountType)) {
            discount = originalPrice * (discountValue / 100.0);
            if (maxDiscountAmount != null) {
                discount = Math.min(discount, maxDiscountAmount);
            }
        } else {
            // FIXED_AMOUNT
            discount = discountValue;
        }

        return Math.min(discount, originalPrice); // Can't discount more than price
    }
}
