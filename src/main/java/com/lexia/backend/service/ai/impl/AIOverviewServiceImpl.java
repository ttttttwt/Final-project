package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.ai.AIUsageOverview;
import com.lexia.backend.repository.AIAlertRepository;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.service.ai.AIOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIOverviewServiceImpl implements AIOverviewService {

    private final AIUsageLogRepository usageLogRepository;
    private final AIAlertRepository alertRepository;

    @Override
    public AIUsageOverview getOverview() {
        Instant startOfDay = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        
        AIUsageOverview overview = new AIUsageOverview();
        overview.setTotalRequestsToday(usageLogRepository.countByCreatedAtGreaterThanEqual(startOfDay));
        overview.setTotalCostToday(usageLogRepository.sumCostSince(startOfDay));
        if (overview.getTotalCostToday() == null) overview.setTotalCostToday(BigDecimal.ZERO);
        
        overview.setActiveUsersToday(usageLogRepository.countActiveUsersSince(startOfDay));
        
        // Top Users - filter out null userIds (e.g., scenario generation without user context)
        List<Object[]> topUsersData = usageLogRepository.findTopUsersSince(startOfDay, PageRequest.of(0, 5));
        List<AIUsageOverview.TopAIUser> topUsers = topUsersData.stream()
            .filter(obj -> obj[0] != null) // Skip records with null userId
            .map(obj -> {
                AIUsageOverview.TopAIUser user = new AIUsageOverview.TopAIUser();
                user.setUserId((UUID) obj[0]);
                user.setTotalRequests((Long) obj[1]);
                user.setTotalCost((BigDecimal) obj[2]);
                user.setLastUsedAt((Instant) obj[3]);
                user.setUserEmail("user-" + user.getUserId().toString().substring(0, 8) + "..."); // Placeholder
                user.setUserFullName("User " + user.getUserId().toString().substring(0, 8)); // Placeholder
                return user;
            }).collect(Collectors.toList());
        overview.setTopUsers(topUsers);
        
        // Recent Alerts
        overview.setRecentAlerts(alertRepository.findByIsReadFalseOrderByCreatedAtDesc().stream().limit(5).collect(Collectors.toList()));
        
        // Feature Usage Chart
        List<Object[]> featureUsage = usageLogRepository.countGlobalByContentTypeSince(startOfDay);
        AIUsageOverview.FeatureUsageChart chart = new AIUsageOverview.FeatureUsageChart();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        
        for (Object[] row : featureUsage) {
            labels.add((String) row[0]);
            data.add((Long) row[1]);
        }
        chart.setLabels(labels);
        chart.setData(data);
        overview.setFeatureUsageChart(chart);
        
        return overview;
    }
}
