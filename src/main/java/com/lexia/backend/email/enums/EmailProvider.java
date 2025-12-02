package com.lexia.backend.email.enums;

/**
 * Enumeration of supported email providers.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public enum EmailProvider {

    /**
     * Standard SMTP server (default for development).
     */
    SMTP("SMTP", "Simple Mail Transfer Protocol"),

    /**
     * Amazon Simple Email Service.
     */
    SES("SES", "Amazon Simple Email Service"),

    /**
     * SendGrid email service.
     */
    SENDGRID("SendGrid", "SendGrid Email API"),

    /**
     * Mailgun email service.
     */
    MAILGUN("Mailgun", "Mailgun Email API");

    private final String code;
    private final String displayName;

    EmailProvider(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Gets the provider code used in database.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the human-readable display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
