package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Public DTO for pricing plans - exposed via public API.
 * Contains only the information needed for pricing page display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPlanResponse {
    private UUID id;
    private String name;
    private String planType;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String billingInterval;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Map<String, Object> features;
}
