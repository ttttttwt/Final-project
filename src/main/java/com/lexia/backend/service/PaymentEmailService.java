package com.lexia.backend.service;

import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.Payment;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending payment-related email notifications.
 * Handles emails for payment success, renewal, failure, refund, and
 * subscription expiration.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEmailService {

    private final EmailService emailService;

    @Value("${lexia.app.base-url:https://lexia.app}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Send email when new subscription payment succeeds.
     *
     * @param user         the user who made the payment
     * @param payment      the payment record
     * @param subscription the subscription
     */
    public void sendPaymentSuccessEmail(User user, Payment payment, Subscription subscription) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send payment success email: user or email is null");
            return;
        }

        Map<String, Object> data = buildPaymentEmailData(user, payment, subscription);
        data.put("isNewSubscription", true);
        data.put("dashboardUrl", baseUrl + "/dashboard");

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getFullName(user))
                .emailType(EmailType.PAYMENT_SUCCESS)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
        log.info("Queued payment success email for user: {}", user.getId());
    }

    /**
     * Send email when subscription renewal succeeds.
     *
     * @param user         the user
     * @param payment      the payment record
     * @param subscription the subscription
     */
    public void sendRenewalSuccessEmail(User user, Payment payment, Subscription subscription) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send renewal success email: user or email is null");
            return;
        }

        Map<String, Object> data = buildPaymentEmailData(user, payment, subscription);
        data.put("isRenewal", true);
        data.put("dashboardUrl", baseUrl + "/dashboard");

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getFullName(user))
                .emailType(EmailType.PAYMENT_RENEWAL)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
        log.info("Queued renewal success email for user: {}", user.getId());
    }

    /**
     * Send email when payment fails.
     *
     * @param user         the user
     * @param subscription the subscription
     * @param invoiceId    the Stripe invoice ID
     */
    public void sendPaymentFailedEmail(User user, Subscription subscription, String invoiceId) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send payment failed email: user or email is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userName", getFullName(user));
        data.put("planType",
                subscription.getPlanType() != null ? formatPlanType(subscription.getPlanType().name()) : "Pro");
        data.put("invoiceId", invoiceId);
        data.put("updatePaymentUrl", baseUrl + "/settings/billing");

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getFullName(user))
                .emailType(EmailType.PAYMENT_FAILED)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
        log.info("Queued payment failed email for user: {}", user.getId());
    }

    /**
     * Send email when refund is processed.
     *
     * @param user    the user
     * @param payment the refunded payment
     */
    public void sendRefundEmail(User user, Payment payment) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send refund email: user or email is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userName", getFullName(user));
        data.put("amount", formatAmount(payment.getAmount(), payment.getCurrency()));
        data.put("refundDate", payment.getCreatedAt() != null ? payment.getCreatedAt().format(DATE_FORMATTER) : "N/A");
        data.put("paymentId", payment.getStripePaymentId());
        data.put("supportUrl", baseUrl + "/support");

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getFullName(user))
                .emailType(EmailType.PAYMENT_REFUND)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
        log.info("Queued refund email for user: {}", user.getId());
    }

    /**
     * Send email when subscription expires.
     *
     * @param user         the user
     * @param subscription the expired subscription
     */
    public void sendSubscriptionExpiredEmail(User user, Subscription subscription) {
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send subscription expired email: user or email is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userName", getFullName(user));
        data.put("planType",
                subscription.getPlanType() != null ? formatPlanType(subscription.getPlanType().name()) : "Pro");
        data.put("expiredDate", subscription.getCurrentPeriodEnd() != null
                ? subscription.getCurrentPeriodEnd().format(DATE_FORMATTER)
                : "N/A");
        data.put("upgradeUrl", baseUrl + "/pricing");

        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getFullName(user))
                .emailType(EmailType.SUBSCRIPTION_EXPIRED)
                .templateData(data)
                .build();

        emailService.queueEmail(request);
        log.info("Queued subscription expired email for user: {}", user.getId());
    }

    // ========== Private Helper Methods ==========

    private Map<String, Object> buildPaymentEmailData(User user, Payment payment, Subscription subscription) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", getFullName(user));
        data.put("amount", formatAmount(payment.getAmount(), payment.getCurrency()));
        data.put("planType", formatPlanType(subscription.getPlanType().name()));
        data.put("paymentDate", payment.getPaidAt() != null
                ? payment.getPaidAt().format(DATE_FORMATTER)
                : "N/A");
        data.put("nextBillingDate", subscription.getCurrentPeriodEnd() != null
                ? subscription.getCurrentPeriodEnd().format(DATE_FORMATTER)
                : "N/A");
        data.put("invoiceId", payment.getStripeInvoiceId());
        return data;
    }

    private String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) {
            return "N/A";
        }
        String currencySymbol = "USD".equalsIgnoreCase(currency) ? "$" : currency.toUpperCase() + " ";
        return String.format("%s%.2f", currencySymbol, amount);
    }

    private String formatPlanType(String planType) {
        if (planType == null) {
            return "Pro";
        }
        return switch (planType.toUpperCase()) {
            case "MONTHLY" -> "Pro Monthly";
            case "YEARLY" -> "Pro Yearly";
            default -> planType;
        };
    }

    private String getFullName(User user) {
        // Get name from user profile if available
        if (user.getProfile() != null && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank()) {
            return user.getProfile().getFullName();
        }
        // Fallback to email username
        return user.getEmail() != null ? user.getEmail().split("@")[0] : "there";
    }
}
