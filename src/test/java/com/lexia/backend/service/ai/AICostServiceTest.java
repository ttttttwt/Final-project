package com.lexia.backend.service.ai;

import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.service.ai.impl.AICostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AICostServiceTest {

    @Mock
    private AIUsageLogRepository usageLogRepository;

    @Mock
    private AIConfigService configService;

    @InjectMocks
    private AICostServiceImpl costService;

    @Test
    void getCostAnalytics_ShouldReturnData() {
        when(usageLogRepository.sumCostSince(any(Instant.class))).thenReturn(BigDecimal.TEN);

        java.util.List<Object[]> modelCosts = new java.util.ArrayList<>();
        modelCosts.add(new Object[] { "gpt-4", BigDecimal.ONE });
        when(usageLogRepository.sumCostByModelSince(any(Instant.class))).thenReturn(modelCosts);

        java.util.List<Object[]> featureCosts = new java.util.ArrayList<>();
        featureCosts.add(new Object[] { "chat", BigDecimal.ONE });
        when(usageLogRepository.sumCostByContentTypeSince(any(Instant.class))).thenReturn(featureCosts);

        // Mock config service
        com.lexia.backend.entity.AIConfig mockConfig = new com.lexia.backend.entity.AIConfig();
        mockConfig.setConfigValue("100.0");
        when(configService.getConfig("global.monthlyBudgetLimit")).thenReturn(mockConfig);

        Map<String, Object> result = costService.getCostAnalytics("monthly");

        assertEquals(BigDecimal.TEN, result.get("totalCost"));
        assertTrue(((Map) result.get("costByModel")).containsKey("gpt-4"));
    }
}
