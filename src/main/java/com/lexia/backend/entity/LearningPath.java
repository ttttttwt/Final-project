package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a structured learning journey organized by CEFR level.
 * 
 * <p>
 * Learning paths define a curated sequence of courses designed to guide
 * learners
 * from one proficiency level to the next. Each path targets a specific CEFR
 * level
 * and contains an ordered list of courses.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> learning_paths
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see LearningPathCourse
 * @see UserLearningPath
 */
@Entity
@Table(name = "learning_paths")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "learningPathCourses", "userLearningPaths" })
public class LearningPath {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Display name of the learning path (e.g., "Beginner Path (A1)").
     * Must be non-blank and not exceed 100 characters.
     */
    @NotBlank(message = "Learning path name is required")
    @Size(max = 100, message = "Learning path name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of the learning path objectives.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Target Common European Framework of Reference level.
     * Valid values: A1 (beginner) to C2 (proficient)
     */
    @NotBlank(message = "CEFR level is required")
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$", message = "CEFR level must be one of: A1, A2, B1, B2, C1, C2")
    @Column(name = "cefr_level", nullable = false, length = 2)
    private String cefrLevel;

    /**
     * System-provided default path (true) vs user-created custom path (false).
     * Default paths are recommended based on user's CEFR level.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Timestamp when learning path was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when learning path was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Courses associated with this learning path in specific order.
     * Ordered by orderIndex in the join table.
     */
    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<LearningPathCourse> learningPathCourses = new ArrayList<>();

    /**
     * User enrollments in this learning path.
     */
    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserLearningPath> userLearningPaths = new ArrayList<>();

    /**
     * Helper method to add a course to this learning path.
     * Maintains bidirectional relationship.
     * 
     * @param learningPathCourse the course association to add
     */
    public void addCourse(LearningPathCourse learningPathCourse) {
        learningPathCourses.add(learningPathCourse);
        learningPathCourse.setLearningPath(this);
    }

    /**
     * Helper method to remove a course from this learning path.
     * Maintains bidirectional relationship.
     * 
     * @param learningPathCourse the course association to remove
     */
    public void removeCourse(LearningPathCourse learningPathCourse) {
        learningPathCourses.remove(learningPathCourse);
        learningPathCourse.setLearningPath(null);
    }
}
