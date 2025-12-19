package com.lexia.backend.dto.admin;

import com.lexia.backend.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for subscription plan management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private UUID id;
    private String name;
    private String planType;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String stripePriceId;
    private String billingInterval;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Map<String, Object> features;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCurrentlyEffective;

    public static SubscriptionPlanDTO fromEntity(SubscriptionPlan plan) {
        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .planType(plan.getPlanType())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .originalPrice(plan.getOriginalPrice())
                .stripePriceId(plan.getStripePriceId())
                .billingInterval(plan.getBillingInterval())
                .isActive(plan.getIsActive())
                .isFeatured(plan.getIsFeatured())
                .displayOrder(plan.getDisplayOrder())
                .features(plan.getFeatures())
                .effectiveFrom(plan.getEffectiveFrom())
                .effectiveUntil(plan.getEffectiveUntil())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .isCurrentlyEffective(plan.isCurrentlyEffective())
                .build();
    }
}

/**
 * DTO for creating/updating subscription plans.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreateSubscriptionPlanDTO {
    private String name;
    private String planType;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String stripePriceId;
    private String billingInterval;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Map<String, Object> features;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;
}
