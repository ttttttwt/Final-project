package com.lexia.backend.service.ai;

import com.lexia.backend.entity.PromptTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing AI prompt templates.
 * Provides methods for retrieving, resolving, and tracking prompt templates.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Template retrieval with caching (5-minute TTL)</li>
 *   <li>Mustache-style variable substitution: {{variable}}</li>
 *   <li>A/B testing support with weighted traffic routing</li>
 *   <li>Usage and performance metrics tracking</li>
 * </ul>
 * 
 * @see PromptTemplate
 */
public interface PromptTemplateService {

    /**
     * Gets the default active template for a given key.
     * Results are cached with a 5-minute TTL.
     * 
     * @param templateKey The template key (e.g., "roleplay_scenario_v1")
     * @return The template if found
     */
    Optional<PromptTemplate> getTemplate(String templateKey);

    /**
     * Gets a specific version of a template.
     * 
     * @param templateKey The template key
     * @param version The version number
     * @return The template if found
     */
    Optional<PromptTemplate> getTemplate(String templateKey, int version);

    /**
     * Gets the latest version of a template regardless of default status.
     * 
     * @param templateKey The template key
     * @return The latest version template if found
     */
    Optional<PromptTemplate> getLatestTemplate(String templateKey);

    /**
     * Gets a template for A/B testing based on traffic percentage.
     * Selects from active templates for the key using weighted random selection.
     * 
     * @param templateKey The template key
     * @return A template selected based on traffic weights
     */
    Optional<PromptTemplate> getTemplateForAbTest(String templateKey);

    /**
     * Resolves variables in a template text using Mustache-style substitution.
     * Replaces {{variable}} placeholders with provided values.
     * 
     * @param templateText The template text with placeholders
     * @param variables Map of variable names to values
     * @return The resolved template text
     */
    String resolveVariables(String templateText, Map<String, Object> variables);

    /**
     * Gets a template and resolves its variables in one operation.
     * 
     * @param templateKey The template key
     * @param variables Map of variable names to values
     * @return The resolved template text, or empty if template not found
     */
    Optional<String> getAndResolve(String templateKey, Map<String, Object> variables);

    /**
     * Records usage metrics for a template.
     * Updates usage_count, success_count, and avg_response_time_ms.
     * 
     * @param templateId The template ID
     * @param success Whether the usage was successful
     * @param responseTimeMs The response time in milliseconds
     */
    void recordUsage(UUID templateId, boolean success, long responseTimeMs);

    /**
     * Gets all templates in a category.
     * 
     * @param category The category (roleplay, grammar, flashcard, etc.)
     * @return List of active templates in the category
     */
    List<PromptTemplate> getTemplatesByCategory(String category);

    /**
     * Gets all distinct template keys.
     * 
     * @return List of unique template keys
     */
    List<String> getAllTemplateKeys();

    /**
     * Gets all versions of a template.
     * 
     * @param templateKey The template key
     * @return List of all versions, ordered by version descending
     */
    List<PromptTemplate> getAllVersions(String templateKey);

    /**
     * Creates a new template or new version.
     * 
     * @param template The template to create
     * @return The saved template
     */
    PromptTemplate createTemplate(PromptTemplate template);

    /**
     * Updates an existing template.
     * 
     * @param templateId The template ID to update
     * @param updates Map of fields to update
     * @return The updated template
     */
    Optional<PromptTemplate> updateTemplate(UUID templateId, Map<String, Object> updates);

    /**
     * Sets a template as the default for its key.
     * 
     * @param templateId The template ID to set as default
     * @return true if successful
     */
    boolean setAsDefault(UUID templateId);

    /**
     * Activates a template.
     * 
     * @param templateId The template ID to activate
     * @return true if successful
     */
    boolean activate(UUID templateId);

    /**
     * Deactivates a template.
     * 
     * @param templateId The template ID to deactivate
     * @return true if successful
     */
    boolean deactivate(UUID templateId);

    /**
     * Updates traffic percentage for A/B testing.
     * 
     * @param templateId The template ID
     * @param percentage The traffic percentage (0-100)
     * @return true if successful
     */
    boolean updateTrafficPercentage(UUID templateId, int percentage);

    /**
     * Evicts all cached templates.
     * Use when templates are updated via admin interface.
     */
    void evictAllCaches();

    /**
     * Evicts cached templates for a specific key.
     * 
     * @param templateKey The template key to evict
     */
    void evictCache(String templateKey);

    /**
     * Validates that a template has all required variables.
     * 
     * @param template The template to validate
     * @param variables The provided variables
     * @return List of missing variable names, empty if all present
     */
    List<String> validateVariables(PromptTemplate template, Map<String, Object> variables);
}
