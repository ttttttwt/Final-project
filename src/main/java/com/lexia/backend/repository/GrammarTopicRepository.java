package com.lexia.backend.repository;

import com.lexia.backend.entity.GrammarTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GrammarTopic entity.
 * Provides CRUD operations and custom queries for grammar topic management.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Find topics by category and CEFR level</li>
 *   <li>Active/inactive topic filtering</li>
 *   <li>PostgreSQL array containment queries</li>
 * </ul>
 * 
 * @see GrammarTopic
 */
@Repository
public interface GrammarTopicRepository extends JpaRepository<GrammarTopic, Integer> {

    // ========== Find by Name ==========

    /**
     * Find a topic by its exact name.
     * 
     * @param name topic name
     * @return Optional containing the topic if found
     */
    Optional<GrammarTopic> findByName(String name);

    /**
     * Find a topic by name (case-insensitive).
     * 
     * @param name topic name
     * @return Optional containing the topic if found
     */
    Optional<GrammarTopic> findByNameIgnoreCase(String name);

    /**
     * Check if a topic exists by name.
     * 
     * @param name topic name
     * @return true if exists
     */
    boolean existsByName(String name);

    // ========== Find by Category ==========

    /**
     * Find all topics in a specific category.
     * 
     * @param category category name
     * @return list of topics in the category
     */
    List<GrammarTopic> findByCategory(String category);

    /**
     * Find all active topics in a specific category.
     * 
     * @param category category name
     * @return list of active topics in the category
     */
    List<GrammarTopic> findByCategoryAndIsActiveTrue(String category);

    /**
     * Get all distinct categories.
     * 
     * @return list of unique category names
     */
    @Query("SELECT DISTINCT t.category FROM GrammarTopic t ORDER BY t.category")
    List<String> findDistinctCategories();

    /**
     * Get all distinct categories (active topics only).
     * 
     * @return list of unique category names
     */
    @Query("SELECT DISTINCT t.category FROM GrammarTopic t WHERE t.isActive = true ORDER BY t.category")
    List<String> findDistinctActiveCategories();

    // ========== Find by CEFR Level ==========

    /**
     * Find topics that include a specific CEFR level.
     * Uses PostgreSQL array containment operator.
     * 
     * @param level CEFR level (A1, A2, B1, B2, C1, C2)
     * @return list of topics applicable to the level
     */
    @Query(value = "SELECT * FROM grammar_topics WHERE :level = ANY(cefr_levels)", nativeQuery = true)
    List<GrammarTopic> findByCefrLevel(@Param("level") String level);

    /**
     * Find active topics that include a specific CEFR level.
     * 
     * @param level CEFR level
     * @return list of active topics applicable to the level
     */
    @Query(value = "SELECT * FROM grammar_topics WHERE :level = ANY(cefr_levels) AND is_active = true", 
           nativeQuery = true)
    List<GrammarTopic> findActiveByCefrLevel(@Param("level") String level);

    /**
     * Find topics by category and CEFR level.
     * 
     * @param category category name
     * @param level CEFR level
     * @return list of matching topics
     */
    @Query(value = "SELECT * FROM grammar_topics WHERE category = :category AND :level = ANY(cefr_levels)", 
           nativeQuery = true)
    List<GrammarTopic> findByCategoryAndCefrLevel(@Param("category") String category, @Param("level") String level);

    /**
     * Find active topics by category and CEFR level.
     * 
     * @param category category name
     * @param level CEFR level
     * @return list of active matching topics
     */
    @Query(value = "SELECT * FROM grammar_topics WHERE category = :category AND :level = ANY(cefr_levels) AND is_active = true", 
           nativeQuery = true)
    List<GrammarTopic> findActiveByCategoryAndCefrLevel(@Param("category") String category, @Param("level") String level);

    // ========== Find Active Topics ==========

    /**
     * Find all active topics.
     * 
     * @return list of active topics
     */
    List<GrammarTopic> findByIsActiveTrue();

    /**
     * Find all active topics ordered by category and name.
     * 
     * @return list of active topics sorted
     */
    List<GrammarTopic> findByIsActiveTrueOrderByCategoryAscNameAsc();

    /**
     * Count active topics.
     * 
     * @return count of active topics
     */
    long countByIsActiveTrue();

    // ========== Search ==========

    /**
     * Search topics by name containing a keyword (case-insensitive).
     * 
     * @param keyword search keyword
     * @return list of matching topics
     */
    List<GrammarTopic> findByNameContainingIgnoreCase(String keyword);

    /**
     * Search active topics by name containing a keyword.
     * 
     * @param keyword search keyword
     * @return list of matching active topics
     */
    List<GrammarTopic> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);

    // ========== Statistics ==========

    /**
     * Count topics by category.
     * 
     * @return list of [category, count] pairs
     */
    @Query("SELECT t.category, COUNT(t) FROM GrammarTopic t GROUP BY t.category ORDER BY t.category")
    List<Object[]> countByCategory();

    /**
     * Count active topics by category.
     * 
     * @return list of [category, count] pairs
     */
    @Query("SELECT t.category, COUNT(t) FROM GrammarTopic t WHERE t.isActive = true GROUP BY t.category ORDER BY t.category")
    List<Object[]> countActiveByCategory();
}
