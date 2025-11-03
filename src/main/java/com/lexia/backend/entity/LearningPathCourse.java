package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;

/**
 * Entity representing the association between a learning path and a course.
 * 
 * <p>
 * This is a join table entity that maintains the many-to-many relationship
 * between learning paths and courses, with an additional orderIndex field
 * to define the sequence of courses within a path.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> learning_path_courses
 * </p>
 * <p>
 * <strong>Composite Primary Key:</strong> (pathId, courseId)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see LearningPath
 * @see Course
 */
@Entity
@Table(name = "learning_path_courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "learningPath", "course" })
@ToString(exclude = { "learningPath", "course" })
@IdClass(LearningPathCourseId.class)
public class LearningPathCourse implements Serializable {

    /**
     * The learning path this association belongs to.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    private LearningPath learningPath;

    /**
     * The course associated with the learning path.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Sequential position of course in learning path (0-based).
     * Must be non-negative.
     */
    @Min(value = 0, message = "Order index must be non-negative")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
