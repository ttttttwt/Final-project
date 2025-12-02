package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.dto.EmailMessage;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.entity.EmailLog;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailProvider;
import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.exception.EmailSendException;
import com.lexia.backend.email.repository.EmailLogRepository;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.EmailProviderService;
import com.lexia.backend.email.service.EmailQueueService;
import com.lexia.backend.email.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of EmailQueueService.
 * Handles queue processing and email delivery.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueServiceImpl implements EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailTemplateService templateService;
    private final EmailProviderService emailProvider;
    private final EmailConfig emailConfig;

    @Override
    @Transactional
    public int processPendingEmails(int batchSize) {
        log.debug("Processing pending emails, batch size: {}", batchSize);

        List<EmailQueue> pendingEmails = emailQueueRepository.findPendingEmailsReadyToProcess(
                Instant.now(),
                PageRequest.of(0, batchSize));

        if (pendingEmails.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (EmailQueue email : pendingEmails) {
            try {
                if (processEmail(email)) {
                    processed++;
                }
            } catch (Exception e) {
                log.error("Error processing email {}: {}", email.getId(), e.getMessage());
            }
        }

        log.info("Processed {}/{} pending emails", processed, pendingEmails.size());
        return processed;
    }

    @Override
    @Transactional
    public int processRetryQueue() {
        log.debug("Processing retry queue");

        List<EmailQueue> retryEmails = emailQueueRepository.findEmailsReadyForRetry(
                Instant.now(),
                PageRequest.of(0, emailConfig.getQueue().getBatchSize()));

        if (retryEmails.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (EmailQueue email : retryEmails) {
            try {
                if (processEmail(email)) {
                    processed++;
                }
            } catch (Exception e) {
                log.error("Error retrying email {}: {}", email.getId(), e.getMessage());
            }
        }

        log.info("Processed {}/{} retry emails", processed, retryEmails.size());
        return processed;
    }

    @Override
    @Transactional
    public int resetStuckEmails() {
        Instant timeout = Instant.now().minus(
                emailConfig.getQueue().getProcessingTimeoutMinutes(),
                ChronoUnit.MINUTES);
        int reset = emailQueueRepository.resetStuckProcessingEmails(timeout);
        if (reset > 0) {
            log.warn("Reset {} stuck processing emails", reset);
        }
        return reset;
    }

    @Override
    @Transactional
    public int cleanupOldEmails(int daysToKeep) {
        Instant cutoff = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);

        // Delete old logs first (due to foreign key)
        int deletedLogs = emailLogRepository.deleteOldLogs(cutoff);

        // Delete old queue entries (only terminal statuses)
        List<EmailStatus> terminalStatuses = List.of(
                EmailStatus.SENT,
                EmailStatus.DELIVERED,
                EmailStatus.FAILED,
                EmailStatus.BOUNCED,
                EmailStatus.CANCELLED);
        int deletedQueue = emailQueueRepository.deleteOldEmails(cutoff, terminalStatuses);

        log.info("Cleaned up {} queue entries and {} logs older than {} days",
                deletedQueue, deletedLogs, daysToKeep);
        return deletedQueue;
    }

    @Override
    @Transactional
    public boolean processEmail(EmailQueue emailQueue) {
        log.debug("Processing email: id={}, type={}, recipient={}",
                emailQueue.getId(), emailQueue.getEmailType(), emailQueue.getRecipientEmail());

        try {
            // Mark as processing
            emailQueue.markAsProcessing();
            emailQueueRepository.save(emailQueue);

            // Render template
            Locale locale = Locale.ENGLISH; // TODO: Get from user preferences
            Map<String, Object> variables = emailQueue.getTemplateData();

            String recipientId = emailQueue.getRecipient() != null
                    ? emailQueue.getRecipient().getId().toString()
                    : null;
            templateService.addCommonVariables(variables, recipientId);

            String htmlBody = templateService.renderTemplate(
                    emailQueue.getTemplateName(),
                    variables,
                    locale);
            String textBody = templateService.renderPlainText(
                    emailQueue.getTemplateName(),
                    variables,
                    locale);

            // Build message
            EmailMessage message = EmailMessage.builder()
                    .from(emailConfig.getFromAddress())
                    .fromName(emailConfig.getFromName())
                    .to(emailQueue.getRecipientEmail())
                    .toName(emailQueue.getRecipientName())
                    .replyTo(emailConfig.getReplyToAddress())
                    .subject(emailQueue.getSubject())
                    .htmlBody(htmlBody)
                    .textBody(textBody)
                    .build();

            // Send email
            EmailSendResult result = emailProvider.send(message);

            if (result.isSuccess()) {
                // Mark as sent
                emailQueue.markAsSent();
                emailQueueRepository.save(emailQueue);

                // Log successful send
                EmailLog emailLog = EmailLog.fromSentEmail(
                        emailQueue,
                        EmailProvider.SMTP,
                        result.getMessageId());
                emailLogRepository.save(emailLog);

                log.info("Email sent successfully: id={}, messageId={}",
                        emailQueue.getId(), result.getMessageId());
                return true;
            } else {
                handleSendFailure(emailQueue, result.getErrorMessage());
                return false;
            }

        } catch (EmailSendException e) {
            handleSendFailure(emailQueue, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error processing email {}: {}", emailQueue.getId(), e.getMessage());
            handleSendFailure(emailQueue, "Unexpected error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles email send failure with retry logic.
     *
     * @param emailQueue   the failed email
     * @param errorMessage the error message
     */
    private void handleSendFailure(EmailQueue emailQueue, String errorMessage) {
        emailQueue.recordFailedAttempt(errorMessage);
        emailQueueRepository.save(emailQueue);

        if (emailQueue.getStatus() == EmailStatus.FAILED) {
            // Log final failure
            EmailLog emailLog = EmailLog.fromFailedEmail(
                    emailQueue,
                    EmailProvider.SMTP,
                    errorMessage);
            emailLogRepository.save(emailLog);

            log.error("Email failed permanently: id={}, attempts={}, error={}",
                    emailQueue.getId(), emailQueue.getAttempts(), errorMessage);
        } else {
            log.warn("Email send failed, will retry: id={}, attempts={}, nextRetry={}, error={}",
                    emailQueue.getId(), emailQueue.getAttempts(),
                    emailQueue.getNextRetryAt(), errorMessage);
        }
    }
}
