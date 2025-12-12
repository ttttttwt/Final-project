package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

/**
 * Entity representing grammar topics available for exercise generation.
 * This is a reference table containing all grammar topics organized by category and CEFR level.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Categorized grammar topics (Tenses, Modals, Conditionals, etc.)</li>
 *   <li>CEFR level mapping for difficulty filtering</li>
 *   <li>Example sentences for each topic</li>
 *   <li>Soft activation/deactivation support</li>
 * </ul>
 * 
 * <p>Database: grammar_topics (V26 migration)</p>
 * 
 * @see GrammarExerciseSet
 * @see com.lexia.backend.repository.GrammarTopicRepository
 */
@Entity
@Table(name = "grammar_topics", indexes = {
    @Index(name = "idx_grammar_topics_category", columnList = "category"),
    @Index(name = "idx_grammar_topics_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarTopic {

    /**
     * Auto-generated serial ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Unique name of the grammar topic.
     * Examples: "Present Simple", "Past Perfect", "First Conditional"
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Category grouping for the topic.
     * Examples: "Tenses", "Modals", "Conditionals", "Articles", "Prepositions"
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * Array of applicable CEFR levels (A1, A2, B1, B2, C1, C2).
     * Stored as PostgreSQL VARCHAR[] array.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cefr_levels", nullable = false, columnDefinition = "VARCHAR(2)[]")
    private String[] cefrLevels;

    /**
     * Detailed description of the grammar topic.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Example sentences demonstrating the grammar point.
     * Stored as PostgreSQL TEXT[] array.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "examples", columnDefinition = "TEXT[]")
    private String[] examples;

    /**
     * Whether this topic is active and available for exercise generation.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp when the topic was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ========== Helper Methods ==========

    /**
     * Checks if this topic is applicable for a specific CEFR level.
     * 
     * @param level CEFR level to check (A1-C2)
     * @return true if the topic applies to the given level
     */
    public boolean hasLevel(String level) {
        if (cefrLevels == null || level == null) {
            return false;
        }
        for (String cefrLevel : cefrLevels) {
            if (cefrLevel.equalsIgnoreCase(level)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the number of CEFR levels this topic covers.
     * 
     * @return count of CEFR levels
     */
    public int getLevelCount() {
        return cefrLevels != null ? cefrLevels.length : 0;
    }

    /**
     * Gets the number of example sentences.
     * 
     * @return count of examples
     */
    public int getExampleCount() {
        return examples != null ? examples.length : 0;
    }

    @Override
    public String toString() {
        return "GrammarTopic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", cefrLevels=" + (cefrLevels != null ? String.join(",", cefrLevels) : "null") +
                ", isActive=" + isActive +
                '}';
    }
}
