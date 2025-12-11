package com.lexia.backend.email.service;

import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Main service interface for email operations.
 * Handles queueing, sending, and managing emails.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface EmailService {

    /**
     * Queues an email for async delivery.
     *
     * @param request the email request
     * @return the ID of the queued email
     */
    UUID queueEmail(EmailRequest request);

    /**
     * Queues multiple emails for async delivery.
     *
     * @param requests the email requests
     * @return list of IDs for the queued emails
     */
    List<UUID> queueBulkEmails(List<EmailRequest> requests);

    /**
     * Sends an email immediately (bypassing the queue).
     * Use this for critical emails that must be sent instantly.
     *
     * @param request the email request
     * @return the result of the send operation
     */
    boolean sendEmailSync(EmailRequest request);

    /**
     * Gets the status of a queued email.
     *
     * @param emailId the email ID
     * @return the email queue DTO if found
     */
    Optional<EmailQueueDTO> getEmailStatus(UUID emailId);

    /**
     * Cancels a pending email.
     *
     * @param emailId the email ID
     * @return true if cancelled successfully
     */
    boolean cancelEmail(UUID emailId);

    /**
     * Retries a failed email.
     *
     * @param emailId the email ID
     * @return true if retry was scheduled
     */
    boolean retryEmail(UUID emailId);

    /**
     * Gets pending email count.
     *
     * @return count of pending emails
     */
    long getPendingCount();

    /**
     * Gets failed email count.
     *
     * @return count of failed emails
     */
    long getFailedCount();

    /**
     * Checks if a similar email was recently sent (to prevent spam).
     *
     * @param request       the email request to check
     * @param minutesWindow time window to check
     * @return true if a similar email was recently sent
     */
    boolean wasRecentlySent(EmailRequest request, int minutesWindow);
}
