package com.lexia.backend.repository;

import com.lexia.backend.entity.Goal;
import com.lexia.backend.entity.Goal.GoalStatus;
import com.lexia.backend.entity.Goal.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link Goal} entity.
 * Provides CRUD operations and custom queries for goal management.
 * 
 * <p>
 * Uses indexes:
 * <ul>
 * <li>idx_goals_user_status - (user_id, status) for active goal queries</li>
 * <li>idx_goals_user_type_date - (user_id, goal_type, start_date) for duplicate checking</li>
 * </ul>
 * </p>
 * 
 * @see Goal
 * @author LEXIA Team
 * @since Sprint 3
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Find all active goals for a specific user.
     * Returns goals with status = ACTIVE, ordered by start_date descending.
     * Uses idx_goals_user_status.
     * 
     * @param userId the user's UUID
     * @return list of active goals, empty if none found
     */
    @Query("SELECT g FROM Goal g WHERE g.userId = :userId AND g.status = 'ACTIVE' ORDER BY g.startDate DESC")
    List<Goal> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Find active goals for a user within a specific date range.
     * Used to find goals for current week/month.
     * Uses idx_goals_user_type_date.
     * 
     * @param userId    the user's UUID
     * @param startDate start of the period
     * @param endDate   end of the period
     * @return list of active goals in the date range
     */
    @Query("SELECT g FROM Goal g WHERE g.userId = :userId AND g.status = 'ACTIVE' " +
           "AND g.startDate >= :startDate AND g.endDate <= :endDate " +
           "ORDER BY g.startDate")
    List<Goal> findActiveByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find goals by user, type, and start date.
     * Used to check if a goal of specific type already exists for a period.
     * Uses idx_goals_user_type_date.
     * 
     * @param userId    the user's UUID
     * @param goalType  the type of goal
     * @param startDate the start date of the period
     * @return optional goal matching the criteria
     */
    Optional<Goal> findByUserIdAndGoalTypeAndStartDate(
            UUID userId,
            GoalType goalType,
            LocalDate startDate);

    /**
     * Find all goals for a user within a specific week (regardless of status).
     * Used for dashboard display to show completed and active goals.
     * 
     * @param userId    the user's UUID
     * @param startDate the Monday of the week
     * @return list of goals for that week
     */
    @Query("SELECT g FROM Goal g WHERE g.userId = :userId AND g.startDate = :startDate ORDER BY g.goalType")
    List<Goal> findByUserIdAndStartDate(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate);

    /**
     * Check if a goal already exists for user, type, and start date.
     * Useful for preventing duplicate goal creation.
     * 
     * @param userId    the user's UUID
     * @param goalType  the type of goal
     * @param startDate the start date of the period
     * @return true if goal exists, false otherwise
     */
    boolean existsByUserIdAndGoalTypeAndStartDate(
            UUID userId,
            GoalType goalType,
            LocalDate startDate);

    /**
     * Find all goals for a user (any status).
     * Orders by created date descending.
     * 
     * @param userId the user's UUID
     * @return list of all goals for the user
     */
    @Query("SELECT g FROM Goal g WHERE g.userId = :userId ORDER BY g.createdAt DESC")
    List<Goal> findByUserId(@Param("userId") UUID userId);

    /**
     * Find goals that need status update (expired but still marked as ACTIVE).
     * Used for batch status cleanup.
     * 
     * @param today current date
     * @return list of goals that should be marked as EXPIRED
     */
    @Query("SELECT g FROM Goal g WHERE g.status = 'ACTIVE' AND g.endDate < :today")
    List<Goal> findExpiredActiveGoals(@Param("today") LocalDate today);

    /**
     * Count active goals for a user.
     * 
     * @param userId the user's UUID
     * @return count of active goals
     */
    long countByUserIdAndStatus(UUID userId, GoalStatus status);
}
