package com.lexia.backend.service;

import com.lexia.backend.dto.admin.PromoCodeDTO;
import com.lexia.backend.dto.admin.SubscriptionPlanDTO;
import com.lexia.backend.entity.PromoCode;
import com.lexia.backend.entity.SubscriptionPlan;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.repository.PromoCodeRepository;
import com.lexia.backend.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for admin subscription and promo code management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminSubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final PromoCodeRepository promoCodeRepository;

    // ==================== Subscription Plans ====================

    /**
     * Get all subscription plans
     */
    public List<SubscriptionPlanDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(SubscriptionPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get active and effective plans
     */
    public List<SubscriptionPlanDTO> getEffectivePlans() {
        return planRepository.findEffectivePlans(LocalDateTime.now()).stream()
                .map(SubscriptionPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get plan by ID
     */
    public SubscriptionPlanDTO getPlan(UUID id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found: " + id));
        return SubscriptionPlanDTO.fromEntity(plan);
    }

    /**
     * Create a new subscription plan
     */
    @Transactional
    public SubscriptionPlanDTO createPlan(
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
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .planType(planType)
                .description(description)
                .price(price)
                .originalPrice(originalPrice)
                .stripePriceId(stripePriceId)
                .billingInterval(billingInterval != null ? billingInterval : "MONTHLY")
                .isActive(true)
                .isFeatured(isFeatured != null ? isFeatured : false)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .features(features != null ? features : Map.of())
                .effectiveFrom(effectiveFrom)
                .effectiveUntil(effectiveUntil)
                .build();

        SubscriptionPlan saved = planRepository.save(plan);
        log.info("Created subscription plan: {} ({})", name, planType);
        return SubscriptionPlanDTO.fromEntity(saved);
    }

    /**
     * Update subscription plan
     */
    @Transactional
    public SubscriptionPlanDTO updatePlan(UUID id, Map<String, Object> updates) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found: " + id));

        if (updates.containsKey("name")) {
            plan.setName((String) updates.get("name"));
        }
        if (updates.containsKey("description")) {
            plan.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("price")) {
            plan.setPrice(new BigDecimal(updates.get("price").toString()));
        }
        if (updates.containsKey("originalPrice")) {
            plan.setOriginalPrice(new BigDecimal(updates.get("originalPrice").toString()));
        }
        if (updates.containsKey("stripePriceId")) {
            plan.setStripePriceId((String) updates.get("stripePriceId"));
        }
        if (updates.containsKey("isActive")) {
            plan.setIsActive((Boolean) updates.get("isActive"));
        }
        if (updates.containsKey("isFeatured")) {
            plan.setIsFeatured((Boolean) updates.get("isFeatured"));
        }
        if (updates.containsKey("displayOrder")) {
            plan.setDisplayOrder(((Number) updates.get("displayOrder")).intValue());
        }
        if (updates.containsKey("features")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> features = (Map<String, Object>) updates.get("features");
            plan.setFeatures(features);
        }
        if (updates.containsKey("effectiveFrom")) {
            plan.setEffectiveFrom(LocalDateTime.parse((String) updates.get("effectiveFrom")));
        }
        if (updates.containsKey("effectiveUntil")) {
            plan.setEffectiveUntil(LocalDateTime.parse((String) updates.get("effectiveUntil")));
        }

        SubscriptionPlan saved = planRepository.save(plan);
        log.info("Updated subscription plan: {}", id);
        return SubscriptionPlanDTO.fromEntity(saved);
    }

    /**
     * Toggle plan active status
     */
    @Transactional
    public void togglePlanActive(UUID id, boolean isActive) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found: " + id));
        plan.setIsActive(isActive);
        planRepository.save(plan);
        log.info("Toggled plan {} active status to {}", id, isActive);
    }

    // ==================== Promo Codes ====================

    /**
     * Get all promo codes with pagination
     */
    public Page<PromoCodeDTO> getPromoCodes(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PromoCode> promoCodes;
        if (search != null && !search.isBlank()) {
            promoCodes = promoCodeRepository.searchPromoCodes(search, pageable);
        } else {
            promoCodes = promoCodeRepository.findAll(pageable);
        }

        return promoCodes.map(PromoCodeDTO::fromEntity);
    }

    /**
     * Get valid promo codes
     */
    public List<PromoCodeDTO> getValidPromoCodes() {
        return promoCodeRepository.findValidPromoCodes(LocalDateTime.now()).stream()
                .map(PromoCodeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get promo code by ID
     */
    public PromoCodeDTO getPromoCode(UUID id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + id));
        return PromoCodeDTO.fromEntity(promo);
    }

    /**
     * Create a new promo code
     */
    @Transactional
    public PromoCodeDTO createPromoCode(
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
        // Check if code already exists
        if (promoCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Promo code already exists: " + code);
        }

        PromoCode promo = PromoCode.builder()
                .code(code.toUpperCase())
                .description(description)
                .discountType(discountType)
                .discountValue(discountValue)
                .maxDiscountAmount(maxDiscountAmount)
                .applicablePlanType(applicablePlanType)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .maxUses(maxUses)
                .maxUsesPerUser(maxUsesPerUser != null ? maxUsesPerUser : 1)
                .isActive(true)
                .build();

        PromoCode saved = promoCodeRepository.save(promo);
        log.info("Created promo code: {} with {}% discount", code, discountValue);
        return PromoCodeDTO.fromEntity(saved);
    }

    /**
     * Update promo code
     */
    @Transactional
    public PromoCodeDTO updatePromoCode(UUID id, Map<String, Object> updates) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + id));

        if (updates.containsKey("description")) {
            promo.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("discountValue")) {
            promo.setDiscountValue(((Number) updates.get("discountValue")).intValue());
        }
        if (updates.containsKey("maxDiscountAmount")) {
            promo.setMaxDiscountAmount(((Number) updates.get("maxDiscountAmount")).intValue());
        }
        if (updates.containsKey("validFrom")) {
            promo.setValidFrom(LocalDateTime.parse((String) updates.get("validFrom")));
        }
        if (updates.containsKey("validUntil")) {
            promo.setValidUntil(LocalDateTime.parse((String) updates.get("validUntil")));
        }
        if (updates.containsKey("maxUses")) {
            promo.setMaxUses(((Number) updates.get("maxUses")).intValue());
        }
        if (updates.containsKey("isActive")) {
            promo.setIsActive((Boolean) updates.get("isActive"));
        }

        PromoCode saved = promoCodeRepository.save(promo);
        log.info("Updated promo code: {}", id);
        return PromoCodeDTO.fromEntity(saved);
    }

    /**
     * Deactivate promo code
     */
    @Transactional
    public void deactivatePromoCode(UUID id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + id));
        promo.setIsActive(false);
        promoCodeRepository.save(promo);
        log.info("Deactivated promo code: {}", promo.getCode());
    }

    /**
     * Validate promo code for a specific plan
     */
    public PromoCodeDTO validatePromoCode(String code, String planType) {
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + code));

        if (!promo.isCurrentlyValid()) {
            throw new IllegalArgumentException("Promo code is not valid or has expired");
        }

        if (!promo.isApplicableForPlan(planType)) {
            throw new IllegalArgumentException("Promo code is not applicable for this plan");
        }

        return PromoCodeDTO.fromEntity(promo);
    }

    /**
     * Get subscription statistics
     */
    public Map<String, Object> getSubscriptionStats() {
        long totalPlans = planRepository.count();
        long activePlans = planRepository.findByIsActiveTrueOrderByDisplayOrderAsc().size();
        long totalPromoCodes = promoCodeRepository.count();
        long activePromoCodes = promoCodeRepository.countByIsActiveTrue();
        List<PromoCodeDTO> validPromoCodes = getValidPromoCodes();

        return Map.of(
                "totalPlans", totalPlans,
                "activePlans", activePlans,
                "totalPromoCodes", totalPromoCodes,
                "activePromoCodes", activePromoCodes,
                "validPromoCodes", validPromoCodes.size());
    }
}
