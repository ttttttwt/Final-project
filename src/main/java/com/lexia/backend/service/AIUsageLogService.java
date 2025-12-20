package com.lexia.backend.service;

import com.lexia.backend.dto.AIUsageLogDTO;
import com.lexia.backend.dto.AIUsageStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for AI usage log operations.
 */
public interface AIUsageLogService {

    /**
     * Get paginated AI usage logs with optional filters.
     *
     * @param featureName Optional filter by feature name
     * @param userId      Optional filter by user ID
     * @param startDate   Optional filter by start date
     * @param endDate     Optional filter by end date
     * @param pageable    Pagination parameters
     * @return Page of AI usage log DTOs
     */
    Page<AIUsageLogDTO> getLogs(String featureName, UUID userId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Get AI usage statistics for a given period.
     *
     * @param period Period: today, week, month, all
     * @return AI usage statistics
     */
    AIUsageStatsDTO getStats(String period);

    /**
     * Log an AI usage event.
     *
     * @param userId       User who made the request
     * @param featureName  AI feature used
     * @param inputTokens  Input tokens consumed
     * @param outputTokens Output tokens generated
     * @return Created AI usage log DTO
     */
    AIUsageLogDTO logUsage(UUID userId, String featureName, int inputTokens, int outputTokens);

    /**
     * Export AI usage logs to CSV format.
     *
     * @param featureName Optional filter by feature name
     * @param userId      Optional filter by user ID
     * @param startDate   Optional filter by start date
     * @param endDate     Optional filter by end date
     * @return Byte array containing CSV data
     */
    byte[] exportLogs(String featureName, UUID userId, Instant startDate, Instant endDate);
}
