package com.lexia.backend.repository;

import com.lexia.backend.entity.AdminActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AdminActivityLog entity.
 * Provides methods to query admin activity logs for dashboard display and Log
 * Management feature.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Repository
public interface AdminActivityLogRepository
                extends JpaRepository<AdminActivityLog, UUID>, JpaSpecificationExecutor<AdminActivityLog> {

        /**
         * Find recent activity logs ordered by creation time.
         * Used for dashboard recent activity display.
         *
         * @param pageable pagination info
         * @return list of recent activity logs
         */
        @Query("SELECT a FROM AdminActivityLog a ORDER BY a.createdAt DESC")
        List<AdminActivityLog> findRecentActivities(Pageable pageable);

        /**
         * Find activity logs by user ID.
         *
         * @param userId   the user ID
         * @param pageable pagination info
         * @return list of activity logs for the user
         */
        @Query("SELECT a FROM AdminActivityLog a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
        List<AdminActivityLog> findByUserId(@Param("userId") UUID userId, Pageable pageable);

        /**
         * Find activity logs by action type.
         *
         * @param action   the action type
         * @param pageable pagination info
         * @return list of activity logs for the action
         */
        List<AdminActivityLog> findByActionOrderByCreatedAtDesc(AdminActivityLog.ActionType action, Pageable pageable);

        /**
         * Find activity logs by entity type and entity ID.
         *
         * @param entityType the entity type
         * @param entityId   the entity ID
         * @return list of activity logs for the entity
         */
        List<AdminActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        AdminActivityLog.EntityType entityType, String entityId);

        /**
         * Find activity logs within a date range.
         *
         * @param startDate the start date
         * @param endDate   the end date
         * @param pageable  pagination info
         * @return list of activity logs within the date range
         */
        @Query("SELECT a FROM AdminActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
        List<AdminActivityLog> findByDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Count activities by action type within a date range.
         *
         * @param action    the action type
         * @param startDate the start date
         * @param endDate   the end date
         * @return count of activities
         */
        @Query("SELECT COUNT(a) FROM AdminActivityLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
        long countByActionAndDateRange(
                        @Param("action") AdminActivityLog.ActionType action,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Count activities by action type.
         *
         * @param action the action type
         * @return count of activities
         */
        long countByAction(AdminActivityLog.ActionType action);

        /**
         * Count activities by entity type.
         *
         * @param entityType the entity type
         * @return count of activities
         */
        long countByEntityType(AdminActivityLog.EntityType entityType);

        /**
         * Count activities within a date range.
         *
         * @param startDate the start date
         * @param endDate   the end date
         * @return count of activities
         */
        @Query("SELECT COUNT(a) FROM AdminActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
        long countByDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get action type counts.
         *
         * @return list of action and count tuples
         */
        @Query("SELECT a.action, COUNT(a) FROM AdminActivityLog a GROUP BY a.action")
        List<Object[]> countGroupByAction();

        /**
         * Get entity type counts.
         *
         * @return list of entity type and count tuples
         */
        @Query("SELECT a.entityType, COUNT(a) FROM AdminActivityLog a GROUP BY a.entityType")
        List<Object[]> countGroupByEntityType();

        /**
         * Get user activity counts (top N).
         *
         * @param pageable pagination for limiting results
         * @return list of user name and count tuples
         */
        @Query("SELECT a.userName, COUNT(a) FROM AdminActivityLog a GROUP BY a.userName ORDER BY COUNT(a) DESC")
        List<Object[]> countGroupByUserName(Pageable pageable);

        /**
         * Get action type counts within a date range.
         *
         * @param startDate the start date
         * @param endDate   the end date
         * @return list of action and count tuples
         */
        @Query("SELECT a.action, COUNT(a) FROM AdminActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.action")
        List<Object[]> countGroupByActionInDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get distinct action types.
         *
         * @return list of distinct action types
         */
        @Query("SELECT DISTINCT a.action FROM AdminActivityLog a")
        List<AdminActivityLog.ActionType> findDistinctActions();

        /**
         * Get distinct user names.
         *
         * @return list of distinct user names
         */
        @Query("SELECT DISTINCT a.userName FROM AdminActivityLog a ORDER BY a.userName")
        List<String> findDistinctUserNames();
}
