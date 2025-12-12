package com.lexia.backend.repository;

import com.lexia.backend.entity.AIUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AI usage logs.
 * Provides CRUD operations and custom queries for AI usage tracking.
 */
@Repository
public interface AIUsageLogRepository extends JpaRepository<AIUsageLog, Long>, JpaSpecificationExecutor<AIUsageLog> {

    /**
     * Find all logs by user ID
     */
    Page<AIUsageLog> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all logs by feature name
     */
    Page<AIUsageLog> findByFeatureName(String featureName, Pageable pageable);

    /**
     * Find logs created between two dates
     */
    Page<AIUsageLog> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    /**
     * Find logs by user and feature
     */
    Page<AIUsageLog> findByUserIdAndFeatureName(UUID userId, String featureName, Pageable pageable);

    /**
     * Count logs by feature name
     */
    @Query("SELECT a.featureName, COUNT(a) FROM AIUsageLog a WHERE a.createdAt >= :startDate GROUP BY a.featureName")
    List<Object[]> countByFeatureNameSince(@Param("startDate") Instant startDate);

    /**
     * Calculate total cost since a date
     */
    @Query("SELECT COALESCE(SUM(a.estimatedCostUsd), 0) FROM AIUsageLog a WHERE a.createdAt >= :startDate")
    java.math.BigDecimal sumCostSince(@Param("startDate") Instant startDate);

    /**
     * Calculate total tokens since a date
     */
    @Query("SELECT COALESCE(SUM(a.inputTokens), 0), COALESCE(SUM(a.outputTokens), 0) FROM AIUsageLog a WHERE a.createdAt >= :startDate")
    List<Object[]> sumTokensSince(@Param("startDate") Instant startDate);

    /**
     * Count total logs since a date
     */
    long countByCreatedAtGreaterThanEqual(Instant startDate);

    // ========== New methods for Sprint 5 (AiUsageTracker) ==========

    /**
     * Count logs by user, content type, and created after a date.
     * Used for daily/monthly quota checks.
     */
    long countByUserIdAndContentTypeAndCreatedAtGreaterThanEqual(
            UUID userId, String contentType, Instant startDate);

    /**
     * Count logs grouped by content type for a specific user and date range.
     */
    @Query("SELECT a.contentType, COUNT(a) FROM AIUsageLog a " +
           "WHERE a.userId = :userId AND a.createdAt >= :startDate AND a.createdAt <= :endDate " +
           "GROUP BY a.contentType")
    List<Object[]> countByContentTypeSince(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Find logs by content type with pagination.
     */
    Page<AIUsageLog> findByContentType(String contentType, Pageable pageable);

    /**
     * Find logs by user and content type with pagination.
     */
    Page<AIUsageLog> findByUserIdAndContentType(UUID userId, String contentType, Pageable pageable);

    /**
     * Find successful logs for a user since a date.
     */
    List<AIUsageLog> findByUserIdAndSuccessTrueAndCreatedAtGreaterThanEqual(
            UUID userId, Instant startDate);

    /**
     * Count failed requests for monitoring.
     */
    @Query("SELECT COUNT(a) FROM AIUsageLog a WHERE a.success = false AND a.createdAt >= :startDate")
    long countFailedSince(@Param("startDate") Instant startDate);

    /**
     * Get average response time by model since a date.
     */
    @Query("SELECT a.modelId, AVG(a.responseTimeMs) FROM AIUsageLog a " +
           "WHERE a.createdAt >= :startDate AND a.responseTimeMs IS NOT NULL " +
           "GROUP BY a.modelId")
    List<Object[]> avgResponseTimeByModelSince(@Param("startDate") Instant startDate);

    /**
     * Get total cost by content type since a date.
     */
    @Query("SELECT a.contentType, COALESCE(SUM(a.estimatedCostUsd), 0) FROM AIUsageLog a " +
           "WHERE a.createdAt >= :startDate " +
           "GROUP BY a.contentType")
    List<Object[]> sumCostByContentTypeSince(@Param("startDate") Instant startDate);
}
