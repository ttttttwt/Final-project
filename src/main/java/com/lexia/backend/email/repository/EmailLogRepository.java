package com.lexia.backend.email.repository;

import com.lexia.backend.email.entity.EmailLog;
import com.lexia.backend.email.entity.EmailLog.LogStatus;
import com.lexia.backend.email.enums.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailLog entity with analytics queries.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID>,
        JpaSpecificationExecutor<EmailLog> {

    /**
     * Finds logs for a specific recipient.
     *
     * @param recipientId the user ID
     * @param pageable    pagination info
     * @return page of logs
     */
    Page<EmailLog> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    /**
     * Finds logs by email type.
     *
     * @param emailType the email type
     * @param pageable  pagination info
     * @return page of logs
     */
    Page<EmailLog> findByEmailTypeOrderByCreatedAtDesc(EmailType emailType, Pageable pageable);

    /**
     * Finds logs by status.
     *
     * @param status   the log status
     * @param pageable pagination info
     * @return page of logs
     */
    Page<EmailLog> findByStatusOrderByCreatedAtDesc(LogStatus status, Pageable pageable);

    /**
     * Finds log by queue ID.
     *
     * @param queueId the email queue ID
     * @return optional log
     */
    Optional<EmailLog> findByEmailQueueId(UUID queueId);

    /**
     * Finds log by provider message ID.
     *
     * @param providerMessageId the provider's message ID
     * @return optional log
     */
    Optional<EmailLog> findByProviderMessageId(String providerMessageId);

    /**
     * Counts emails by status within a time range.
     *
     * @param status the status
     * @param start  start time (inclusive)
     * @param end    end time (exclusive)
     * @return count
     */
    long countByStatusAndCreatedAtBetween(LogStatus status, Instant start, Instant end);

    /**
     * Counts emails by type within a time range.
     *
     * @param emailType the email type
     * @param start     start time
     * @param end       end time
     * @return count
     */
    long countByEmailTypeAndCreatedAtBetween(EmailType emailType, Instant start, Instant end);

    /**
     * Counts opened emails within a time range.
     *
     * @param start start time
     * @param end   end time
     * @return count of opened emails
     */
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.openedAt IS NOT NULL AND e.openedAt BETWEEN :start AND :end")
    long countOpenedBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Counts clicked emails within a time range.
     *
     * @param start start time
     * @param end   end time
     * @return count of clicked emails
     */
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.clickedAt IS NOT NULL AND e.clickedAt BETWEEN :start AND :end")
    long countClickedBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Gets aggregate statistics for a time period.
     *
     * @param start start time
     * @param end   end time
     * @return array with [sent, delivered, opened, clicked, bounced, complained]
     *         counts
     */
    @Query("""
            SELECT
                COUNT(e),
                SUM(CASE WHEN e.status = 'DELIVERED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.openedAt IS NOT NULL THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.clickedAt IS NOT NULL THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'BOUNCED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'COMPLAINED' THEN 1 ELSE 0 END)
            FROM EmailLog e
            WHERE e.createdAt BETWEEN :start AND :end
            """)
    Object[] getStatsSummary(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Gets statistics grouped by email type.
     *
     * @param start start time
     * @param end   end time
     * @return list of [emailType, sent, opened, clicked] tuples
     */
    @Query("""
            SELECT
                e.emailType,
                COUNT(e),
                SUM(CASE WHEN e.openedAt IS NOT NULL THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.clickedAt IS NOT NULL THEN 1 ELSE 0 END)
            FROM EmailLog e
            WHERE e.createdAt BETWEEN :start AND :end
            GROUP BY e.emailType
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> getStatsByType(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Gets daily email counts for trend analysis.
     *
     * @param start start time
     * @param end   end time
     * @return list of [date, sent, opened, clicked] tuples
     */
    @Query(value = """
            SELECT
                DATE(created_at) as date,
                COUNT(*) as sent,
                SUM(CASE WHEN opened_at IS NOT NULL THEN 1 ELSE 0 END) as opened,
                SUM(CASE WHEN clicked_at IS NOT NULL THEN 1 ELSE 0 END) as clicked
            FROM email_logs
            WHERE created_at BETWEEN :start AND :end
            GROUP BY DATE(created_at)
            ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getDailyStats(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Deletes old logs for cleanup.
     *
     * @param before delete logs created before this time
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM EmailLog e WHERE e.createdAt < :before")
    int deleteOldLogs(@Param("before") Instant before);

    /**
     * Finds bounced emails for a recipient (for suppression list).
     *
     * @param recipientEmail the email address
     * @return list of bounced logs
     */
    @Query("SELECT e FROM EmailLog e WHERE e.recipientEmail = :email AND e.status = 'BOUNCED'")
    List<EmailLog> findBouncedByEmail(@Param("email") String recipientEmail);

    /**
     * Checks if an email address has hard bounced recently (for suppression).
     *
     * @param recipientEmail the email address
     * @param since          check bounces since this time
     * @return true if email has bounced
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM EmailLog e
            WHERE e.recipientEmail = :email
            AND e.status = 'BOUNCED'
            AND e.bouncedAt > :since
            """)
    boolean hasRecentBounce(@Param("email") String recipientEmail, @Param("since") Instant since);

    /**
     * Counts complaints for monitoring.
     *
     * @param start start time
     * @param end   end time
     * @return complaint count
     */
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.complainedAt IS NOT NULL AND e.complainedAt BETWEEN :start AND :end")
    long countComplaintsBetween(@Param("start") Instant start, @Param("end") Instant end);
}
