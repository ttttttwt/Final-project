package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import com.lexia.backend.entity.AIUsageLog;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for tracking AI usage across all AI features.
 * Provides comprehensive logging of AI API calls with support for:
 * <ul>
 * <li>Token usage and cost tracking</li>
 * <li>Response time metrics</li>
 * <li>Success/failure logging</li>
 * <li>Per-feature usage statistics</li>
 * <li>User quota management</li>
 * </ul>
 * 
 * <p>
 * This service is designed to be called after each AI API call
 * (both successful and failed) to maintain comprehensive usage records.
 * </p>
 * 
 * @see AiUsageTrackingRequest
 * @see com.lexia.backend.entity.AIUsageLog
 */
public interface AiUsageTracker {

    /**
     * Valid content types for AI features.
     */
    String CONTENT_TYPE_ROLEPLAY = "roleplay";
    String CONTENT_TYPE_GRAMMAR = "grammar";
    String CONTENT_TYPE_FLASHCARD = "flashcard";
    String CONTENT_TYPE_CONTENT_GENERATION = "content_generation";
    String CONTENT_TYPE_PRONUNCIATION = "pronunciation_feedback";

    /**
     * Custom Material - Granular tracking for cost analytics.
     * Each source type is tracked separately for detailed cost breakdown.
     */
    String CONTENT_TYPE_CM_PDF_EXTRACTION = "cm_pdf_extraction";
    String CONTENT_TYPE_CM_DOCX_EXTRACTION = "cm_docx_extraction";
    String CONTENT_TYPE_CM_IMAGE_OCR = "cm_image_ocr";
    String CONTENT_TYPE_CM_YOUTUBE_TRANSCRIPT = "cm_youtube_transcript";
    String CONTENT_TYPE_CM_WEBSITE_EXTRACTION = "cm_website_extraction";
    String CONTENT_TYPE_CM_TEXT_INPUT = "cm_text_input";
    String CONTENT_TYPE_CM_CONTENT_GENERATION = "cm_content_generation";

    /**
     * Tracks an AI usage event synchronously.
     * Use this when you need confirmation that the log was saved.
     * 
     * @param request The tracking request containing usage details
     * @return The saved usage log entity
     */
    AIUsageLog trackUsage(AiUsageTrackingRequest request);

    /**
     * Tracks an AI usage event asynchronously.
     * Use this for non-blocking tracking that won't affect response latency.
     * 
     * @param request The tracking request containing usage details
     * @return CompletableFuture that completes when tracking is done
     */
    CompletableFuture<AIUsageLog> trackUsageAsync(AiUsageTrackingRequest request);

    /**
     * Gets usage statistics for a specific user.
     * 
     * @param userId    The user ID to get stats for
     * @param startDate Start of the period
     * @param endDate   End of the period
     * @return Map of content type to request count
     */
    Map<String, Long> getUsageByContentType(UUID userId, Instant startDate, Instant endDate);

    /**
     * Gets the daily usage count for a user and content type.
     * Used for checking against daily quotas.
     * 
     * @param userId      The user ID
     * @param contentType The content type (roleplay, grammar, etc.)
     * @return Number of requests made today
     */
    long getDailyUsageCount(UUID userId, String contentType);

    /**
     * Gets the monthly usage count for a user and content type.
     * Used for checking against monthly quotas.
     * 
     * @param userId      The user ID
     * @param contentType The content type (roleplay, grammar, etc.)
     * @return Number of requests made this month
     */
    long getMonthlyUsageCount(UUID userId, String contentType);

    /**
     * Checks if user has exceeded their daily quota for a feature.
     * 
     * @param userId      The user ID
     * @param contentType The content type to check
     * @return true if quota is exceeded
     */
    boolean isDailyQuotaExceeded(UUID userId, String contentType);

    /**
     * Checks if user has exceeded their monthly quota for a feature.
     * 
     * @param userId      The user ID
     * @param contentType The content type to check
     * @return true if quota is exceeded
     */
    boolean isMonthlyQuotaExceeded(UUID userId, String contentType);

    /**
     * Gets the remaining daily quota for a user and content type.
     * 
     * @param userId      The user ID
     * @param contentType The content type
     * @return Number of remaining requests for today
     */
    int getRemainingDailyQuota(UUID userId, String contentType);

    /**
     * Gets the remaining monthly quota for a user and content type.
     * 
     * @param userId      The user ID
     * @param contentType The content type
     * @return Number of remaining requests for this month
     */
    int getRemainingMonthlyQuota(UUID userId, String contentType);

    /**
     * Calculates the estimated cost in USD for a given token usage.
     * Uses the configured pricing for the specified model.
     * 
     * @param modelId      The AI model ID
     * @param inputTokens  Number of input tokens
     * @param outputTokens Number of output tokens
     * @return Estimated cost in USD
     */
    java.math.BigDecimal calculateCost(String modelId, int inputTokens, int outputTokens);
}
