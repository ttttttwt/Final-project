package com.lexia.backend.email.enums;

/**
 * Enumeration of all email types supported by the LEXIA email system.
 * Each type maps to a specific template and trigger event.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public enum EmailType {

    // Authentication emails
    EMAIL_VERIFICATION("Email Verification", "auth/verification", true),
    PASSWORD_RESET("Password Reset", "auth/password-reset", true),
    WELCOME("Welcome Email", "auth/welcome", false),

    // Learning emails
    ENROLLMENT_CONFIRMATION("Enrollment Confirmation", "learning/enrollment", false),
    COURSE_COMPLETED("Course Completed", "learning/course-completed", false),
    CERTIFICATE_DELIVERY("Certificate Delivery", "learning/certificate", false),

    // Engagement emails
    STREAK_REMINDER("Streak Reminder", "engagement/streak-reminder", false),
    STREAK_LOST("Streak Lost", "engagement/streak-lost", false),
    STREAK_MILESTONE("Streak Milestone", "engagement/streak-milestone", false),
    LEVEL_UP("Level Up", "achievement/level-up", false),
    WEEKLY_PROGRESS("Weekly Progress", "engagement/weekly-progress", false),

    // Account emails
    ACCOUNT_DEACTIVATION("Account Deactivation", "account/deactivation", true),

    // System emails
    SYSTEM_ANNOUNCEMENT("System Announcement", "system/announcement", false),
    MAINTENANCE_NOTICE("Maintenance Notice", "system/maintenance", false),
    SECURITY_ALERT("Security Alert", "system/security-alert", true),
    PASSWORD_CHANGED("Password Changed", "auth/password-changed", true),
    DEVICE_LOGIN("New Device Login", "auth/device-login", false);

    private final String displayName;
    private final String templateName;
    private final boolean mandatory; // Cannot be disabled by user preferences

    EmailType(String displayName, String templateName, boolean mandatory) {
        this.displayName = displayName;
        this.templateName = templateName;
        this.mandatory = mandatory;
    }

    /**
     * Gets the human-readable display name of the email type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the Thymeleaf template path relative to templates/email/.
     *
     * @return the template name
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Checks if this email type is mandatory (cannot be disabled by user).
     * Critical emails like verification, password reset, and security alerts are
     * mandatory.
     *
     * @return true if mandatory, false if user can opt-out
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Gets the category of the email type.
     *
     * @return the category name
     */
    public String getCategory() {
        return switch (this) {
            case EMAIL_VERIFICATION, PASSWORD_RESET, WELCOME, PASSWORD_CHANGED, DEVICE_LOGIN -> "Authentication";
            case ENROLLMENT_CONFIRMATION, COURSE_COMPLETED, CERTIFICATE_DELIVERY -> "Learning";
            case STREAK_REMINDER, STREAK_LOST, STREAK_MILESTONE, WEEKLY_PROGRESS -> "Engagement";
            case LEVEL_UP -> "Achievement";
            case ACCOUNT_DEACTIVATION -> "Account";
            case SYSTEM_ANNOUNCEMENT, MAINTENANCE_NOTICE, SECURITY_ALERT -> "System";
        };
    }
}
