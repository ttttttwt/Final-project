package com.lexia.backend.email.service;

import com.lexia.backend.email.enums.EmailType;

import java.util.Locale;
import java.util.Map;

/**
 * Service for rendering email templates.
 * Uses Thymeleaf for template processing.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface EmailTemplateService {

    /**
     * Renders an email template to HTML.
     *
     * @param templateName the template name (relative to templates/email/)
     * @param variables    the template variables
     * @param locale       the locale for i18n
     * @return the rendered HTML
     */
    String renderTemplate(String templateName, Map<String, Object> variables, Locale locale);

    /**
     * Renders an email template to plain text.
     * Used as fallback for email clients that don't support HTML.
     *
     * @param templateName the template name
     * @param variables    the template variables
     * @param locale       the locale for i18n
     * @return the rendered plain text
     */
    String renderPlainText(String templateName, Map<String, Object> variables, Locale locale);

    /**
     * Gets the subject line for an email type.
     *
     * @param emailType the email type
     * @param variables variables for subject interpolation
     * @param locale    the locale for i18n
     * @return the subject line
     */
    String getSubject(EmailType emailType, Map<String, Object> variables, Locale locale);

    /**
     * Adds common variables to the template context.
     * These include logo URL, base URL, unsubscribe URL, etc.
     *
     * @param variables   the existing variables (will be modified)
     * @param recipientId the recipient user ID (optional)
     */
    void addCommonVariables(Map<String, Object> variables, String recipientId);
}
