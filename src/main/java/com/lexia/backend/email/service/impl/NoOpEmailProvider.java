package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.dto.EmailMessage;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.exception.EmailSendException;
import com.lexia.backend.email.service.EmailProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * No-operation email provider for environments without mail server.
 * Logs email content instead of sending.
 * 
 * <p>
 * This provider is activated when JavaMailSender bean is not available,
 * typically in test environments or local development without MailHog.
 * </p>
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@ConditionalOnMissingBean(JavaMailSender.class)
@Slf4j
public class NoOpEmailProvider implements EmailProviderService {

    @Override
    public EmailSendResult send(EmailMessage message) throws EmailSendException {
        log.info("=== NO-OP EMAIL PROVIDER (Mail server not configured) ===");
        log.info("To: {} <{}>", message.getToName(), message.getTo());
        log.info("From: {} <{}>", message.getFromName(), message.getFrom());
        log.info("Subject: {}", message.getSubject());
        log.debug("HTML Body length: {} chars",
                message.getHtmlBody() != null ? message.getHtmlBody().length() : 0);
        log.debug("Text Body length: {} chars",
                message.getTextBody() != null ? message.getTextBody().length() : 0);
        log.info("=========================================================");

        // Simulate successful send
        String fakeMessageId = "noop-" + java.util.UUID.randomUUID().toString() + "@local";
        return EmailSendResult.success(fakeMessageId);
    }

    @Override
    public boolean isHealthy() {
        // NoOp provider is always "healthy" since it doesn't actually send
        return true;
    }

    @Override
    public String getProviderName() {
        return "NOOP";
    }
}
