package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's progress on an individual lesson.
 * Tracks completion status, scores, attempts, and detailed results.
 * 
 * <p>
 * Table: lesson_progress
 * </p>
 * <p>
 * Constraints:
 * <ul>
 * <li>UNIQUE (user_id, lesson_id) - one progress record per user per
 * lesson</li>
 * <li>status CHECK (NOT_STARTED, IN_PROGRESS, COMPLETED)</li>
 * <li>score CHECK (0-100)</li>
 * <li>attempts CHECK (>= 0)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * JSONB result_details structure varies by lesson type:
 * <ul>
 * <li>READING: questions[], totalQuestions, correctAnswers, timeSpent</li>
 * <li>LISTENING: questions[], audioPlayCount, totalQuestions, correctAnswers,
 * timeSpent</li>
 * <li>QUIZ: answers[], totalQuestions, correctAnswers, timeSpent</li>
 * <li>SPEAKING: recordings[], totalPrompts, averagePronunciation,
 * averageFluency, timeSpent</li>
 * </ul>
 * See DATABASE-SCHEMA.md section 2.3 for detailed schemas.
 * </p>
 * 
 * @see Lesson
 * @see User
 * @author LEXIA Team
 * @since Sprint 2
 */
@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_lesson_progress", columnNames = { "user_id", "lesson_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "lesson" })
public class LessonProgress {

    /**
     * Lesson progress status enum.
     * Represents the current state of a user's lesson.
     */
    public enum Status {
        /** Lesson not yet started by the user */
        NOT_STARTED,
        /** Lesson started but not yet completed */
        IN_PROGRESS,
        /** Lesson completed successfully */
        COMPLETED
    }

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to users table (UUID).
     * User who is progressing through the lesson.
     */
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Foreign key to lessons table.
     * The lesson being tracked.
     */
    @NotNull(message = "Lesson is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lesson_progress_lesson"))
    private Lesson lesson;

    /**
     * Current status of the lesson progress.
     * Defaults to NOT_STARTED.
     * 
     * @see Status
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.NOT_STARTED;

    /**
     * Score achieved in the lesson (0-100).
     * NULL if not applicable or not completed.
     * For QUIZ and READING lessons with assessments.
     */
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must be at most 100")
    @Column(name = "score")
    private Integer score;

    /**
     * Number of times user attempted the lesson.
     * Incremented each time lesson is started or retaken.
     * Defaults to 0 on creation.
     */
    @Min(value = 0, message = "Attempts must be non-negative")
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    /**
     * JSONB field storing detailed lesson results.
     * Structure varies by lesson type (see class JavaDoc).
     * 
     * <p>
     * Stored as TEXT for H2 compatibility, JSONB in PostgreSQL.
     * </p>
     * 
     * @see Lesson.LessonType
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_details", columnDefinition = "TEXT")
    private String resultDetails;

    /**
     * Timestamp when lesson was first completed.
     * NULL if lesson is incomplete.
     * Set when status changes to COMPLETED.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Timestamp when progress record was created.
     * Auto-generated on creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when progress record was last updated.
     * Auto-updated on any field change.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if the lesson is completed.
     * 
     * @return true if status is COMPLETED, false otherwise
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    /**
     * Mark lesson as completed with optional score.
     * Sets status to COMPLETED, increments attempts, and records completion
     * timestamp.
     * 
     * @param score         the score achieved (0-100), can be null
     * @param resultDetails the detailed JSONB results
     */
    public void markCompleted(Integer score, String resultDetails) {
        this.status = Status.COMPLETED;
        this.score = score;
        this.resultDetails = resultDetails;
        this.attempts += 1;
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Mark lesson as in progress.
     * Increments attempts counter.
     */
    public void markInProgress() {
        this.status = Status.IN_PROGRESS;
        this.attempts += 1;
    }
}
