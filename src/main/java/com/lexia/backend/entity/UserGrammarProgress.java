package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing user progress and scores for grammar exercise sets.
 * Tracks individual answers, scores, and completion status.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Per-user exercise completion tracking</li>
 *   <li>Detailed answer history with correctness and timing</li>
 *   <li>Score calculation with percentage</li>
 *   <li>Time tracking for performance analysis</li>
 *   <li>Unique constraint on user + exercise set combination</li>
 * </ul>
 * 
 * <p>Database: user_grammar_progress (V26 migration)</p>
 * 
 * <p>JSONB Answers Structure:</p>
 * <pre>
 * [
 *   {
 *     "questionIndex": 0,
 *     "answer": "A",
 *     "correct": true,
 *     "timeMs": 5000
 *   },
 *   {
 *     "questionIndex": 1,
 *     "answer": "B",
 *     "correct": false,
 *     "timeMs": 8000
 *   }
 * ]
 * </pre>
 * 
 * @see GrammarExerciseSet
 * @see com.lexia.backend.repository.UserGrammarProgressRepository
 */
@Entity
@Table(name = "user_grammar_progress", 
    indexes = {
        @Index(name = "idx_user_grammar_progress_user", columnList = "user_id"),
        @Index(name = "idx_user_grammar_progress_set", columnList = "exercise_set_id"),
        @Index(name = "idx_user_grammar_progress_completed", columnList = "completed_at"),
        @Index(name = "idx_user_grammar_progress_user_completed", columnList = "user_id, completed_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_grammar_progress_user_set", columnNames = {"user_id", "exercise_set_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGrammarProgress {

    /**
     * Auto-generated serial ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user who completed the exercises.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Reference to the exercise set.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_set_id", nullable = false)
    private GrammarExerciseSet exerciseSet;

    /**
     * JSONB array containing user answers with details.
     * Structure defined in class documentation.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> answers = new ArrayList<>();

    /**
     * Number of correct answers.
     */
    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    /**
     * Maximum possible score (total questions).
     */
    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Integer maxScore = 0;

    /**
     * Score as percentage (0.00-100.00).
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentage = BigDecimal.ZERO;

    /**
     * Total time spent on the exercise set in seconds.
     */
    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    /**
     * Timestamp when the exercise set was completed.
     * Null if not yet completed.
     */
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Timestamp when the progress record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ========== Helper Methods ==========

    /**
     * Checks if the exercise set has been completed.
     * 
     * @return true if completed_at is set
     */
    public boolean isCompleted() {
        return completedAt != null;
    }

    /**
     * Gets the exercise set ID.
     * 
     * @return exercise set UUID or null
     */
    public UUID getExerciseSetId() {
        return exerciseSet != null ? exerciseSet.getId() : null;
    }

    /**
     * Calculates and updates the percentage based on score and maxScore.
     */
    public void calculatePercentage() {
        if (maxScore != null && maxScore > 0 && score != null) {
            double pct = (score * 100.0) / maxScore;
            this.percentage = BigDecimal.valueOf(pct).setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.percentage = BigDecimal.ZERO;
        }
    }

    /**
     * Gets the number of answered questions.
     * 
     * @return count of answers
     */
    public int getAnswerCount() {
        return answers != null ? answers.size() : 0;
    }

    /**
     * Gets time spent in minutes.
     * 
     * @return time in minutes
     */
    public int getTimeSpentMinutes() {
        return timeSpentSeconds != null ? timeSpentSeconds / 60 : 0;
    }

    /**
     * Checks if the user passed (typically >= 70%).
     * 
     * @param passingPercentage minimum percentage to pass
     * @return true if percentage >= passingPercentage
     */
    public boolean isPassed(double passingPercentage) {
        return percentage != null && percentage.doubleValue() >= passingPercentage;
    }

    /**
     * Checks if the user passed with default threshold (70%).
     * 
     * @return true if percentage >= 70%
     */
    public boolean isPassed() {
        return isPassed(70.0);
    }

    /**
     * Marks the progress as completed with current timestamp.
     */
    public void markCompleted() {
        this.completedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "UserGrammarProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", exerciseSetId=" + getExerciseSetId() +
                ", score=" + score + "/" + maxScore +
                ", percentage=" + percentage +
                ", completed=" + isCompleted() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGrammarProgress that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
