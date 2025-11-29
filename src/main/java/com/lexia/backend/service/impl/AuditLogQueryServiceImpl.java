package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AuditLogDTO;
import com.lexia.backend.entity.AuditLog;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.repository.AuditLogRepository;
import com.lexia.backend.service.AuditLogQueryService;
import com.lexia.backend.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of AuditLogQueryService.
 * Provides query capabilities for the Log Management feature.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<AuditLogDTO> getLogs(
            String action,
            String entityType,
            UUID userId,
            String userEmail,
            String ipAddress,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable) {

        log.debug("Fetching audit logs with filters - action: {}, entityType: {}, userId: {}",
                action, entityType, userId);

        Specification<AuditLog> spec = Specification.where(null);

        if (action != null && !action.isBlank()) {
            spec = spec.and(AuditLogSpecification.hasAction(action));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and(AuditLogSpecification.hasEntityType(entityType));
        }
        if (userId != null) {
            spec = spec.and(AuditLogSpecification.hasUserId(userId));
        }
        if (userEmail != null && !userEmail.isBlank()) {
            spec = spec.and(AuditLogSpecification.userEmailContains(userEmail));
        }
        if (ipAddress != null && !ipAddress.isBlank()) {
            spec = spec.and(AuditLogSpecification.hasIpAddress(ipAddress));
        }
        if (startDate != null) {
            spec = spec.and(AuditLogSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AuditLogSpecification.createdBefore(endDate));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(AuditLogSpecification.changesContains(search));
        }

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        return page.map(AuditLogDTO::fromEntity);
    }

    @Override
    public byte[] exportToCsv(
            String action,
            String entityType,
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Exporting audit logs to CSV");

        Specification<AuditLog> spec = Specification.where(null);

        if (action != null && !action.isBlank()) {
            spec = spec.and(AuditLogSpecification.hasAction(action));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and(AuditLogSpecification.hasEntityType(entityType));
        }
        if (userId != null) {
            spec = spec.and(AuditLogSpecification.hasUserId(userId));
        }
        if (startDate != null) {
            spec = spec.and(AuditLogSpecification.createdAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AuditLogSpecification.createdBefore(endDate));
        }

        List<AuditLog> logs = auditLogRepository.findAll(spec);

        return generateCsv(logs);
    }

    @Override
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
    }

    @Override
    public List<String> getDistinctEntityTypes() {
        return auditLogRepository.findDistinctEntityTypes();
    }

    private byte[] generateCsv(List<AuditLog> logs) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // BOM for Excel UTF-8 compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Header
            writer.println(
                    "ID,User ID,User Email,User Name,Action,Entity Type,Entity ID,Changes,IP Address,User Agent,Created At");

            // Data rows
            for (AuditLog log : logs) {
                String userEmail = log.getUser() != null ? log.getUser().getEmail() : "";
                String userName = getUserName(log);
                UUID userId = log.getUser() != null ? log.getUser().getId() : null;

                writer.println(String.format("%s,%s,\"%s\",\"%s\",%s,%s,%s,\"%s\",\"%s\",\"%s\",%s",
                        log.getId(),
                        userId != null ? userId.toString() : "",
                        escapeCsvField(userEmail),
                        escapeCsvField(userName),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        escapeCsvField(log.getChanges()),
                        escapeCsvField(log.getIpAddress()),
                        escapeCsvField(truncateUserAgent(log.getUserAgent())),
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

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        // Truncate long user agent strings for CSV
        if (userAgent.length() > 100) {
            return userAgent.substring(0, 100) + "...";
        }
        return userAgent;
    }

    private String getUserName(AuditLog log) {
        if (log.getUser() == null) {
            return "";
        }
        UserProfile profile = log.getUser().getProfile();
        if (profile == null) {
            return "";
        }
        if (profile.getFirstName() != null || profile.getLastName() != null) {
            String name = (profile.getFirstName() != null ? profile.getFirstName() : "")
                    + " "
                    + (profile.getLastName() != null ? profile.getLastName() : "");
            return name.trim();
        } else if (profile.getFullName() != null) {
            return profile.getFullName();
        }
        return "";
    }
}
