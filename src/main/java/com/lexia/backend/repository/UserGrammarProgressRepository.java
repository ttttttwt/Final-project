package com.lexia.backend.repository;

import com.lexia.backend.entity.UserGrammarProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserGrammarProgress entity.
 * Provides CRUD operations and custom queries for tracking user progress on grammar exercises.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Track completion status and scores</li>
 *   <li>User history and statistics</li>
 *   <li>Progress aggregation queries</li>
 * </ul>
 * 
 * @see UserGrammarProgress
 */
@Repository
public interface UserGrammarProgressRepository extends JpaRepository<UserGrammarProgress, Long> {

    // ========== Find by User ==========

    /**
     * Find all progress records for a user.
     * 
     * @param userId user UUID
     * @return list of progress records
     */
    List<UserGrammarProgress> findByUserId(UUID userId);

    /**
     * Find progress records for a user with pagination.
     * 
     * @param userId user UUID
     * @param pageable pagination info
     * @return page of progress records
     */
    Page<UserGrammarProgress> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find progress records ordered by completion date (newest first).
     * 
     * @param userId user UUID
     * @return list of progress records
     */
    List<UserGrammarProgress> findByUserIdOrderByCompletedAtDesc(UUID userId);

    /**
     * Find completed progress records for a user.
     * 
     * @param userId user UUID
     * @return list of completed progress records
     */
    List<UserGrammarProgress> findByUserIdAndCompletedAtIsNotNull(UUID userId);

    /**
     * Find incomplete progress records for a user.
     * 
     * @param userId user UUID
     * @return list of incomplete progress records
     */
    List<UserGrammarProgress> findByUserIdAndCompletedAtIsNull(UUID userId);

    // ========== Find by Exercise Set ==========

    /**
     * Find all progress records for an exercise set.
     * 
     * @param exerciseSetId exercise set UUID
     * @return list of progress records
     */
    List<UserGrammarProgress> findByExerciseSet_Id(UUID exerciseSetId);

    /**
     * Find progress records for an exercise set with pagination.
     * 
     * @param exerciseSetId exercise set UUID
     * @param pageable pagination info
     * @return page of progress records
     */
    Page<UserGrammarProgress> findByExerciseSet_Id(UUID exerciseSetId, Pageable pageable);

    // ========== Find by User and Exercise Set ==========

    /**
     * Find progress for a specific user and exercise set.
     * 
     * @param userId user UUID
     * @param exerciseSetId exercise set UUID
     * @return Optional containing the progress record if exists
     */
    Optional<UserGrammarProgress> findByUserIdAndExerciseSet_Id(UUID userId, UUID exerciseSetId);

    /**
     * Check if a user has attempted an exercise set.
     * 
     * @param userId user UUID
     * @param exerciseSetId exercise set UUID
     * @return true if progress record exists
     */
    boolean existsByUserIdAndExerciseSet_Id(UUID userId, UUID exerciseSetId);

    /**
     * Check if a user has completed an exercise set.
     * 
     * @param userId user UUID
     * @param exerciseSetId exercise set UUID
     * @return true if completed progress record exists
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId AND p.exerciseSet.id = :exerciseSetId AND p.completedAt IS NOT NULL")
    boolean existsCompletedByUserIdAndExerciseSetId(
            @Param("userId") UUID userId, 
            @Param("exerciseSetId") UUID exerciseSetId);

    // ========== User Statistics ==========

    /**
     * Count total exercises attempted by a user.
     * 
     * @param userId user UUID
     * @return count of attempted exercises
     */
    long countByUserId(UUID userId);

    /**
     * Count completed exercises by a user.
     * 
     * @param userId user UUID
     * @return count of completed exercises
     */
    long countByUserIdAndCompletedAtIsNotNull(UUID userId);

    /**
     * Calculate average score percentage for a user.
     * 
     * @param userId user UUID
     * @return average percentage or null if no completed exercises
     */
    @Query("SELECT AVG(p.percentage) FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL")
    BigDecimal findAveragePercentageByUserId(@Param("userId") UUID userId);

    /**
     * Calculate total score for a user.
     * 
     * @param userId user UUID
     * @return sum of scores
     */
    @Query("SELECT COALESCE(SUM(p.score), 0) FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL")
    Integer findTotalScoreByUserId(@Param("userId") UUID userId);

    /**
     * Calculate total time spent on grammar exercises by a user.
     * 
     * @param userId user UUID
     * @return total time in seconds
     */
    @Query("SELECT COALESCE(SUM(p.timeSpentSeconds), 0) FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId")
    Integer findTotalTimeSpentByUserId(@Param("userId") UUID userId);

    /**
     * Find user's best scores by grammar point.
     * 
     * @param userId user UUID
     * @return list of [grammarPoint, maxPercentage] pairs
     */
    @Query("SELECT e.grammarPoint, MAX(p.percentage) FROM UserGrammarProgress p " +
           "JOIN p.exerciseSet e " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL " +
           "GROUP BY e.grammarPoint " +
           "ORDER BY e.grammarPoint")
    List<Object[]> findBestScoresByGrammarPoint(@Param("userId") UUID userId);

    /**
     * Count exercises completed by a user grouped by CEFR level.
     * 
     * @param userId user UUID
     * @return list of [cefrLevel, count] pairs
     */
    @Query("SELECT e.cefrLevel, COUNT(p) FROM UserGrammarProgress p " +
           "JOIN p.exerciseSet e " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL " +
           "GROUP BY e.cefrLevel " +
           "ORDER BY e.cefrLevel")
    List<Object[]> countCompletedByUserAndCefrLevel(@Param("userId") UUID userId);

    // ========== Date-based Queries ==========

    /**
     * Find progress records completed after a specific date.
     * 
     * @param userId user UUID
     * @param date start date
     * @return list of progress records
     */
    List<UserGrammarProgress> findByUserIdAndCompletedAtAfter(UUID userId, Instant date);

    /**
     * Find progress records completed between two dates.
     * 
     * @param userId user UUID
     * @param start start date
     * @param end end date
     * @return list of progress records
     */
    List<UserGrammarProgress> findByUserIdAndCompletedAtBetween(UUID userId, Instant start, Instant end);

    /**
     * Count exercises completed by a user after a specific date.
     * 
     * @param userId user UUID
     * @param date start date
     * @return count of completed exercises
     */
    long countByUserIdAndCompletedAtAfter(UUID userId, Instant date);

    // ========== Performance Filtering ==========

    /**
     * Find progress records where user passed (percentage >= threshold).
     * 
     * @param userId user UUID
     * @param threshold passing percentage
     * @return list of passed progress records
     */
    @Query("SELECT p FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL AND p.percentage >= :threshold")
    List<UserGrammarProgress> findPassedByUserId(
            @Param("userId") UUID userId, 
            @Param("threshold") BigDecimal threshold);

    /**
     * Find progress records where user failed (percentage < threshold).
     * 
     * @param userId user UUID
     * @param threshold passing percentage
     * @return list of failed progress records
     */
    @Query("SELECT p FROM UserGrammarProgress p " +
           "WHERE p.userId = :userId AND p.completedAt IS NOT NULL AND p.percentage < :threshold")
    List<UserGrammarProgress> findFailedByUserId(
            @Param("userId") UUID userId, 
            @Param("threshold") BigDecimal threshold);

    // ========== Recent Activity ==========

    /**
     * Find the most recent completed exercise for a user.
     * 
     * @param userId user UUID
     * @return Optional containing the most recent progress record
     */
    Optional<UserGrammarProgress> findFirstByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(UUID userId);

    /**
     * Find recent completed exercises for a user (limited).
     * 
     * @param userId user UUID
     * @param pageable pagination info (use PageRequest.of(0, limit))
     * @return page of recent progress records
     */
    Page<UserGrammarProgress> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(
            UUID userId, Pageable pageable);

    // ========== Global Statistics ==========

    /**
     * Calculate average score percentage across all completed exercises.
     * 
     * @return average percentage
     */
    @Query("SELECT AVG(p.percentage) FROM UserGrammarProgress p WHERE p.completedAt IS NOT NULL")
    BigDecimal findGlobalAveragePercentage();

    /**
     * Count total completed exercises.
     * 
     * @return count of completed exercises
     */
    long countByCompletedAtIsNotNull();

    /**
     * Count unique users who completed at least one exercise.
     * 
     * @return count of unique users
     */
    @Query("SELECT COUNT(DISTINCT p.userId) FROM UserGrammarProgress p WHERE p.completedAt IS NOT NULL")
    long countDistinctUsersWithCompletedExercises();
}
