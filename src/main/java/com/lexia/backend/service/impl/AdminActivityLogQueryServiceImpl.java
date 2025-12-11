package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AdminActivityLogDTO;
import com.lexia.backend.dto.AdminActivityLogStatsDTO;
import com.lexia.backend.entity.AdminActivityLog;
import com.lexia.backend.repository.AdminActivityLogRepository;
import com.lexia.backend.service.AdminActivityLogQueryService;
import com.lexia.backend.specification.AdminActivityLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of AdminActivityLogQueryService.
 * Provides query capabilities for the Log Management feature.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminActivityLogQueryServiceImpl implements AdminActivityLogQueryService {

    private final AdminActivityLogRepository adminActivityLogRepository;

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<AdminActivityLogDTO> getLogs(
            AdminActivityLog.ActionType action,
            AdminActivityLog.EntityType entityType,
            UUID userId,
            String userName,
            String entityName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable) {

        log.debug("Fetching activity logs with filters - action: {}, entityType: {}, userId: {}",
                action, entityType, userId);

        Specification<AdminActivityLog> spec = Specification.where(null);

        if (action != null) {
            spec = spec.and(AdminActivityLogSpecification.hasAction(action));
        }
        if (entityType != null) {
            spec = spec.and(AdminActivityLogSpecification.hasEntityType(entityType));
        }
        if (userId != null) {
            spec = spec.and(AdminActivityLogSpecification.hasUserId(userId));
        }
        if (userName != null && !userName.isBlank()) {
            spec = spec.and(AdminActivityLogSpecification.userNameContains(userName));
        }
        if (entityName != null && !entityName.isBlank()) {
            spec = spec.and(AdminActivityLogSpecification.entityNameContains(entityName));
        }
        if (startDate != null) {
            spec = spec.and(AdminActivityLogSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AdminActivityLogSpecification.createdBefore(endDate));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(AdminActivityLogSpecification.descriptionContains(search));
        }

        Page<AdminActivityLog> page = adminActivityLogRepository.findAll(spec, pageable);

        return page.map(AdminActivityLogDTO::fromEntity);
    }

    @Override
    public AdminActivityLogStatsDTO getStats(String period) {
        log.debug("Fetching activity stats for period: {}", period);

        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        // Get total count
        long totalActivities;
        Map<String, Long> activitiesByAction;
        Map<String, Long> activitiesByEntityType;

        if (startDate != null) {
            totalActivities = adminActivityLogRepository.countByDateRange(startDate, endDate);
            activitiesByAction = getActionCountsInDateRange(startDate, endDate);
        } else {
            totalActivities = adminActivityLogRepository.count();
            activitiesByAction = getActionCounts();
        }

        activitiesByEntityType = getEntityTypeCounts();
        Map<String, Long> topActiveUsers = getTopActiveUsers(5);
        Map<String, Long> activitiesByUser = getTopActiveUsers(20); // Get more for full list

        AdminActivityLogStatsDTO.ActivityStatsSummary summary = AdminActivityLogStatsDTO.ActivityStatsSummary.builder()
                .totalActivities(totalActivities)
                .activitiesByAction(activitiesByAction)
                .activitiesByEntityType(activitiesByEntityType)
                .activitiesByUser(activitiesByUser)
                .topActiveUsers(topActiveUsers)
                .build();

        return AdminActivityLogStatsDTO.builder()
                .period(period)
                .stats(summary)
                .build();
    }

    @Override
    public byte[] exportToCsv(
            AdminActivityLog.ActionType action,
            AdminActivityLog.EntityType entityType,
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Exporting activity logs to CSV");

        Specification<AdminActivityLog> spec = Specification.where(null);

        if (action != null) {
            spec = spec.and(AdminActivityLogSpecification.hasAction(action));
        }
        if (entityType != null) {
            spec = spec.and(AdminActivityLogSpecification.hasEntityType(entityType));
        }
        if (userId != null) {
            spec = spec.and(AdminActivityLogSpecification.hasUserId(userId));
        }
        if (startDate != null) {
            spec = spec.and(AdminActivityLogSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AdminActivityLogSpecification.createdBefore(endDate));
        }

        List<AdminActivityLog> logs = adminActivityLogRepository.findAll(spec);

        return generateCsv(logs);
    }

    @Override
    public List<AdminActivityLog.ActionType> getDistinctActions() {
        return adminActivityLogRepository.findDistinctActions();
    }

    @Override
    public List<String> getDistinctUserNames() {
        return adminActivityLogRepository.findDistinctUserNames();
    }

    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDate today = LocalDate.now();

        return switch (period.toLowerCase()) {
            case "today" -> today.atStartOfDay();
            case "week" -> today.minusWeeks(1).atStartOfDay();
            case "month" -> today.minusMonths(1).atStartOfDay();
            case "all" -> null;
            default -> today.atStartOfDay();
        };
    }

    private Map<String, Long> getActionCounts() {
        List<Object[]> results = adminActivityLogRepository.countGroupByAction();
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Object[] result : results) {
            AdminActivityLog.ActionType actionType = (AdminActivityLog.ActionType) result[0];
            Long count = (Long) result[1];
            counts.put(actionType.name(), count);
        }

        return counts;
    }

    private Map<String, Long> getActionCountsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = adminActivityLogRepository.countGroupByActionInDateRange(startDate, endDate);
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Object[] result : results) {
            AdminActivityLog.ActionType actionType = (AdminActivityLog.ActionType) result[0];
            Long count = (Long) result[1];
            counts.put(actionType.name(), count);
        }

        return counts;
    }

    private Map<String, Long> getEntityTypeCounts() {
        List<Object[]> results = adminActivityLogRepository.countGroupByEntityType();
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Object[] result : results) {
            AdminActivityLog.EntityType entityType = (AdminActivityLog.EntityType) result[0];
            Long count = (Long) result[1];
            counts.put(entityType.name(), count);
        }

        return counts;
    }

    private Map<String, Long> getTopActiveUsers(int limit) {
        List<Object[]> results = adminActivityLogRepository.countGroupByUserName(PageRequest.of(0, limit));
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Object[] result : results) {
            String userName = (String) result[0];
            Long count = (Long) result[1];
            counts.put(userName, count);
        }

        return counts;
    }

    private byte[] generateCsv(List<AdminActivityLog> logs) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // BOM for Excel UTF-8 compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Header
            writer.println("ID,User ID,User Name,Action,Entity Type,Entity ID,Entity Name,Description,Created At");

            // Data rows
            for (AdminActivityLog log : logs) {
                writer.println(String.format("%s,%s,\"%s\",%s,%s,%s,\"%s\",\"%s\",%s",
                        log.getId(),
                        log.getUserId(),
                        escapeCsvField(log.getUserName()),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        escapeCsvField(log.getEntityName()),
                        escapeCsvField(log.getDescription()),
                        log.getCreatedAt().format(CSV_DATE_FORMAT)));
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate CSV: {}", e.getMessage());
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        return field.replace("\"", "\"\"");
    }
}
