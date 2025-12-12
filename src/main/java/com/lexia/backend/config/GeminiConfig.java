package com.lexia.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;

/**
 * Configuration class for Google Gemini AI integration.
 * Provides beans for Gemini client and content generation configuration.
 * 
 * <p>Configuration properties:</p>
 * <ul>
 *   <li>gemini.api.key - API key for authentication (required)</li>
 *   <li>gemini.model.default - Default model for cost-efficient operations</li>
 *   <li>gemini.model.premium - Premium model for complex tasks</li>
 *   <li>gemini.max-output-tokens - Maximum tokens per response</li>
 *   <li>gemini.temperature - Response creativity (0.0-1.0)</li>
 * </ul>
 * 
 * @see <a href="https://ai.google.dev/gemini-api/docs">Gemini API Documentation</a>
 */
@Configuration
public class GeminiConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiConfig.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model.default:gemini-2.0-flash-exp}")
    private String defaultModel;

    @Value("${gemini.model.premium:gemini-1.5-pro}")
    private String premiumModel;

    @Value("${gemini.max-output-tokens:2000}")
    private int maxOutputTokens;

    @Value("${gemini.temperature:0.7}")
    private float temperature;

    /**
     * Creates the Gemini client bean for API communication.
     * 
     * @return configured Gemini Client instance, or null if API key not set
     * @throws IllegalStateException if API key is invalid format
     */
    @Bean
    public Client geminiClient() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key not configured. AI features will use fallback content.");
            return null;
        }

        log.info("Initializing Gemini client with default model: {}", defaultModel);
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Creates the default content generation configuration.
     * Used for standard AI operations (grammar exercises, flashcards).
     * 
     * @return GenerateContentConfig with default settings
     */
    @Bean
    public GenerateContentConfig defaultContentConfig() {
        return GenerateContentConfig.builder()
                .maxOutputTokens(maxOutputTokens)
                .temperature(temperature)
                .build();
    }

    /**
     * Creates a stricter content generation configuration.
     * Used for structured outputs (JSON responses, exercises).
     * 
     * @return GenerateContentConfig with low temperature for consistent outputs
     */
    @Bean
    public GenerateContentConfig structuredContentConfig() {
        return GenerateContentConfig.builder()
                .maxOutputTokens(maxOutputTokens)
                .temperature(0.3f) // Lower temperature for more consistent structured output
                .build();
    }

    /**
     * Creates a creative content generation configuration.
     * Used for role-play scenarios and conversational AI.
     * 
     * @return GenerateContentConfig with higher temperature for varied responses
     */
    @Bean
    public GenerateContentConfig creativeContentConfig() {
        return GenerateContentConfig.builder()
                .maxOutputTokens(maxOutputTokens)
                .temperature(0.9f) // Higher temperature for more creative responses
                .build();
    }

    // Getters for configuration properties (used by services)

    /**
     * Gets the default model name for cost-efficient operations.
     * @return default model identifier (e.g., "gemini-2.0-flash-exp")
     */
    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * Gets the premium model name for complex tasks.
     * @return premium model identifier (e.g., "gemini-1.5-pro")
     */
    public String getPremiumModel() {
        return premiumModel;
    }

    /**
     * Gets the maximum output tokens configured.
     * @return max output tokens limit
     */
    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    /**
     * Gets the temperature setting for response creativity.
     * @return temperature value between 0.0 and 1.0
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * Checks if Gemini API is properly configured.
     * @return true if API key is set and not blank
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
