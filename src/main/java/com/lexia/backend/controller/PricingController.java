package com.lexia.backend.controller;

import com.lexia.backend.dto.PricingPlanResponse;
import com.lexia.backend.entity.SubscriptionPlan;
import com.lexia.backend.repository.SubscriptionPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Public controller for pricing information.
 * No authentication required - for pricing page display.
 */
@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Public pricing information")
public class PricingController {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get all currently effective subscription plans.
     * Public endpoint - no authentication required.
     * 
     * @return List of active, effective pricing plans
     */
    @GetMapping("/plans")
    @Operation(summary = "Get available pricing plans", description = "Returns all active and currently effective subscription plans for display on pricing page")
    public ResponseEntity<List<PricingPlanResponse>> getPlans() {
        List<SubscriptionPlan> effectivePlans = subscriptionPlanRepository
                .findEffectivePlans(LocalDateTime.now());

        List<PricingPlanResponse> response = effectivePlans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private PricingPlanResponse toResponse(SubscriptionPlan plan) {
        return PricingPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .planType(plan.getPlanType())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .originalPrice(plan.getOriginalPrice())
                .billingInterval(plan.getBillingInterval())
                .isFeatured(plan.getIsFeatured())
                .displayOrder(plan.getDisplayOrder())
                .features(plan.getFeatures())
                .build();
    }
}
