package com.lexia.backend.email.repository;

import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailStatus;
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
import java.util.UUID;

/**
 * Repository for EmailQueue entity with custom queue processing queries.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, UUID>,
        JpaSpecificationExecutor<EmailQueue> {

    /**
     * Finds pending emails ready to process, ordered by priority and creation time.
     * Emails are ready if status is PENDING and either no retry time is set,
     * or the retry time has passed.
     *
     * @param pageable pagination info
     * @return page of emails ready to process
     */
    @Query("""
            SELECT e FROM EmailQueue e
            WHERE e.status = 'PENDING'
            AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
            ORDER BY
                CASE e.priority
                    WHEN 'CRITICAL' THEN 1
                    WHEN 'HIGH' THEN 2
                    WHEN 'NORMAL' THEN 3
                    WHEN 'LOW' THEN 4
                END,
                e.createdAt ASC
            """)
    List<EmailQueue> findPendingEmailsReadyToProcess(@Param("now") Instant now, Pageable pageable);

    /**
     * Finds pending emails by priority.
     *
     * @param priority the priority level
     * @param pageable pagination info
     * @return list of pending emails
     */
    @Query("""
            SELECT e FROM EmailQueue e
            WHERE e.status = 'PENDING'
            AND e.priority = :priority
            AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
            ORDER BY e.createdAt ASC
            """)
    List<EmailQueue> findPendingByPriority(@Param("priority") EmailPriority priority,
            @Param("now") Instant now,
            Pageable pageable);

    /**
     * Finds emails for a specific recipient.
     *
     * @param recipientId the user ID
     * @param pageable    pagination info
     * @return page of emails
     */
    Page<EmailQueue> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    /**
     * Finds emails by status.
     *
     * @param status   the email status
     * @param pageable pagination info
     * @return page of emails
     */
    Page<EmailQueue> findByStatusOrderByCreatedAtDesc(EmailStatus status, Pageable pageable);

    /**
     * Finds emails by type.
     *
     * @param emailType the email type
     * @param pageable  pagination info
     * @return page of emails
     */
    Page<EmailQueue> findByEmailTypeOrderByCreatedAtDesc(EmailType emailType, Pageable pageable);

    /**
     * Counts emails by status.
     *
     * @param status the status to count
     * @return count of emails
     */
    long countByStatus(EmailStatus status);

    /**
     * Counts emails by status created after a given time.
     *
     * @param status the status
     * @param after  the time threshold
     * @return count of emails
     */
    long countByStatusAndCreatedAtAfter(EmailStatus status, Instant after);

    /**
     * Counts pending emails in queue.
     *
     * @return count of pending emails
     */
    @Query("SELECT COUNT(e) FROM EmailQueue e WHERE e.status = 'PENDING'")
    long countPending();

    /**
     * Counts failed emails.
     *
     * @return count of failed emails
     */
    @Query("SELECT COUNT(e) FROM EmailQueue e WHERE e.status = 'FAILED'")
    long countFailed();

    /**
     * Deletes old processed emails for cleanup.
     *
     * @param before   delete emails created before this time
     * @param statuses statuses to delete (typically terminal statuses)
     * @return number of deleted records
     */
    @Modifying
    @Query("""
            DELETE FROM EmailQueue e
            WHERE e.createdAt < :before
            AND e.status IN :statuses
            """)
    int deleteOldEmails(@Param("before") Instant before,
            @Param("statuses") List<EmailStatus> statuses);

    /**
     * Updates status for stuck processing emails (timeout recovery).
     * Emails stuck in PROCESSING for too long are reset to PENDING for retry.
     *
     * @param timeout emails in PROCESSING longer than this are reset
     * @return number of updated records
     */
    @Modifying
    @Query("""
            UPDATE EmailQueue e
            SET e.status = 'PENDING',
                e.lastError = 'Processing timeout - reset for retry'
            WHERE e.status = 'PROCESSING'
            AND e.createdAt < :timeout
            """)
    int resetStuckProcessingEmails(@Param("timeout") Instant timeout);

    /**
     * Finds emails that are ready for retry.
     *
     * @param now      current time
     * @param pageable pagination
     * @return list of emails ready for retry
     */
    @Query("""
            SELECT e FROM EmailQueue e
            WHERE e.status = 'PENDING'
            AND e.nextRetryAt IS NOT NULL
            AND e.nextRetryAt <= :now
            ORDER BY e.priority, e.nextRetryAt
            """)
    List<EmailQueue> findEmailsReadyForRetry(@Param("now") Instant now, Pageable pageable);

    /**
     * Checks if a verification email was recently sent to avoid spam.
     *
     * @param recipientEmail the recipient email
     * @param emailType      the email type
     * @param since          check for emails since this time
     * @return true if a similar email was recently sent
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM EmailQueue e
            WHERE e.recipientEmail = :email
            AND e.emailType = :type
            AND e.createdAt > :since
            AND e.status IN ('PENDING', 'PROCESSING', 'SENT', 'DELIVERED')
            """)
    boolean existsRecentEmail(@Param("email") String recipientEmail,
            @Param("type") EmailType emailType,
            @Param("since") Instant since);

    /**
     * Gets queue statistics summary.
     *
     * @return array with [pending, processing, sent, failed, bounced] counts
     */
    @Query("""
            SELECT
                SUM(CASE WHEN e.status = 'PENDING' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'PROCESSING' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'SENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'DELIVERED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'FAILED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = 'BOUNCED' THEN 1 ELSE 0 END)
            FROM EmailQueue e
            """)
    Object[] getQueueStatistics();
}
