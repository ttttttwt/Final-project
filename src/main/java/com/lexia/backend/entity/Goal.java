package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's learning goal.
 * 
 * <p>
 * Goals track user objectives over a specific time period (weekly, monthly, etc.).
 * Each goal has a target value and current progress that is updated in real-time.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> goals
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * <p>
 * <strong>Indexes:</strong>
 * <ul>
 * <li>idx_goals_user_status - (user_id, status) for active goal queries</li>
 * <li>idx_goals_user_type_date - (user_id, goal_type, start_date) for duplicate checking</li>
 * </ul>
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 3
 */
@Entity
@Table(name = "goals", indexes = {
        @Index(name = "idx_goals_user_status", columnList = "user_id, status"),
        @Index(name = "idx_goals_user_type_date", columnList = "user_id, goal_type, start_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Goal {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to users table (UUID).
     * User who owns this goal.
     */
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Type of goal (e.g., WEEKLY_LESSONS, WEEKLY_STREAK, MONTHLY_COURSES).
     */
    @NotNull(message = "Goal type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    /**
     * Display title of the goal.
     */
    @NotBlank(message = "Goal title is required")
    @Size(max = 100, message = "Goal title must not exceed 100 characters")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * Detailed description of the goal.
     */
    @NotBlank(message = "Goal description is required")
    @Size(max = 500, message = "Goal description must not exceed 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /**
     * Target value to achieve (e.g., 5 lessons, 3 days streak).
     */
    @NotNull(message = "Target value is required")
    @Min(value = 1, message = "Target value must be at least 1")
    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    /**
     * Current progress towards the goal.
     * Updated in real-time when queried.
     */
    @NotNull(message = "Current value is required")
    @Min(value = 0, message = "Current value must be at least 0")
    @Column(name = "current_value", nullable = false)
    private Integer currentValue;

    /**
     * Unit of measurement (e.g., "lessons", "days", "courses").
     */
    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /**
     * Start date of the goal period.
     */
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the goal period.
     */
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Current status of the goal.
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    /**
     * Timestamp when goal was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when goal was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum for goal types.
     */
    public enum GoalType {
        WEEKLY_LESSONS,
        WEEKLY_STREAK,
        MONTHLY_COURSES,
        DAILY_PRACTICE
    }

    /**
     * Enum for goal status.
     */
    public enum GoalStatus {
        ACTIVE,
        COMPLETED,
        EXPIRED
    }

    /**
     * Check if the goal is completed.
     * 
     * @return true if current value meets or exceeds target
     */
    public boolean isCompleted() {
        return currentValue != null && targetValue != null && currentValue >= targetValue;
    }

    /**
     * Check if the goal is expired (past end date and not completed).
     * 
     * @return true if today is after end date and goal not completed
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate) && !isCompleted();
    }

    /**
     * Update the status based on current conditions.
     * Should be called after updating currentValue or when checking goal state.
     */
    public void updateStatus() {
        if (isCompleted()) {
            this.status = GoalStatus.COMPLETED;
        } else if (isExpired()) {
            this.status = GoalStatus.EXPIRED;
        } else {
            this.status = GoalStatus.ACTIVE;
        }
    }
}
