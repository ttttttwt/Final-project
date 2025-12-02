package com.lexia.backend.email.dto;

import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for queueing an email.
 * Contains all information needed to send an email.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    /**
     * Optional reference to user ID (for tracking and preferences).
     */
    private UUID recipientId;

    /**
     * Email address of the recipient (required).
     */
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String recipientEmail;

    /**
     * Display name of the recipient (optional).
     */
    @Size(max = 255, message = "Recipient name must not exceed 255 characters")
    private String recipientName;

    /**
     * Type of email to send (required).
     */
    @NotNull(message = "Email type is required")
    private EmailType emailType;

    /**
     * Custom subject line. If not provided, default from i18n will be used.
     */
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;

    /**
     * Template name override. If not provided, default from EmailType will be used.
     */
    @Size(max = 100, message = "Template name must not exceed 100 characters")
    private String templateName;

    /**
     * Priority level. If not provided, default from EmailType will be used.
     */
    private EmailPriority priority;

    /**
     * Template variables to be rendered.
     */
    @Builder.Default
    private Map<String, Object> templateData = new HashMap<>();

    /**
     * Locale for template rendering (e.g., "en", "vi").
     */
    @Builder.Default
    private String locale = "en";

    /**
     * Gets the template name, falling back to default from email type.
     *
     * @return the template name
     */
    public String getEffectiveTemplateName() {
        return templateName != null ? templateName : emailType.getTemplateName();
    }

    /**
     * Gets the priority, falling back to default from email type.
     *
     * @return the priority
     */
    public EmailPriority getEffectivePriority() {
        return priority != null ? priority : EmailPriority.forEmailType(emailType);
    }

    /**
     * Adds a template variable.
     *
     * @param key   the variable name
     * @param value the value
     * @return this request for chaining
     */
    public EmailRequest withVariable(String key, Object value) {
        if (this.templateData == null) {
            this.templateData = new HashMap<>();
        }
        this.templateData.put(key, value);
        return this;
    }
}
