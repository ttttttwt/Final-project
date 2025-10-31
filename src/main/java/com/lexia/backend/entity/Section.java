package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a section within a course.
 * 
 * <p>
 * Sections organize lessons into logical groups and are ordered within a
 * course.
 * Each section has a unique order_index within its parent course.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> sections
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * <p>
 * <strong>Unique Constraint:</strong> (course_id, order_index)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Course
 * @see Lesson
 */
@Entity
@Table(name = "sections", uniqueConstraints = @UniqueConstraint(name = "unique_course_section_order", columnNames = {
        "course_id", "order_index" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "course", "lessons" })
public class Section implements Comparable<Section> {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent course to which this section belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Course is required")
    private Course course;

    /**
     * Section title displayed to users.
     */
    @NotBlank(message = "Section title is required")
    @Size(max = 255, message = "Section title must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Order position within the course (0-based).
     * Must be unique per course.
     */
    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /**
     * Timestamp when section was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Lessons belonging to this section.
     * Ordered by orderIndex.
     * Cascade ALL operations to lessons, remove orphaned lessons.
     */
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    /**
     * Helper method to add a lesson to this section.
     * Maintains bidirectional relationship.
     * 
     * @param lesson the lesson to add
     */
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setSection(this);
    }

    /**
     * Helper method to remove a lesson from this section.
     * Maintains bidirectional relationship.
     * 
     * @param lesson the lesson to remove
     */
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setSection(null);
    }

    /**
     * Compare sections by their order index.
     * Used for natural ordering in collections.
     * 
     * @param other the section to compare to
     * @return negative if this comes before other, positive if after, 0 if equal
     */
    @Override
    public int compareTo(Section other) {
        if (this.orderIndex == null && other.orderIndex == null) {
            return 0;
        }
        if (this.orderIndex == null) {
            return 1;
        }
        if (other.orderIndex == null) {
            return -1;
        }
        return this.orderIndex.compareTo(other.orderIndex);
    }
}
