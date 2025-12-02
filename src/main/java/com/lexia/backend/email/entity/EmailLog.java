package com.lexia.backend.email.entity;

import com.lexia.backend.email.enums.EmailProvider;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for logging email delivery events and tracking engagement.
 * Stores historical data for analytics and troubleshooting.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_email_logs_recipient", columnList = "recipient_id, created_at"),
        @Index(name = "idx_email_logs_type", columnList = "email_type, created_at"),
        @Index(name = "idx_email_logs_status", columnList = "status, created_at"),
        @Index(name = "idx_email_logs_queue", columnList = "queue_id"),
        @Index(name = "idx_email_logs_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    /**
     * Status of logged email event.
     */
    public enum LogStatus {
        SENT,
        DELIVERED,
        OPENED,
        CLICKED,
        BOUNCED,
        COMPLAINED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id")
    private EmailQueue emailQueue;

    // Recipient info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    // Email info
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String subject;

    // Provider info
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmailProvider provider;

    @Size(max = 255)
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    // Status
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Tracking timestamps
    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "clicked_at")
    private Instant clickedAt;

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;

    @Column(name = "bounced_at")
    private Instant bouncedAt;

    @Column(name = "complained_at")
    private Instant complainedAt;

    // Metadata
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Records an open event.
     *
     * @param ipAddress the IP address from which email was opened
     * @param userAgent the user agent of the email client
     */
    public void recordOpen(String ipAddress, String userAgent) {
        if (this.openedAt == null) {
            this.openedAt = Instant.now();
            this.status = LogStatus.OPENED;
        }
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Records a click event.
     *
     * @param ipAddress the IP address from which link was clicked
     * @param userAgent the user agent of the email client
     */
    public void recordClick(String ipAddress, String userAgent) {
        if (this.clickedAt == null) {
            this.clickedAt = Instant.now();
            this.status = LogStatus.CLICKED;
        }
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Records a bounce event.
     */
    public void recordBounce(String errorMessage) {
        this.bouncedAt = Instant.now();
        this.status = LogStatus.BOUNCED;
        this.errorMessage = errorMessage;
    }

    /**
     * Records a spam complaint.
     */
    public void recordComplaint() {
        this.complainedAt = Instant.now();
        this.status = LogStatus.COMPLAINED;
    }

    /**
     * Records an unsubscribe event.
     */
    public void recordUnsubscribe() {
        this.unsubscribedAt = Instant.now();
    }

    /**
     * Creates a log entry from a sent email queue item.
     *
     * @param emailQueue the queue item
     * @param provider   the provider used
     * @param messageId  the provider's message ID
     * @return the log entry
     */
    public static EmailLog fromSentEmail(EmailQueue emailQueue, EmailProvider provider, String messageId) {
        return EmailLog.builder()
                .emailQueue(emailQueue)
                .recipient(emailQueue.getRecipient())
                .recipientEmail(emailQueue.getRecipientEmail())
                .emailType(emailQueue.getEmailType())
                .subject(emailQueue.getSubject())
                .provider(provider)
                .providerMessageId(messageId)
                .status(LogStatus.SENT)
                .build();
    }

    /**
     * Creates a log entry for a failed email.
     *
     * @param emailQueue   the queue item
     * @param provider     the provider used
     * @param errorMessage the error message
     * @return the log entry
     */
    public static EmailLog fromFailedEmail(EmailQueue emailQueue, EmailProvider provider, String errorMessage) {
        return EmailLog.builder()
                .emailQueue(emailQueue)
                .recipient(emailQueue.getRecipient())
                .recipientEmail(emailQueue.getRecipientEmail())
                .emailType(emailQueue.getEmailType())
                .subject(emailQueue.getSubject())
                .provider(provider)
                .status(LogStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
    }
}
