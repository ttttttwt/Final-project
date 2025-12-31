package com.lexia.backend.service;

import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.repository.UserRepository;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void handleCheckoutSessionCompleted(Session session) {
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

            try {
                com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
                subscription.setCurrentPeriodEnd(
                        LocalDateTime.ofEpochSecond(stripeSub.getCurrentPeriodEnd(), 0, ZoneOffset.UTC));
            } catch (Exception e) {
                log.error("Error retrieving subscription details from Stripe", e);
                // Log error or ignore
            }

            subscriptionRepository.save(subscription);

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
            subscription.setCurrentPeriodEnd(LocalDateTime
                    .ofEpochSecond(invoice.getLines().getData().get(0).getPeriod().getEnd(), 0, ZoneOffset.UTC));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);

            // Sync AI quota with renewed subscription
            User user = subscription.getUser();
            subscriptionQuotaService.syncQuotaWithSubscription(user, subscription);

            log.info("Subscription renewed successfully for subscriptionId: {}", stripeSubscriptionId);
        }, () -> log.warn("Subscription not found for subscriptionId: {}", stripeSubscriptionId));
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
}
