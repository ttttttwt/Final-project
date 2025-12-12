package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing AI-generated or fallback grammar exercise sets.
 * Stores exercises as JSONB content for flexible exercise structures.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>CEFR-level appropriate exercise generation</li>
 *   <li>Flexible JSONB content structure for various exercise types</li>
 *   <li>Support for MCQ, fill-in-blank, transformation, error-correction</li>
 *   <li>Fallback content support when AI is unavailable</li>
 *   <li>Time-limited exercise sessions</li>
 * </ul>
 * 
 * <p>Database: grammar_exercise_sets (V26 migration)</p>
 * 
 * <p>JSONB Content Structure:</p>
 * <pre>
 * {
 *   "explanation": {
 *     "rule": "Description of the grammar rule",
 *     "examples": ["Example 1", "Example 2"],
 *     "commonMistakes": ["Mistake 1", "Mistake 2"]
 *   },
 *   "exercises": [
 *     {
 *       "type": "multiple_choice|fill_blank|transformation|error_correction",
 *       "instruction": "Instructions for the exercise",
 *       "question": "Question text",
 *       "options": ["A", "B", "C", "D"],  // for MCQ
 *       "blanks": ["___", "___"],         // for fill_blank
 *       "correctAnswer": "A",
 *       "hint": "Optional hint",
 *       "explanation": "Why this is correct",
 *       "difficulty": "easy|medium|hard"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @see GrammarTopic
 * @see UserGrammarProgress
 * @see com.lexia.backend.repository.GrammarExerciseSetRepository
 */
@Entity
@Table(name = "grammar_exercise_sets", indexes = {
    @Index(name = "idx_grammar_exercise_sets_level", columnList = "cefr_level"),
    @Index(name = "idx_grammar_exercise_sets_point", columnList = "grammar_point"),
    @Index(name = "idx_grammar_exercise_sets_user", columnList = "user_id"),
    @Index(name = "idx_grammar_exercise_sets_fallback", columnList = "is_fallback"),
    @Index(name = "idx_grammar_exercise_sets_level_point", columnList = "cefr_level, grammar_point"),
    @Index(name = "idx_grammar_exercise_sets_topic", columnList = "topic_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarExerciseSet {

    /**
     * Unique identifier for the exercise set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Optional reference to the user who requested the exercise set.
     * Null for fallback/system-generated content.
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Reference to the grammar topic.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private GrammarTopic topic;

    /**
     * Grammar point being tested (e.g., "Present Simple", "Past Perfect").
     */
    @Column(name = "grammar_point", nullable = false, length = 100)
    private String grammarPoint;

    /**
     * CEFR level of the exercises (A1, A2, B1, B2, C1, C2).
     */
    @Column(name = "cefr_level", nullable = false, length = 2)
    private String cefrLevel;

    /**
     * Contextual theme for the exercises (e.g., "workplace", "travel", "technology").
     */
    @Column(name = "theme", length = 50)
    private String theme;

    /**
     * JSONB content containing explanation and exercises.
     * Structure defined in class documentation.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> content;

    /**
     * Number of exercises in the set.
     */
    @Column(name = "exercise_count")
    @Builder.Default
    private Integer exerciseCount = 0;

    /**
     * Optional time limit in seconds (default 10 minutes).
     */
    @Column(name = "time_limit_seconds")
    @Builder.Default
    private Integer timeLimitSeconds = 600;

    /**
     * Whether this is pre-seeded fallback content.
     */
    @Column(name = "is_fallback", nullable = false)
    @Builder.Default
    private Boolean isFallback = false;

    /**
     * Timestamp when the exercise set was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ========== Helper Methods ==========

    /**
     * Checks if the exercise set has a user association.
     * 
     * @return true if userId is set
     */
    public boolean hasUser() {
        return userId != null;
    }

    /**
     * Checks if the exercise set has a topic association.
     * 
     * @return true if topic is set
     */
    public boolean hasTopic() {
        return topic != null;
    }

    /**
     * Gets the topic ID if available.
     * 
     * @return topic ID or null
     */
    public Integer getTopicId() {
        return topic != null ? topic.getId() : null;
    }

    /**
     * Gets the topic name if available.
     * 
     * @return topic name or grammar point as fallback
     */
    public String getTopicName() {
        return topic != null ? topic.getName() : grammarPoint;
    }

    /**
     * Checks if this exercise set has a time limit.
     * 
     * @return true if time limit is set and greater than 0
     */
    public boolean hasTimeLimit() {
        return timeLimitSeconds != null && timeLimitSeconds > 0;
    }

    /**
     * Gets time limit in minutes.
     * 
     * @return time limit in minutes, or 0 if not set
     */
    public int getTimeLimitMinutes() {
        return hasTimeLimit() ? timeLimitSeconds / 60 : 0;
    }

    @Override
    public String toString() {
        return "GrammarExerciseSet{" +
                "id=" + id +
                ", grammarPoint='" + grammarPoint + '\'' +
                ", cefrLevel='" + cefrLevel + '\'' +
                ", theme='" + theme + '\'' +
                ", exerciseCount=" + exerciseCount +
                ", isFallback=" + isFallback +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GrammarExerciseSet that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
