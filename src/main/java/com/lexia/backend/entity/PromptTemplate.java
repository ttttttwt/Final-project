package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing AI prompt templates.
 * Supports versioning, A/B testing, and performance tracking.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Mustache-style variable placeholders: {{variable}}</li>
 *   <li>Template versioning with unique key+version constraint</li>
 *   <li>A/B testing via traffic_percentage routing</li>
 *   <li>Performance metrics tracking (usage, success rate, response time)</li>
 *   <li>Category-based organization (roleplay, grammar, flashcard)</li>
 * </ul>
 * 
 * @see com.lexia.backend.service.ai.PromptTemplateService
 */
@Entity
@Table(name = "ai_prompt_templates", 
    indexes = {
        @Index(name = "idx_prompt_templates_key", columnList = "template_key"),
        @Index(name = "idx_prompt_templates_category", columnList = "category")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_template_key_version", columnNames = {"template_key", "version"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique identifier for the template (e.g., "roleplay_scenario_v1").
     * Combined with version forms a unique constraint.
     */
    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    /**
     * Version number for this template. Starts at 1.
     */
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Full prompt template text with {{variable}} placeholders.
     */
    @Column(name = "template_text", nullable = false, columnDefinition = "TEXT")
    private String templateText;

    /**
     * List of variable names used in the template.
     * Stored as JSON array: ["cefr_level", "domain", "industry"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> variables = new ArrayList<>();

    /**
     * Human-readable description of what this template does.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Category for organization: roleplay, grammar, flashcard, general, system
     */
    @Column(name = "category", nullable = false, length = 50)
    @Builder.Default
    private String category = "general";

    // ========== Status ==========

    /**
     * Whether this template is active and can be used.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is the default template for its key.
     * Only one template per key should be marked as default.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // ========== A/B Testing ==========

    /**
     * Percentage of traffic to route to this template (0-100).
     * Used for A/B testing when multiple active templates exist for the same key.
     */
    @Column(name = "traffic_percentage")
    @Builder.Default
    private Integer trafficPercentage = 100;

    /**
     * Experiment identifier for A/B testing grouping.
     */
    @Column(name = "experiment_id", length = 100)
    private String experimentId;

    // ========== Performance Metrics ==========

    /**
     * Total number of times this template has been used.
     */
    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * Number of successful uses of this template.
     */
    @Column(name = "success_count")
    @Builder.Default
    private Long successCount = 0L;

    /**
     * Average response time in milliseconds.
     */
    @Column(name = "avg_response_time_ms", precision = 10, scale = 2)
    private BigDecimal avgResponseTimeMs;

    /**
     * Success rate as a percentage (0.00 - 100.00).
     */
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    // ========== Audit Fields ==========

    /**
     * User who created this template.
     */
    @Column(name = "created_by")
    private UUID createdBy;

    /**
     * User who last updated this template.
     */
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ========== Helper Methods ==========

    /**
     * Increments usage count and optionally success count.
     */
    public void incrementUsage(boolean success) {
        this.usageCount = (this.usageCount != null ? this.usageCount : 0) + 1;
        if (success) {
            this.successCount = (this.successCount != null ? this.successCount : 0) + 1;
        }
        updateSuccessRate();
    }

    /**
     * Updates the success rate based on current counts.
     */
    public void updateSuccessRate() {
        if (usageCount != null && usageCount > 0 && successCount != null) {
            this.successRate = BigDecimal.valueOf(successCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(usageCount), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Updates the running average response time.
     */
    public void updateAvgResponseTime(long responseTimeMs) {
        if (avgResponseTimeMs == null || usageCount == null || usageCount == 0) {
            this.avgResponseTimeMs = BigDecimal.valueOf(responseTimeMs);
        } else {
            // Running average: newAvg = oldAvg + (newValue - oldAvg) / count
            BigDecimal diff = BigDecimal.valueOf(responseTimeMs).subtract(avgResponseTimeMs);
            this.avgResponseTimeMs = avgResponseTimeMs.add(
                    diff.divide(BigDecimal.valueOf(usageCount), 2, java.math.RoundingMode.HALF_UP)
            );
        }
    }

    /**
     * Checks if this template has all required variables present.
     */
    public boolean hasAllVariables(java.util.Map<String, Object> providedVars) {
        if (variables == null || variables.isEmpty()) {
            return true;
        }
        if (providedVars == null) {
            return false;
        }
        return providedVars.keySet().containsAll(variables);
    }

    /**
     * Gets a formatted template key with version (e.g., "roleplay_scenario_v1").
     */
    public String getVersionedKey() {
        return templateKey + "_v" + version;
    }

    /**
     * Valid category values.
     */
    public static final String CATEGORY_ROLEPLAY = "roleplay";
    public static final String CATEGORY_GRAMMAR = "grammar";
    public static final String CATEGORY_FLASHCARD = "flashcard";
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_SYSTEM = "system";
}
