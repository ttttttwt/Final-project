package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.dto.EmailAttachment;
import com.lexia.backend.email.dto.EmailMessage;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.exception.EmailSendException;
import com.lexia.backend.email.service.EmailProviderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * SMTP implementation of EmailProviderService.
 * Uses Spring's JavaMailSender for sending emails via SMTP.
 * 
 * <p>
 * This service is only activated when JavaMailSender bean is available,
 * which requires spring.mail.host configuration to be present.
 * </p>
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@ConditionalOnBean(JavaMailSender.class)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailProvider implements EmailProviderService {

    private final JavaMailSender mailSender;

    @Override
    public EmailSendResult send(EmailMessage message) throws EmailSendException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    message.hasAttachments(),
                    "UTF-8");

            // Set from address
            if (message.getFromName() != null && !message.getFromName().isBlank()) {
                helper.setFrom(new InternetAddress(message.getFrom(), message.getFromName()));
            } else {
                helper.setFrom(message.getFrom());
            }

            // Set to address
            if (message.getToName() != null && !message.getToName().isBlank()) {
                helper.setTo(new InternetAddress(message.getTo(), message.getToName()));
            } else {
                helper.setTo(message.getTo());
            }

            // Set reply-to if provided
            if (message.getReplyTo() != null && !message.getReplyTo().isBlank()) {
                helper.setReplyTo(message.getReplyTo());
            }

            // Set subject
            helper.setSubject(message.getSubject());

            // Set content (HTML with text fallback)
            if (message.hasHtmlBody() && message.hasTextBody()) {
                helper.setText(message.getTextBody(), message.getHtmlBody());
            } else if (message.hasHtmlBody()) {
                helper.setText(message.getHtmlBody(), true);
            } else if (message.hasTextBody()) {
                helper.setText(message.getTextBody(), false);
            }

            // Add custom headers
            if (message.getHeaders() != null) {
                for (var entry : message.getHeaders().entrySet()) {
                    mimeMessage.addHeader(entry.getKey(), entry.getValue());
                }
            }

            // Add attachments
            if (message.hasAttachments()) {
                for (EmailAttachment attachment : message.getAttachments()) {
                    addAttachment(helper, attachment);
                }
            }

            // Send the email
            mailSender.send(mimeMessage);

            String messageId = mimeMessage.getMessageID();
            log.debug("Email sent successfully to {} with messageId: {}", message.getTo(), messageId);

            return EmailSendResult.success(messageId);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", message.getTo(), e.getMessage());
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Adds an attachment to the email.
     *
     * @param helper     the MimeMessageHelper
     * @param attachment the attachment to add
     * @throws MessagingException if adding attachment fails
     */
    private void addAttachment(MimeMessageHelper helper, EmailAttachment attachment)
            throws MessagingException {

        if (attachment.getDataSource() != null) {
            // Use DataSource if provided
            if (attachment.isInline()) {
                helper.addInline(attachment.getContentId(), attachment.getDataSource());
            } else {
                helper.addAttachment(attachment.getFilename(), attachment.getDataSource());
            }
        } else if (attachment.getContent() != null) {
            // Use byte array content
            ByteArrayResource resource = new ByteArrayResource(attachment.getContent());
            if (attachment.isInline()) {
                helper.addInline(
                        attachment.getContentId(),
                        resource,
                        attachment.getContentType());
            } else {
                helper.addAttachment(attachment.getFilename(), resource);
            }
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            if (mailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                javaMailSenderImpl.testConnection();
                return true;
            }
            // For other implementations, assume healthy
            return true;
        } catch (MessagingException e) {
            log.warn("SMTP connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }
}
