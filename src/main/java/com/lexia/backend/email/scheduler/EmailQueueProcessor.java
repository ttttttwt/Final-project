package com.lexia.backend.email.scheduler;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled processor for the email queue.
 * Periodically processes pending emails and handles retries.
 * 
 * Can be disabled via configuration property: lexia.email.queue.enabled=false
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "lexia.email.queue.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EmailQueueProcessor {

    private final EmailQueueService emailQueueService;
    private final EmailConfig emailConfig;

    /**
     * Processes pending emails from the queue.
     * Runs at a fixed interval configured via properties.
     * Default: every 5 seconds.
     */
    @Scheduled(fixedDelayString = "${lexia.email.queue.process-interval-ms:5000}")
    public void processPendingEmails() {
        try {
            int batchSize = emailConfig.getQueue().getBatchSize();
            int processed = emailQueueService.processPendingEmails(batchSize);

            if (processed > 0) {
                log.info("Email queue processor: processed {} emails", processed);
            }
        } catch (Exception e) {
            log.error("Error in email queue processor: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes emails that are ready for retry.
     * Runs every minute.
     */
    @Scheduled(fixedDelay = 60000) // Every 1 minute
    public void processRetryQueue() {
        try {
            int retried = emailQueueService.processRetryQueue();

            if (retried > 0) {
                log.info("Email retry processor: retried {} emails", retried);
            }
        } catch (Exception e) {
            log.error("Error in email retry processor: {}", e.getMessage(), e);
        }
    }

    /**
     * Resets emails stuck in PROCESSING state.
     * This handles cases where the processor crashed while sending.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void resetStuckEmails() {
        try {
            int reset = emailQueueService.resetStuckEmails();

            if (reset > 0) {
                log.warn("Reset {} stuck processing emails", reset);
            }
        } catch (Exception e) {
            log.error("Error resetting stuck emails: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleans up old processed emails.
     * Runs daily at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3:00 AM
    public void cleanupOldEmails() {
        try {
            int retentionDays = emailConfig.getQueue().getRetentionDays();
            int deleted = emailQueueService.cleanupOldEmails(retentionDays);

            log.info("Email cleanup: deleted {} old emails (retention: {} days)",
                    deleted, retentionDays);
        } catch (Exception e) {
            log.error("Error cleaning up old emails: {}", e.getMessage(), e);
        }
    }
}
