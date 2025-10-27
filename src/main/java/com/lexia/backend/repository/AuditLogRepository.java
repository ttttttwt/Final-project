package com.lexia.backend.repository;

import com.lexia.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog entity.
 * Provides methods to query audit logs for compliance and security purposes.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find all audit logs for a specific user.
     *
     * @param userId the user ID
     * @return list of audit logs for the user
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find audit logs by entity type and entity ID.
     *
     * @param entityType the entity type (e.g., "UserProfile")
     * @param entityId   the entity ID
     * @return list of audit logs for the entity
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    /**
     * Find audit logs by action type.
     *
     * @param action the action type (e.g., "PROFILE_UPDATE")
     * @return list of audit logs for the action
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs within a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of audit logs within the date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent audit logs for a user (last N entries).
     *
     * @param userId the user ID
     * @param limit  the maximum number of entries to return
     * @return list of recent audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC LIMIT :limit")
    List<AuditLog> findRecentByUserId(@Param("userId") UUID userId, @Param("limit") int limit);
}
