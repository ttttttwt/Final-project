package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AiUsageTrackerImpl service.
 * Tests usage tracking, cost calculation, and quota management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiUsageTracker Service Tests")
class AiUsageTrackerImplTest {

    @Mock
    private AIUsageLogRepository aiUsageLogRepository;

    @Mock
    private UserAiQuotaRepository userAiQuotaRepository;

    @InjectMocks
    private AiUsageTrackerImpl aiUsageTracker;

    private UUID testUserId;
    private AiUsageTrackingRequest successRequest;
    private AiUsageTrackingRequest failureRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        successRequest = AiUsageTrackingRequest.builder()
                .userId(testUserId)
                .contentType(AiUsageTracker.CONTENT_TYPE_ROLEPLAY)
                .modelId("gemini-2.0-flash-exp")
                .inputTokens(500)
                .outputTokens(300)
                .responseTimeMs(1500)
                .success(true)
                .metadata(Map.of("cefrLevel", "B1", "domain", "meetings"))
                .promptVersion("roleplay_scenario_v1")
                .build();

        failureRequest = AiUsageTrackingRequest.failure(
                testUserId,
                AiUsageTracker.CONTENT_TYPE_GRAMMAR,
                "gemini-1.5-flash",
                5000,
                "Timeout error"
        );
    }

    @Nested
    @DisplayName("trackUsage() Tests")
    class TrackUsageTests {

        @Test
        @DisplayName("Should track successful usage and save to repository")
        void trackUsage_Success_SavesLog() {
            // Arrange
            when(aiUsageLogRepository.save(any(AIUsageLog.class)))
                    .thenAnswer(invocation -> {
                        AIUsageLog log = invocation.getArgument(0);
                        log.setId(1L);
                        return log;
                    });
            when(userAiQuotaRepository.incrementUsage(any(UUID.class), anyString()))
                    .thenReturn(1);

            // Act
            AIUsageLog result = aiUsageTracker.trackUsage(successRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getContentType()).isEqualTo("roleplay");
            assertThat(result.getModelId()).isEqualTo("gemini-2.0-flash-exp");
            assertThat(result.getInputTokens()).isEqualTo(500);
            assertThat(result.getOutputTokens()).isEqualTo(300);
            assertThat(result.getResponseTimeMs()).isEqualTo(1500);
            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getPromptVersion()).isEqualTo("roleplay_scenario_v1");
            assertThat(result.getRequestMetadata()).containsEntry("cefrLevel", "B1");
            
            verify(aiUsageLogRepository).save(any(AIUsageLog.class));
            verify(userAiQuotaRepository).incrementUsage(testUserId, "roleplay");
        }

        @Test
        @DisplayName("Should track failed usage without incrementing quota")
        void trackUsage_Failure_DoesNotIncrementQuota() {
            // Arrange
            when(aiUsageLogRepository.save(any(AIUsageLog.class)))
                    .thenAnswer(invocation -> {
                        AIUsageLog log = invocation.getArgument(0);
                        log.setId(2L);
                        return log;
                    });

            // Act
            AIUsageLog result = aiUsageTracker.trackUsage(failureRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("Timeout error");
            
            verify(aiUsageLogRepository).save(any(AIUsageLog.class));
            verify(userAiQuotaRepository, never()).incrementUsage(any(), any());
        }

        @Test
        @DisplayName("Should create quota record if not exists")
        void trackUsage_NoQuotaExists_CreatesNewQuota() {
            // Arrange
            when(aiUsageLogRepository.save(any(AIUsageLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(userAiQuotaRepository.incrementUsage(any(UUID.class), anyString()))
                    .thenReturn(0); // No existing quota
            when(userAiQuotaRepository.save(any(UserAiQuota.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            aiUsageTracker.trackUsage(successRequest);

            // Assert
            verify(userAiQuotaRepository).save(any(UserAiQuota.class));
        }
    }

    @Nested
    @DisplayName("calculateCost() Tests")
    class CalculateCostTests {

        @Test
        @DisplayName("Should calculate cost for Gemini 2.0 Flash")
        void calculateCost_Gemini2Flash_CorrectCalculation() {
            // Gemini 2.0 Flash: $0.075/M input, $0.30/M output
            // 1000 input tokens = 0.000075, 500 output tokens = 0.00015
            BigDecimal cost = aiUsageTracker.calculateCost(
                    "gemini-2.0-flash-exp", 1000, 500);
            
            // Expected: (1000 * 0.075 / 1000000) + (500 * 0.30 / 1000000) = 0.000225
            assertThat(cost).isEqualByComparingTo(new BigDecimal("0.000225"));
        }

        @Test
        @DisplayName("Should calculate cost for Gemini 1.5 Pro")
        void calculateCost_Gemini15Pro_CorrectCalculation() {
            // Gemini 1.5 Pro: $1.25/M input, $5.00/M output
            BigDecimal cost = aiUsageTracker.calculateCost(
                    "gemini-1.5-pro", 10000, 5000);
            
            // Expected: (10000 * 1.25 / 1000000) + (5000 * 5.00 / 1000000) = 0.0375
            assertThat(cost).isEqualByComparingTo(new BigDecimal("0.037500"));
        }

        @Test
        @DisplayName("Should use default pricing for unknown model")
        void calculateCost_UnknownModel_UsesDefaultPricing() {
            // Default: $0.10/M input, $0.30/M output
            BigDecimal cost = aiUsageTracker.calculateCost(
                    "unknown-model", 1000000, 1000000);
            
            // Expected: (1M * 0.10 / 1M) + (1M * 0.30 / 1M) = 0.40
            assertThat(cost).isEqualByComparingTo(new BigDecimal("0.400000"));
        }

        @Test
        @DisplayName("Should handle null model ID")
        void calculateCost_NullModel_UsesDefaultPricing() {
            BigDecimal cost = aiUsageTracker.calculateCost(null, 1000, 1000);
            
            assertThat(cost).isNotNull();
            assertThat(cost.compareTo(BigDecimal.ZERO)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return zero for zero tokens")
        void calculateCost_ZeroTokens_ReturnsZero() {
            BigDecimal cost = aiUsageTracker.calculateCost("gemini-2.0-flash-exp", 0, 0);
            
            assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Quota Check Tests")
    class QuotaCheckTests {

        @Test
        @DisplayName("Should return false when quota not exceeded")
        void isDailyQuotaExceeded_UnderLimit_ReturnsFalse() {
            // Arrange
            UserAiQuota quota = createTestQuota(10, 100);
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.of(quota));

            // Act
            boolean exceeded = aiUsageTracker.isDailyQuotaExceeded(testUserId, "roleplay");

            // Assert
            assertThat(exceeded).isFalse();
        }

        @Test
        @DisplayName("Should return true when quota exceeded")
        void isDailyQuotaExceeded_OverLimit_ReturnsTrue() {
            // Arrange
            UserAiQuota quota = createTestQuota(20, 200);
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.of(quota));

            // Act
            boolean exceeded = aiUsageTracker.isDailyQuotaExceeded(testUserId, "roleplay");

            // Assert
            assertThat(exceeded).isTrue();
        }

        @Test
        @DisplayName("Should return true when user is suspended")
        void isDailyQuotaExceeded_Suspended_ReturnsTrue() {
            // Arrange
            UserAiQuota quota = createTestQuota(0, 0);
            quota.setSuspended(true);
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.of(quota));

            // Act
            boolean exceeded = aiUsageTracker.isDailyQuotaExceeded(testUserId, "roleplay");

            // Assert
            assertThat(exceeded).isTrue();
        }

        @Test
        @DisplayName("Should create quota if not exists and return false")
        void isDailyQuotaExceeded_NoQuota_CreatesAndReturnsFalse() {
            // Arrange
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.empty());
            when(userAiQuotaRepository.save(any(UserAiQuota.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            boolean exceeded = aiUsageTracker.isDailyQuotaExceeded(testUserId, "roleplay");

            // Assert
            assertThat(exceeded).isFalse();
            verify(userAiQuotaRepository).save(any(UserAiQuota.class));
        }
    }

    @Nested
    @DisplayName("Remaining Quota Tests")
    class RemainingQuotaTests {

        @Test
        @DisplayName("Should return correct remaining daily quota")
        void getRemainingDailyQuota_ReturnsCorrectValue() {
            // Arrange
            UserAiQuota quota = createTestQuota(5, 50);
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.of(quota));

            // Act
            int remaining = aiUsageTracker.getRemainingDailyQuota(testUserId, "roleplay");

            // Assert - Default limit is 20, used is 5, so 15 remaining
            assertThat(remaining).isEqualTo(15);
        }

        @Test
        @DisplayName("Should return zero when suspended")
        void getRemainingDailyQuota_Suspended_ReturnsZero() {
            // Arrange
            UserAiQuota quota = createTestQuota(0, 0);
            quota.setSuspended(true);
            when(userAiQuotaRepository.findByUserId(testUserId))
                    .thenReturn(Optional.of(quota));

            // Act
            int remaining = aiUsageTracker.getRemainingDailyQuota(testUserId, "roleplay");

            // Assert
            assertThat(remaining).isZero();
        }
    }

    @Nested
    @DisplayName("Usage Statistics Tests")
    class UsageStatisticsTests {

        @Test
        @DisplayName("Should get daily usage count")
        void getDailyUsageCount_ReturnsCorrectCount() {
            // Arrange
            when(aiUsageLogRepository.countByUserIdAndContentTypeAndCreatedAtGreaterThanEqual(
                    eq(testUserId), eq("roleplay"), any(Instant.class)))
                    .thenReturn(5L);

            // Act
            long count = aiUsageTracker.getDailyUsageCount(testUserId, "roleplay");

            // Assert
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should get monthly usage count")
        void getMonthlyUsageCount_ReturnsCorrectCount() {
            // Arrange
            when(aiUsageLogRepository.countByUserIdAndContentTypeAndCreatedAtGreaterThanEqual(
                    eq(testUserId), eq("grammar"), any(Instant.class)))
                    .thenReturn(45L);

            // Act
            long count = aiUsageTracker.getMonthlyUsageCount(testUserId, "grammar");

            // Assert
            assertThat(count).isEqualTo(45L);
        }
    }

    // ========== Helper Methods ==========

    private UserAiQuota createTestQuota(int dailyUsed, int monthlyUsed) {
        UserAiQuota quota = UserAiQuota.createForUser(testUserId);
        
        // Set feature usage
        Map<String, Map<String, Integer>> featureUsage = new HashMap<>();
        featureUsage.put("roleplay", new HashMap<>(Map.of("daily", dailyUsed, "monthly", monthlyUsed)));
        featureUsage.put("grammar", new HashMap<>(Map.of("daily", 0, "monthly", 0)));
        featureUsage.put("flashcard", new HashMap<>(Map.of("daily", 0, "monthly", 0)));
        quota.setFeatureUsage(featureUsage);
        
        quota.setDailyUsed(dailyUsed);
        quota.setMonthlyUsed(monthlyUsed);
        
        return quota;
    }
}
