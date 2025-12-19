package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing subscription plans with dynamic pricing.
 * Allows admin to manage different subscription tiers and their features.
 */
@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_subscription_plans_active", columnList = "is_active"),
        @Index(name = "idx_subscription_plans_plan_type", columnList = "plan_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Display name for the plan (e.g., "Monthly Pro", "Yearly Pro", "Pro Plus")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Internal plan type identifier
     */
    @Column(name = "plan_type", nullable = false, length = 20)
    private String planType; // MONTHLY, YEARLY, PRO_PLUS

    /**
     * Plan description for marketing purposes
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Price in USD
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    /**
     * Original price (for showing discounts)
     */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    /**
     * Stripe Price ID for payment processing
     */
    @Column(name = "stripe_price_id", length = 100)
    private String stripePriceId;

    /**
     * Billing interval: MONTHLY, YEARLY, LIFETIME
     */
    @Column(name = "billing_interval", length = 20)
    @Builder.Default
    private String billingInterval = "MONTHLY";

    /**
     * Whether this plan is currently active and available for purchase
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is the featured/recommended plan
     */
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Display order for UI sorting
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Feature flags and quotas as JSONB
     * Structure: {
     * "roleplaySessionsLimit": 100,
     * "flashcardDecksLimit": 50,
     * "grammarExercisesLimit": 200,
     * "prioritySupport": true,
     * "customAvatars": true,
     * ...
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> features = new HashMap<>();

    /**
     * Date when this price becomes effective (for scheduled price changes)
     */
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    /**
     * Date when this price expires (null = no expiry)
     */
    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if plan is currently effective
     */
    public boolean isCurrentlyEffective() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = effectiveFrom == null || !now.isBefore(effectiveFrom);
        boolean beforeEnd = effectiveUntil == null || !now.isAfter(effectiveUntil);
        return afterStart && beforeEnd;
    }

    /**
     * Get a feature value with default
     */
    @SuppressWarnings("unchecked")
    public <T> T getFeature(String key, T defaultValue) {
        if (features == null || !features.containsKey(key)) {
            return defaultValue;
        }
        try {
            return (T) features.get(key);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
