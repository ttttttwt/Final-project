package com.lexia.backend.email.enums;

/**
 * Enumeration of email processing statuses.
 * Tracks the lifecycle of an email from queue to delivery.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public enum EmailStatus {

    /**
     * Email is queued and waiting to be processed.
     */
    PENDING("Pending", "Email is queued for delivery"),

    /**
     * Email is currently being processed/sent.
     */
    PROCESSING("Processing", "Email is being sent"),

    /**
     * Email was successfully sent to the mail server.
     */
    SENT("Sent", "Email was sent to mail server"),

    /**
     * Email was confirmed delivered to recipient's inbox.
     */
    DELIVERED("Delivered", "Email was delivered to recipient"),

    /**
     * Email sending failed after all retry attempts.
     */
    FAILED("Failed", "Email delivery failed"),

    /**
     * Email bounced (invalid address or mailbox full).
     */
    BOUNCED("Bounced", "Email bounced back"),

    /**
     * Email was cancelled before sending.
     */
    CANCELLED("Cancelled", "Email was cancelled");

    private final String displayName;
    private final String description;

    EmailStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of this status.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the email is in a terminal state (no more processing).
     *
     * @return true if terminal state
     */
    public boolean isTerminal() {
        return this == SENT || this == DELIVERED || this == FAILED ||
                this == BOUNCED || this == CANCELLED;
    }

    /**
     * Checks if the email can be retried.
     *
     * @return true if can be retried
     */
    public boolean canRetry() {
        return this == FAILED;
    }

    /**
     * Checks if the email can be cancelled.
     *
     * @return true if can be cancelled
     */
    public boolean canCancel() {
        return this == PENDING;
    }

    /**
     * Checks if this represents a successful delivery.
     *
     * @return true if successfully delivered
     */
    public boolean isSuccess() {
        return this == SENT || this == DELIVERED;
    }
}
