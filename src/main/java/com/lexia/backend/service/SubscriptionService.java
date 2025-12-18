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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

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

        if (userIdStr != null) {
            UUID userId = UUID.fromString(userIdStr);
            Subscription subscription = getSubscription(userId);
            subscription.setStripeCustomerId(stripeCustomerId);
            subscription.setStripeSubscriptionId(stripeSubscriptionId);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            // Assuming default to MONTHLY if not specified, or logic to determine plan
            // For now, we can leave planType as is or update it if we pass metadata
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void handleInvoicePaymentSucceeded(Invoice invoice) {
        String stripeSubscriptionId = invoice.getSubscription();
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            subscription.setCurrentPeriodEnd(LocalDateTime.ofEpochSecond(invoice.getLines().getData().get(0).getPeriod().getEnd(), 0, ZoneOffset.UTC));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);
        });
    }

    @Transactional
    public void handleSubscriptionDeleted(com.stripe.model.Subscription stripeSubscription) {
        String stripeSubscriptionId = stripeSubscription.getId();
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setPlanType(PlanType.FREE);
            subscriptionRepository.save(subscription);
        });
    }
}
