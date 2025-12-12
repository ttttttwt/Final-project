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
 * Entity representing AI usage logs.
 * Tracks API usage, costs, response times, and metadata for AI features.
 * 
 * <p>Enhanced in Sprint 5 (V23 migration) to support comprehensive AI tracking:</p>
 * <ul>
 *   <li>Content type classification (roleplay, grammar, flashcard)</li>
 *   <li>Model identification for cost tracking</li>
 *   <li>Response time metrics</li>
 *   <li>Success/failure tracking with error messages</li>
 *   <li>Flexible metadata storage via JSONB</li>
 *   <li>Prompt version tracking for A/B testing</li>
 * </ul>
 * 
 * @see com.lexia.backend.service.ai.AiUsageTracker
 */
@Entity
@Table(name = "ai_usage_logs", indexes = {
    @Index(name = "idx_ai_usage_logs_content_type", columnList = "content_type"),
    @Index(name = "idx_ai_usage_logs_success", columnList = "success"),
    @Index(name = "idx_ai_usage_logs_model_id", columnList = "model_id"),
    @Index(name = "idx_ai_usage_logs_user_date", columnList = "user_id, created_at")
})
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

    /**
     * Legacy feature name field. Use contentType for new integrations.
     * @deprecated Use {@link #contentType} instead
     */
    @Deprecated
    @Column(name = "feature_name", length = 100)
    private String featureName;

    /**
     * Standardized content type: roleplay, grammar, flashcard, content_generation, 
     * pronunciation_feedback, magic_flashcard, grammar_sandbox
     */
    @Column(name = "content_type", length = 50)
    private String contentType;

    /**
     * AI model identifier (e.g., gemini-1.5-flash, gemini-1.5-pro, gemini-2.0-flash-exp)
     */
    @Column(name = "model_id", length = 100)
    private String modelId;

    @Column(name = "input_tokens")
    @Builder.Default
    private Integer inputTokens = 0;

    @Column(name = "output_tokens")
    @Builder.Default
    private Integer outputTokens = 0;

    /**
     * Total tokens - computed column in DB (input_tokens + output_tokens).
     * Note: This is read-only; the database calculates it automatically.
     */
    @Column(name = "total_tokens", insertable = false, updatable = false)
    private Integer totalTokens;

    /**
     * Estimated cost in USD based on token usage and model pricing.
     * Renamed from 'cost' to 'estimated_cost_usd' in V23.
     */
    @Column(name = "estimated_cost_usd", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal estimatedCostUsd = BigDecimal.ZERO;

    /**
     * API response time in milliseconds.
     */
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    /**
     * Whether the AI request completed successfully.
     */
    @Column(name = "success")
    @Builder.Default
    private Boolean success = true;

    /**
     * Error message if the request failed.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Flexible metadata storage (JSONB).
     * May contain: cefr_level, domain, scenario_id, conversation_id, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> requestMetadata = new HashMap<>();

    /**
     * Version of the prompt template used for this request.
     * Links to ai_prompt_templates.template_key + version.
     */
    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        // Sync featureName and contentType for backward compatibility
        if (contentType != null && featureName == null) {
            featureName = contentType;
        } else if (featureName != null && contentType == null) {
            contentType = featureName.toLowerCase();
        }
    }

    /**
     * Calculate total tokens (for use before entity is persisted).
     * After persistence, use the DB-computed totalTokens field.
     */
    public int calculateTotalTokens() {
        return (inputTokens != null ? inputTokens : 0) + (outputTokens != null ? outputTokens : 0);
    }
}
