package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's enrollment in a course.
 * Tracks overall course progress and completion status.
 * 
 * <p>
 * Table: enrollments
 * </p>
 * <p>
 * Constraints:
 * <ul>
 * <li>UNIQUE (user_id, course_id) - prevents duplicate enrollments</li>
 * <li>progress_percentage CHECK (0-100)</li>
 * </ul>
 * </p>
 * 
 * @see Course
 * @see User
 * @author LEXIA Team
 * @since Sprint 2
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_course_enrollment", columnNames = { "user_id", "course_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "course" })
public class Enrollment {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to users table (UUID).
     * User enrolled in the course.
     */
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Foreign key to courses table.
     * The course the user is enrolled in.
     */
    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_enrollment_course"))
    private Course course;

    /**
     * Timestamp when user enrolled in the course.
     * Auto-generated on creation.
     */
    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    /**
     * Overall course completion percentage (0-100).
     * Calculated from completed lessons in the course.
     * Updated when lessons are completed.
     */
    @Min(value = 0, message = "Progress percentage must be at least 0")
    @Max(value = 100, message = "Progress percentage must be at most 100")
    @Column(name = "progress_percentage", nullable = false)
    @Builder.Default
    private Integer progressPercentage = 0;

    /**
     * Timestamp when user completed all lessons in the course.
     * NULL if course is incomplete.
     * Set when progressPercentage reaches 100.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Check if the course enrollment is completed.
     * 
     * @return true if completed_at is not null, false otherwise
     */
    public boolean isCompleted() {
        return completedAt != null;
    }

    /**
     * Update progress percentage and set completion timestamp if 100%.
     * 
     * @param newProgress the new progress percentage (0-100)
     */
    public void updateProgress(Integer newProgress) {
        this.progressPercentage = newProgress;
        if (newProgress >= 100 && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
