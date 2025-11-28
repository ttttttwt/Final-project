package com.lexia.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for AI usage log response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIUsageLogDTO {

    private Long id;
    private UUID userId;
    private String userEmail;
    private String featureName;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal cost;
    private Instant createdAt;
}
