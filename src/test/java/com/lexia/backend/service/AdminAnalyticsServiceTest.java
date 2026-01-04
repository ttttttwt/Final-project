package com.lexia.backend.service;

import com.lexia.backend.dto.admin.AnalyticsDTO.AnalyticsResponse;
import com.lexia.backend.dto.admin.AnalyticsDTO.OverviewStats;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.PaymentRepository;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIUsageLogRepository aiUsageLogRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminAnalyticsService adminAnalyticsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAnalytics_ShouldReturnData() {
        // Mock repository calls for Overview Stats
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countActiveUsersAfter(any(LocalDateTime.class))).thenReturn(50L);
        when(userRepository.countUsersCreatedAfter(any(LocalDateTime.class))).thenReturn(10L);
        
        when(subscriptionRepository.countByStatusAndPlanType(any(), any())).thenReturn(20L);
        
        when(paymentRepository.sumTotalRevenue()).thenReturn(BigDecimal.TEN);
        when(paymentRepository.sumRevenueAfter(any(LocalDateTime.class))).thenReturn(BigDecimal.ONE);
        
        when(aiUsageLogRepository.count()).thenReturn(1000L);
        when(aiUsageLogRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(100L);
        
        // For monthly stats
        when(userRepository.countUsersCreatedBetween(any(), any())).thenReturn(5L);
        when(userRepository.countActiveUsersBetween(any(), any())).thenReturn(40L);
        when(paymentRepository.sumRevenueBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(aiUsageLogRepository.countByCreatedAtBetween(any(Instant.class), any(Instant.class))).thenReturn(50L);

        // For user distribution
        when(userRepository.countByLevel(any())).thenReturn(10L);

        // For AI Usage stats
        when(aiUsageLogRepository.countRoleplayRequestsAfter(any(Instant.class))).thenReturn(10L);
        when(aiUsageLogRepository.countGrammarRequestsAfter(any(Instant.class))).thenReturn(15L);
        when(aiUsageLogRepository.countFlashcardRequestsAfter(any(Instant.class))).thenReturn(20L);
        when(aiUsageLogRepository.countCustomMaterialRequestsAfter(any(Instant.class))).thenReturn(5L);
        when(aiUsageLogRepository.countBySuccessAndCreatedAtAfter(any(Boolean.class), any(Instant.class))).thenReturn(90L);
        when(aiUsageLogRepository.averageResponseTimeAfter(any(Instant.class))).thenReturn(150.0);
        when(aiUsageLogRepository.sumTokensUsedBetween(any(Instant.class), any(Instant.class))).thenReturn(1000L);

        AnalyticsResponse response = adminAnalyticsService.getAnalytics();

        assertNotNull(response);
        assertNotNull(response.getOverview());
        assertNotNull(response.getMonthlyStats());
        assertNotNull(response.getAiUsage());
    }
}
