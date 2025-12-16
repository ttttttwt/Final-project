package com.lexia.backend.service.ai;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public interface AICostService {
    Map<String, Object> getCostAnalytics(String period);
    BigDecimal getTotalCost(Instant start, Instant end);
    Map<String, BigDecimal> getCostByModel(Instant start, Instant end);
    Map<String, Object> getProjection();
    byte[] exportReport(String period);
    java.util.List<Map<String, Object>> getCostsByUser(String period, int limit);
}
