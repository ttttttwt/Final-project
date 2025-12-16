package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.AIUsageOverview;
import com.lexia.backend.repository.AIAlertRepository;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.service.ai.impl.AIOverviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIOverviewServiceTest {

    @Mock
    private AIUsageLogRepository usageLogRepository;

    @Mock
    private AIAlertRepository alertRepository;

    @InjectMocks
    private AIOverviewServiceImpl overviewService;

    @Test
    void getOverview_ShouldReturnData() {
        when(usageLogRepository.countByCreatedAtGreaterThanEqual(any(Instant.class))).thenReturn(100L);
        when(usageLogRepository.sumCostSince(any(Instant.class))).thenReturn(BigDecimal.TEN);
        when(usageLogRepository.countActiveUsersSince(any(Instant.class))).thenReturn(50L);
        
        List<Object[]> topUsers = new java.util.ArrayList<>();
        topUsers.add(new Object[]{UUID.randomUUID(), 10L, BigDecimal.ONE, Instant.now()});
        when(usageLogRepository.findTopUsersSince(any(Instant.class), any(Pageable.class))).thenReturn(topUsers);
        
        when(alertRepository.findByIsReadFalseOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        
        List<Object[]> featureUsage = new java.util.ArrayList<>();
        featureUsage.add(new Object[]{"chat", 20L});
        when(usageLogRepository.countGlobalByContentTypeSince(any(Instant.class))).thenReturn(featureUsage);

        AIUsageOverview result = overviewService.getOverview();

        assertEquals(100L, result.getTotalRequestsToday());
        assertEquals(BigDecimal.TEN, result.getTotalCostToday());
        assertEquals(50L, result.getActiveUsersToday());
        assertEquals(1, result.getTopUsers().size());
        assertEquals(1, result.getFeatureUsageChart().getLabels().size());
    }
}
