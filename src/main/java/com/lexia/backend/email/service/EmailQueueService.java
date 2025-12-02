package com.lexia.backend.email.service;

import com.lexia.backend.email.entity.EmailQueue;

/**
 * Service for managing the email queue.
 * Handles processing, retrying, and cleaning up emails.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface EmailQueueService {

    /**
     * Processes pending emails from the queue.
     * This method is called by the scheduler.
     *
     * @param batchSize maximum number of emails to process
     * @return number of emails processed
     */
    int processPendingEmails(int batchSize);

    /**
     * Processes emails that are ready for retry.
     *
     * @return number of retries processed
     */
    int processRetryQueue();

    /**
     * Resets emails stuck in PROCESSING state.
     * This handles cases where the processor crashed.
     *
     * @return number of emails reset
     */
    int resetStuckEmails();

    /**
     * Cleans up old processed emails.
     *
     * @param daysToKeep number of days to retain
     * @return number of emails deleted
     */
    int cleanupOldEmails(int daysToKeep);

    /**
     * Processes a single email from the queue.
     *
     * @param emailQueue the email to process
     * @return true if sent successfully
     */
    boolean processEmail(EmailQueue emailQueue);
}
