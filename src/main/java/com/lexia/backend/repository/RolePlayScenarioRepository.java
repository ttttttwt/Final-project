package com.lexia.backend.repository;

import com.lexia.backend.entity.RolePlayScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RolePlayScenario entity.
 * 
 * <p>Includes methods for fallback scenario retrieval (Task B7).</p>
 */
@Repository
public interface RolePlayScenarioRepository extends JpaRepository<RolePlayScenario, UUID>, JpaSpecificationExecutor<RolePlayScenario> {
    
    List<RolePlayScenario> findByCefrLevelAndDomain(String cefrLevel, String domain);
    
    List<RolePlayScenario> findByIsFallbackTrue();
    
    Page<RolePlayScenario> findByCefrLevel(String cefrLevel, Pageable pageable);

    // ========== Fallback Scenario Methods (Task B7) ==========

    /**
     * Finds all fallback scenarios matching CEFR level and domain.
     * 
     * @param cefrLevel the CEFR level
     * @param domain the domain
     * @return list of matching fallback scenarios
     */
    List<RolePlayScenario> findByIsFallbackTrueAndCefrLevelAndDomain(String cefrLevel, String domain);

    /**
     * Finds all fallback scenarios for a CEFR level (any domain).
     * 
     * @param cefrLevel the CEFR level
     * @return list of fallback scenarios for the level
     */
    List<RolePlayScenario> findByIsFallbackTrueAndCefrLevel(String cefrLevel);

    /**
     * Finds all fallback scenarios for a domain (any CEFR level).
     * 
     * @param domain the domain
     * @return list of fallback scenarios for the domain
     */
    List<RolePlayScenario> findByIsFallbackTrueAndDomain(String domain);

    /**
     * Counts fallback scenarios matching CEFR level and domain.
     * 
     * @param cefrLevel the CEFR level
     * @param domain the domain
     * @return count of matching fallback scenarios
     */
    long countByIsFallbackTrueAndCefrLevelAndDomain(String cefrLevel, String domain);

    /**
     * Counts all fallback scenarios for a CEFR level.
     * 
     * @param cefrLevel the CEFR level
     * @return count of fallback scenarios
     */
    long countByIsFallbackTrueAndCefrLevel(String cefrLevel);

    /**
     * Counts all fallback scenarios for a domain.
     * 
     * @param domain the domain
     * @return count of fallback scenarios
     */
    long countByIsFallbackTrueAndDomain(String domain);

    /**
     * Counts all fallback scenarios.
     * 
     * @return total count of fallback scenarios
     */
    long countByIsFallbackTrue();

    /**
     * Checks if any fallback scenarios exist for CEFR level and domain.
     * 
     * @param cefrLevel the CEFR level
     * @param domain the domain
     * @return true if at least one exists
     */
    boolean existsByIsFallbackTrueAndCefrLevelAndDomain(String cefrLevel, String domain);

    /**
     * Finds a random fallback scenario matching CEFR level and domain.
     * Uses native query with RANDOM() for PostgreSQL (RAND() for MySQL).
     * 
     * @param cefrLevel the CEFR level
     * @param domain the domain
     * @return Optional containing a random fallback scenario
     */
    @Query(value = "SELECT * FROM roleplay_scenarios WHERE is_fallback = true " +
            "AND cefr_level = :cefrLevel AND domain = :domain " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<RolePlayScenario> findRandomFallbackByCefrLevelAndDomain(
            @Param("cefrLevel") String cefrLevel, 
            @Param("domain") String domain);

    /**
     * Finds a random fallback scenario for a CEFR level (any domain).
     * 
     * @param cefrLevel the CEFR level
     * @return Optional containing a random fallback scenario
     */
    @Query(value = "SELECT * FROM roleplay_scenarios WHERE is_fallback = true " +
            "AND cefr_level = :cefrLevel " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<RolePlayScenario> findRandomFallbackByCefrLevel(@Param("cefrLevel") String cefrLevel);

    /**
     * Finds a random fallback scenario for a domain (any CEFR level).
     * 
     * @param domain the domain
     * @return Optional containing a random fallback scenario
     */
    @Query(value = "SELECT * FROM roleplay_scenarios WHERE is_fallback = true " +
            "AND domain = :domain " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<RolePlayScenario> findRandomFallbackByDomain(@Param("domain") String domain);

    /**
     * Finds any random fallback scenario.
     * 
     * @return Optional containing a random fallback scenario
     */
    @Query(value = "SELECT * FROM roleplay_scenarios WHERE is_fallback = true " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<RolePlayScenario> findRandomFallback();
}
