package com.lexia.backend.email.dto;

import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.enums.EmailType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for email queue information.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailQueueDTO {

    private UUID id;
    private UUID recipientId;
    private String recipientEmail;
    private String recipientName;
    private EmailType emailType;
    private String subject;
    private String templateName;
    private EmailStatus status;
    private String priority;
    private Integer attempts;
    private Integer maxAttempts;
    private Instant nextRetryAt;
    private String lastError;
    private Instant createdAt;
    private Instant sentAt;
    private Instant deliveredAt;
}
