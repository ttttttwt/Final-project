package com.lexia.backend.repository;

import com.lexia.backend.entity.GrammarExerciseSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for GrammarExerciseSet entity.
 * Provides CRUD operations and custom queries for grammar exercise management.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Find exercises by CEFR level, grammar point, and theme</li>
 *   <li>Support for fallback content retrieval</li>
 *   <li>User-specific exercise history</li>
 *   <li>Pagination support</li>
 * </ul>
 * 
 * @see GrammarExerciseSet
 */
@Repository
public interface GrammarExerciseSetRepository extends JpaRepository<GrammarExerciseSet, UUID>,
        JpaSpecificationExecutor<GrammarExerciseSet> {

    // ========== Find by CEFR Level ==========

    /**
     * Find all exercise sets for a specific CEFR level.
     * 
     * @param cefrLevel CEFR level (A1, A2, B1, B2, C1, C2)
     * @return list of exercise sets for the level
     */
    List<GrammarExerciseSet> findByCefrLevel(String cefrLevel);

    /**
     * Find exercise sets for a CEFR level with pagination.
     * 
     * @param cefrLevel CEFR level
     * @param pageable pagination info
     * @return page of exercise sets
     */
    Page<GrammarExerciseSet> findByCefrLevel(String cefrLevel, Pageable pageable);

    /**
     * Find non-fallback exercise sets for a CEFR level.
     * 
     * @param cefrLevel CEFR level
     * @return list of AI-generated exercise sets
     */
    List<GrammarExerciseSet> findByCefrLevelAndIsFallbackFalse(String cefrLevel);

    // ========== Find by Grammar Point ==========

    /**
     * Find all exercise sets for a specific grammar point.
     * 
     * @param grammarPoint grammar topic name
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByGrammarPoint(String grammarPoint);

    /**
     * Find exercise sets by grammar point (case-insensitive).
     * 
     * @param grammarPoint grammar topic name
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByGrammarPointIgnoreCase(String grammarPoint);

    /**
     * Find exercise sets by grammar point containing keyword.
     * 
     * @param keyword search keyword
     * @return list of matching exercise sets
     */
    List<GrammarExerciseSet> findByGrammarPointContainingIgnoreCase(String keyword);

    // ========== Find by Level and Grammar Point ==========

    /**
     * Find exercise sets by CEFR level and grammar point.
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return list of matching exercise sets
     */
    List<GrammarExerciseSet> findByCefrLevelAndGrammarPoint(String cefrLevel, String grammarPoint);

    /**
     * Find exercise sets by CEFR level and grammar point (case-insensitive).
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return list of matching exercise sets
     */
    List<GrammarExerciseSet> findByCefrLevelAndGrammarPointIgnoreCase(String cefrLevel, String grammarPoint);

    /**
     * Find one exercise set by CEFR level and grammar point (most recent).
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return Optional containing the most recent exercise set
     */
    Optional<GrammarExerciseSet> findFirstByCefrLevelAndGrammarPointOrderByCreatedAtDesc(
            String cefrLevel, String grammarPoint);

    // ========== Find by Theme ==========

    /**
     * Find exercise sets by theme.
     * 
     * @param theme theme name
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByTheme(String theme);

    /**
     * Find exercise sets by CEFR level and theme.
     * 
     * @param cefrLevel CEFR level
     * @param theme theme name
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByCefrLevelAndTheme(String cefrLevel, String theme);

    /**
     * Find exercise sets by CEFR level, grammar point, and theme.
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @param theme theme name
     * @return list of matching exercise sets
     */
    List<GrammarExerciseSet> findByCefrLevelAndGrammarPointAndTheme(
            String cefrLevel, String grammarPoint, String theme);

    // ========== Find Fallback Content ==========

    /**
     * Find all fallback exercise sets.
     * 
     * @return list of fallback exercise sets
     */
    List<GrammarExerciseSet> findByIsFallbackTrue();

    /**
     * Find fallback exercise sets for a CEFR level.
     * 
     * @param cefrLevel CEFR level
     * @return list of fallback exercise sets
     */
    List<GrammarExerciseSet> findByIsFallbackTrueAndCefrLevel(String cefrLevel);

    /**
     * Find fallback exercise sets for a CEFR level and grammar point.
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return list of fallback exercise sets
     */
    List<GrammarExerciseSet> findByIsFallbackTrueAndCefrLevelAndGrammarPoint(
            String cefrLevel, String grammarPoint);

    /**
     * Find a random fallback exercise set for a CEFR level.
     * 
     * @param cefrLevel CEFR level
     * @return Optional containing a random fallback exercise set
     */
    @Query(value = "SELECT * FROM grammar_exercise_sets WHERE is_fallback = true AND cefr_level = :level ORDER BY RANDOM() LIMIT 1",
           nativeQuery = true)
    Optional<GrammarExerciseSet> findRandomFallbackByCefrLevel(@Param("level") String cefrLevel);

    /**
     * Find a random fallback exercise set for a CEFR level and grammar point.
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return Optional containing a random fallback exercise set
     */
    @Query(value = "SELECT * FROM grammar_exercise_sets WHERE is_fallback = true AND cefr_level = :level AND grammar_point = :point ORDER BY RANDOM() LIMIT 1",
           nativeQuery = true)
    Optional<GrammarExerciseSet> findRandomFallbackByCefrLevelAndGrammarPoint(
            @Param("level") String cefrLevel, @Param("point") String grammarPoint);

    /**
     * Find all fallback exercise sets for a CEFR level and grammar point.
     * 
     * @param cefrLevel CEFR level
     * @param grammarPoint grammar topic name
     * @return list of fallback exercise sets
     */
    @Query("SELECT e FROM GrammarExerciseSet e WHERE e.isFallback = true AND e.cefrLevel = :level AND LOWER(e.grammarPoint) = LOWER(:point)")
    List<GrammarExerciseSet> findFallbackByCefrLevelAndGrammarPoint(
            @Param("level") String cefrLevel, @Param("point") String grammarPoint);

    // ========== Find by User ==========

    /**
     * Find exercise sets created by a specific user.
     * 
     * @param userId user UUID
     * @return list of user's exercise sets
     */
    List<GrammarExerciseSet> findByUserId(UUID userId);

    /**
     * Find exercise sets by user with pagination.
     * 
     * @param userId user UUID
     * @param pageable pagination info
     * @return page of exercise sets
     */
    Page<GrammarExerciseSet> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find exercise sets by user, ordered by creation date (newest first).
     * 
     * @param userId user UUID
     * @return list of exercise sets ordered by creation date
     */
    List<GrammarExerciseSet> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Count exercise sets by user.
     * 
     * @param userId user UUID
     * @return count of exercise sets
     */
    long countByUserId(UUID userId);

    // ========== Find by Topic ==========

    /**
     * Find exercise sets by topic ID.
     * 
     * @param topicId topic ID
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByTopic_Id(Integer topicId);

    /**
     * Find exercise sets by topic ID and CEFR level.
     * 
     * @param topicId topic ID
     * @param cefrLevel CEFR level
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByTopic_IdAndCefrLevel(Integer topicId, String cefrLevel);

    // ========== Statistics ==========

    /**
     * Count exercise sets by CEFR level.
     * 
     * @return list of [cefrLevel, count] pairs
     */
    @Query("SELECT e.cefrLevel, COUNT(e) FROM GrammarExerciseSet e GROUP BY e.cefrLevel ORDER BY e.cefrLevel")
    List<Object[]> countByCefrLevel();

    /**
     * Count exercise sets by grammar point.
     * 
     * @return list of [grammarPoint, count] pairs
     */
    @Query("SELECT e.grammarPoint, COUNT(e) FROM GrammarExerciseSet e GROUP BY e.grammarPoint ORDER BY COUNT(e) DESC")
    List<Object[]> countByGrammarPoint();

    /**
     * Count fallback vs AI-generated exercise sets.
     * 
     * @return list of [isFallback, count] pairs
     */
    @Query("SELECT e.isFallback, COUNT(e) FROM GrammarExerciseSet e GROUP BY e.isFallback")
    List<Object[]> countByFallbackStatus();

    /**
     * Get distinct themes.
     * 
     * @return list of unique theme names
     */
    @Query("SELECT DISTINCT e.theme FROM GrammarExerciseSet e WHERE e.theme IS NOT NULL ORDER BY e.theme")
    List<String> findDistinctThemes();

    /**
     * Get distinct grammar points.
     * 
     * @return list of unique grammar point names
     */
    @Query("SELECT DISTINCT e.grammarPoint FROM GrammarExerciseSet e ORDER BY e.grammarPoint")
    List<String> findDistinctGrammarPoints();

    // ========== Date-based Queries ==========

    /**
     * Find exercise sets created after a specific date.
     * 
     * @param date start date
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByCreatedAtAfter(Instant date);

    /**
     * Find exercise sets created between two dates.
     * 
     * @param start start date
     * @param end end date
     * @return list of exercise sets
     */
    List<GrammarExerciseSet> findByCreatedAtBetween(Instant start, Instant end);

    /**
     * Count exercise sets created after a specific date.
     * 
     * @param date start date
     * @return count of exercise sets
     */
    long countByCreatedAtAfter(Instant date);
}
