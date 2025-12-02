package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.dto.EmailMessage;
import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.exception.EmailSendException;
import com.lexia.backend.email.mapper.EmailMapper;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.EmailProviderService;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.email.service.EmailTemplateService;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of EmailService.
 * Handles email queueing and synchronous sending.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailServiceImpl implements EmailService {

    private final EmailQueueRepository emailQueueRepository;
    private final UserRepository userRepository;
    private final EmailTemplateService templateService;
    private final EmailProviderService emailProvider;
    private final EmailConfig emailConfig;

    @Override
    public UUID queueEmail(EmailRequest request) {
        log.debug("Queueing email of type {} to {}", request.getEmailType(), request.getRecipientEmail());

        // Get recipient user if ID provided
        User recipient = null;
        if (request.getRecipientId() != null) {
            recipient = userRepository.findById(request.getRecipientId()).orElse(null);
        }

        // Get subject from template service or request
        String subject = request.getSubject();
        if (subject == null || subject.isBlank()) {
            Locale locale = Locale.forLanguageTag(request.getLocale());
            subject = templateService.getSubject(
                    request.getEmailType(),
                    request.getTemplateData(),
                    locale);
        }

        // Create queue entry
        EmailQueue emailQueue = EmailMapper.toEntity(request, recipient, subject);
        emailQueue = emailQueueRepository.save(emailQueue);

        log.info("Email queued successfully: id={}, type={}, recipient={}",
                emailQueue.getId(), request.getEmailType(), request.getRecipientEmail());

        return emailQueue.getId();
    }

    @Override
    public List<UUID> queueBulkEmails(List<EmailRequest> requests) {
        log.debug("Queueing {} bulk emails", requests.size());

        return requests.stream()
                .map(this::queueEmail)
                .collect(Collectors.toList());
    }

    @Override
    public boolean sendEmailSync(EmailRequest request) {
        log.debug("Sending email synchronously of type {} to {}",
                request.getEmailType(), request.getRecipientEmail());

        try {
            // Render template
            Locale locale = Locale.forLanguageTag(request.getLocale());
            Map<String, Object> variables = request.getTemplateData();
            templateService.addCommonVariables(variables,
                    request.getRecipientId() != null ? request.getRecipientId().toString() : null);

            String htmlBody = templateService.renderTemplate(
                    request.getEffectiveTemplateName(),
                    variables,
                    locale);
            String textBody = templateService.renderPlainText(
                    request.getEffectiveTemplateName(),
                    variables,
                    locale);

            // Get subject
            String subject = request.getSubject();
            if (subject == null || subject.isBlank()) {
                subject = templateService.getSubject(request.getEmailType(), variables, locale);
            }

            // Build message
            EmailMessage message = EmailMessage.builder()
                    .from(emailConfig.getFromAddress())
                    .fromName(emailConfig.getFromName())
                    .to(request.getRecipientEmail())
                    .toName(request.getRecipientName())
                    .replyTo(emailConfig.getReplyToAddress())
                    .subject(subject)
                    .htmlBody(htmlBody)
                    .textBody(textBody)
                    .build();

            // Send
            EmailSendResult result = emailProvider.send(message);

            if (result.isSuccess()) {
                log.info("Email sent successfully (sync): type={}, recipient={}, messageId={}",
                        request.getEmailType(), request.getRecipientEmail(), result.getMessageId());
                return true;
            } else {
                log.error("Email send failed (sync): type={}, recipient={}, error={}",
                        request.getEmailType(), request.getRecipientEmail(), result.getErrorMessage());
                return false;
            }

        } catch (EmailSendException e) {
            log.error("Email send exception (sync): type={}, recipient={}, error={}",
                    request.getEmailType(), request.getRecipientEmail(), e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailQueueDTO> getEmailStatus(UUID emailId) {
        return emailQueueRepository.findById(emailId)
                .map(EmailMapper::toDTO);
    }

    @Override
    public boolean cancelEmail(UUID emailId) {
        return emailQueueRepository.findById(emailId)
                .filter(email -> email.getStatus() == EmailStatus.PENDING)
                .map(email -> {
                    email.setStatus(EmailStatus.CANCELLED);
                    emailQueueRepository.save(email);
                    log.info("Email cancelled: id={}", emailId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean retryEmail(UUID emailId) {
        return emailQueueRepository.findById(emailId)
                .filter(email -> email.getStatus() == EmailStatus.FAILED)
                .map(email -> {
                    email.setStatus(EmailStatus.PENDING);
                    email.setNextRetryAt(null);
                    emailQueueRepository.save(email);
                    log.info("Email retry scheduled: id={}", emailId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return emailQueueRepository.countPending();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFailedCount() {
        return emailQueueRepository.countFailed();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean wasRecentlySent(EmailRequest request, int minutesWindow) {
        Instant since = Instant.now().minus(minutesWindow, ChronoUnit.MINUTES);
        return emailQueueRepository.existsRecentEmail(
                request.getRecipientEmail(),
                request.getEmailType(),
                since);
    }
}
