package com.lexia.backend.controller;

import com.lexia.backend.dto.AdminActivityLogDTO;
import com.lexia.backend.dto.AdminActivityLogStatsDTO;
import com.lexia.backend.entity.AdminActivityLog;
import com.lexia.backend.service.AdminActivityLogQueryService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Admin Activity Log Management.
 * Provides endpoints for viewing, filtering, and exporting admin activity logs.
 * 
 * Access: ADMIN only
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/admin/activity-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Activity Logs", description = "Endpoints for managing admin activity logs (ADMIN only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityLogController {

    private final AdminActivityLogQueryService adminActivityLogQueryService;

    @GetMapping
    @Operation(summary = "Get activity logs", description = "Retrieve paginated admin activity logs with optional filters")
    public ResponseEntity<Page<AdminActivityLogDTO>> getLogs(
            @Parameter(description = "Filter by action type") @RequestParam(required = false) AdminActivityLog.ActionType action,

            @Parameter(description = "Filter by entity type") @RequestParam(required = false) AdminActivityLog.EntityType entityType,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by user name (partial match)") @RequestParam(required = false) String userName,

            @Parameter(description = "Filter by entity name (partial match)") @RequestParam(required = false) String entityName,

            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Search in description") @RequestParam(required = false) String search,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("GET /api/v1/admin/activity-logs - Fetching activity logs");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminActivityLogDTO> logs = adminActivityLogQueryService.getLogs(
                action, entityType, userId, userName, entityName, startDate, endDate, search, pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get activity statistics", description = "Retrieve aggregated activity statistics for a given period")
    public ResponseEntity<AdminActivityLogStatsDTO> getStats(
            @Parameter(description = "Time period: today, week, month, all") @RequestParam(defaultValue = "today") String period) {

        log.debug("GET /api/v1/admin/activity-logs/stats - Fetching activity stats for period: {}", period);

        AdminActivityLogStatsDTO stats = adminActivityLogQueryService.getStats(period);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export")
    @Operation(summary = "Export activity logs to CSV", description = "Download activity logs as CSV file with optional filters")
    public ResponseEntity<byte[]> exportToCsv(
            @Parameter(description = "Filter by action type") @RequestParam(required = false) AdminActivityLog.ActionType action,

            @Parameter(description = "Filter by entity type") @RequestParam(required = false) AdminActivityLog.EntityType entityType,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("GET /api/v1/admin/activity-logs/export - Exporting activity logs to CSV");

        byte[] csv = adminActivityLogQueryService.exportToCsv(action, entityType, userId, startDate, endDate);

        String filename = "activity-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/actions")
    @Operation(summary = "Get distinct action types", description = "Retrieve list of action types that have logs")
    public ResponseEntity<List<AdminActivityLog.ActionType>> getDistinctActions() {
        log.debug("GET /api/v1/admin/activity-logs/actions - Fetching distinct action types");

        List<AdminActivityLog.ActionType> actions = adminActivityLogQueryService.getDistinctActions();

        return ResponseEntity.ok(actions);
    }

    @GetMapping("/users")
    @Operation(summary = "Get distinct user names", description = "Retrieve list of user names that have logs")
    public ResponseEntity<List<String>> getDistinctUserNames() {
        log.debug("GET /api/v1/admin/activity-logs/users - Fetching distinct user names");

        List<String> userNames = adminActivityLogQueryService.getDistinctUserNames();

        return ResponseEntity.ok(userNames);
    }
}
