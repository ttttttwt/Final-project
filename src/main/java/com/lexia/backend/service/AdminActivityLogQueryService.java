package com.lexia.backend.service;

import com.lexia.backend.dto.AdminActivityLogDTO;
import com.lexia.backend.dto.AdminActivityLogStatsDTO;
import com.lexia.backend.entity.AdminActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for admin activity log queries (Log Management feature).
 * Extends the existing AdminActivityLogService for read operations.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
public interface AdminActivityLogQueryService {

    /**
     * Get paginated activity logs with optional filters.
     *
     * @param action     filter by action type (optional)
     * @param entityType filter by entity type (optional)
     * @param userId     filter by user ID (optional)
     * @param userName   filter by user name (partial match, optional)
     * @param entityName filter by entity name (partial match, optional)
     * @param startDate  filter by start date (optional)
     * @param endDate    filter by end date (optional)
     * @param search     search in description (optional)
     * @param pageable   pagination and sorting
     * @return page of activity log DTOs
     */
    Page<AdminActivityLogDTO> getLogs(
            AdminActivityLog.ActionType action,
            AdminActivityLog.EntityType entityType,
            UUID userId,
            String userName,
            String entityName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable);

    /**
     * Get activity statistics for a given period.
     *
     * @param period the time period (today, week, month, all)
     * @return activity statistics DTO
     */
    AdminActivityLogStatsDTO getStats(String period);

    /**
     * Export activity logs to CSV format.
     *
     * @param action     filter by action type (optional)
     * @param entityType filter by entity type (optional)
     * @param userId     filter by user ID (optional)
     * @param startDate  filter by start date (optional)
     * @param endDate    filter by end date (optional)
     * @return CSV content as byte array
     */
    byte[] exportToCsv(
            AdminActivityLog.ActionType action,
            AdminActivityLog.EntityType entityType,
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Get distinct action types that have logs.
     *
     * @return list of action types
     */
    List<AdminActivityLog.ActionType> getDistinctActions();

    /**
     * Get distinct user names that have logs.
     *
     * @return list of user names
     */
    List<String> getDistinctUserNames();
}
