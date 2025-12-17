package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing user AI quota configuration and usage tracking.
 * Manages daily/monthly limits and feature-specific quotas.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Global daily/monthly request limits</li>
 *   <li>Per-feature quotas (roleplay, grammar, flashcard)</li>
 *   <li>Premium tier support with multipliers</li>
 *   <li>Suspension capability for abuse prevention</li>
 *   <li>Automatic quota reset tracking</li>
 * </ul>
 * 
 * @see com.lexia.backend.service.ai.AiUsageTracker
 */
@Entity
@Table(name = "user_ai_quotas", indexes = {
    @Index(name = "idx_user_ai_quotas_suspended", columnList = "suspended"),
    @Index(name = "idx_user_ai_quotas_premium", columnList = "is_premium")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAiQuota {

    /**
     * User ID - also serves as primary key (1:1 with users table).
     */
    @Id
    @Column(name = "user_id")
    private UUID userId;

    // ========== Daily Limits ==========

    /**
     * Maximum AI requests allowed per day (global).
     */
    @Column(name = "daily_limit", nullable = false)
    @Builder.Default
    private Integer dailyLimit = 50;

    /**
     * Number of AI requests used today.
     */
    @Column(name = "daily_used", nullable = false)
    @Builder.Default
    private Integer dailyUsed = 0;

    /**
     * Timestamp of last daily quota reset.
     */
    @Column(name = "last_reset_daily", nullable = false)
    @Builder.Default
    private Instant lastResetDaily = Instant.now();

    // ========== Monthly Limits ==========

    /**
     * Maximum AI requests allowed per month (global).
     */
    @Column(name = "monthly_limit", nullable = false)
    @Builder.Default
    private Integer monthlyLimit = 500;

    /**
     * Number of AI requests used this month.
     */
    @Column(name = "monthly_used", nullable = false)
    @Builder.Default
    private Integer monthlyUsed = 0;

    /**
     * Timestamp of last monthly quota reset.
     */
    @Column(name = "last_reset_monthly", nullable = false)
    @Builder.Default
    private Instant lastResetMonthly = Instant.now();

    // ========== Premium Status ==========

    /**
     * Whether user has premium subscription.
     */
    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    /**
     * Multiplier applied to limits for premium users (1.00-10.00).
     */
    @Column(name = "premium_multiplier", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal premiumMultiplier = BigDecimal.ONE;

    // ========== Suspension Status ==========

    /**
     * If true, user cannot use AI features.
     */
    @Column(name = "suspended", nullable = false)
    @Builder.Default
    private Boolean suspended = false;

    /**
     * Reason for suspension (if suspended).
     */
    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    private String suspensionReason;

    // ========== Feature-Specific Limits ==========

    /**
     * Per-feature limits as JSONB.
     * Structure: {"roleplay": {"daily": 20, "monthly": 200}, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_limits", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Map<String, Integer>> featureLimits = createDefaultFeatureLimits();

    /**
     * Per-feature usage counters as JSONB.
     * Structure: {"roleplay": {"daily": 5, "monthly": 45}, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_usage", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Map<String, Integer>> featureUsage = createDefaultFeatureUsage();

    // ========== Timestamps ==========

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ========== Helper Methods ==========

    /**
     * Creates default feature limits structure.
     */
    private static Map<String, Map<String, Integer>> createDefaultFeatureLimits() {
        Map<String, Map<String, Integer>> limits = new HashMap<>();
        limits.put("roleplay", Map.of("daily", 20, "monthly", 200));
        limits.put("grammar", Map.of("daily", 30, "monthly", 300));
        limits.put("flashcard", Map.of("daily", 50, "monthly", 500));
        return limits;
    }

    /**
     * Creates default feature usage structure (all zeros).
     */
    private static Map<String, Map<String, Integer>> createDefaultFeatureUsage() {
        Map<String, Map<String, Integer>> usage = new HashMap<>();
        usage.put("roleplay", new HashMap<>(Map.of("daily", 0, "monthly", 0)));
        usage.put("grammar", new HashMap<>(Map.of("daily", 0, "monthly", 0)));
        usage.put("flashcard", new HashMap<>(Map.of("daily", 0, "monthly", 0)));
        return usage;
    }

    /**
     * Gets the daily limit for a specific feature, applying premium multiplier if applicable.
     */
    public int getFeatureDailyLimit(String contentType) {
        if (featureLimits == null) return dailyLimit;
        Map<String, Integer> limits = featureLimits.get(contentType);
        if (limits == null) {
            return dailyLimit; // Fall back to global limit
        }
        int baseLimit = limits.getOrDefault("daily", dailyLimit);
        if (Boolean.TRUE.equals(isPremium) && premiumMultiplier != null) {
            return (int) (baseLimit * premiumMultiplier.doubleValue());
        }
        return baseLimit;
    }

    /**
     * Gets the monthly limit for a specific feature, applying premium multiplier if applicable.
     */
    public int getFeatureMonthlyLimit(String contentType) {
        if (featureLimits == null) return monthlyLimit;
        Map<String, Integer> limits = featureLimits.get(contentType);
        if (limits == null) {
            return monthlyLimit; // Fall back to global limit
        }
        int baseLimit = limits.getOrDefault("monthly", monthlyLimit);
        if (Boolean.TRUE.equals(isPremium) && premiumMultiplier != null) {
            return (int) (baseLimit * premiumMultiplier.doubleValue());
        }
        return baseLimit;
    }

    /**
     * Gets the daily usage count for a specific feature.
     */
    public int getFeatureDailyUsage(String contentType) {
        if (featureUsage == null) return 0;
        Map<String, Integer> usage = featureUsage.get(contentType);
        if (usage == null) {
            return 0;
        }
        return usage.getOrDefault("daily", 0);
    }

    /**
     * Gets the monthly usage count for a specific feature.
     */
    public int getFeatureMonthlyUsage(String contentType) {
        if (featureUsage == null) return 0;
        Map<String, Integer> usage = featureUsage.get(contentType);
        if (usage == null) {
            return 0;
        }
        return usage.getOrDefault("monthly", 0);
    }

    /**
     * Increments the usage count for a specific feature.
     * Also increments global daily/monthly used counts.
     */
    public void incrementFeatureUsage(String contentType) {
        // Increment global usage
        dailyUsed = (dailyUsed != null ? dailyUsed : 0) + 1;
        monthlyUsed = (monthlyUsed != null ? monthlyUsed : 0) + 1;

        // Increment feature-specific usage
        if (featureUsage == null) {
            featureUsage = createDefaultFeatureUsage();
        }
        
        Map<String, Integer> usage = featureUsage.get(contentType);
        if (usage == null) {
            usage = new HashMap<>(Map.of("daily", 0, "monthly", 0));
            featureUsage.put(contentType, usage);
        }
        
        // Create mutable copy if needed
        if (!(usage instanceof HashMap)) {
            usage = new HashMap<>(usage);
            featureUsage.put(contentType, usage);
        }
        
        usage.put("daily", usage.getOrDefault("daily", 0) + 1);
        usage.put("monthly", usage.getOrDefault("monthly", 0) + 1);
    }

    /**
     * Checks if the user has exceeded their daily quota for a feature.
     */
    public boolean isFeatureDailyQuotaExceeded(String contentType) {
        return getFeatureDailyUsage(contentType) >= getFeatureDailyLimit(contentType);
    }

    /**
     * Checks if the user has exceeded their monthly quota for a feature.
     */
    public boolean isFeatureMonthlyQuotaExceeded(String contentType) {
        return getFeatureMonthlyUsage(contentType) >= getFeatureMonthlyLimit(contentType);
    }

    /**
     * Gets remaining daily quota for a feature.
     */
    public int getFeatureRemainingDailyQuota(String contentType) {
        return Math.max(0, getFeatureDailyLimit(contentType) - getFeatureDailyUsage(contentType));
    }

    /**
     * Gets remaining monthly quota for a feature.
     */
    public int getFeatureRemainingMonthlyQuota(String contentType) {
        return Math.max(0, getFeatureMonthlyLimit(contentType) - getFeatureMonthlyUsage(contentType));
    }

    /**
     * Resets daily usage counters.
     */
    public void resetDailyUsage() {
        dailyUsed = 0;
        lastResetDaily = Instant.now();
        
        if (featureUsage != null) {
            for (Map<String, Integer> usage : featureUsage.values()) {
                if (usage instanceof HashMap) {
                    usage.put("daily", 0);
                }
            }
        }
    }

    /**
     * Resets monthly usage counters.
     */
    public void resetMonthlyUsage() {
        monthlyUsed = 0;
        lastResetMonthly = Instant.now();
        
        if (featureUsage != null) {
            for (Map<String, Integer> usage : featureUsage.values()) {
                if (usage instanceof HashMap) {
                    usage.put("monthly", 0);
                }
            }
        }
    }

    /**
     * Creates a new quota record for a user with default limits.
     */
    public static UserAiQuota createForUser(UUID userId) {
        return UserAiQuota.builder()
                .userId(userId)
                .build();
    }
}
