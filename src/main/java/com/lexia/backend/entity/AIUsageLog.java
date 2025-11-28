package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing AI usage logs.
 * Tracks API usage, costs, and frequency for AI features.
 */
@Entity
@Table(name = "ai_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;

    @Column(name = "input_tokens")
    @Builder.Default
    private Integer inputTokens = 0;

    @Column(name = "output_tokens")
    @Builder.Default
    private Integer outputTokens = 0;

    @Column(name = "cost", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
