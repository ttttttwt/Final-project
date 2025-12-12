package com.lexia.backend.dto.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AI DTOs.
 */
@DisplayName("AI DTO Tests")
class AiDtoTest {

    @Nested
    @DisplayName("TokenUsageDTO Tests")
    class TokenUsageDtoTests {

        @Test
        @DisplayName("Should create with all fields")
        void shouldCreateWithAllFields() {
            TokenUsageDTO dto = new TokenUsageDTO(100, 50, 150, 0.001);

            assertThat(dto.inputTokens()).isEqualTo(100);
            assertThat(dto.outputTokens()).isEqualTo(50);
            assertThat(dto.totalTokens()).isEqualTo(150);
            assertThat(dto.estimatedCostUsd()).isEqualTo(0.001);
        }

        @Test
        @DisplayName("Should create using factory method")
        void shouldCreateUsingFactory() {
            TokenUsageDTO dto = TokenUsageDTO.of(100, 50, 0.002);

            assertThat(dto.inputTokens()).isEqualTo(100);
            assertThat(dto.outputTokens()).isEqualTo(50);
            assertThat(dto.totalTokens()).isEqualTo(150);
            assertThat(dto.estimatedCostUsd()).isEqualTo(0.002);
        }

        @Test
        @DisplayName("Should create empty DTO")
        void shouldCreateEmpty() {
            TokenUsageDTO dto = TokenUsageDTO.empty();

            assertThat(dto.inputTokens()).isEqualTo(0);
            assertThat(dto.outputTokens()).isEqualTo(0);
            assertThat(dto.totalTokens()).isEqualTo(0);
            assertThat(dto.estimatedCostUsd()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate total correctly in factory")
        void shouldCalculateTotalInFactory() {
            TokenUsageDTO dto = TokenUsageDTO.of(250, 750, 0.005);

            assertThat(dto.totalTokens()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("GeminiResponseDTO Tests")
    class GeminiResponseDtoTests {

        @Test
        @DisplayName("Should create with all fields")
        void shouldCreateWithAllFields() {
            TokenUsageDTO usage = TokenUsageDTO.of(100, 50, 0.001);
            Instant now = Instant.now();
            
            GeminiResponseDTO dto = new GeminiResponseDTO(
                    "Generated text",
                    "gemini-2.0-flash-exp",
                    usage,
                    now,
                    150L,
                    false,
                    "STOP"
            );

            assertThat(dto.content()).isEqualTo("Generated text");
            assertThat(dto.model()).isEqualTo("gemini-2.0-flash-exp");
            assertThat(dto.tokenUsage()).isEqualTo(usage);
            assertThat(dto.timestamp()).isEqualTo(now);
            assertThat(dto.responseTimeMs()).isEqualTo(150L);
            assertThat(dto.isFallback()).isFalse();
            assertThat(dto.finishReason()).isEqualTo("STOP");
        }

        @Test
        @DisplayName("Should create success response")
        void shouldCreateSuccessResponse() {
            TokenUsageDTO usage = TokenUsageDTO.of(50, 100, 0.002);
            
            GeminiResponseDTO dto = GeminiResponseDTO.success(
                    "Success content",
                    "gemini-1.5-pro",
                    usage,
                    200L,
                    "MAX_TOKENS"
            );

            assertThat(dto.content()).isEqualTo("Success content");
            assertThat(dto.model()).isEqualTo("gemini-1.5-pro");
            assertThat(dto.tokenUsage()).isEqualTo(usage);
            assertThat(dto.responseTimeMs()).isEqualTo(200L);
            assertThat(dto.isFallback()).isFalse();
            assertThat(dto.finishReason()).isEqualTo("MAX_TOKENS");
            assertThat(dto.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should create fallback response")
        void shouldCreateFallbackResponse() {
            GeminiResponseDTO dto = GeminiResponseDTO.fallback(
                    "Fallback content",
                    "gemini-2.0-flash-exp"
            );

            assertThat(dto.content()).isEqualTo("Fallback content");
            assertThat(dto.model()).isEqualTo("gemini-2.0-flash-exp");
            assertThat(dto.isFallback()).isTrue();
            assertThat(dto.finishReason()).isEqualTo("FALLBACK");
            assertThat(dto.responseTimeMs()).isEqualTo(0);
            assertThat(dto.tokenUsage().totalTokens()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create empty response")
        void shouldCreateEmptyResponse() {
            GeminiResponseDTO dto = GeminiResponseDTO.empty("model");

            assertThat(dto.content()).isEmpty();
            assertThat(dto.model()).isEqualTo("model");
            assertThat(dto.isFallback()).isFalse();
            assertThat(dto.finishReason()).isEqualTo("ERROR");
        }
    }

    @Nested
    @DisplayName("SseEventDTO Tests")
    class SseEventDtoTests {

        @Test
        @DisplayName("Should create token event")
        void shouldCreateTokenEvent() {
            SseEventDTO dto = SseEventDTO.token("Hello", 0);

            assertThat(dto.event()).isEqualTo("token");
            assertThat(dto.data()).isEqualTo("Hello");
            assertThat(dto.index()).isEqualTo(0);
            assertThat(dto.messageId()).isNull();
            assertThat(dto.totalTokens()).isNull();
            assertThat(dto.error()).isNull();
        }

        @Test
        @DisplayName("Should create token event with index")
        void shouldCreateTokenEventWithIndex() {
            SseEventDTO dto = SseEventDTO.token("World", 5);

            assertThat(dto.index()).isEqualTo(5);
            assertThat(dto.data()).isEqualTo("World");
        }

        @Test
        @DisplayName("Should create complete event")
        void shouldCreateCompleteEvent() {
            SseEventDTO dto = SseEventDTO.complete("msg-123", 150);

            assertThat(dto.event()).isEqualTo("complete");
            assertThat(dto.messageId()).isEqualTo("msg-123");
            assertThat(dto.totalTokens()).isEqualTo(150);
            assertThat(dto.data()).isNull();
            assertThat(dto.error()).isNull();
        }

        @Test
        @DisplayName("Should create error event")
        void shouldCreateErrorEvent() {
            SseEventDTO dto = SseEventDTO.error("Rate limit exceeded");

            assertThat(dto.event()).isEqualTo("error");
            assertThat(dto.error()).isEqualTo("Rate limit exceeded");
            assertThat(dto.data()).isNull();
            assertThat(dto.messageId()).isNull();
        }
    }
}
