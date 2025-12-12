package com.lexia.backend.service.ai;

import com.lexia.backend.config.CacheConfig;
import com.lexia.backend.entity.PromptTemplate;
import com.lexia.backend.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of PromptTemplateService with caching and A/B testing support.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Caffeine cache with 5-minute TTL for template retrieval</li>
 *   <li>Mustache-style variable substitution using regex</li>
 *   <li>Weighted random selection for A/B testing</li>
 *   <li>Async metrics updates to avoid blocking</li>
 * </ul>
 * 
 * @see PromptTemplateService
 * @see PromptTemplate
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateRepository promptTemplateRepository;

    /**
     * Regex pattern for Mustache-style variables: {{variable_name}}
     * Captures the variable name inside the braces.
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    @Override
    @Cacheable(value = CacheConfig.CACHE_PROMPT_TEMPLATES, key = "#templateKey", unless = "#result == null")
    public Optional<PromptTemplate> getTemplate(String templateKey) {
        log.debug("Fetching template for key: {} (cache miss)", templateKey);
        
        // First try to get the default template
        Optional<PromptTemplate> defaultTemplate = 
                promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue(templateKey);
        
        if (defaultTemplate.isPresent()) {
            return defaultTemplate;
        }
        
        // Fall back to latest version
        return promptTemplateRepository.findLatestByTemplateKey(templateKey);
    }

    @Override
    public Optional<PromptTemplate> getTemplate(String templateKey, int version) {
        log.debug("Fetching template for key: {}, version: {}", templateKey, version);
        return promptTemplateRepository.findByTemplateKeyAndVersion(templateKey, version);
    }

    @Override
    public Optional<PromptTemplate> getLatestTemplate(String templateKey) {
        return promptTemplateRepository.findLatestByTemplateKey(templateKey);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_PROMPT_TEMPLATES_BY_KEY, key = "'ab_' + #templateKey", unless = "#result == null")
    public Optional<PromptTemplate> getTemplateForAbTest(String templateKey) {
        log.debug("Selecting template for A/B test: {}", templateKey);
        
        List<PromptTemplate> activeTemplates = 
                promptTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey);
        
        if (activeTemplates.isEmpty()) {
            log.warn("No active templates found for key: {}", templateKey);
            return Optional.empty();
        }
        
        if (activeTemplates.size() == 1) {
            return Optional.of(activeTemplates.get(0));
        }
        
        // Weighted random selection based on traffic_percentage
        return selectByTrafficWeight(activeTemplates);
    }

    @Override
    public String resolveVariables(String templateText, Map<String, Object> variables) {
        if (templateText == null || templateText.isEmpty()) {
            return templateText;
        }
        
        if (variables == null || variables.isEmpty()) {
            return templateText;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            
            // Replace with value or keep placeholder if not found
            String replacement = value != null ? 
                    Matcher.quoteReplacement(String.valueOf(value)) : 
                    matcher.group(0);
            
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }

    @Override
    public Optional<String> getAndResolve(String templateKey, Map<String, Object> variables) {
        return getTemplate(templateKey)
                .map(template -> resolveVariables(template.getTemplateText(), variables));
    }

    @Override
    @Async
    @Transactional
    public void recordUsage(UUID templateId, boolean success, long responseTimeMs) {
        log.debug("Recording usage for template: {}, success: {}, time: {}ms", 
                templateId, success, responseTimeMs);
        
        try {
            int updated = promptTemplateRepository.updateMetrics(templateId, success, responseTimeMs);
            if (updated == 0) {
                log.warn("Failed to update metrics for template: {} - not found", templateId);
            }
        } catch (Exception e) {
            // Don't fail the main operation for metrics update failure
            log.error("Error updating template metrics: {}", e.getMessage());
        }
    }

    @Override
    public List<PromptTemplate> getTemplatesByCategory(String category) {
        return promptTemplateRepository.findByCategoryAndIsActiveTrue(category);
    }

    @Override
    public List<String> getAllTemplateKeys() {
        return promptTemplateRepository.findDistinctTemplateKeys();
    }

    @Override
    public List<PromptTemplate> getAllVersions(String templateKey) {
        return promptTemplateRepository.findAllVersionsByTemplateKey(templateKey);
    }

    @Override
    @Transactional
    public PromptTemplate createTemplate(PromptTemplate template) {
        log.info("Creating new template: {} v{}", template.getTemplateKey(), template.getVersion());
        
        // Auto-increment version if key exists
        if (promptTemplateRepository.existsByTemplateKeyAndVersion(
                template.getTemplateKey(), template.getVersion())) {
            // Find max version and increment
            List<PromptTemplate> versions = getAllVersions(template.getTemplateKey());
            int maxVersion = versions.stream()
                    .mapToInt(PromptTemplate::getVersion)
                    .max()
                    .orElse(0);
            template.setVersion(maxVersion + 1);
        }
        
        // Extract variables from template text
        List<String> extractedVars = extractVariables(template.getTemplateText());
        if (template.getVariables() == null || template.getVariables().isEmpty()) {
            template.setVariables(extractedVars);
        }
        
        PromptTemplate saved = promptTemplateRepository.save(template);
        evictCache(template.getTemplateKey());
        
        return saved;
    }

    @Override
    @Transactional
    public Optional<PromptTemplate> updateTemplate(UUID templateId, Map<String, Object> updates) {
        return promptTemplateRepository.findById(templateId)
                .map(template -> {
                    if (updates.containsKey("templateText")) {
                        template.setTemplateText((String) updates.get("templateText"));
                        // Re-extract variables
                        template.setVariables(extractVariables(template.getTemplateText()));
                    }
                    if (updates.containsKey("description")) {
                        template.setDescription((String) updates.get("description"));
                    }
                    if (updates.containsKey("category")) {
                        template.setCategory((String) updates.get("category"));
                    }
                    if (updates.containsKey("trafficPercentage")) {
                        template.setTrafficPercentage((Integer) updates.get("trafficPercentage"));
                    }
                    if (updates.containsKey("experimentId")) {
                        template.setExperimentId((String) updates.get("experimentId"));
                    }
                    
                    PromptTemplate saved = promptTemplateRepository.save(template);
                    evictCache(template.getTemplateKey());
                    return saved;
                });
    }

    @Override
    @Transactional
    public boolean setAsDefault(UUID templateId) {
        return promptTemplateRepository.findById(templateId)
                .map(template -> {
                    int updated = promptTemplateRepository.setAsDefault(templateId, template.getTemplateKey());
                    evictCache(template.getTemplateKey());
                    return updated > 0;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean activate(UUID templateId) {
        int updated = promptTemplateRepository.activate(templateId);
        if (updated > 0) {
            promptTemplateRepository.findById(templateId)
                    .ifPresent(t -> evictCache(t.getTemplateKey()));
        }
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deactivate(UUID templateId) {
        int updated = promptTemplateRepository.deactivate(templateId);
        if (updated > 0) {
            promptTemplateRepository.findById(templateId)
                    .ifPresent(t -> evictCache(t.getTemplateKey()));
        }
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updateTrafficPercentage(UUID templateId, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Traffic percentage must be between 0 and 100");
        }
        int updated = promptTemplateRepository.updateTrafficPercentage(templateId, percentage);
        if (updated > 0) {
            promptTemplateRepository.findById(templateId)
                    .ifPresent(t -> evictCache(t.getTemplateKey()));
        }
        return updated > 0;
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_PROMPT_TEMPLATES, CacheConfig.CACHE_PROMPT_TEMPLATES_BY_KEY}, allEntries = true)
    public void evictAllCaches() {
        log.info("Evicting all prompt template caches");
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_PROMPT_TEMPLATES, CacheConfig.CACHE_PROMPT_TEMPLATES_BY_KEY}, key = "#templateKey")
    public void evictCache(String templateKey) {
        log.debug("Evicting cache for template key: {}", templateKey);
    }

    @Override
    public List<String> validateVariables(PromptTemplate template, Map<String, Object> variables) {
        if (template.getVariables() == null || template.getVariables().isEmpty()) {
            return Collections.emptyList();
        }
        
        if (variables == null) {
            return new ArrayList<>(template.getVariables());
        }
        
        return template.getVariables().stream()
                .filter(var -> !variables.containsKey(var))
                .collect(Collectors.toList());
    }

    // ========== Private Helper Methods ==========

    /**
     * Selects a template using weighted random selection based on traffic_percentage.
     */
    private Optional<PromptTemplate> selectByTrafficWeight(List<PromptTemplate> templates) {
        // Calculate total weight
        int totalWeight = templates.stream()
                .mapToInt(t -> t.getTrafficPercentage() != null ? t.getTrafficPercentage() : 0)
                .sum();
        
        if (totalWeight == 0) {
            // If no weights, return the default or first
            return templates.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getIsDefault()))
                    .findFirst()
                    .or(() -> Optional.of(templates.get(0)));
        }
        
        // Random selection based on weight
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        
        for (PromptTemplate template : templates) {
            int weight = template.getTrafficPercentage() != null ? template.getTrafficPercentage() : 0;
            cumulative += weight;
            if (random < cumulative) {
                return Optional.of(template);
            }
        }
        
        // Fallback (shouldn't reach here)
        return Optional.of(templates.get(0));
    }

    /**
     * Extracts variable names from template text.
     */
    private List<String> extractVariables(String templateText) {
        if (templateText == null || templateText.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<String> variables = new LinkedHashSet<>(); // Preserve order, no duplicates
        Matcher matcher = VARIABLE_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return new ArrayList<>(variables);
    }
}
