package com.lexia.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 * Entity representing an AI role-play scenario.
 *
 * <p>
 * Table: roleplay_scenarios (V25 migration)
 * </p>
 */
@Entity
@Table(name = "roleplay_scenarios", indexes = {
        @Index(name = "idx_roleplay_scenarios_level_domain", columnList = "cefr_level, domain"),
        @Index(name = "idx_roleplay_scenarios_fallback", columnList = "is_fallback"),
        @Index(name = "idx_roleplay_scenarios_industry", columnList = "industry")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class RolePlayScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(name = "context", nullable = false, columnDefinition = "TEXT")
    private String context;

    @NotBlank
    @Size(max = 255)
    @Column(name = "your_role", nullable = false, length = 255)
    private String yourRole;

    @NotBlank
    @Size(max = 255)
    @Column(name = "ai_role", nullable = false, length = 255)
    private String aiRole;

    @NotBlank
    @Size(max = 2)
    @Column(name = "cefr_level", nullable = false, length = 2)
    private String cefrLevel;

    @NotBlank
    @Size(max = 50)
    @Column(name = "domain", nullable = false, length = 50)
    private String domain;

    @Size(max = 50)
    @Column(name = "industry", length = 50)
    private String industry;

    /**
     * JSON array of objectives.
     * Stored as TEXT for test DB compatibility (PostgreSQL uses JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "objectives", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private List<String> objectives = new ArrayList<>();

    /**
     * JSON array of key vocabulary entries.
     * Example: [{"term":"deadline","definition":"...","example":"..."}]
     * Stored as TEXT for test DB compatibility (PostgreSQL uses JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_vocabulary", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private List<Map<String, Object>> keyVocabulary = new ArrayList<>();

    @NotBlank
    @Column(name = "opening_line", nullable = false, columnDefinition = "TEXT")
    private String openingLine;

    @Min(1)
    @Column(name = "suggested_duration")
    @Builder.Default
    private Integer suggestedDuration = 10;

    @Column(name = "is_fallback")
    @Builder.Default
    private Boolean isFallback = false;

    /**
     * JSON array of suggested prompts for the user.
     * Example: ["Can you give me an update?", "What blockers do you have?"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "suggested_prompts", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> suggestedPrompts = new ArrayList<>();

    /**
     * JSON object with detailed context information.
     * Contains: setting, situation, keyInfo, yourGoal, tips
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_details", columnDefinition = "TEXT")
    private Map<String, Object> contextDetails;

    /**
     * JSON array of agenda/discussion topics.
     * Example: ["Project status", "Timeline review", "Blockers"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "agenda", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> agenda = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (objectives == null) {
            objectives = new ArrayList<>();
        }
        if (keyVocabulary == null) {
            keyVocabulary = new ArrayList<>();
        }
    }

    public static Map<String, Object> vocabItem(String term, String definition, String example) {
        Map<String, Object> item = new HashMap<>();
        item.put("term", term);
        item.put("definition", definition);
        item.put("example", example);
        return item;
    }
}
