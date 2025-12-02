package com.lexia.backend.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO representing an email message ready to be sent.
 * Contains all rendered content and metadata.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    /**
     * Sender email address.
     */
    private String from;

    /**
     * Sender display name.
     */
    private String fromName;

    /**
     * Recipient email address.
     */
    private String to;

    /**
     * Recipient display name.
     */
    private String toName;

    /**
     * Reply-to email address (optional).
     */
    private String replyTo;

    /**
     * Email subject line.
     */
    private String subject;

    /**
     * HTML body content.
     */
    private String htmlBody;

    /**
     * Plain text body content (fallback).
     */
    private String textBody;

    /**
     * Email attachments.
     */
    @Builder.Default
    private List<EmailAttachment> attachments = new ArrayList<>();

    /**
     * Custom headers.
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * Tags for categorization and tracking.
     */
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    /**
     * Adds an attachment.
     *
     * @param attachment the attachment to add
     * @return this message for chaining
     */
    public EmailMessage addAttachment(EmailAttachment attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
        return this;
    }

    /**
     * Adds a custom header.
     *
     * @param name  header name
     * @param value header value
     * @return this message for chaining
     */
    public EmailMessage addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(name, value);
        return this;
    }

    /**
     * Adds a tag.
     *
     * @param key   tag key
     * @param value tag value
     * @return this message for chaining
     */
    public EmailMessage addTag(String key, String value) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(key, value);
        return this;
    }

    /**
     * Checks if this message has HTML content.
     *
     * @return true if HTML body is present
     */
    public boolean hasHtmlBody() {
        return htmlBody != null && !htmlBody.isBlank();
    }

    /**
     * Checks if this message has plain text content.
     *
     * @return true if text body is present
     */
    public boolean hasTextBody() {
        return textBody != null && !textBody.isBlank();
    }

    /**
     * Checks if this message has attachments.
     *
     * @return true if attachments exist
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
}
