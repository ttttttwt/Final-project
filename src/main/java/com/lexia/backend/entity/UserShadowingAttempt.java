package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a user's shadowing practice attempt.
 * 
 * <p>
 * Stores the user's recording and AI-generated pronunciation feedback
 * for a specific sentence from the custom material's shadowing content.
 * </p>
 * 
 * <p>
 * Table: user_shadowing_attempts (V38 migration)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserCustomMaterial
 */
@Entity
@Table(name = "user_shadowing_attempts", indexes = {
        @Index(name = "idx_shadowing_attempts_material_id", columnList = "material_id"),
        @Index(name = "idx_shadowing_attempts_user_id", columnList = "user_id"),
        @Index(name = "idx_shadowing_attempts_user_material", columnList = "user_id, material_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "feedback" })
public class UserShadowingAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private UserCustomMaterial material;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * ID of the sentence in the material's generated shadowing content.
     * References generatedContent.shadowing[].id
     */
    @NotBlank
    @Column(name = "sentence_id", nullable = false, length = 50)
    private String sentenceId;

    /**
     * URL to the user's recording file in object storage.
     */
    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    /**
     * Pronunciation score (0-100).
     */
    @Min(0)
    @Max(100)
    @Column(name = "score")
    private Integer score;

    /**
     * Detailed pronunciation feedback from AI.
     * Contains phoneme breakdown, intonation notes, stress patterns.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback", columnDefinition = "TEXT")
    private Map<String, Object> feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // ===== Helper Methods =====

    /**
     * Checks if this attempt has been scored.
     * 
     * @return true if score is set
     */
    public boolean isScored() {
        return score != null;
    }

    /**
     * Gets the score category.
     * 
     * @return "excellent" (90+), "good" (70-89), "needs_work" (<70), or null if not
     *         scored
     */
    public String getScoreCategory() {
        if (score == null) {
            return null;
        }
        if (score >= 90) {
            return "excellent";
        } else if (score >= 70) {
            return "good";
        } else {
            return "needs_work";
        }
    }
}
