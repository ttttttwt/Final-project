package com.lexia.backend.email.entity;

import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an email in the processing queue.
 * Supports async processing with retry logic and priority-based ordering.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "email_queue", indexes = {
        @Index(name = "idx_email_queue_pending", columnList = "status, priority, created_at"),
        @Index(name = "idx_email_queue_retry", columnList = "next_retry_at"),
        @Index(name = "idx_email_queue_recipient", columnList = "recipient_id, created_at"),
        @Index(name = "idx_email_queue_type", columnList = "email_type, created_at"),
        @Index(name = "idx_email_queue_status", columnList = "status, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    // Recipient info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Size(max = 255)
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    // Email content
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String subject;

    @NotBlank
    @Size(max = 100)
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> templateData = new HashMap<>();

    // Status tracking
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmailPriority priority = EmailPriority.NORMAL;

    // Retry handling
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /**
     * Locale for template rendering (e.g., "en", "vi").
     * Defaults to English if not specified.
     */
    @Size(max = 10)
    @Column(name = "locale", length = 10)
    @Builder.Default
    private String locale = "en";

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    /**
     * Increments the attempt count and updates status/retry time.
     * Call this after a failed send attempt.
     */
    public void recordFailedAttempt(String errorMessage) {
        this.attempts++;
        this.lastError = errorMessage;

        if (this.attempts >= this.maxAttempts) {
            this.status = EmailStatus.FAILED;
            this.nextRetryAt = null;
        } else {
            this.status = EmailStatus.PENDING;
            int delayMinutes = this.priority.getRetryDelayMinutes(this.attempts);
            this.nextRetryAt = Instant.now().plusSeconds(delayMinutes * 60L);
        }
    }

    /**
     * Marks the email as successfully sent.
     */
    public void markAsSent() {
        this.status = EmailStatus.SENT;
        this.sentAt = Instant.now();
        this.attempts++;
        this.lastError = null;
        this.nextRetryAt = null;
    }

    /**
     * Marks the email as processing (being sent).
     */
    public void markAsProcessing() {
        this.status = EmailStatus.PROCESSING;
    }

    /**
     * Marks the email as delivered.
     */
    public void markAsDelivered() {
        this.status = EmailStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    /**
     * Cancels the email if it's still pending.
     *
     * @return true if cancelled, false if not in cancellable state
     */
    public boolean cancel() {
        if (this.status == EmailStatus.PENDING) {
            this.status = EmailStatus.CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * Checks if the email can be processed now.
     *
     * @return true if ready for processing
     */
    public boolean isReadyToProcess() {
        if (this.status != EmailStatus.PENDING) {
            return false;
        }
        if (this.nextRetryAt == null) {
            return true;
        }
        return Instant.now().isAfter(this.nextRetryAt);
    }
}
