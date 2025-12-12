package com.lexia.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for the application.
 * Uses Caffeine for high-performance in-memory caching.
 * 
 * <p>Configured caches:</p>
 * <ul>
 *   <li><b>promptTemplates</b>: AI prompt templates with 5-minute TTL</li>
 *   <li><b>userQuotas</b>: User AI quota data with 1-minute TTL</li>
 * </ul>
 * 
 * @see com.lexia.backend.service.ai.PromptTemplateService
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * TTL for prompt templates cache in minutes. Default: 5 minutes.
     */
    @Value("${lexia.cache.prompt-templates.ttl-minutes:5}")
    private int promptTemplatesTtlMinutes;

    /**
     * Maximum entries for prompt templates cache. Default: 100.
     */
    @Value("${lexia.cache.prompt-templates.max-size:100}")
    private int promptTemplatesMaxSize;

    /**
     * TTL for user quotas cache in seconds. Default: 60 seconds.
     */
    @Value("${lexia.cache.user-quotas.ttl-seconds:60}")
    private int userQuotasTtlSeconds;

    /**
     * Maximum entries for user quotas cache. Default: 1000.
     */
    @Value("${lexia.cache.user-quotas.max-size:1000}")
    private int userQuotasMaxSize;

    /**
     * Cache names used in the application.
     */
    public static final String CACHE_PROMPT_TEMPLATES = "promptTemplates";
    public static final String CACHE_PROMPT_TEMPLATES_BY_KEY = "promptTemplatesByKey";
    public static final String CACHE_USER_QUOTAS = "userQuotas";

    /**
     * Creates the cache manager with Caffeine configuration.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Register different caches with different configurations
        cacheManager.registerCustomCache(CACHE_PROMPT_TEMPLATES, 
                buildPromptTemplatesCache().build());
        
        cacheManager.registerCustomCache(CACHE_PROMPT_TEMPLATES_BY_KEY, 
                buildPromptTemplatesCache().build());
        
        cacheManager.registerCustomCache(CACHE_USER_QUOTAS, 
                buildUserQuotasCache().build());
        
        // Default cache configuration for any unregistered caches
        cacheManager.setCaffeine(buildDefaultCache());
        
        return cacheManager;
    }

    /**
     * Builds the Caffeine configuration for prompt templates cache.
     * - TTL: 5 minutes (configurable)
     * - Max size: 100 entries (configurable)
     * - Records stats for monitoring
     */
    private Caffeine<Object, Object> buildPromptTemplatesCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(promptTemplatesTtlMinutes, TimeUnit.MINUTES)
                .maximumSize(promptTemplatesMaxSize)
                .recordStats();
    }

    /**
     * Builds the Caffeine configuration for user quotas cache.
     * - TTL: 60 seconds (configurable)
     * - Max size: 1000 entries (configurable)
     * - Records stats for monitoring
     */
    private Caffeine<Object, Object> buildUserQuotasCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(userQuotasTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(userQuotasMaxSize)
                .recordStats();
    }

    /**
     * Builds default cache configuration.
     * - TTL: 10 minutes
     * - Max size: 500 entries
     */
    private Caffeine<Object, Object> buildDefaultCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500);
    }
}
