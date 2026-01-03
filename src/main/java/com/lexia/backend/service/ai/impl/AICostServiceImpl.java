package com.lexia.backend.service.ai.impl;

import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.ai.AICostService;
import com.lexia.backend.service.ai.AIConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AICostServiceImpl implements AICostService {

    private final AIUsageLogRepository usageLogRepository;
    private final AIConfigService configService;
    private final UserAiQuotaRepository quotaRepository;

    @Override
    public Map<String, Object> getCostAnalytics(String period) {
        Instant start = getStartDate(period);
        
        BigDecimal totalCost = usageLogRepository.sumCostSince(start);
        if (totalCost == null) totalCost = BigDecimal.ZERO;

        // Get total requests count
        long totalRequests = usageLogRepository.countByCreatedAtAfter(start);
        
        // Get active users count
        long activeUsers = usageLogRepository.countActiveUsersSince(start);
        
        // Get token usage
        List<Object[]> tokenData = usageLogRepository.sumTokensSince(start);
        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        if (tokenData != null && !tokenData.isEmpty()) {
            Object[] tokens = tokenData.get(0);
            totalInputTokens = ((Number) tokens[0]).longValue();
            totalOutputTokens = ((Number) tokens[1]).longValue();
        }
        
        // Calculate average cost per request
        BigDecimal averageCostPerRequest = totalRequests > 0 
            ? totalCost.divide(BigDecimal.valueOf(totalRequests), 6, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
            
        // Calculate average cost per user
        BigDecimal averageCostPerUser = activeUsers > 0
            ? totalCost.divide(BigDecimal.valueOf(activeUsers), 6, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        List<Object[]> costByFeatureRaw = usageLogRepository.sumCostByContentTypeSince(start);
        List<Map<String, Object>> costByFeature = buildFeatureCostBreakdown(costByFeatureRaw, totalCost, start);
        
        // Build daily costs data
        List<Map<String, Object>> dailyCosts = buildDailyCosts(start);
        
        // Get cost by plan (free vs pro)
        Map<String, Object> costByPlan = buildCostByPlan(start);

        Map<String, Object> result = new HashMap<>();
        result.put("period", period != null ? period : "month");
        result.put("totalCost", totalCost);
        result.put("totalRequests", totalRequests);
        result.put("totalInputTokens", totalInputTokens);
        result.put("totalOutputTokens", totalOutputTokens);
        result.put("averageCostPerRequest", averageCostPerRequest);
        result.put("averageCostPerUser", averageCostPerUser);
        result.put("activeUsers", activeUsers);
        result.put("costByFeature", costByFeature);
        result.put("dailyCosts", dailyCosts);
        result.put("costByPlan", costByPlan);
        
        // Add budget info
        double budgetLimit = Double.parseDouble(configService.getConfig("global.monthlyBudgetLimit").getConfigValue());
        result.put("budgetLimit", budgetLimit);
        
        double percentage = budgetLimit > 0 ? (totalCost.doubleValue() / budgetLimit) * 100 : 0;
        result.put("budgetUsedPercentage", percentage);
        
        // Add projected monthly cost
        Map<String, Object> projection = getProjection();
        result.put("projectedMonthlyCost", projection.get("projectedMonthCost"));
        
        return result;
    }
    
    private List<Map<String, Object>> buildFeatureCostBreakdown(List<Object[]> costByFeatureRaw, BigDecimal totalCost, Instant start) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Get request counts by feature
        List<Object[]> requestsByFeature = usageLogRepository.countGlobalByContentTypeSince(start);
        Map<String, Long> requestCountMap = new HashMap<>();
        for (Object[] obj : requestsByFeature) {
            requestCountMap.put((String) obj[0], ((Number) obj[1]).longValue());
        }
        
        for (Object[] obj : costByFeatureRaw) {
            String featureName = (String) obj[0];
            BigDecimal cost = (BigDecimal) obj[1];
            long requests = requestCountMap.getOrDefault(featureName, 0L);
            
            Map<String, Object> feature = new HashMap<>();
            feature.put("featureName", featureName);
            feature.put("totalCost", cost);
            feature.put("totalRequests", requests);
            feature.put("totalInputTokens", 0); // Could be calculated per feature if needed
            feature.put("totalOutputTokens", 0);
            feature.put("averageCostPerRequest", requests > 0 
                ? cost.divide(BigDecimal.valueOf(requests), 6, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO);
            feature.put("percentage", totalCost.compareTo(BigDecimal.ZERO) > 0 
                ? cost.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
            result.add(feature);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> buildDailyCosts(Instant start) {
        List<Map<String, Object>> dailyCosts = new ArrayList<>();
        
        // Use actual query to get daily costs
        List<Object[]> dailyData = usageLogRepository.getDailyCostsSince(start);
        
        for (Object[] row : dailyData) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", row[0] != null ? row[0].toString() : "");
            day.put("totalCost", row[1] != null ? row[1] : BigDecimal.ZERO);
            day.put("totalRequests", row[2] != null ? ((Number) row[2]).longValue() : 0L);
            day.put("rolePlayCost", row[3] != null ? row[3] : BigDecimal.ZERO);
            day.put("grammarCost", row[4] != null ? row[4] : BigDecimal.ZERO);
            day.put("flashcardCost", row[5] != null ? row[5] : BigDecimal.ZERO);
            dailyCosts.add(day);
        }
        
        return dailyCosts;
    }
    
    private Map<String, Object> buildCostByPlan(Instant start) {
        Map<String, Object> result = new HashMap<>();
        
        // Initialize with zeros
        BigDecimal freeCost = BigDecimal.ZERO;
        BigDecimal proCost = BigDecimal.ZERO;
        long freeRequests = 0;
        long proRequests = 0;
        long freeUsers = 0;
        long proUsers = 0;
        
        // Get actual data from query
        List<Object[]> planData = usageLogRepository.getCostByPlanSince(start);
        
        for (Object[] row : planData) {
            String planType = row[0] != null ? row[0].toString() : "FREE";
            BigDecimal cost = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            long requests = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long users = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            
            if ("MONTHLY".equals(planType) || "YEARLY".equals(planType)) {
                proCost = proCost.add(cost);
                proRequests += requests;
                proUsers += users;
            } else {
                freeCost = freeCost.add(cost);
                freeRequests += requests;
                freeUsers += users;
            }
        }
        
        result.put("freeCost", freeCost);
        result.put("proCost", proCost);
        result.put("freeUsers", freeUsers);
        result.put("proUsers", proUsers);
        result.put("freeRequests", freeRequests);
        result.put("proRequests", proRequests);
        
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
            case "today": 
            case "daily": 
                return java.time.LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "week":
            case "weekly": 
                return now.minus(7, ChronoUnit.DAYS);
            case "month":
            case "monthly": 
                return java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "quarter":
                return now.minus(90, ChronoUnit.DAYS);
            case "year":
            case "yearly":
                return java.time.LocalDate.now().withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "all":
                return Instant.EPOCH;
            default: 
                return now.minus(30, ChronoUnit.DAYS);
        }
    }

    private Map<String, BigDecimal> convertToMap(List<Object[]> list) {
        return list.stream().collect(Collectors.toMap(
                obj -> (String) obj[0],
                obj -> (BigDecimal) obj[1]
        ));
    }

    @Override
    public boolean isBudgetExceeded() {
        try {
            double budgetLimit = Double.parseDouble(configService.getConfig("global.monthlyBudgetLimit").getConfigValue());
            if (budgetLimit <= 0) return false;

            Instant startOfMonth = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
            BigDecimal currentMonthCost = usageLogRepository.sumCostSince(startOfMonth);
            if (currentMonthCost == null) currentMonthCost = BigDecimal.ZERO;

            return currentMonthCost.doubleValue() >= budgetLimit;
        } catch (Exception e) {
            // Log error and default to false to avoid blocking on config error
            return false;
        }
    }
}
