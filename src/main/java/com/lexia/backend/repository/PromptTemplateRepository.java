package com.lexia.backend.repository;

import com.lexia.backend.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PromptTemplate entity.
 * Provides methods for retrieving and managing AI prompt templates.
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {

    /**
     * Finds all active templates for a given key.
     * Used for A/B testing when multiple templates exist.
     */
    List<PromptTemplate> findByTemplateKeyAndIsActiveTrue(String templateKey);

    /**
     * Finds the default template for a given key.
     */
    Optional<PromptTemplate> findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue(String templateKey);

    /**
     * Finds a specific version of a template.
     */
    Optional<PromptTemplate> findByTemplateKeyAndVersion(String templateKey, Integer version);

    /**
     * Finds the latest version of a template by key.
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.templateKey = :templateKey AND p.isActive = true " +
           "ORDER BY p.version DESC LIMIT 1")
    Optional<PromptTemplate> findLatestByTemplateKey(@Param("templateKey") String templateKey);

    /**
     * Finds all templates in a specific category.
     */
    List<PromptTemplate> findByCategoryAndIsActiveTrue(String category);

    /**
     * Finds all templates for an A/B test experiment.
     */
    List<PromptTemplate> findByExperimentIdAndIsActiveTrue(String experimentId);

    /**
     * Checks if a template key exists.
     */
    boolean existsByTemplateKey(String templateKey);

    /**
     * Checks if a specific version exists for a template key.
     */
    boolean existsByTemplateKeyAndVersion(String templateKey, Integer version);

    /**
     * Gets distinct template keys.
     */
    @Query("SELECT DISTINCT p.templateKey FROM PromptTemplate p WHERE p.isActive = true")
    List<String> findDistinctTemplateKeys();

    /**
     * Gets all versions of a template.
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.templateKey = :templateKey ORDER BY p.version DESC")
    List<PromptTemplate> findAllVersionsByTemplateKey(@Param("templateKey") String templateKey);

    /**
     * Increments usage count for a template.
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.usageCount = p.usageCount + 1, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int incrementUsageCount(@Param("id") UUID id);

    /**
     * Increments both usage and success count for a template.
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.usageCount = p.usageCount + 1, p.successCount = p.successCount + 1, " +
           "p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int incrementUsageAndSuccessCount(@Param("id") UUID id);

    /**
     * Updates metrics for a template (usage, success, response time).
     */
    @Modifying
    @Query(value = """
        UPDATE ai_prompt_templates 
        SET usage_count = usage_count + 1,
            success_count = CASE WHEN :success THEN success_count + 1 ELSE success_count END,
            avg_response_time_ms = CASE 
                WHEN avg_response_time_ms IS NULL THEN :responseTimeMs
                ELSE avg_response_time_ms + ((:responseTimeMs - avg_response_time_ms) / usage_count)
            END,
            success_rate = CASE WHEN usage_count > 0 
                THEN (CASE WHEN :success THEN success_count + 1 ELSE success_count END)::numeric * 100 / (usage_count + 1)
                ELSE NULL 
            END,
            updated_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    int updateMetrics(@Param("id") UUID id, @Param("success") boolean success, @Param("responseTimeMs") long responseTimeMs);

    /**
     * Sets a template as the default for its key (and unsets others).
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.isDefault = (p.id = :id), p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.templateKey = :templateKey")
    int setAsDefault(@Param("id") UUID id, @Param("templateKey") String templateKey);

    /**
     * Deactivates a template.
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.isActive = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int deactivate(@Param("id") UUID id);

    /**
     * Activates a template.
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.isActive = true, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int activate(@Param("id") UUID id);

    /**
     * Updates traffic percentage for A/B testing.
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.trafficPercentage = :percentage, p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id")
    int updateTrafficPercentage(@Param("id") UUID id, @Param("percentage") int percentage);

    /**
     * Finds templates with highest success rate for a category.
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.category = :category AND p.isActive = true " +
           "AND p.usageCount > :minUsage ORDER BY p.successRate DESC")
    List<PromptTemplate> findTopPerformingByCategory(
            @Param("category") String category, 
            @Param("minUsage") long minUsage);
}
