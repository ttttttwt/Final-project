package com.lexia.backend.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for the LEXIA email service.
 * Properties are prefixed with 'lexia.email'.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "lexia.email")
@Validated
@Data
public class EmailConfig {

    /**
     * Sender email address (From field).
     */
    @NotBlank
    private String fromAddress = "noreply@lexia.local";

    /**
     * Sender display name.
     */
    @NotBlank
    private String fromName = "LEXIA Learning";

    /**
     * Reply-to email address (optional).
     */
    private String replyToAddress;

    /**
     * Base URL for the web application (used in email links).
     */
    @NotBlank
    private String baseUrl = "http://localhost:3000";

    /**
     * URL to the logo image for email templates.
     */
    @NotBlank
    private String logoUrl = "http://localhost:3000/images/logo.png";

    /**
     * Whether email tracking is enabled.
     */
    private boolean trackingEnabled = false;

    /**
     * Base URL for unsubscribe links.
     */
    private String unsubscribeBaseUrl = "http://localhost:3000/unsubscribe";

    /**
     * Secret key for signing unsubscribe tokens.
     */
    private String unsubscribeSecret = "default-unsubscribe-secret-change-in-production";

    /**
     * Queue processing configuration.
     */
    private QueueConfig queue = new QueueConfig();

    /**
     * Rate limiting configuration.
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * Creates message source for email i18n.
     *
     * @return configured MessageSource
     */
    @Bean(name = "emailMessageSource")
    public MessageSource emailMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/email/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // 1 hour cache
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Queue processing settings.
     */
    @Data
    public static class QueueConfig {
        /**
         * Number of emails to process per batch.
         */
        @Positive
        private int batchSize = 10;

        /**
         * Interval between processing batches (milliseconds).
         */
        @Positive
        private long processInterval = 5000;

        /**
         * Number of days to retain processed emails.
         */
        @Positive
        private int retentionDays = 30;

        /**
         * Timeout for stuck processing emails (minutes).
         */
        @Positive
        private int processingTimeoutMinutes = 10;
    }

    /**
     * Rate limiting settings.
     */
    @Data
    public static class RateLimitConfig {
        /**
         * Maximum emails per minute.
         */
        @Positive
        private int perMinute = 100;

        /**
         * Maximum emails per hour.
         */
        @Positive
        private int perHour = 5000;

        /**
         * Maximum emails per day.
         */
        @Positive
        private int perDay = 50000;
    }

    /**
     * Gets the full URL for email verification.
     *
     * @param token the verification token
     * @return full verification URL
     */
    public String getVerificationUrl(String token) {
        return baseUrl + "/verify-email?token=" + token;
    }

    /**
     * Gets the full URL for password reset.
     *
     * @param token the reset token
     * @return full reset URL
     */
    public String getPasswordResetUrl(String token) {
        return baseUrl + "/reset-password?token=" + token;
    }

    /**
     * Gets the full URL for the user dashboard.
     *
     * @return dashboard URL
     */
    public String getDashboardUrl() {
        return baseUrl + "/dashboard";
    }

    /**
     * Gets the full unsubscribe URL for a given token.
     *
     * @param token the unsubscribe token
     * @return full unsubscribe URL
     */
    public String getUnsubscribeUrl(String token) {
        return unsubscribeBaseUrl + "?token=" + token;
    }

    /**
     * Gets the email preferences URL.
     *
     * @return preferences URL
     */
    public String getPreferencesUrl() {
        return baseUrl + "/settings/notifications";
    }
}
