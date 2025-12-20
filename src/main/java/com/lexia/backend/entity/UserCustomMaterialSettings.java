package com.lexia.backend.entity;

import com.lexia.backend.enums.AiCorrectionMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity storing user preferences for a custom material.
 * 
 * <p>
 * Settings include:
 * </p>
 * <ul>
 * <li>Target options: which content types to generate (VOCABULARY, QUIZ,
 * etc.)</li>
 * <li>AI correction mode: STRICT (immediate) or POLITE (end summary)</li>
 * <li>Style learn mode: include explanations in style transform</li>
 * <li>SRS sync: sync vocabulary to spaced repetition system</li>
 * </ul>
 * 
 * <p>
 * Table: user_custom_material_settings (V38 migration)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserCustomMaterial
 */
@Entity
@Table(name = "user_custom_material_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class UserCustomMaterialSettings {

    /**
     * Available target options for content generation.
     */
    public static final String OPTION_VOCABULARY = "VOCABULARY";
    public static final String OPTION_QUIZ = "QUIZ";
    public static final String OPTION_SUMMARY = "SUMMARY";
    public static final String OPTION_ROLE_PLAY = "ROLE_PLAY";
    public static final String OPTION_SHADOWING = "SHADOWING";
    public static final String OPTION_STYLE_TRANSFORM = "STYLE_TRANSFORM";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private UserCustomMaterial material;

    /**
     * List of target content types to generate.
     * Valid values: VOCABULARY, QUIZ, SUMMARY, ROLE_PLAY, SHADOWING,
     * STYLE_TRANSFORM
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_options", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private List<String> targetOptions = new ArrayList<>(List.of(OPTION_VOCABULARY));

    /**
     * AI correction mode for role-play conversations.
     * STRICT: correct immediately; POLITE: summarize at end (default).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_correction_mode", nullable = false, length = 20)
    @Builder.Default
    private AiCorrectionMode aiCorrectionMode = AiCorrectionMode.POLITE;

    /**
     * Whether to include explanations in style transform output.
     * true = "Learn Mode" with explanations (default).
     * false = "Quick Result" with just the transformed text.
     */
    @Column(name = "style_learn_mode", nullable = false)
    @Builder.Default
    private Boolean styleLearnMode = true;

    /**
     * Whether to sync extracted vocabulary to the SRS system.
     * false by default to avoid cluttering the main vocabulary deck.
     */
    @Column(name = "sync_vocab_to_srs", nullable = false)
    @Builder.Default
    private Boolean syncVocabToSrs = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (targetOptions == null || targetOptions.isEmpty()) {
            targetOptions = new ArrayList<>(List.of(OPTION_VOCABULARY));
        }
    }

    // ===== Helper Methods =====

    /**
     * Checks if vocabulary generation is enabled.
     * 
     * @return true if VOCABULARY is in target options
     */
    public boolean hasVocabulary() {
        return targetOptions != null && targetOptions.contains(OPTION_VOCABULARY);
    }

    /**
     * Checks if quiz generation is enabled.
     * 
     * @return true if QUIZ is in target options
     */
    public boolean hasQuiz() {
        return targetOptions != null && targetOptions.contains(OPTION_QUIZ);
    }

    /**
     * Checks if summary generation is enabled.
     * 
     * @return true if SUMMARY is in target options
     */
    public boolean hasSummary() {
        return targetOptions != null && targetOptions.contains(OPTION_SUMMARY);
    }

    /**
     * Checks if role-play generation is enabled.
     * 
     * @return true if ROLE_PLAY is in target options
     */
    public boolean hasRolePlay() {
        return targetOptions != null && targetOptions.contains(OPTION_ROLE_PLAY);
    }

    /**
     * Checks if shadowing content is enabled.
     * 
     * @return true if SHADOWING is in target options
     */
    public boolean hasShadowing() {
        return targetOptions != null && targetOptions.contains(OPTION_SHADOWING);
    }

    /**
     * Sets target options from a list of strings.
     * 
     * @param options list of option strings
     */
    public void setTargetOptionsFromStrings(List<String> options) {
        if (options == null || options.isEmpty()) {
            this.targetOptions = new ArrayList<>(List.of(OPTION_VOCABULARY));
        } else {
            this.targetOptions = new ArrayList<>(options);
        }
    }
}
