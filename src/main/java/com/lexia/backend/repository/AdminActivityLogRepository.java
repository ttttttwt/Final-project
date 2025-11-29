package com.lexia.backend.repository;

import com.lexia.backend.entity.AdminActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AdminActivityLog entity.
 * Provides methods to query admin activity logs for dashboard display.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, UUID> {

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
}
