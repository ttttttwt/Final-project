package com.lexia.backend.service;

import com.lexia.backend.dto.AIUsageLogDTO;
import com.lexia.backend.dto.AIUsageStatsDTO;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.impl.AIUsageLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIUsageLogServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AIUsageLogService Tests")
class AIUsageLogServiceTest {

    @Mock
    private AIUsageLogRepository aiUsageLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AIUsageLogServiceImpl aiUsageLogService;

    private UUID userId;
    private User testUser;
    private AIUsageLog testLog;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testLog = AIUsageLog.builder()
                .id(1L)
                .userId(userId)
                .featureName("MAGIC_FLASHCARD")
                .inputTokens(100)
                .outputTokens(50)
                .estimatedCostUsd(new BigDecimal("0.000250"))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should get paginated AI usage logs")
    void getLogs_ShouldReturnPaginatedLogs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AIUsageLog> logs = Arrays.asList(testLog);
        Page<AIUsageLog> page = new PageImpl<>(logs, pageable, 1);

        when(aiUsageLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Page<AIUsageLogDTO> result = aiUsageLogService.getLogs(null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFeatureName()).isEqualTo("MAGIC_FLASHCARD");
        assertThat(result.getContent().get(0).getUserEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should get AI usage stats for today")
    void getStats_ShouldReturnStatsForToday() {
        // Given
        when(aiUsageLogRepository.countByCreatedAtGreaterThanEqual(any(Instant.class))).thenReturn(10L);
        List<Object[]> tokenResult = new java.util.ArrayList<>();
        tokenResult.add(new Object[] { 1000L, 500L });
        when(aiUsageLogRepository.sumTokensSince(any(Instant.class))).thenReturn(tokenResult);
        when(aiUsageLogRepository.sumCostSince(any(Instant.class))).thenReturn(new BigDecimal("0.005000"));
        List<Object[]> featureResult = new java.util.ArrayList<>();
        featureResult.add(new Object[] { "MAGIC_FLASHCARD", 5L });
        featureResult.add(new Object[] { "ROLEPLAY", 5L });
        when(aiUsageLogRepository.countByFeatureNameSince(any(Instant.class))).thenReturn(featureResult);

        // When
        AIUsageStatsDTO result = aiUsageLogService.getStats("today");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPeriod()).isEqualTo("today");
        assertThat(result.getStats().getTotalCalls()).isEqualTo(10L);
        assertThat(result.getStats().getTotalInputTokens()).isEqualTo(1000L);
        assertThat(result.getStats().getTotalOutputTokens()).isEqualTo(500L);
        assertThat(result.getStats().getCallsByFeature()).containsKey("MAGIC_FLASHCARD");
    }

    @Test
    @DisplayName("Should log AI usage")
    void logUsage_ShouldCreateAndReturnLog() {
        // Given
        AIUsageLog savedLog = AIUsageLog.builder()
                .id(1L)
                .userId(userId)
                .featureName("ROLEPLAY")
                .inputTokens(200)
                .outputTokens(100)
                .estimatedCostUsd(new BigDecimal("0.000500"))
                .createdAt(Instant.now())
                .build();

        when(aiUsageLogRepository.save(any(AIUsageLog.class))).thenReturn(savedLog);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        AIUsageLogDTO result = aiUsageLogService.logUsage(userId, "ROLEPLAY", 200, 100);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFeatureName()).isEqualTo("ROLEPLAY");
        assertThat(result.getInputTokens()).isEqualTo(200);
        assertThat(result.getOutputTokens()).isEqualTo(100);
        assertThat(result.getUserEmail()).isEqualTo("test@example.com");

        verify(aiUsageLogRepository).save(any(AIUsageLog.class));
    }

    @Test
    @DisplayName("Should handle empty stats result")
    void getStats_ShouldHandleEmptyResults() {
        // Given
        when(aiUsageLogRepository.countByCreatedAtGreaterThanEqual(any(Instant.class))).thenReturn(0L);
        List<Object[]> emptyTokenResult = new java.util.ArrayList<>();
        emptyTokenResult.add(new Object[] { null, null });
        when(aiUsageLogRepository.sumTokensSince(any(Instant.class))).thenReturn(emptyTokenResult);
        when(aiUsageLogRepository.sumCostSince(any(Instant.class))).thenReturn(null);
        when(aiUsageLogRepository.countByFeatureNameSince(any(Instant.class))).thenReturn(new java.util.ArrayList<>());

        // When
        AIUsageStatsDTO result = aiUsageLogService.getStats("month");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPeriod()).isEqualTo("month");
        assertThat(result.getStats().getTotalCalls()).isEqualTo(0);
        assertThat(result.getStats().getTotalCost()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getStats().getCallsByFeature()).isEmpty();
    }

    @Test
    @DisplayName("Should filter logs by feature name")
    void getLogs_ShouldFilterByFeatureName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AIUsageLog> logs = Arrays.asList(testLog);
        Page<AIUsageLog> page = new PageImpl<>(logs, pageable, 1);

        when(aiUsageLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Page<AIUsageLogDTO> result = aiUsageLogService.getLogs("MAGIC_FLASHCARD", null, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(aiUsageLogRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should map contentType to featureName when featureName is null")
    void getLogs_ShouldMapContentTypeToFeatureName() {
        // Given
        AIUsageLog logWithContentType = AIUsageLog.builder()
                .id(2L)
                .userId(userId)
                .featureName(null)
                .contentType("magic_flashcard")
                .inputTokens(100)
                .outputTokens(50)
                .estimatedCostUsd(new BigDecimal("0.000250"))
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        List<AIUsageLog> logs = Arrays.asList(logWithContentType);
        Page<AIUsageLog> page = new PageImpl<>(logs, pageable, 1);

        when(aiUsageLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Page<AIUsageLogDTO> result = aiUsageLogService.getLogs(null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFeatureName()).isEqualTo("MAGIC_FLASHCARD");
    }
}
