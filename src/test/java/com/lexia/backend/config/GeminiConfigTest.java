package com.lexia.backend.config;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GeminiConfig.
 * Tests configuration bean creation and property injection.
 */
class GeminiConfigTest {

    private GeminiConfig geminiConfig;

    @BeforeEach
    void setUp() {
        geminiConfig = new GeminiConfig();
    }

    @Nested
    @DisplayName("Configuration Properties Tests")
    class ConfigurationPropertiesTests {

        @Test
        @DisplayName("getDefaultModel returns configured model")
        void getDefaultModel_ReturnsConfiguredValue() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "defaultModel", "gemini-2.0-flash-exp");

            // When
            String result = geminiConfig.getDefaultModel();

            // Then
            assertThat(result).isEqualTo("gemini-2.0-flash-exp");
        }

        @Test
        @DisplayName("getPremiumModel returns configured model")
        void getPremiumModel_ReturnsConfiguredValue() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "premiumModel", "gemini-1.5-pro");

            // When
            String result = geminiConfig.getPremiumModel();

            // Then
            assertThat(result).isEqualTo("gemini-1.5-pro");
        }

        @Test
        @DisplayName("getMaxOutputTokens returns configured value")
        void getMaxOutputTokens_ReturnsConfiguredValue() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "maxOutputTokens", 3000);

            // When
            int result = geminiConfig.getMaxOutputTokens();

            // Then
            assertThat(result).isEqualTo(3000);
        }

        @Test
        @DisplayName("getTemperature returns configured value")
        void getTemperature_ReturnsConfiguredValue() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "temperature", 0.8f);

            // When
            float result = geminiConfig.getTemperature();

            // Then
            assertThat(result).isEqualTo(0.8f);
        }
    }

    @Nested
    @DisplayName("isConfigured Tests")
    class IsConfiguredTests {

        @Test
        @DisplayName("isConfigured returns true when API key is set")
        void isConfigured_WithApiKey_ReturnsTrue() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", "test-api-key-12345");

            // When
            boolean result = geminiConfig.isConfigured();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isConfigured returns false when API key is null")
        void isConfigured_WithNullApiKey_ReturnsFalse() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", null);

            // When
            boolean result = geminiConfig.isConfigured();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isConfigured returns false when API key is blank")
        void isConfigured_WithBlankApiKey_ReturnsFalse() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", "   ");

            // When
            boolean result = geminiConfig.isConfigured();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isConfigured returns false when API key is empty")
        void isConfigured_WithEmptyApiKey_ReturnsFalse() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", "");

            // When
            boolean result = geminiConfig.isConfigured();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Gemini Client Bean Tests")
    class GeminiClientBeanTests {

        @Test
        @DisplayName("geminiClient returns null when API key not configured")
        void geminiClient_WithoutApiKey_ReturnsNull() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", "");
            ReflectionTestUtils.setField(geminiConfig, "defaultModel", "gemini-2.0-flash-exp");

            // When
            Client client = geminiConfig.geminiClient();

            // Then
            assertThat(client).isNull();
        }

        @Test
        @DisplayName("geminiClient returns null when API key is null")
        void geminiClient_WithNullApiKey_ReturnsNull() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", null);
            ReflectionTestUtils.setField(geminiConfig, "defaultModel", "gemini-2.0-flash-exp");

            // When
            Client client = geminiConfig.geminiClient();

            // Then
            assertThat(client).isNull();
        }

        @Test
        @DisplayName("geminiClient creates client when API key is provided")
        void geminiClient_WithApiKey_ReturnsClient() {
            // Given
            ReflectionTestUtils.setField(geminiConfig, "apiKey", "test-api-key-12345");
            ReflectionTestUtils.setField(geminiConfig, "defaultModel", "gemini-2.0-flash-exp");

            // When
            Client client = geminiConfig.geminiClient();

            // Then
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Content Config Bean Tests")
    class ContentConfigBeanTests {

        @BeforeEach
        void setUpConfigValues() {
            ReflectionTestUtils.setField(geminiConfig, "maxOutputTokens", 2000);
            ReflectionTestUtils.setField(geminiConfig, "temperature", 0.7f);
        }

        @Test
        @DisplayName("defaultContentConfig creates config with default settings")
        void defaultContentConfig_CreatesConfigWithDefaultSettings() {
            // When
            GenerateContentConfig config = geminiConfig.defaultContentConfig();

            // Then
            assertThat(config).isNotNull();
            assertThat(config.maxOutputTokens()).isPresent().hasValue(2000);
            assertThat(config.temperature()).isPresent().hasValue(0.7f);
        }

        @Test
        @DisplayName("structuredContentConfig creates config with low temperature")
        void structuredContentConfig_CreatesConfigWithLowTemperature() {
            // When
            GenerateContentConfig config = geminiConfig.structuredContentConfig();

            // Then
            assertThat(config).isNotNull();
            assertThat(config.maxOutputTokens()).isPresent().hasValue(2000);
            assertThat(config.temperature()).isPresent().hasValue(0.3f);
        }

        @Test
        @DisplayName("creativeContentConfig creates config with high temperature")
        void creativeContentConfig_CreatesConfigWithHighTemperature() {
            // When
            GenerateContentConfig config = geminiConfig.creativeContentConfig();

            // Then
            assertThat(config).isNotNull();
            assertThat(config.maxOutputTokens()).isPresent().hasValue(2000);
            assertThat(config.temperature()).isPresent().hasValue(0.9f);
        }
    }
}
