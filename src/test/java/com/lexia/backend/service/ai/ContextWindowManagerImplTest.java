package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.GeminiResponseDTO;
import com.lexia.backend.dto.ai.TokenUsageDTO;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.entity.RolePlayScenario;
import com.lexia.backend.service.ai.impl.ContextWindowManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContextWindowManager implementation.
 * 
 * Tests B9: Context window management (sliding window + summarization).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContextWindowManager Tests")
class ContextWindowManagerImplTest {

    @Mock
    private GeminiClientService geminiClientService;

    @InjectMocks
    private ContextWindowManagerImpl contextWindowManager;

    private RolePlayConversation conversation;
    private RolePlayScenario scenario;

    @BeforeEach
    void setUp() {
        scenario = RolePlayScenario.builder()
                .id(UUID.randomUUID())
                .title("Test Scenario")
                .context("Business meeting context")
                .yourRole("Sales Manager")
                .aiRole("Client")
                .cefrLevel("B1")
                .domain("meetings")
                .build();

        conversation = RolePlayConversation.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .scenario(scenario)
                .mode("immersive")
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .messages(new ArrayList<>())
                .build();
    }

    private Map<String, Object> createMessage(String role, String content) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        msg.put("timestamp", Instant.now().toString());
        return msg;
    }

    private void addMessages(int count) {
        for (int i = 0; i < count; i++) {
            String role = i % 2 == 0 ? "user" : "ai";
            conversation.getMessages().add(createMessage(role, "Message " + (i + 1)));
        }
    }

    // ========== Token Estimation Tests ==========

    @Nested
    @DisplayName("estimateTokens")
    class EstimateTokensTests {

        @Test
        @DisplayName("should estimate tokens for simple text")
        void shouldEstimateTokensForSimpleText() {
            // Given
            String text = "Hello world"; // 11 chars → ~3 tokens

            // When
            int tokens = contextWindowManager.estimateTokens(text);

            // Then
            assertThat(tokens).isEqualTo(3); // ceil(11/4) = 3
        }

        @Test
        @DisplayName("should return 0 for null text")
        void shouldReturnZeroForNullText() {
            // When
            int tokens = contextWindowManager.estimateTokens(null);

            // Then
            assertThat(tokens).isZero();
        }

        @Test
        @DisplayName("should return 0 for empty text")
        void shouldReturnZeroForEmptyText() {
            // When
            int tokens = contextWindowManager.estimateTokens("");

            // Then
            assertThat(tokens).isZero();
        }

        @Test
        @DisplayName("should estimate correctly for long text")
        void shouldEstimateCorrectlyForLongText() {
            // Given
            String text = "a".repeat(1000); // 1000 chars → 250 tokens

            // When
            int tokens = contextWindowManager.estimateTokens(text);

            // Then
            assertThat(tokens).isEqualTo(250);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 4, 5, 100, 1000, 4000})
        @DisplayName("should round up token count")
        void shouldRoundUpTokenCount(int charCount) {
            // Given
            String text = "a".repeat(charCount);

            // When
            int tokens = contextWindowManager.estimateTokens(text);

            // Then
            assertThat(tokens).isEqualTo((int) Math.ceil(charCount / 4.0));
        }
    }

    // ========== Estimate Total Tokens Tests ==========

    @Nested
    @DisplayName("estimateTotalTokens")
    class EstimateTotalTokensTests {

        @Test
        @DisplayName("should return 0 for empty conversation")
        void shouldReturnZeroForEmptyConversation() {
            // When
            int tokens = contextWindowManager.estimateTotalTokens(conversation);

            // Then
            assertThat(tokens).isZero();
        }

        @Test
        @DisplayName("should count tokens from all messages")
        void shouldCountTokensFromAllMessages() {
            // Given
            conversation.getMessages().add(createMessage("user", "Hello")); // 5 chars → 2 tokens
            conversation.getMessages().add(createMessage("ai", "Hi there!")); // 9 chars → 3 tokens

            // When
            int tokens = contextWindowManager.estimateTotalTokens(conversation);

            // Then
            assertThat(tokens).isEqualTo(5); // 2 + 3
        }

        @Test
        @DisplayName("should include context summary tokens")
        void shouldIncludeContextSummaryTokens() {
            // Given
            conversation.setContextSummary("Previous summary text"); // 21 chars → 6 tokens
            conversation.getMessages().add(createMessage("user", "Hello")); // 5 chars → 2 tokens

            // When
            int tokens = contextWindowManager.estimateTotalTokens(conversation);

            // Then
            assertThat(tokens).isEqualTo(8); // 6 + 2
        }
    }

    // ========== Build Context Window Tests ==========

    @Nested
    @DisplayName("buildContextWindow")
    class BuildContextWindowTests {

        @Test
        @DisplayName("should return empty string for empty conversation")
        void shouldReturnEmptyForEmptyConversation() {
            // When
            String context = contextWindowManager.buildContextWindow(conversation);

            // Then
            assertThat(context).isEmpty();
        }

        @Test
        @DisplayName("should include all messages when under window size")
        void shouldIncludeAllMessagesWhenUnderWindowSize() {
            // Given
            conversation.getMessages().add(createMessage("user", "Hello"));
            conversation.getMessages().add(createMessage("ai", "Hi there!"));
            conversation.getMessages().add(createMessage("user", "How are you?"));

            // When
            String context = contextWindowManager.buildContextWindow(conversation);

            // Then
            assertThat(context)
                    .contains("user: Hello")
                    .contains("ai: Hi there!")
                    .contains("user: How are you?")
                    .contains("Conversation:");
        }

        @Test
        @DisplayName("should only include last N messages when over window size")
        void shouldOnlyIncludeLastNMessagesWhenOverWindowSize() {
            // Given - Add 15 messages
            for (int i = 1; i <= 15; i++) {
                String role = i % 2 == 1 ? "user" : "ai";
                conversation.getMessages().add(createMessage(role, "Message_" + i + "_end"));
            }

            // When (default window size is 10)
            String context = contextWindowManager.buildContextWindow(conversation);

            // Then - Should not contain first 5 messages (6-15 should be included)
            assertThat(context)
                    .doesNotContain("Message_1_end")
                    .doesNotContain("Message_5_end")
                    .contains("Message_6_end")
                    .contains("Message_15_end");
        }

        @Test
        @DisplayName("should include context summary when present")
        void shouldIncludeContextSummaryWhenPresent() {
            // Given
            conversation.setContextSummary("User discussed project timeline and budget");
            conversation.getMessages().add(createMessage("user", "Let's continue"));

            // When
            String context = contextWindowManager.buildContextWindow(conversation);

            // Then
            assertThat(context)
                    .contains("[Previous context summary:")
                    .contains("User discussed project timeline and budget")
                    .contains("user: Let's continue");
        }

        @Test
        @DisplayName("should respect custom window size")
        void shouldRespectCustomWindowSize() {
            // Given - Add 10 messages
            for (int i = 1; i <= 10; i++) {
                conversation.getMessages().add(createMessage("user", "Message " + i));
            }

            // When (custom window size of 3)
            String context = contextWindowManager.buildContextWindow(conversation, 3);

            // Then - Should only contain last 3 messages
            assertThat(context)
                    .doesNotContain("Message 7")
                    .contains("Message 8")
                    .contains("Message 9")
                    .contains("Message 10");
        }
    }

    // ========== Should Summarize Tests ==========

    @Nested
    @DisplayName("shouldSummarize")
    class ShouldSummarizeTests {

        @Test
        @DisplayName("should return false for empty conversation")
        void shouldReturnFalseForEmptyConversation() {
            // When
            boolean result = contextWindowManager.shouldSummarize(conversation);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when message count under window size")
        void shouldReturnFalseWhenUnderWindowSize() {
            // Given
            addMessages(8); // Under 10 (default window size)

            // When
            boolean result = contextWindowManager.shouldSummarize(conversation);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when at window size but under token threshold")
        void shouldReturnFalseWhenAtWindowSizeButUnderTokenThreshold() {
            // Given
            addMessages(10); // At window size, short messages

            // When
            boolean result = contextWindowManager.shouldSummarize(conversation);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return true when over token threshold")
        void shouldReturnTrueWhenOverTokenThreshold() {
            // Given - Add many long messages to exceed 3000 tokens
            for (int i = 0; i < 20; i++) {
                String role = i % 2 == 0 ? "user" : "ai";
                // Each message is 600 chars → 150 tokens, 20 msgs → 3000 tokens
                String content = "This is a very long message that will help exceed the token limit. ".repeat(10);
                conversation.getMessages().add(createMessage(role, content));
            }

            // When
            boolean result = contextWindowManager.shouldSummarize(conversation);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when null messages")
        void shouldReturnFalseWhenNullMessages() {
            // Given
            conversation.setMessages(null);

            // When
            boolean result = contextWindowManager.shouldSummarize(conversation);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========== Summarize Old Messages Tests ==========

    @Nested
    @DisplayName("summarizeOldMessages")
    class SummarizeOldMessagesTests {

        @Test
        @DisplayName("should return false when no messages to summarize")
        void shouldReturnFalseWhenNoMessagesToSummarize() {
            // Given
            addMessages(8); // Under window size

            // When
            boolean result = contextWindowManager.summarizeOldMessages(conversation);

            // Then
            assertThat(result).isFalse();
            verify(geminiClientService, never()).generateContent(anyString());
        }

        @Test
        @DisplayName("should summarize old messages and preserve all messages")
        void shouldSummarizeOldMessagesAndPreserveAllMessages() {
            // Given - Add 15 messages
            for (int i = 1; i <= 15; i++) {
                String role = i % 2 == 1 ? "user" : "ai";
                conversation.getMessages().add(createMessage(role, "Message " + i));
            }
            
            GeminiResponseDTO mockResponse = GeminiResponseDTO.success(
                    "Summary: The user and AI discussed messages 1-5.",
                    "gemini-2.0-flash-exp",
                    new TokenUsageDTO(100, 50, 150, 0.001),
                    500L,
                    "STOP"
            );
            when(geminiClientService.generateContent(anyString())).thenReturn(mockResponse);

            // When
            boolean result = contextWindowManager.summarizeOldMessages(conversation);

            // Then
            assertThat(result).isTrue();
            assertThat(conversation.getContextSummary()).isEqualTo("Summary: The user and AI discussed messages 1-5.");
            // CRITICAL: Messages must be preserved (no data loss)
            assertThat(conversation.getMessages()).hasSize(15); // All messages preserved
            assertThat(conversation.getMessages().get(0).get("content")).isEqualTo("Message 1"); // First message still there
            assertThat(conversation.getMessages().get(14).get("content")).isEqualTo("Message 15"); // Last message still there
            verify(geminiClientService).generateContent(anyString());
        }

        @Test
        @DisplayName("should include existing summary in summarization prompt")
        void shouldIncludeExistingSummaryInPrompt() {
            // Given
            conversation.setContextSummary("Previous important context");
            for (int i = 1; i <= 15; i++) {
                conversation.getMessages().add(createMessage("user", "Message " + i));
            }
            
            GeminiResponseDTO mockResponse = GeminiResponseDTO.success(
                    "Combined summary",
                    "gemini-2.0-flash-exp",
                    null,
                    500L,
                    "STOP"
            );
            when(geminiClientService.generateContent(anyString())).thenReturn(mockResponse);

            // When
            contextWindowManager.summarizeOldMessages(conversation);

            // Then
            verify(geminiClientService).generateContent(argThat(prompt -> 
                    prompt.contains("Previous summary: Previous important context")));
        }

        @Test
        @DisplayName("should handle Gemini failure gracefully")
        void shouldHandleGeminiFailureGracefully() {
            // Given
            for (int i = 1; i <= 15; i++) {
                conversation.getMessages().add(createMessage("user", "Message " + i));
            }
            when(geminiClientService.generateContent(anyString()))
                    .thenThrow(new RuntimeException("API failure"));

            // When
            boolean result = contextWindowManager.summarizeOldMessages(conversation);

            // Then
            assertThat(result).isFalse();
            assertThat(conversation.getMessages()).hasSize(15); // Messages unchanged
            assertThat(conversation.getContextSummary()).isNull(); // Summary not set
        }

        @Test
        @DisplayName("should return false for null messages")
        void shouldReturnFalseForNullMessages() {
            // Given
            conversation.setMessages(null);

            // When
            boolean result = contextWindowManager.summarizeOldMessages(conversation);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========== Window Messages Tests ==========

    @Nested
    @DisplayName("getWindowMessages and getOldMessages")
    class WindowMessagesTests {

        @Test
        @DisplayName("should return all messages when under window size")
        void shouldReturnAllMessagesWhenUnderWindowSize() {
            // Given
            addMessages(5);

            // When
            List<Map<String, Object>> windowMessages = contextWindowManager.getWindowMessages(conversation);

            // Then
            assertThat(windowMessages).hasSize(5);
        }

        @Test
        @DisplayName("should return only last N messages when over window size")
        void shouldReturnOnlyLastNMessagesWhenOverWindowSize() {
            // Given
            for (int i = 1; i <= 15; i++) {
                conversation.getMessages().add(createMessage("user", "Msg " + i));
            }

            // When
            List<Map<String, Object>> windowMessages = contextWindowManager.getWindowMessages(conversation);

            // Then
            assertThat(windowMessages).hasSize(10);
            assertThat(windowMessages.get(0).get("content")).isEqualTo("Msg 6");
            assertThat(windowMessages.get(9).get("content")).isEqualTo("Msg 15");
        }

        @Test
        @DisplayName("should return empty list for empty conversation")
        void shouldReturnEmptyListForEmptyConversation() {
            // When
            List<Map<String, Object>> windowMessages = contextWindowManager.getWindowMessages(conversation);

            // Then
            assertThat(windowMessages).isEmpty();
        }

        @Test
        @DisplayName("should return old messages correctly")
        void shouldReturnOldMessagesCorrectly() {
            // Given
            for (int i = 1; i <= 15; i++) {
                conversation.getMessages().add(createMessage("user", "Msg " + i));
            }

            // When
            List<Map<String, Object>> oldMessages = contextWindowManager.getOldMessages(conversation);

            // Then
            assertThat(oldMessages).hasSize(5);
            assertThat(oldMessages.get(0).get("content")).isEqualTo("Msg 1");
            assertThat(oldMessages.get(4).get("content")).isEqualTo("Msg 5");
        }

        @Test
        @DisplayName("should return empty old messages when under window size")
        void shouldReturnEmptyOldMessagesWhenUnderWindowSize() {
            // Given
            addMessages(8);

            // When
            List<Map<String, Object>> oldMessages = contextWindowManager.getOldMessages(conversation);

            // Then
            assertThat(oldMessages).isEmpty();
        }
    }

    // ========== Window Size Tests ==========

    @Nested
    @DisplayName("Window Size Configuration")
    class WindowSizeTests {

        @Test
        @DisplayName("should have default window size of 10")
        void shouldHaveDefaultWindowSize() {
            // Then
            assertThat(contextWindowManager.getWindowSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("should allow setting valid window size")
        void shouldAllowSettingValidWindowSize() {
            // When
            contextWindowManager.setWindowSize(15);

            // Then
            assertThat(contextWindowManager.getWindowSize()).isEqualTo(15);
        }

        @Test
        @DisplayName("should enforce minimum window size")
        void shouldEnforceMinimumWindowSize() {
            // When
            contextWindowManager.setWindowSize(2);

            // Then
            assertThat(contextWindowManager.getWindowSize()).isEqualTo(5); // Min is 5
        }

        @Test
        @DisplayName("should enforce maximum window size")
        void shouldEnforceMaximumWindowSize() {
            // When
            contextWindowManager.setWindowSize(50);

            // Then
            assertThat(contextWindowManager.getWindowSize()).isEqualTo(20); // Max is 20
        }
    }

    // ========== Format Messages Tests ==========

    @Nested
    @DisplayName("formatMessages")
    class FormatMessagesTests {

        @Test
        @DisplayName("should format messages correctly")
        void shouldFormatMessagesCorrectly() {
            // Given
            List<Map<String, Object>> messages = List.of(
                    createMessage("user", "Hello"),
                    createMessage("ai", "Hi there!")
            );

            // When
            String formatted = contextWindowManager.formatMessages(messages);

            // Then
            assertThat(formatted)
                    .contains("user: Hello")
                    .contains("ai: Hi there!");
        }

        @Test
        @DisplayName("should return empty string for null messages")
        void shouldReturnEmptyStringForNullMessages() {
            // When
            String formatted = contextWindowManager.formatMessages(null);

            // Then
            assertThat(formatted).isEmpty();
        }

        @Test
        @DisplayName("should return empty string for empty list")
        void shouldReturnEmptyStringForEmptyList() {
            // When
            String formatted = contextWindowManager.formatMessages(Collections.emptyList());

            // Then
            assertThat(formatted).isEmpty();
        }

        @Test
        @DisplayName("should handle missing role or content")
        void shouldHandleMissingRoleOrContent() {
            // Given
            Map<String, Object> msgWithMissingContent = new HashMap<>();
            msgWithMissingContent.put("role", "user");
            // content is missing, will show as "null" when converted via String.valueOf()
            List<Map<String, Object>> messages = List.of(msgWithMissingContent);

            // When
            String formatted = contextWindowManager.formatMessages(messages);

            // Then - Check that it doesn't crash and produces some output
            assertThat(formatted)
                    .isNotBlank()
                    .contains("user:");
        }
    }
}
