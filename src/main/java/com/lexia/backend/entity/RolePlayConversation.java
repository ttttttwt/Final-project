package com.lexia.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a user's role-play conversation session.
 *
 * <p>
 * Table: roleplay_conversations (V25 migration)
 * </p>
 */
@Entity
@Table(name = "roleplay_conversations", indexes = {
        @Index(name = "idx_roleplay_conversations_user", columnList = "user_id"),
        @Index(name = "idx_roleplay_conversations_status", columnList = "status"),
        @Index(name = "idx_roleplay_conversations_user_status", columnList = "user_id, status"),
        @Index(name = "idx_roleplay_conversations_created", columnList = "created_at"),
        @Index(name = "idx_roleplay_conversations_scenario", columnList = "scenario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "scenario" })
public class RolePlayConversation {

    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ABANDONED = "abandoned";

    public static final String MODE_IMMERSIVE = "immersive";
    public static final String MODE_LEARNING = "learning";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private RolePlayScenario scenario;

    /**
     * JSON array of conversation messages.
     * Stored as TEXT for test DB compatibility (PostgreSQL uses JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "messages", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private List<Map<String, Object>> messages = new ArrayList<>();

    @Column(name = "context_summary", columnDefinition = "TEXT")
    private String contextSummary;

    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_IN_PROGRESS;

    @NotBlank
    @Column(name = "mode", nullable = false, length = 20)
    @Builder.Default
    private String mode = MODE_IMMERSIVE;

    /**
     * JSON object of conversation metrics.
     * Stored as TEXT for test DB compatibility (PostgreSQL uses JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Object> metrics = createDefaultMetrics();

    /**
     * JSON object for end-of-conversation feedback.
     * Stored as TEXT for test DB compatibility (PostgreSQL uses JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback_summary", columnDefinition = "TEXT")
    private Map<String, Object> feedbackSummary;

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
        if (messages == null) {
            messages = new ArrayList<>();
        }
        if (metrics == null) {
            metrics = createDefaultMetrics();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private static Map<String, Object> createDefaultMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messageCount", 0);
        metrics.put("userWordCount", 0);
        metrics.put("aiWordCount", 0);
        metrics.put("grammarScore", null);
        metrics.put("vocabularyScore", null);
        return metrics;
    }
}
