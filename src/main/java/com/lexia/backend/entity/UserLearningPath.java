package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing user enrollment and progress in a learning path.
 * 
 * <p>
 * This entity tracks when a user starts a learning path, their current position
 * (currentCourse), and when they complete the entire path. A user can be
 * enrolled
 * in multiple learning paths simultaneously.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> user_learning_paths
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * <p>
 * <strong>Unique Constraint:</strong> (userId, pathId) - user can only enroll
 * once per path
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see User
 * @see LearningPath
 * @see Course
 */
@Entity
@Table(name = "user_learning_paths", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_path", columnNames = { "user_id", "path_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "user", "learningPath", "currentCourse" })
public class UserLearningPath {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user enrolled in the learning path.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "UUID")
    private User user;

    /**
     * The learning path the user is enrolled in.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    private LearningPath learningPath;

    /**
     * Current course user is working on in this path.
     * NULL if path completed or not started.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_course_id")
    private Course currentCourse;

    /**
     * Timestamp when user enrolled in the path.
     */
    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    /**
     * Timestamp when user completed all courses in path.
     * NULL if incomplete.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Check if the learning path is completed.
     * 
     * @return true if completedAt is not null, false otherwise
     */
    public boolean isCompleted() {
        return completedAt != null;
    }
}
