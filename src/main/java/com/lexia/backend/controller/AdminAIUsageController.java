package com.lexia.backend.controller;

import com.lexia.backend.dto.AIUsageLogDTO;
import com.lexia.backend.dto.AIUsageStatsDTO;
import com.lexia.backend.service.AIUsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for AI usage monitoring.
 * Provides endpoints for viewing AI API usage logs and statistics.
 * 
 * Access: ADMIN only
 */
@RestController
@RequestMapping("/api/v1/admin/ai-usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Usage Monitoring", description = "Endpoints for monitoring AI API usage (ADMIN only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIUsageController {

    private final AIUsageLogService aiUsageLogService;

    @GetMapping
    @Operation(summary = "Get AI usage logs", description = "Retrieve paginated AI usage logs with optional filters")
    public ResponseEntity<Page<AIUsageLogDTO>> getLogs(
            @Parameter(description = "Filter by feature name") @RequestParam(required = false) String featureName,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,

            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("GET /api/v1/admin/ai-usage - Fetching AI usage logs");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AIUsageLogDTO> logs = aiUsageLogService.getLogs(featureName, userId, startDate, endDate, pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get AI usage statistics", description = "Retrieve aggregated AI usage statistics for a given period")
    public ResponseEntity<AIUsageStatsDTO> getStats(
            @Parameter(description = "Time period: today, week, month, all") @RequestParam(defaultValue = "today") String period) {
        log.debug("GET /api/v1/admin/ai-usage/stats - Fetching AI usage stats for period: {}", period);

        AIUsageStatsDTO stats = aiUsageLogService.getStats(period);

        return ResponseEntity.ok(stats);
    }
}
