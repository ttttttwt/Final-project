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

       /**
        * Get total cost by model since a date.
        */
       @Query("SELECT a.modelId, COALESCE(SUM(a.estimatedCostUsd), 0) FROM AIUsageLog a " +
                     "WHERE a.createdAt >= :startDate " +
                     "GROUP BY a.modelId")
       List<Object[]> sumCostByModelSince(@Param("startDate") Instant startDate);

       /**
        * Get total cost by user since a date.
        */
       @Query("SELECT a.userId, COALESCE(SUM(a.estimatedCostUsd), 0), COUNT(a) FROM AIUsageLog a " +
                     "WHERE a.createdAt >= :startDate " +
                     "GROUP BY a.userId ORDER BY SUM(a.estimatedCostUsd) DESC")
       List<Object[]> sumCostByUserSince(@Param("startDate") Instant startDate, Pageable pageable);

       @Query("SELECT COUNT(DISTINCT a.userId) FROM AIUsageLog a WHERE a.createdAt >= :startDate")
       long countActiveUsersSince(@Param("startDate") Instant startDate);

       @Query("SELECT a.userId, COUNT(a), SUM(a.estimatedCostUsd), MAX(a.createdAt) FROM AIUsageLog a " +
                     "WHERE a.createdAt >= :startDate " +
                     "GROUP BY a.userId ORDER BY COUNT(a) DESC")
       List<Object[]> findTopUsersSince(@Param("startDate") Instant startDate, Pageable pageable);

       @Query("SELECT a.contentType, COUNT(a) FROM AIUsageLog a " +
                     "WHERE a.createdAt >= :startDate " +
                     "GROUP BY a.contentType")
       List<Object[]> countGlobalByContentTypeSince(@Param("startDate") Instant startDate);

       /**
        * Find recent AI usage logs for a user, ordered by creation date desc.
        */
       List<AIUsageLog> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);

       /**
        * Find AI usage logs for a user with pagination, ordered by creation date desc.
        */
       List<AIUsageLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

       /**
        * Find users with high request volume since a given time.
        * Returns userId and request count for users exceeding threshold.
        */
       @Query("SELECT a.userId, COUNT(a) FROM AIUsageLog a " +
                     "WHERE a.createdAt >= :since " +
                     "GROUP BY a.userId " +
                     "HAVING COUNT(a) > :threshold")
       List<Object[]> findUsersWithHighRequestVolume(
                     @Param("since") Instant since,
                     @Param("threshold") int threshold);

       // ==================== Analytics Methods (Instant) ====================

       /**
        * Count logs created after a specific date (for analytics)
        */
       @Query("SELECT COUNT(a) FROM AIUsageLog a WHERE a.createdAt >= :date")
       long countByCreatedAtAfter(@Param("date") Instant date);

       /**
        * Count logs between dates
        */
       @Query("SELECT COUNT(a) FROM AIUsageLog a WHERE a.createdAt BETWEEN :start AND :end")
       long countByCreatedAtBetween(@Param("start") Instant start,
                     @Param("end") Instant end);

       /**
        * Count by feature/content type after date
        */
       @Query("SELECT COUNT(a) FROM AIUsageLog a WHERE a.contentType = :feature AND a.createdAt >= :date")
       long countByFeatureAndCreatedAtAfter(@Param("feature") String feature,
                     @Param("date") Instant date);

       /**
        * Count successful requests after date
        */
       @Query("SELECT COUNT(a) FROM AIUsageLog a WHERE a.success = :success AND a.createdAt >= :date")
       long countBySuccessAndCreatedAtAfter(@Param("success") boolean success,
                     @Param("date") Instant date);

       /**
        * Average response time after date
        */
       @Query("SELECT AVG(a.responseTimeMs) FROM AIUsageLog a WHERE a.createdAt >= :date AND a.responseTimeMs IS NOT NULL")
       Double averageResponseTimeAfter(@Param("date") Instant date);

       /**
        * Sum tokens used between dates
        */
       @Query("SELECT COALESCE(SUM(a.inputTokens + a.outputTokens), 0) FROM AIUsageLog a WHERE a.createdAt BETWEEN :start AND :end")
       Long sumTokensUsedBetween(@Param("start") Instant start,
                     @Param("end") Instant end);

       /**
        * Find logs with dynamic filters for export
        */
       @Query("SELECT a FROM AIUsageLog a WHERE " +
              "(:featureName IS NULL OR a.featureName = :featureName) AND " +
              "(:userId IS NULL OR a.userId = :userId) AND " +
              "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
              "(:endDate IS NULL OR a.createdAt <= :endDate) " +
              "ORDER BY a.createdAt DESC")
       List<AIUsageLog> findWithFilters(
               @Param("featureName") String featureName,
               @Param("userId") UUID userId,
               @Param("startDate") Instant startDate,
               @Param("endDate") Instant endDate);

       /**
        * Get daily cost aggregation since a date.
        * Returns date (as string), totalCost, totalRequests.
        */
       @Query(value = "SELECT DATE(created_at) as date, " +
                      "COALESCE(SUM(estimated_cost_usd), 0) as totalCost, " +
                      "COUNT(*) as totalRequests, " +
                      "COALESCE(SUM(CASE WHEN content_type = 'roleplay' THEN estimated_cost_usd ELSE 0 END), 0) as rolePlayCost, " +
                      "COALESCE(SUM(CASE WHEN content_type = 'grammar' OR content_type = 'grammar_sandbox' THEN estimated_cost_usd ELSE 0 END), 0) as grammarCost, " +
                      "COALESCE(SUM(CASE WHEN content_type = 'flashcard' OR content_type = 'magic_flashcard' THEN estimated_cost_usd ELSE 0 END), 0) as flashcardCost " +
                      "FROM ai_usage_logs " +
                      "WHERE created_at >= :startDate " +
                      "GROUP BY DATE(created_at) " +
                      "ORDER BY DATE(created_at)", nativeQuery = true)
       List<Object[]> getDailyCostsSince(@Param("startDate") Instant startDate);

       /**
        * Get cost grouped by user plan type since a date.
        * Returns planType, totalCost, totalRequests, userCount.
        */
       @Query(value = "SELECT COALESCE(q.plan_type, 'FREE') as planType, " +
                      "COALESCE(SUM(l.estimated_cost_usd), 0) as totalCost, " +
                      "COUNT(*) as totalRequests, " +
                      "COUNT(DISTINCT l.user_id) as userCount " +
                      "FROM ai_usage_logs l " +
                      "LEFT JOIN user_ai_quotas q ON l.user_id = q.user_id " +
                      "WHERE l.created_at >= :startDate " +
                      "GROUP BY COALESCE(q.plan_type, 'FREE')", nativeQuery = true)
       List<Object[]> getCostByPlanSince(@Param("startDate") Instant startDate);
}
