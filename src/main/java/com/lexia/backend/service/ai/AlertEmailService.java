package com.lexia.backend.service.ai;

import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending AI-related alert emails to administrators.
 * Handles budget and quota alert notifications.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEmailService {

    private final EmailService emailService;

    @Value("${app.admin.email:admin@lexia.com}")
    private String adminEmail;

    @Value("${app.admin.name:LEXIA Admin}")
    private String adminName;

    @Value("${app.name:LEXIA}")
    private String appName;

    /**
     * Sends a budget warning email to administrators.
     *
     * @param currentCost Current month's cost
     * @param budgetLimit Monthly budget limit
     * @param percentage  Percentage of budget used
     */
    public void sendBudgetWarningEmail(double currentCost, double budgetLimit, double percentage) {
        log.info("Sending budget warning email. Current: ${}, Limit: ${}, Usage: {}%",
                currentCost, budgetLimit, percentage);

        Map<String, Object> data = new HashMap<>();
        data.put("appName", appName);
        data.put("currentCost", String.format("$%.2f", currentCost));
        data.put("budgetLimit", String.format("$%.2f", budgetLimit));
        data.put("percentage", String.format("%.1f%%", percentage));
        data.put("alertType", "Warning");

        EmailRequest request = EmailRequest.builder()
                .recipientEmail(adminEmail)
                .recipientName(adminName)
                .emailType(EmailType.AI_BUDGET_WARNING)
                .subject(appName + " - AI Budget Warning: " + String.format("%.0f%%", percentage) + " Used")
                .templateData(data)
                .build();

        emailService.queueEmail(request);
    }

    /**
     * Sends a budget exceeded email to administrators.
     *
     * @param currentCost Current month's cost
     * @param budgetLimit Monthly budget limit
     * @param overage     Amount over budget
     */
    public void sendBudgetExceededEmail(double currentCost, double budgetLimit, double overage) {
        log.warn("Sending budget exceeded email. Current: ${}, Limit: ${}, Overage: ${}",
                currentCost, budgetLimit, overage);

        Map<String, Object> data = new HashMap<>();
        data.put("appName", appName);
        data.put("currentCost", String.format("$%.2f", currentCost));
        data.put("budgetLimit", String.format("$%.2f", budgetLimit));
        data.put("overage", String.format("$%.2f", overage));
        data.put("alertType", "Critical");

        EmailRequest request = EmailRequest.builder()
                .recipientEmail(adminEmail)
                .recipientName(adminName)
                .emailType(EmailType.AI_BUDGET_EXCEEDED)
                .subject("🚨 " + appName + " - AI Budget EXCEEDED by $" + String.format("%.2f", overage))
                .templateData(data)
                .build();

        // Send immediately for critical alerts
        emailService.sendEmailSync(request);
    }

    /**
     * Sends a quota warning email for a specific user.
     *
     * @param userEmail   User's email
     * @param userName    User's name
     * @param planType    User's plan type (FREE/PRO)
     * @param featureType Feature that hit the warning (roleplay, grammar,
     *                    flashcard)
     * @param used        Current usage
     * @param limit       Quota limit
     */
    public void sendQuotaWarningEmail(String userEmail, String userName, String planType,
            String featureType, int used, int limit) {
        log.info("Sending quota warning email for user {} ({}) - {} {}/{}",
                userName, planType, featureType, used, limit);

        Map<String, Object> data = new HashMap<>();
        data.put("appName", appName);
        data.put("userEmail", userEmail);
        data.put("userName", userName);
        data.put("planType", planType);
        data.put("featureType", formatFeatureName(featureType));
        data.put("used", used);
        data.put("limit", limit);
        data.put("percentage", String.format("%.0f%%", (used * 100.0) / limit));
        data.put("alertType", "Warning");

        EmailRequest request = EmailRequest.builder()
                .recipientEmail(adminEmail)
                .recipientName(adminName)
                .emailType(EmailType.AI_QUOTA_WARNING)
                .subject(appName + " - User Quota Warning: " + userName + " (" + planType + ")")
                .templateData(data)
                .build();

        emailService.queueEmail(request);
    }

    /**
     * Sends a quota exceeded email for a specific user.
     *
     * @param userEmail   User's email
     * @param userName    User's name
     * @param planType    User's plan type (FREE/PRO)
     * @param featureType Feature that exceeded quota
     * @param used        Current usage
     * @param limit       Quota limit
     */
    public void sendQuotaExceededEmail(String userEmail, String userName, String planType,
            String featureType, int used, int limit) {
        log.warn("Sending quota exceeded email for user {} ({}) - {} {}/{}",
                userName, planType, featureType, used, limit);

        Map<String, Object> data = new HashMap<>();
        data.put("appName", appName);
        data.put("userEmail", userEmail);
        data.put("userName", userName);
        data.put("planType", planType);
        data.put("featureType", formatFeatureName(featureType));
        data.put("used", used);
        data.put("limit", limit);
        data.put("alertType", "Critical");

        EmailRequest request = EmailRequest.builder()
                .recipientEmail(adminEmail)
                .recipientName(adminName)
                .emailType(EmailType.AI_QUOTA_EXCEEDED)
                .subject("⚠️ " + appName + " - User Quota EXCEEDED: " + userName)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
    }

    /**
     * Format feature name for display.
     */
    private String formatFeatureName(String featureType) {
        if (featureType == null)
            return "Unknown";
        return switch (featureType.toLowerCase()) {
            case "roleplay" -> "Role-Play Sessions";
            case "grammar" -> "Grammar Exercises";
            case "flashcard" -> "Flashcard Decks";
            default -> featureType;
        };
    }
}
