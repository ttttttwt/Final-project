package com.lexia.backend.service.ai.impl;

import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.service.ai.AICostService;
import com.lexia.backend.service.ai.AIConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AICostServiceImpl implements AICostService {

    private final AIUsageLogRepository usageLogRepository;
    private final AIConfigService configService;

    @Override
    public Map<String, Object> getCostAnalytics(String period) {
        Instant start = getStartDate(period);
        
        BigDecimal totalCost = usageLogRepository.sumCostSince(start);
        if (totalCost == null) totalCost = BigDecimal.ZERO;

        List<Object[]> costByModel = usageLogRepository.sumCostByModelSince(start);
        List<Object[]> costByFeature = usageLogRepository.sumCostByContentTypeSince(start);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCost", totalCost);
        result.put("costByModel", convertToMap(costByModel));
        result.put("costByFeature", convertToMap(costByFeature));
        
        // Add budget info
        double budgetLimit = Double.parseDouble(configService.getConfig("global.monthlyBudgetLimit").getConfigValue());
        result.put("budgetLimit", budgetLimit);
        
        double percentage = budgetLimit > 0 ? (totalCost.doubleValue() / budgetLimit) * 100 : 0;
        result.put("budgetUsedPercentage", percentage);
        
        return result;
    }

    @Override
    public BigDecimal getTotalCost(Instant start, Instant end) {
        return usageLogRepository.sumCostSince(start);
    }

    @Override
    public Map<String, BigDecimal> getCostByModel(Instant start, Instant end) {
        List<Object[]> results = usageLogRepository.sumCostByModelSince(start);
        return convertToMap(results);
    }

    @Override
    public Map<String, Object> getProjection() {
        Instant now = Instant.now();
        Instant startOfMonth = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        
        BigDecimal currentMonthCost = usageLogRepository.sumCostSince(startOfMonth);
        if (currentMonthCost == null) currentMonthCost = BigDecimal.ZERO;
        
        long daysPassed = ChronoUnit.DAYS.between(startOfMonth, now) + 1;
        long totalDaysInMonth = java.time.YearMonth.from(now.atZone(java.time.ZoneId.systemDefault())).lengthOfMonth();
        long daysRemaining = totalDaysInMonth - daysPassed;
        
        BigDecimal avgDailyCost = daysPassed > 0 ? currentMonthCost.divide(BigDecimal.valueOf(daysPassed), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal projectedCost = currentMonthCost.add(avgDailyCost.multiply(BigDecimal.valueOf(daysRemaining)));
        
        Map<String, Object> result = new HashMap<>();
        result.put("currentMonthCost", currentMonthCost);
        result.put("projectedMonthCost", projectedCost);
        result.put("daysRemaining", daysRemaining);
        result.put("averageDailyCost", avgDailyCost);
        return result;
    }

    @Override
    public byte[] exportReport(String period) {
        StringBuilder csv = new StringBuilder("Category,Cost\n");
        Map<String, Object> analytics = getCostAnalytics(period);
        
        csv.append("Total,").append(analytics.get("totalCost")).append("\n");
        
        Map<String, BigDecimal> byModel = (Map<String, BigDecimal>) analytics.get("costByModel");
        if (byModel != null) {
            byModel.forEach((k, v) -> csv.append("Model: ").append(k).append(",").append(v).append("\n"));
        }
        
        return csv.toString().getBytes();
    }

    @Override
    public List<Map<String, Object>> getCostsByUser(String period, int limit) {
        Instant start = getStartDate(period);
        List<Object[]> results = usageLogRepository.sumCostByUserSince(start, org.springframework.data.domain.PageRequest.of(0, limit));
        
        return results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", obj[0]);
            map.put("totalCost", obj[1]);
            map.put("totalRequests", obj[2]);
            map.put("userEmail", "user-" + obj[0].toString().substring(0, 8) + "..."); 
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> updateBudget(double budget) {
        configService.updateConfig("global.monthlyBudgetLimit", String.valueOf(budget));
        return Map.of("budget", budget);
    }

    private Instant getStartDate(String period) {
        Instant now = Instant.now();
        if (period == null) return now.minus(30, ChronoUnit.DAYS);
        switch (period.toLowerCase()) {
            case "daily": return now.minus(1, ChronoUnit.DAYS);
            case "weekly": return now.minus(7, ChronoUnit.DAYS);
            case "monthly": return now.minus(30, ChronoUnit.DAYS);
            default: return now.minus(30, ChronoUnit.DAYS);
        }
    }

    private Map<String, BigDecimal> convertToMap(List<Object[]> list) {
        return list.stream().collect(Collectors.toMap(
                obj -> (String) obj[0],
                obj -> (BigDecimal) obj[1]
        ));
    }
}
