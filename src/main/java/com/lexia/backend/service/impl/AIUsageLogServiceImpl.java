package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AIUsageLogDTO;
import com.lexia.backend.dto.AIUsageStatsDTO;
import com.lexia.backend.dto.AIUsageStatsDTO.AIUsageSummary;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.AIUsageLogService;
import com.lexia.backend.service.ai.AIAlertService;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.specification.AIUsageLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of AI usage log service.
 * Provides operations for tracking and analyzing AI API usage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIUsageLogServiceImpl implements AIUsageLogService {

    private final AIUsageLogRepository aiUsageLogRepository;
    private final UserRepository userRepository;
    private final AIAlertService alertService;
    private final AIConfigService configService;

    // Cost per 1K tokens (example pricing based on common AI providers)
    private static final BigDecimal INPUT_COST_PER_1K = new BigDecimal("0.0015");
    private static final BigDecimal OUTPUT_COST_PER_1K = new BigDecimal("0.002");

    @Override
    public Page<AIUsageLogDTO> getLogs(String featureName, UUID userId, Instant startDate, Instant endDate,
            Pageable pageable) {
        log.debug("Fetching AI usage logs with filters - feature: {}, userId: {}, startDate: {}, endDate: {}",
                featureName, userId, startDate, endDate);

        Specification<AIUsageLog> spec = Specification.where(null);

        if (featureName != null && !featureName.isBlank()) {
            spec = spec.and(AIUsageLogSpecification.hasFeatureName(featureName));
        }
        if (userId != null) {
            spec = spec.and(AIUsageLogSpecification.hasUserId(userId));
        }
        if (startDate != null) {
            spec = spec.and(AIUsageLogSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AIUsageLogSpecification.createdBefore(endDate));
        }

        Page<AIUsageLog> logs = aiUsageLogRepository.findAll(spec, pageable);

        return logs.map(this::toDTO);
    }

    @Override
    public AIUsageStatsDTO getStats(String period) {
        log.debug("Getting AI usage stats for period: {}", period);

        Instant startDate = getStartDateForPeriod(period);

        // Get total calls
        long totalCalls = aiUsageLogRepository.countByCreatedAtGreaterThanEqual(startDate);

        // Get token sums
        List<Object[]> tokenSums = aiUsageLogRepository.sumTokensSince(startDate);
        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        if (!tokenSums.isEmpty() && tokenSums.get(0) != null) {
            Object[] result = tokenSums.get(0);
            totalInputTokens = result[0] != null ? ((Number) result[0]).longValue() : 0;
            totalOutputTokens = result[1] != null ? ((Number) result[1]).longValue() : 0;
        }

        // Get total cost
        BigDecimal totalCost = aiUsageLogRepository.sumCostSince(startDate);
        if (totalCost == null) {
            totalCost = BigDecimal.ZERO;
        }

        // Calculate average cost per call
        BigDecimal averageCostPerCall = totalCalls > 0
                ? totalCost.divide(BigDecimal.valueOf(totalCalls), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get calls by feature
        List<Object[]> featureCounts = aiUsageLogRepository.countByFeatureNameSince(startDate);
        Map<String, Long> callsByFeature = new HashMap<>();
        for (Object[] row : featureCounts) {
            String feature = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            callsByFeature.put(feature, count);
        }

        AIUsageSummary summary = AIUsageSummary.builder()
                .totalCalls(totalCalls)
                .totalInputTokens(totalInputTokens)
                .totalOutputTokens(totalOutputTokens)
                .totalCost(totalCost)
                .averageCostPerCall(averageCostPerCall)
                .callsByFeature(callsByFeature)
                .build();

        return AIUsageStatsDTO.builder()
                .period(period)
                .stats(summary)
                .build();
    }

    @Override
    @Transactional
    public AIUsageLogDTO logUsage(UUID userId, String featureName, int inputTokens, int outputTokens) {
        log.debug("Logging AI usage - userId: {}, feature: {}, inputTokens: {}, outputTokens: {}",
                userId, featureName, inputTokens, outputTokens);

        // Calculate cost
        BigDecimal inputCost = INPUT_COST_PER_1K
                .multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        BigDecimal outputCost = OUTPUT_COST_PER_1K
                .multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        BigDecimal totalCost = inputCost.add(outputCost);

        AIUsageLog log = AIUsageLog.builder()
                .userId(userId)
                .featureName(featureName)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .estimatedCostUsd(totalCost)
                .build();

        AIUsageLog saved = aiUsageLogRepository.save(log);
        
        // Check budget alerts
        checkBudgetAlerts(totalCost);
        
        return toDTO(saved);
    }

    private void checkBudgetAlerts(BigDecimal newCost) {
        try {
            var settings = configService.getSettings();
            if (!settings.isGlobalEnabled()) return;

            double budgetLimit = settings.getMonthlyBudgetLimit();
            if (budgetLimit <= 0) return;

            Instant startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            BigDecimal currentMonthCost = aiUsageLogRepository.sumCostSince(startOfMonth);
            if (currentMonthCost == null) currentMonthCost = BigDecimal.ZERO;
            
            // Add the new cost as it might not be committed/visible to sum query yet in same transaction
            // Actually, save() was called but transaction not committed. 
            // If sumCostSince uses READ_COMMITTED, it won't see it. 
            // If it uses same EntityManager, it might flush.
            // To be safe, let's assume sumCostSince includes it or we add it.
            // But wait, we are in @Transactional, so repository query should see it if flushed.
            // Let's rely on sumCostSince.

            double currentCost = currentMonthCost.doubleValue();
            double threshold = budgetLimit * (settings.getAlertThresholdPercentage() / 100.0);

            if (currentCost >= budgetLimit) {
                if (!alertService.hasRecentAlert("BUDGET_EXCEEDED", "CRITICAL", Duration.ofHours(24))) {
                    alertService.createAlert("BUDGET_EXCEEDED", 
                        String.format("Monthly AI budget exceeded! Current: $%.2f, Limit: $%.2f", currentCost, budgetLimit), 
                        "CRITICAL");
                }
            } else if (currentCost >= threshold) {
                if (!alertService.hasRecentAlert("BUDGET_WARNING", "WARNING", Duration.ofHours(24))) {
                    alertService.createAlert("BUDGET_WARNING", 
                        String.format("Monthly AI budget threshold reached. Current: $%.2f, Limit: $%.2f", currentCost, budgetLimit), 
                        "WARNING");
                }
            }
        } catch (Exception e) {
            log.error("Failed to check budget alerts", e);
        }
    }

    /**
     * Convert entity to DTO with user email lookup.
     */
    private AIUsageLogDTO toDTO(AIUsageLog entity) {
        String userEmail = null;
        if (entity.getUserId() != null) {
            userEmail = userRepository.findById(entity.getUserId())
                    .map(User::getEmail)
                    .orElse(null);
        }

        String feature = entity.getFeatureName();
        if (feature == null && entity.getContentType() != null) {
            feature = entity.getContentType().toUpperCase();
        }

        return AIUsageLogDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(userEmail)
                .featureName(feature)
                .inputTokens(entity.getInputTokens())
                .outputTokens(entity.getOutputTokens())
                .cost(entity.getEstimatedCostUsd())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Get the start date for a given period.
     */
    private Instant getStartDateForPeriod(String period) {
        LocalDate today = LocalDate.now();
        return switch (period.toLowerCase()) {
            case "today" -> today.atStartOfDay(ZoneOffset.UTC).toInstant();
            case "week" -> today.minusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();
            case "month" -> today.minusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            case "all" -> Instant.EPOCH;
            default -> today.atStartOfDay(ZoneOffset.UTC).toInstant();
        };
    }

    @Override
    public byte[] exportLogs(String featureName, UUID userId, Instant startDate, Instant endDate) {
        List<AIUsageLog> logs = aiUsageLogRepository.findWithFilters(featureName, userId, startDate, endDate);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            
            // Write BOM for Excel compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // Write header
            writer.println("ID,User ID,Feature,Model,Tokens Used,Cost,Duration (ms),Status,Timestamp");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC);
            
            for (AIUsageLog log : logs) {
                writer.printf("%s,%s,%s,%s,%d,%.6f,%d,%s,%s%n",
                    log.getId(),
                    log.getUserId(),
                    escapeCsv(log.getFeatureName()),
                    escapeCsv(log.getModelId()),
                    log.getTotalTokens() != null ? log.getTotalTokens() : (log.getInputTokens() + log.getOutputTokens()),
                    log.getEstimatedCostUsd(),
                    log.getResponseTimeMs(),
                    log.getSuccess() != null && log.getSuccess() ? "SUCCESS" : "FAILURE",
                    formatter.format(log.getCreatedAt())
                );
            }
            
            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
