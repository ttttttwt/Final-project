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
    @Query("SELECT COALESCE(SUM(a.cost), 0) FROM AIUsageLog a WHERE a.createdAt >= :startDate")
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
}
