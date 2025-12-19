package com.lexia.backend.dto.admin;

import com.lexia.backend.entity.PromoCode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for promo code management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {
    private UUID id;
    private String code;
    private String description;
    private String discountType;
    private Integer discountValue;
    private Integer maxDiscountAmount;
    private String applicablePlanType;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer maxUses;
    private Integer usedCount;
    private Integer maxUsesPerUser;
    private Boolean isActive;
    private String stripeCouponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCurrentlyValid;

    public static PromoCodeDTO fromEntity(PromoCode promo) {
        return PromoCodeDTO.builder()
                .id(promo.getId())
                .code(promo.getCode())
                .description(promo.getDescription())
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .maxDiscountAmount(promo.getMaxDiscountAmount())
                .applicablePlanType(promo.getApplicablePlanType())
                .validFrom(promo.getValidFrom())
                .validUntil(promo.getValidUntil())
                .maxUses(promo.getMaxUses())
                .usedCount(promo.getUsedCount())
                .maxUsesPerUser(promo.getMaxUsesPerUser())
                .isActive(promo.getIsActive())
                .stripeCouponId(promo.getStripeCouponId())
                .createdAt(promo.getCreatedAt())
                .updatedAt(promo.getUpdatedAt())
                .isCurrentlyValid(promo.isCurrentlyValid())
                .build();
    }
}

/**
 * DTO for creating a new promo code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreatePromoCodeDTO {
    @NotBlank(message = "Promo code is required")
    @Size(min = 3, max = 50, message = "Code must be 3-50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must be uppercase alphanumeric with _ or -")
    private String code;

    @Size(max = 255)
    private String description;

    @NotNull(message = "Discount type is required")
    @Pattern(regexp = "^(PERCENTAGE|FIXED_AMOUNT)$", message = "Invalid discount type")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @Min(value = 1, message = "Discount must be at least 1")
    @Max(value = 100, message = "Percentage discount cannot exceed 100")
    private Integer discountValue;

    private Integer maxDiscountAmount;

    private String applicablePlanType;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    private Integer maxUses;

    @Builder.Default
    private Integer maxUsesPerUser = 1;

    @Builder.Default
    private Boolean isActive = true;
}
