package com.lexia.backend.exception.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AI exception classes.
 */
@DisplayName("AI Exception Tests")
class AiExceptionTest {

    @Nested
    @DisplayName("AiServiceException Tests")
    class AiServiceExceptionTests {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            AiServiceException exception = new AiServiceException("Test error");

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getErrorCode()).isEqualTo("AI_ERROR");
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            AiServiceException exception = new AiServiceException("Test error", cause);

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("Should create with custom error code")
        void shouldCreateWithCustomErrorCode() {
            AiServiceException exception = new AiServiceException("Test", "CUSTOM_CODE", false);

            assertThat(exception.getErrorCode()).isEqualTo("CUSTOM_CODE");
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should create with all parameters")
        void shouldCreateWithAllParameters() {
            RuntimeException cause = new RuntimeException("Cause");
            AiServiceException exception = new AiServiceException("Msg", cause, "CODE", true);

            assertThat(exception.getMessage()).isEqualTo("Msg");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getErrorCode()).isEqualTo("CODE");
            assertThat(exception.isRetryable()).isTrue();
        }
    }

    @Nested
    @DisplayName("AiConfigurationException Tests")
    class AiConfigurationExceptionTests {

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            AiConfigurationException exception = new AiConfigurationException("Config error");

            assertThat(exception.getMessage()).isEqualTo("Config error");
            assertThat(exception.getErrorCode()).isEqualTo("AI_CONFIG_ERROR");
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithCause() {
            Exception cause = new Exception("Root");
            AiConfigurationException exception = new AiConfigurationException("Error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should be instance of AiServiceException")
        void shouldExtendAiServiceException() {
            AiConfigurationException exception = new AiConfigurationException("Test");

            assertThat(exception).isInstanceOf(AiServiceException.class);
        }
    }

    @Nested
    @DisplayName("AiRateLimitException Tests")
    class AiRateLimitExceptionTests {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            AiRateLimitException exception = new AiRateLimitException("Rate limit");

            assertThat(exception.getMessage()).isEqualTo("Rate limit");
            assertThat(exception.getErrorCode()).isEqualTo("AI_RATE_LIMIT");
            assertThat(exception.isRetryable()).isTrue();
            assertThat(exception.getRetryAfter()).isNull();
            assertThat(exception.getLimitType()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("Should create with retry after")
        void shouldCreateWithRetryAfter() {
            Instant retryAfter = Instant.now().plusSeconds(60);
            AiRateLimitException exception = new AiRateLimitException("Limited", retryAfter);

            assertThat(exception.getRetryAfter()).isEqualTo(retryAfter);
        }

        @Test
        @DisplayName("Should create with full quota details")
        void shouldCreateWithFullDetails() {
            Instant retryAfter = Instant.now().plusSeconds(120);
            AiRateLimitException exception = new AiRateLimitException(
                    "Quota exceeded", retryAfter, "DAILY", 100, 100);

            assertThat(exception.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(exception.getLimitType()).isEqualTo("DAILY");
            assertThat(exception.getCurrentUsage()).isEqualTo(100);
            assertThat(exception.getMaxAllowed()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should be retryable")
        void shouldBeRetryable() {
            AiRateLimitException exception = new AiRateLimitException("Test");

            assertThat(exception.isRetryable()).isTrue();
        }
    }

    @Nested
    @DisplayName("AiTimeoutException Tests")
    class AiTimeoutExceptionTests {

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessage() {
            AiTimeoutException exception = new AiTimeoutException("Timeout");

            assertThat(exception.getMessage()).isEqualTo("Timeout");
            assertThat(exception.getErrorCode()).isEqualTo("AI_TIMEOUT");
            assertThat(exception.isRetryable()).isTrue();
            assertThat(exception.getTimeoutMs()).isEqualTo(0);
            assertThat(exception.getOperation()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("Should create with timeout details")
        void shouldCreateWithDetails() {
            AiTimeoutException exception = new AiTimeoutException("Timed out", 10000, "generateContent");

            assertThat(exception.getTimeoutMs()).isEqualTo(10000);
            assertThat(exception.getOperation()).isEqualTo("generateContent");
        }

        @Test
        @DisplayName("Should create with cause")
        void shouldCreateWithCause() {
            Exception cause = new Exception("Network timeout");
            AiTimeoutException exception = new AiTimeoutException("Timeout", cause, 5000, "stream");

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getTimeoutMs()).isEqualTo(5000);
            assertThat(exception.getOperation()).isEqualTo("stream");
        }

        @Test
        @DisplayName("Should be retryable")
        void shouldBeRetryable() {
            AiTimeoutException exception = new AiTimeoutException("Test");

            assertThat(exception.isRetryable()).isTrue();
        }
    }
}
