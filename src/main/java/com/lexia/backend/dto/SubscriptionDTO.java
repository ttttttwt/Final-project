package com.lexia.backend.dto;

import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private UUID id;
    private PlanType planType;
    private SubscriptionStatus status;
    private LocalDateTime currentPeriodEnd;
    private String stripeCustomerId;
}
