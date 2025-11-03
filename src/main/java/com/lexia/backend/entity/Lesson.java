package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing a lesson within a section.
 * 
 * <p>
 * Lessons contain type-specific content stored as JSONB in PostgreSQL.
 * The content structure varies based on the lesson type (READING, LISTENING,
 * QUIZ, SPEAKING).
 * Content must be validated against the appropriate schema before saving.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> lessons
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * <p>
 * <strong>Unique Constraint:</strong> (section_id, order_index)
 * </p>
 * <p>
 * <strong>Content Schemas:</strong> See DATABASE-SCHEMA.md section 2.3
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Section
 * @see LessonType
 */
@Entity
@Table(name = "lessons", uniqueConstraints = @UniqueConstraint(name = "unique_section_lesson_order", columnNames = {
        "section_id", "order_index" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "section")
public class Lesson {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent section to which this lesson belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @NotNull(message = "Section is required")
    private Section section;

    /**
     * Lesson title displayed to users.
     */
    @NotBlank(message = "Lesson title is required")
    @Size(max = 255, message = "Lesson title must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Type of lesson: READING, LISTENING, QUIZ, or SPEAKING.
     * Determines the expected structure of the content field.
     */
    @NotNull(message = "Lesson type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, columnDefinition = "lesson_type_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private LessonType lessonType;

    /**
     * JSONB content - schema varies by lesson_type.
     * Must be validated using LessonContentValidator before saving.
     * 
     * <p>
     * Schema references:
     * </p>
     * <ul>
     * <li>READING: DATABASE-SCHEMA.md section 2.3.1</li>
     * <li>LISTENING: DATABASE-SCHEMA.md section 2.3.2</li>
     * <li>QUIZ: DATABASE-SCHEMA.md section 2.3.3</li>
     * <li>SPEAKING: DATABASE-SCHEMA.md section 2.3.4</li>
     * </ul>
     */
    @NotBlank(message = "Lesson content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    /**
     * Order position within the section (0-based).
     * Must be unique per section.
     */
    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /**
     * Estimated time to complete the lesson (1-240 minutes).
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 15;

    /**
     * Timestamp when lesson was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when lesson was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum representing the type of lesson.
     * Maps to the PostgreSQL lesson_type_enum.
     */
    public enum LessonType {
        /**
         * Text-based reading comprehension exercise.
         */
        READING,

        /**
         * Audio-based listening comprehension exercise.
         */
        LISTENING,

        /**
         * Standalone assessment quiz.
         */
        QUIZ,

        /**
         * Speaking practice with AI role-play.
         */
        SPEAKING
    }
}
