package com.lexia.backend.email.dto;

import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for admin to send emails to specific users or broadcast.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin email send request")
public class AdminEmailRequest {

    /**
     * List of user IDs to send email to.
     * Required for targeted emails.
     */
    @Schema(description = "List of user IDs to send email to", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<UUID> userIds;

    /**
     * Whether to send to all active users.
     */
    @Schema(description = "Send to all active users (broadcast)", example = "false")
    @Builder.Default
    private boolean broadcast = false;

    /**
     * Type of email to send.
     */
    @NotNull(message = "Email type is required")
    @Schema(description = "Type of email", example = "SYSTEM_ANNOUNCEMENT")
    private EmailType emailType;

    /**
     * Custom email subject (optional).
     */
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    @Schema(description = "Custom email subject", example = "Important System Announcement")
    private String subject;

    /**
     * Email priority.
     */
    @Schema(description = "Email priority", example = "NORMAL")
    @Builder.Default
    private EmailPriority priority = EmailPriority.NORMAL;

    /**
     * Template data/variables for email rendering.
     */
    @Schema(description = "Template variables for email content")
    @Builder.Default
    private Map<String, Object> templateData = new HashMap<>();

    /**
     * Locale for email content.
     */
    @Schema(description = "Locale for email content", example = "en")
    @Builder.Default
    private String locale = "en";
}
