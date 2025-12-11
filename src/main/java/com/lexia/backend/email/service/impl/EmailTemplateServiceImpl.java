package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailTemplateService;
import com.lexia.backend.email.service.UnsubscribeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of EmailTemplateService.
 * Uses Thymeleaf for template rendering with email-specific MessageSource.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final EmailConfig emailConfig;
    private final UnsubscribeTokenService unsubscribeTokenService;

    public EmailTemplateServiceImpl(
            @Qualifier("emailTemplateEngine") SpringTemplateEngine templateEngine,
            @Qualifier("emailMessageSource") MessageSource messageSource,
            EmailConfig emailConfig,
            UnsubscribeTokenService unsubscribeTokenService) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.emailConfig = emailConfig;
        this.unsubscribeTokenService = unsubscribeTokenService;
    }

    @Override
    public String renderTemplate(String templateName, Map<String, Object> variables, Locale locale) {
        log.debug("Rendering template: {}, locale: {}", templateName, locale);

        Context context = new Context(locale);
        context.setVariables(variables);

        try {
            return templateEngine.process("email/" + templateName, context);
        } catch (Exception e) {
            log.error("Failed to render template {}: {}", templateName, e.getMessage());
            // Return a simple fallback template
            return renderFallbackTemplate(templateName, variables);
        }
    }

    @Override
    public String renderPlainText(String templateName, Map<String, Object> variables, Locale locale) {
        // First try to render a dedicated text template
        String textTemplateName = templateName + "-text";
        try {
            Context context = new Context(locale);
            context.setVariables(variables);
            // Check if template exists before processing to avoid exception noise
            // Note: Thymeleaf doesn't have a simple "exists" check without trying to resolve,
            // but we can catch the specific TemplateInputException which indicates missing template
            return templateEngine.process("email/" + textTemplateName, context);
        } catch (org.thymeleaf.exceptions.TemplateInputException e) {
            // Template not found, fall back to HTML-to-text conversion
            // This is expected behavior for templates that don't have a dedicated text version
            log.debug("No text template found for {}, falling back to HTML conversion", templateName);
            String html = renderTemplate(templateName, variables, locale);
            return convertHtmlToPlainText(html);
        } catch (Exception e) {
            // Other errors (e.g. parsing error in existing template)
            log.warn("Error rendering text template {}: {}", textTemplateName, e.getMessage());
            String html = renderTemplate(templateName, variables, locale);
            return convertHtmlToPlainText(html);
        }
    }

    @Override
    public String getSubject(EmailType emailType, Map<String, Object> variables, Locale locale) {
        String messageKey = "email." + emailType.name().toLowerCase().replace("_", ".") + ".subject";

        try {
            // Try to get localized subject
            Object[] args = extractSubjectArgs(emailType, variables);
            return messageSource.getMessage(messageKey, args, locale);
        } catch (Exception e) {
            // Fallback to email type display name
            return emailType.getDisplayName();
        }
    }

    @Override
    public void addCommonVariables(Map<String, Object> variables, String recipientId) {
        if (variables == null) {
            variables = new HashMap<>();
        }

        // Add standard URLs and branding
        variables.put("logoUrl", emailConfig.getLogoUrl());
        variables.put("baseUrl", emailConfig.getBaseUrl());
        variables.put("dashboardUrl", emailConfig.getDashboardUrl());
        variables.put("preferencesUrl", emailConfig.getPreferencesUrl());
        variables.put("companyName", "LEXIA");
        variables.put("supportEmail", "support@lexia.app");
        variables.put("currentYear", java.time.Year.now().getValue());

        // Add tracking flag
        variables.put("trackingEnabled", emailConfig.isTrackingEnabled());

        // Add unsubscribe URL with cryptographically signed token
        if (recipientId != null && !recipientId.isBlank()) {
            try {
                UUID userId = UUID.fromString(recipientId);
                String unsubscribeToken = unsubscribeTokenService.generateToken(userId);
                variables.put("unsubscribeUrl", emailConfig.getUnsubscribeUrl(unsubscribeToken));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid recipient ID format for unsubscribe token: {}", recipientId);
                variables.put("unsubscribeUrl", emailConfig.getUnsubscribeBaseUrl());
            }
        } else {
            variables.put("unsubscribeUrl", emailConfig.getUnsubscribeBaseUrl());
        }
    }

    /**
     * Converts HTML to plain text by stripping tags.
     *
     * @param html the HTML content
     * @return plain text content
     */
    private String convertHtmlToPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        // Use Jsoup to parse and extract text
        return Jsoup.parse(html).text();
    }

    /**
     * Renders a simple fallback template when the main template fails.
     *
     * @param templateName the failed template name
     * @param variables    the variables
     * @return simple HTML fallback
     */
    private String renderFallbackTemplate(String templateName, Map<String, Object> variables) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>");
        html.append("<h1>LEXIA</h1>");
        html.append("<p>").append(variables.getOrDefault("message", "")).append("</p>");

        if (variables.containsKey("actionUrl")) {
            html.append("<p><a href=\"").append(variables.get("actionUrl")).append("\">Click here</a></p>");
        }

        html.append("<hr><p>LEXIA Learning Platform</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Extracts arguments for subject message interpolation.
     *
     * @param emailType the email type
     * @param variables the variables
     * @return array of arguments
     */
    private Object[] extractSubjectArgs(EmailType emailType, Map<String, Object> variables) {
        return switch (emailType) {
            case COURSE_COMPLETED -> new Object[] { variables.getOrDefault("courseTitle", "") };
            case ENROLLMENT_CONFIRMATION -> new Object[] { variables.getOrDefault("courseTitle", "") };
            case STREAK_MILESTONE -> new Object[] { variables.getOrDefault("streakDays", "") };
            case LEVEL_UP -> new Object[] { variables.getOrDefault("newLevel", "") };
            default -> new Object[] {};
        };
    }
}
