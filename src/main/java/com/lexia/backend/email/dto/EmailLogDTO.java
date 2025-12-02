package com.lexia.backend.email.dto;

import com.lexia.backend.email.entity.EmailLog.LogStatus;
import com.lexia.backend.email.enums.EmailProvider;
import com.lexia.backend.email.enums.EmailType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for email log information.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLogDTO {

    private UUID id;
    private UUID queueId;
    private UUID recipientId;
    private String recipientEmail;
    private EmailType emailType;
    private String subject;
    private EmailProvider provider;
    private String providerMessageId;
    private LogStatus status;
    private String errorMessage;
    private Instant openedAt;
    private Instant clickedAt;
    private Instant unsubscribedAt;
    private Instant bouncedAt;
    private Instant complainedAt;
    private Instant createdAt;
}
