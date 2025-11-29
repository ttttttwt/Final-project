package com.lexia.backend.controller;

import com.lexia.backend.dto.AuditLogDTO;
import com.lexia.backend.service.AuditLogQueryService;
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
 * REST controller for Audit Log Management.
 * Provides endpoints for viewing, filtering, and exporting audit logs.
 * 
 * Access: ADMIN only
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Endpoints for managing audit logs (ADMIN only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    @Operation(summary = "Get audit logs", description = "Retrieve paginated audit logs with optional filters")
    public ResponseEntity<Page<AuditLogDTO>> getLogs(
            @Parameter(description = "Filter by action type") @RequestParam(required = false) String action,

            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by user email (partial match)") @RequestParam(required = false) String userEmail,

            @Parameter(description = "Filter by IP address") @RequestParam(required = false) String ipAddress,

            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Search in changes") @RequestParam(required = false) String search,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("GET /api/v1/admin/audit-logs - Fetching audit logs");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLogDTO> logs = auditLogQueryService.getLogs(
                action, entityType, userId, userEmail, ipAddress, startDate, endDate, search, pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/export")
    @Operation(summary = "Export audit logs to CSV", description = "Download audit logs as CSV file with optional filters")
    public ResponseEntity<byte[]> exportToCsv(
            @Parameter(description = "Filter by action type") @RequestParam(required = false) String action,

            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("GET /api/v1/admin/audit-logs/export - Exporting audit logs to CSV");

        byte[] csv = auditLogQueryService.exportToCsv(action, entityType, userId, startDate, endDate);

        String filename = "audit-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/actions")
    @Operation(summary = "Get distinct action types", description = "Retrieve list of action types that have audit logs")
    public ResponseEntity<List<String>> getDistinctActions() {
        log.debug("GET /api/v1/admin/audit-logs/actions - Fetching distinct action types");

        List<String> actions = auditLogQueryService.getDistinctActions();

        return ResponseEntity.ok(actions);
    }

    @GetMapping("/entity-types")
    @Operation(summary = "Get distinct entity types", description = "Retrieve list of entity types that have audit logs")
    public ResponseEntity<List<String>> getDistinctEntityTypes() {
        log.debug("GET /api/v1/admin/audit-logs/entity-types - Fetching distinct entity types");

        List<String> entityTypes = auditLogQueryService.getDistinctEntityTypes();

        return ResponseEntity.ok(entityTypes);
    }
}
