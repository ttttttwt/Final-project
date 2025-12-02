package com.lexia.backend.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the result of an email send operation.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResult {

    /**
     * Whether the send was successful.
     */
    private boolean success;

    /**
     * Message ID returned by the email provider.
     */
    private String messageId;

    /**
     * Error message if send failed.
     */
    private String errorMessage;

    /**
     * Error code if send failed.
     */
    private String errorCode;

    /**
     * Creates a successful result.
     *
     * @param messageId the provider's message ID (can be null)
     * @return success result
     */
    public static EmailSendResult success(String messageId) {
        return EmailSendResult.builder()
                .success(true)
                .messageId(messageId != null ? messageId : generateFallbackMessageId())
                .build();
    }

    /**
     * Generates a fallback message ID when the provider doesn't return one.
     *
     * @return generated message ID
     */
    private static String generateFallbackMessageId() {
        return "lexia-" + java.util.UUID.randomUUID().toString() + "@local";
    }

    /**
     * Creates a failure result.
     *
     * @param errorMessage the error message
     * @return failure result
     */
    public static EmailSendResult failure(String errorMessage) {
        return EmailSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Creates a failure result with error code.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @return failure result
     */
    public static EmailSendResult failure(String errorCode, String errorMessage) {
        return EmailSendResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
