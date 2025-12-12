package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AIUsageLogDTO;
import com.lexia.backend.dto.AIUsageStatsDTO;
import com.lexia.backend.dto.AIUsageStatsDTO.AIUsageSummary;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.AIUsageLogService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // Cost per 1K tokens (example pricing based on common AI providers)
    private static final BigDecimal INPUT_COST_PER_1K = new BigDecimal("0.0015");
    private static final BigDecimal OUTPUT_COST_PER_1K = new BigDecimal("0.002");

    @Override
    public Page<AIUsageLogDTO> getLogs(String featureName, UUID userId, Instant startDate, Instant endDate,
            Pageable pageable) {
        log.debug("Fetching AI usage logs with filters - feature: {}, userId: {}, startDate: {}, endDate: {}",
                featureName, userId, startDate, endDate);

        Specification<AIUsageLog> spec = Specification.allOf();

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
        return toDTO(saved);
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

        return AIUsageLogDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(userEmail)
                .featureName(entity.getFeatureName())
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
}
