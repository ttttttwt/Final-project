package com.lexia.backend.email.enums;

/**
 * Enumeration of email priority levels.
 * Higher priority emails are processed first and have more retry attempts.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public enum EmailPriority {

    /**
     * Critical priority for authentication and security emails.
     * Processed immediately with 5 retry attempts.
     * Retry delays: 1m, 5m, 15m, 1h, 4h
     */
    CRITICAL(5, new int[] { 1, 5, 15, 60, 240 }),

    /**
     * High priority for achievements and completions.
     * Processed with high preference and 3 retry attempts.
     * Retry delays: 5m, 30m, 2h
     */
    HIGH(3, new int[] { 5, 30, 120 }),

    /**
     * Normal priority for enrollments and reminders.
     * Standard processing with 2 retry attempts.
     * Retry delays: 15m, 2h
     */
    NORMAL(2, new int[] { 15, 120 }),

    /**
     * Low priority for weekly reports and tips.
     * Processed last with 1 retry attempt.
     * Retry delays: 1h
     */
    LOW(1, new int[] { 60 });

    private final int maxAttempts;
    private final int[] retryDelaysMinutes;

    EmailPriority(int maxAttempts, int[] retryDelaysMinutes) {
        this.maxAttempts = maxAttempts;
        this.retryDelaysMinutes = retryDelaysMinutes;
    }

    /**
     * Gets the maximum number of retry attempts for this priority.
     *
     * @return the maximum attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Gets the retry delay in minutes for a specific attempt number.
     *
     * @param attempt the attempt number (0-based)
     * @return the delay in minutes before the next retry
     */
    public int getRetryDelayMinutes(int attempt) {
        if (attempt < 0) {
            return retryDelaysMinutes[0];
        }
        return retryDelaysMinutes[Math.min(attempt, retryDelaysMinutes.length - 1)];
    }

    /**
     * Gets all retry delays in minutes.
     *
     * @return array of retry delays
     */
    public int[] getRetryDelaysMinutes() {
        return retryDelaysMinutes.clone();
    }

    /**
     * Determines the appropriate priority for a given email type.
     *
     * @param emailType the email type
     * @return the recommended priority
     */
    public static EmailPriority forEmailType(EmailType emailType) {
        return switch (emailType) {
            case EMAIL_VERIFICATION, PASSWORD_RESET, SECURITY_ALERT, ACCOUNT_DEACTIVATION,
                    AI_BUDGET_WARNING, AI_BUDGET_EXCEEDED, AI_QUOTA_WARNING, AI_QUOTA_EXCEEDED,
                    PAYMENT_FAILED, PAYMENT_REFUND ->
                CRITICAL;
            case WELCOME, COURSE_COMPLETED, CERTIFICATE_DELIVERY, STREAK_MILESTONE, LEVEL_UP, PASSWORD_CHANGED,
                    PAYMENT_SUCCESS, PAYMENT_RENEWAL ->
                HIGH;
            case ENROLLMENT_CONFIRMATION, STREAK_REMINDER, STREAK_LOST, DEVICE_LOGIN, SUBSCRIPTION_EXPIRED -> NORMAL;
            case WEEKLY_PROGRESS, SYSTEM_ANNOUNCEMENT, MAINTENANCE_NOTICE -> LOW;
        };
    }
}
