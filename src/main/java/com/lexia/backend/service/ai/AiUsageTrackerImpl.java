package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import com.lexia.backend.entity.AIUsageLog;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.repository.AIUsageLogRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AiUsageTracker service.
 * Provides comprehensive AI usage logging and quota management.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Synchronous and asynchronous tracking</li>
 * <li>Cost calculation based on model pricing</li>
 * <li>Per-feature quota enforcement</li>
 * <li>Automatic quota record creation for new users</li>
 * </ul>
 * 
 * @see AiUsageTracker
 * @see AiUsageTrackingRequest
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiUsageTrackerImpl implements AiUsageTracker {

    private final AIUsageLogRepository aiUsageLogRepository;
    private final UserAiQuotaRepository userAiQuotaRepository;
    private final AIConfigService aiConfigService;

    // ========== Pricing Constants (per million tokens) ==========

    /** Gemini 2.0 Flash Experimental - Input cost per million tokens */
    private static final BigDecimal GEMINI_2_FLASH_INPUT_COST = new BigDecimal("0.075");

    /** Gemini 2.0 Flash Experimental - Output cost per million tokens */
    private static final BigDecimal GEMINI_2_FLASH_OUTPUT_COST = new BigDecimal("0.30");

    /** Gemini 1.5 Flash - Input cost per million tokens */
    private static final BigDecimal GEMINI_15_FLASH_INPUT_COST = new BigDecimal("0.075");

    /** Gemini 1.5 Flash - Output cost per million tokens */
    private static final BigDecimal GEMINI_15_FLASH_OUTPUT_COST = new BigDecimal("0.30");

    /** Gemini 1.5 Pro - Input cost per million tokens */
    private static final BigDecimal GEMINI_15_PRO_INPUT_COST = new BigDecimal("1.25");

    /** Gemini 1.5 Pro - Output cost per million tokens */
    private static final BigDecimal GEMINI_15_PRO_OUTPUT_COST = new BigDecimal("5.00");

    /** Default cost for unknown models */
    private static final BigDecimal DEFAULT_INPUT_COST = new BigDecimal("0.10");
    private static final BigDecimal DEFAULT_OUTPUT_COST = new BigDecimal("0.30");

    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

    @Override
    @Transactional
    public AIUsageLog trackUsage(AiUsageTrackingRequest request) {
        log.debug("Tracking AI usage - user: {}, contentType: {}, model: {}, tokens: {}/{}",
                request.getUserId(), request.getContentType(), request.getModelId(),
                request.getInputTokens(), request.getOutputTokens());

        // Calculate cost - use override if provided (for fixed-price content like
        // images)
        BigDecimal cost;
        if (request.getOverrideCostUsd() != null && request.getOverrideCostUsd() > 0) {
            cost = BigDecimal.valueOf(request.getOverrideCostUsd()).setScale(6, RoundingMode.HALF_UP);
        } else {
            cost = calculateCost(
                    request.getModelId(),
                    request.getInputTokens(),
                    request.getOutputTokens());
        }

        // Create and save usage log
        AIUsageLog usageLog = AIUsageLog.builder()
                .userId(request.getUserId())
                .contentType(request.getContentType())
                .featureName(request.getContentType()) // Backward compatibility
                .modelId(request.getModelId())
                .inputTokens(request.getInputTokens())
                .outputTokens(request.getOutputTokens())
                .estimatedCostUsd(cost)
                .responseTimeMs(request.getResponseTimeMs())
                .success(request.isSuccess())
                .errorMessage(request.getErrorMessage())
                .requestMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .promptVersion(request.getPromptVersion())
                .build();

        AIUsageLog saved = aiUsageLogRepository.save(usageLog);

        // Update user quota (only for successful requests)
        if (request.isSuccess()) {
            updateUserQuota(request.getUserId(), request.getContentType());
        }

        log.info("AI usage tracked - id: {}, cost: ${}, tokens: {}",
                saved.getId(), cost, request.getTotalTokens());

        return saved;
    }

    @Override
    @Async("aiUsageExecutor")
    @Transactional
    public CompletableFuture<AIUsageLog> trackUsageAsync(AiUsageTrackingRequest request) {
        try {
            AIUsageLog result = trackUsage(request);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async AI usage tracking failed - user: {}, type: {}, error: {}",
                    request.getUserId(), request.getContentType(), e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Map<String, Long> getUsageByContentType(UUID userId, Instant startDate, Instant endDate) {
        log.debug("Getting usage by content type - user: {}, period: {} to {}",
                userId, startDate, endDate);

        List<Object[]> results = aiUsageLogRepository.countByContentTypeSince(
                userId, startDate, endDate);

        Map<String, Long> usageMap = new HashMap<>();
        for (Object[] row : results) {
            String contentType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            usageMap.put(contentType, count);
        }

        return usageMap;
    }

    @Override
    public long getDailyUsageCount(UUID userId, String contentType) {
        Instant startOfDay = LocalDate.now()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        return aiUsageLogRepository.countByUserIdAndContentTypeAndCreatedAtGreaterThanEqual(
                userId, contentType, startOfDay);
    }

    @Override
    public long getMonthlyUsageCount(UUID userId, String contentType) {
        Instant startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        return aiUsageLogRepository.countByUserIdAndContentTypeAndCreatedAtGreaterThanEqual(
                userId, contentType, startOfMonth);
    }

    @Override
    public boolean isDailyQuotaExceeded(UUID userId, String contentType) {
        UserAiQuota quota = getOrCreateQuota(userId);

        // Check if suspended
        if (Boolean.TRUE.equals(quota.getSuspended())) {
            return true;
        }

        return quota.isFeatureDailyQuotaExceeded(contentType);
    }

    @Override
    public boolean isMonthlyQuotaExceeded(UUID userId, String contentType) {
        UserAiQuota quota = getOrCreateQuota(userId);

        // Check if suspended
        if (Boolean.TRUE.equals(quota.getSuspended())) {
            return true;
        }

        return quota.isFeatureMonthlyQuotaExceeded(contentType);
    }

    @Override
    public int getRemainingDailyQuota(UUID userId, String contentType) {
        UserAiQuota quota = getOrCreateQuota(userId);

        if (Boolean.TRUE.equals(quota.getSuspended())) {
            return 0;
        }

        return quota.getFeatureRemainingDailyQuota(contentType);
    }

    @Override
    public int getRemainingMonthlyQuota(UUID userId, String contentType) {
        UserAiQuota quota = getOrCreateQuota(userId);

        if (Boolean.TRUE.equals(quota.getSuspended())) {
            return 0;
        }

        return quota.getFeatureRemainingMonthlyQuota(contentType);
    }

    @Override
    public BigDecimal calculateCost(String modelId, int inputTokens, int outputTokens) {
        var settings = aiConfigService.getSettings();

        // Use configured global cost per token
        BigDecimal inputCostPerToken = BigDecimal.valueOf(settings.getCostPerInputToken());
        BigDecimal outputCostPerToken = BigDecimal.valueOf(settings.getCostPerOutputToken());

        BigDecimal inputTotal = inputCostPerToken.multiply(BigDecimal.valueOf(inputTokens));
        BigDecimal outputTotal = outputCostPerToken.multiply(BigDecimal.valueOf(outputTokens));

        return inputTotal.add(outputTotal).setScale(6, RoundingMode.HALF_UP);
    }

    // ========== Private Helper Methods ==========

    /**
     * Updates the user's quota after a successful AI request.
     */
    @Transactional
    protected void updateUserQuota(UUID userId, String contentType) {
        // Skip quota update if userId is null (e.g., scenario generation without user context)
        if (userId == null) {
            log.debug("Skipping quota update - no userId provided for contentType: {}", contentType);
            return;
        }

        try {
            // First try atomic update via native query
            int updated = userAiQuotaRepository.incrementUsage(userId, contentType);

            if (updated == 0) {
                // No quota record exists, create one
                UserAiQuota quota = UserAiQuota.createForUser(userId);
                quota.incrementFeatureUsage(contentType);
                userAiQuotaRepository.save(quota);
                log.debug("Created new quota record for user: {}", userId);
            }
        } catch (Exception e) {
            // Log but don't fail the main tracking operation
            log.warn("Failed to update quota for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Gets existing quota or creates a new one for the user.
     */
    private UserAiQuota getOrCreateQuota(UUID userId) {
        return userAiQuotaRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.debug("Creating default quota for user: {}", userId);
                    UserAiQuota newQuota = UserAiQuota.createForUser(userId);
                    return userAiQuotaRepository.save(newQuota);
                });
    }
}
