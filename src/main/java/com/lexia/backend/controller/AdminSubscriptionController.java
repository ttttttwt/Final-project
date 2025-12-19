package com.lexia.backend.controller;

import com.lexia.backend.dto.admin.PromoCodeDTO;
import com.lexia.backend.dto.admin.SubscriptionPlanDTO;
import com.lexia.backend.service.AdminSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for subscription and promo code management.
 */
@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Subscription Management", description = "Manage subscription plans and promo codes")
public class AdminSubscriptionController {

    private final AdminSubscriptionService subscriptionService;

    // ==================== Subscription Plans ====================

    @GetMapping("/plans")
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @GetMapping("/plans/effective")
    @Operation(summary = "Get currently effective plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getEffectivePlans() {
        return ResponseEntity.ok(subscriptionService.getEffectivePlans());
    }

    @GetMapping("/plans/{id}")
    @Operation(summary = "Get plan by ID")
    public ResponseEntity<SubscriptionPlanDTO> getPlan(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getPlan(id));
    }

    @PostMapping("/plans")
    @Operation(summary = "Create a new subscription plan")
    public ResponseEntity<SubscriptionPlanDTO> createPlan(@RequestBody @Valid CreatePlanRequest request) {
        SubscriptionPlanDTO plan = subscriptionService.createPlan(
                request.name(),
                request.planType(),
                request.description(),
                request.price(),
                request.originalPrice(),
                request.stripePriceId(),
                request.billingInterval(),
                request.isFeatured(),
                request.displayOrder(),
                request.features(),
                request.effectiveFrom(),
                request.effectiveUntil());
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Update a subscription plan")
    public ResponseEntity<SubscriptionPlanDTO> updatePlan(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(subscriptionService.updatePlan(id, updates));
    }

    @PatchMapping("/plans/{id}/active")
    @Operation(summary = "Toggle plan active status")
    public ResponseEntity<Void> togglePlanActive(
            @PathVariable UUID id,
            @RequestParam boolean isActive) {
        subscriptionService.togglePlanActive(id, isActive);
        return ResponseEntity.noContent().build();
    }

    // ==================== Promo Codes ====================

    @GetMapping("/promo-codes")
    @Operation(summary = "Get all promo codes with pagination")
    public ResponseEntity<Page<PromoCodeDTO>> getPromoCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(subscriptionService.getPromoCodes(page, size, search));
    }

    @GetMapping("/promo-codes/valid")
    @Operation(summary = "Get currently valid promo codes")
    public ResponseEntity<List<PromoCodeDTO>> getValidPromoCodes() {
        return ResponseEntity.ok(subscriptionService.getValidPromoCodes());
    }

    @GetMapping("/promo-codes/{id}")
    @Operation(summary = "Get promo code by ID")
    public ResponseEntity<PromoCodeDTO> getPromoCode(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getPromoCode(id));
    }

    @PostMapping("/promo-codes")
    @Operation(summary = "Create a new promo code")
    public ResponseEntity<PromoCodeDTO> createPromoCode(@RequestBody @Valid CreatePromoCodeRequest request) {
        PromoCodeDTO promo = subscriptionService.createPromoCode(
                request.code(),
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.maxDiscountAmount(),
                request.applicablePlanType(),
                request.validFrom(),
                request.validUntil(),
                request.maxUses(),
                request.maxUsesPerUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(promo);
    }

    @PutMapping("/promo-codes/{id}")
    @Operation(summary = "Update a promo code")
    public ResponseEntity<PromoCodeDTO> updatePromoCode(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(subscriptionService.updatePromoCode(id, updates));
    }

    @DeleteMapping("/promo-codes/{id}")
    @Operation(summary = "Deactivate a promo code")
    public ResponseEntity<Void> deactivatePromoCode(@PathVariable UUID id) {
        subscriptionService.deactivatePromoCode(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promo-codes/validate")
    @Operation(summary = "Validate a promo code for a plan")
    public ResponseEntity<PromoCodeDTO> validatePromoCode(
            @RequestParam String code,
            @RequestParam String planType) {
        return ResponseEntity.ok(subscriptionService.validatePromoCode(code, planType));
    }

    // ==================== Statistics ====================

    @GetMapping("/stats")
    @Operation(summary = "Get subscription statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(subscriptionService.getSubscriptionStats());
    }

    // ==================== Request Records ====================

    record CreatePlanRequest(
            String name,
            String planType,
            String description,
            BigDecimal price,
            BigDecimal originalPrice,
            String stripePriceId,
            String billingInterval,
            Boolean isFeatured,
            Integer displayOrder,
            Map<String, Object> features,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil) {
    }

    record CreatePromoCodeRequest(
            String code,
            String description,
            String discountType,
            Integer discountValue,
            Integer maxDiscountAmount,
            String applicablePlanType,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer maxUses,
            Integer maxUsesPerUser) {
    }
}
