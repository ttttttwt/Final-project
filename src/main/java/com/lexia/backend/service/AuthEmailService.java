package com.lexia.backend.service;

import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending authentication-related emails.
 * Handles password reset, password changed notifications, and email
 * verification.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEmailService {

    private final EmailService emailService;

    @Value("${lexia.app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${lexia.app.support-url:http://localhost:3000/support}")
    private String supportUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("MMM dd, yyyy 'at' hh:mm a");

    /**
     * Send password reset email with reset link.
     *
     * @param user  the user requesting password reset
     * @param token the plain reset token (will be included in URL)
     */
    public void sendPasswordResetEmail(User user, String token) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send password reset email: user or email is null");
            return;
        }

        String resetUrl = baseUrl + "/reset-password?token=" + token;

        Map<String, Object> data = new HashMap<>();
        data.put("userName", getUserDisplayName(user));
        data.put("resetUrl", resetUrl);

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getUserDisplayName(user))
                .emailType(EmailType.PASSWORD_RESET)
                .templateData(data)
                .build();

        // Send immediately for password reset (critical email)
        boolean sent = emailService.sendEmailSync(request);

        if (sent) {
            log.info("Password reset email sent to user: {}", user.getId());
        } else {
            log.error("Failed to send password reset email to user: {}", user.getId());
        }
    }

    /**
     * Send notification email after password has been changed.
     *
     * @param user       the user whose password was changed
     * @param deviceInfo optional device information (browser, OS)
     * @param ipAddress  optional IP address
     */
    public void sendPasswordChangedEmail(User user, String deviceInfo, String ipAddress) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send password changed email: user or email is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userName", getUserDisplayName(user));
        data.put("changedAt", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        data.put("supportUrl", supportUrl);

        if (deviceInfo != null && !deviceInfo.isBlank()) {
            data.put("deviceInfo", deviceInfo);
        }

        // Note: Location lookup from IP would require a GeoIP service
        // For now, we'll just include IP if available
        if (ipAddress != null && !ipAddress.isBlank()) {
            data.put("location", "IP: " + ipAddress);
        }

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getUserDisplayName(user))
                .emailType(EmailType.PASSWORD_CHANGED)
                .templateData(data)
                .build();

        // Queue for delivery (not critical - notification only)
        emailService.queueEmail(request);
        log.info("Queued password changed notification for user: {}", user.getId());
    }

    /**
     * Overloaded method for password change without device info.
     */
    public void sendPasswordChangedEmail(User user) {
        sendPasswordChangedEmail(user, null, null);
    }

    /**
     * Get user display name from profile or email.
     */
    private String getUserDisplayName(User user) {
        if (user.getProfile() != null && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank()) {
            return user.getProfile().getFullName();
        }
        // Fallback to email username
        return user.getEmail() != null ? user.getEmail().split("@")[0] : "there";
    }
}
