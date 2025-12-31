package com.lexia.backend.service;

import com.lexia.backend.entity.Payment;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.PaymentRepository;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.repository.UserRepository;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionQuotaService subscriptionQuotaService;
    private final PaymentRepository paymentRepository;

    public Subscription getSubscription(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSubscription(userId));
    }

    @Transactional
    public Subscription createDefaultSubscription(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE) // Free plan is always active
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void handleCheckoutSessionCompleted(Session session, LocalDateTime periodEnd) {
        String userIdStr = session.getClientReferenceId();
        String stripeCustomerId = session.getCustomer();
        String stripeSubscriptionId = session.getSubscription();

        log.info("Handling checkout session completed. UserId: {}, CustomerId: {}, SubscriptionId: {}", userIdStr,
                stripeCustomerId, stripeSubscriptionId);

        if (userIdStr != null) {
            UUID userId = UUID.fromString(userIdStr);
            Subscription subscription = getSubscription(userId);
            subscription.setStripeCustomerId(stripeCustomerId);
            subscription.setStripeSubscriptionId(stripeSubscriptionId);
            subscription.setStatus(SubscriptionStatus.ACTIVE);

            if (session.getMetadata() != null && session.getMetadata().containsKey("plan_type")) {
                String planTypeStr = session.getMetadata().get("plan_type");
                log.info("Updating plan type to: {}", planTypeStr);
                try {
                    subscription.setPlanType(PlanType.valueOf(planTypeStr));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid plan type: {}. Defaulting to MONTHLY.", planTypeStr);
                    // Fallback to MONTHLY if invalid
                    subscription.setPlanType(PlanType.MONTHLY);
                }
            }

            if (periodEnd != null) {
                subscription.setCurrentPeriodEnd(periodEnd);
            }

            subscriptionRepository.save(subscription);

            // Save payment record
            savePaymentFromSession(session, subscription);

            // Sync AI quota with new subscription
            User user = subscription.getUser();
            subscriptionQuotaService.syncQuotaWithSubscription(user, subscription);

            log.info("Subscription updated successfully for user: {}", userId);
        } else {
            log.warn("User ID is null in checkout session");
        }
    }

    @Transactional
    public void handleInvoicePaymentSucceeded(Invoice invoice) {
        String stripeSubscriptionId = invoice.getSubscription();
        if (stripeSubscriptionId == null) {
            log.warn("Stripe subscription ID is null in invoice. Skipping processing.");
            return;
        }
        log.info("Handling invoice payment succeeded. SubscriptionId: {}", stripeSubscriptionId);
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresentOrElse(subscription -> {
            if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
                subscription.setCurrentPeriodEnd(LocalDateTime
                        .ofEpochSecond(invoice.getLines().getData().get(0).getPeriod().getEnd(), 0, ZoneOffset.UTC));
            } else {
                log.warn("No line items found in invoice: {}", invoice.getId());
            }
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);

            // Save payment record
            savePaymentFromInvoice(invoice, subscription);

            // Sync AI quota with renewed subscription
            User user = subscription.getUser();
            subscriptionQuotaService.syncQuotaWithSubscription(user, subscription);

            log.info("Subscription renewed successfully for subscriptionId: {}", stripeSubscriptionId);
        }, () -> log.warn("Subscription not found for subscriptionId: {}", stripeSubscriptionId));
    }

    @Transactional
    public void handleInvoicePaymentFailed(Invoice invoice) {
        String stripeSubscriptionId = invoice.getSubscription();
        if (stripeSubscriptionId == null) {
            log.warn("Stripe subscription ID is null in failed invoice. Skipping processing.");
            return;
        }
        log.info("Handling invoice payment failed. SubscriptionId: {}", stripeSubscriptionId);
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            // Record failed payment
            saveFailedPaymentFromInvoice(invoice, subscription);
        });
    }

    @Transactional
    public void handleChargeRefunded(com.stripe.model.Charge charge) {
        String paymentIntentId = charge.getPaymentIntent();
        if (paymentIntentId == null) {
            log.warn("No payment intent ID in charge refund, skipping");
            return;
        }

        log.info("Handling charge refunded. PaymentIntentId: {}", paymentIntentId);
        paymentRepository.findByStripePaymentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Payment record updated to REFUNDED for paymentIntentId: {}", paymentIntentId);
        });
    }

    @Transactional
    public void handleSubscriptionDeleted(com.stripe.model.Subscription stripeSubscription) {
        String stripeSubscriptionId = stripeSubscription.getId();
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setPlanType(PlanType.FREE);
            subscriptionRepository.save(subscription);

            // Sync AI quota with canceled subscription (downgrade to FREE)
            User user = subscription.getUser();
            subscriptionQuotaService.syncQuotaWithSubscription(user, subscription);

            log.info("Subscription canceled and quota synced for subscriptionId: {}", stripeSubscriptionId);
        });
    }

    private void savePaymentFromSession(Session session, Subscription subscription) {
        String invoiceId = session.getInvoice();
        if (invoiceId == null) {
            log.warn("No invoice ID in checkout session, skipping payment record");
            return;
        }

        Payment payment = paymentRepository.findByStripeInvoiceId(invoiceId)
                .orElse(new Payment());

        if (payment.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            log.info("Payment record already exists and succeeded for invoice: {}", invoiceId);
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = session.getCurrency() != null ? session.getCurrency().toUpperCase() : "USD";

        payment.setUser(subscription.getUser());
        payment.setSubscription(subscription);
        payment.setStripePaymentId(session.getPaymentIntent());
        payment.setStripeInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        payment.setPaymentType(Payment.PaymentType.SUBSCRIPTION_NEW);
        payment.setDescription("New subscription: " + subscription.getPlanType());
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Payment record saved/updated for new subscription. Invoice: {}", invoiceId);
    }

    private void savePaymentFromInvoice(Invoice invoice, Subscription subscription) {
        String invoiceId = invoice.getId();
        Payment payment = paymentRepository.findByStripeInvoiceId(invoiceId)
                .orElse(new Payment());

        if (payment.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            log.info("Payment record already exists and succeeded for invoice: {}", invoiceId);
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(invoice.getAmountPaid())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = invoice.getCurrency() != null ? invoice.getCurrency().toUpperCase() : "USD";

        // Determine payment type
        Payment.PaymentType type = Payment.PaymentType.SUBSCRIPTION_RENEWAL;
        if ("subscription_create".equals(invoice.getBillingReason())) {
            type = Payment.PaymentType.SUBSCRIPTION_NEW;
        }

        payment.setUser(subscription.getUser());
        payment.setSubscription(subscription);
        payment.setStripePaymentId(invoice.getPaymentIntent());
        payment.setStripeInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        payment.setPaymentType(type);
        payment.setDescription(type == Payment.PaymentType.SUBSCRIPTION_NEW ?
                "New subscription: " + subscription.getPlanType() :
                "Subscription renewal: " + subscription.getPlanType());
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Payment record saved/updated for invoice: {}. Type: {}", invoiceId, type);
    }

    private void saveFailedPaymentFromInvoice(Invoice invoice, Subscription subscription) {
        String invoiceId = invoice.getId();
        
        // Don't overwrite a successful payment with a failed one (unlikely but safe)
        if (paymentRepository.findByStripeInvoiceId(invoiceId).isPresent()) {
            log.info("Payment record already exists for invoice: {}. Skipping failed record.", invoiceId);
            return;
        }
        
        BigDecimal amount = BigDecimal.valueOf(invoice.getAmountDue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = invoice.getCurrency() != null ? invoice.getCurrency().toUpperCase() : "USD";

        Payment payment = Payment.builder()
                .user(subscription.getUser())
                .subscription(subscription)
                .stripePaymentId(invoice.getPaymentIntent())
                .stripeInvoiceId(invoiceId)
                .amount(amount)
                .currency(currency)
                .status(Payment.PaymentStatus.FAILED)
                .paymentType(Payment.PaymentType.SUBSCRIPTION_RENEWAL)
                .description("Payment failed for invoice: " + invoiceId)
                .build();

        paymentRepository.save(payment);
        log.info("Failed payment record saved for invoice: {}", invoiceId);
    }
}
