package com.lexia.backend.service;

import com.lexia.backend.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for audit log queries (Log Management feature).
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
public interface AuditLogQueryService {

    /**
     * Get paginated audit logs with optional filters.
     *
     * @param action     filter by action type (optional)
     * @param entityType filter by entity type (optional)
     * @param userId     filter by user ID (optional)
     * @param userEmail  filter by user email (partial match, optional)
     * @param ipAddress  filter by IP address (optional)
     * @param startDate  filter by start date (optional)
     * @param endDate    filter by end date (optional)
     * @param search     search in changes (optional)
     * @param pageable   pagination and sorting
     * @return page of audit log DTOs
     */
    Page<AuditLogDTO> getLogs(
            String action,
            String entityType,
            UUID userId,
            String userEmail,
            String ipAddress,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable);

    /**
     * Export audit logs to CSV format.
     *
     * @param action     filter by action type (optional)
     * @param entityType filter by entity type (optional)
     * @param userId     filter by user ID (optional)
     * @param startDate  filter by start date (optional)
     * @param endDate    filter by end date (optional)
     * @return CSV content as byte array
     */
    byte[] exportToCsv(
            String action,
            String entityType,
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Get distinct action types that have logs.
     *
     * @return list of action types
     */
    List<String> getDistinctActions();

    /**
     * Get distinct entity types that have logs.
     *
     * @return list of entity types
     */
    List<String> getDistinctEntityTypes();
}
