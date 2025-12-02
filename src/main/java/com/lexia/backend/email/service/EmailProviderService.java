package com.lexia.backend.email.service;

import com.lexia.backend.email.dto.EmailMessage;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.exception.EmailSendException;

/**
 * Interface for email providers.
 * Implementations handle the actual sending of emails through different
 * services.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface EmailProviderService {

    /**
     * Sends an email message.
     *
     * @param message the email message to send
     * @return the result of the send operation
     * @throws EmailSendException if sending fails
     */
    EmailSendResult send(EmailMessage message) throws EmailSendException;

    /**
     * Checks if the email provider is healthy and available.
     *
     * @return true if the provider is ready to send emails
     */
    boolean isHealthy();

    /**
     * Gets the name of the email provider.
     *
     * @return the provider name (e.g., "SMTP", "SES", "SendGrid")
     */
    String getProviderName();
}
