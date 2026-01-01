package com.lexia.backend.scheduler;

import com.lexia.backend.entity.Subscription;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import com.lexia.backend.repository.SubscriptionRepository;
import com.lexia.backend.service.PaymentEmailService;
import com.lexia.backend.service.SubscriptionQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to check and expire subscriptions that have passed their period
 * end.
 * Runs daily to ensure subscriptions are properly expired and users are
 * downgraded.
 * 
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionQuotaService subscriptionQuotaService;
    private final PaymentEmailService paymentEmailService;

    /**
     * Check for expired subscriptions and downgrade them.
     * Runs daily at 1:00 AM to avoid peak hours.
     */
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1:00 AM
    @Transactional
    public void checkAndExpireSubscriptions() {
        log.info("Starting subscription expiration check...");

        LocalDateTime now = LocalDateTime.now();

        // Find ACTIVE or PAST_DUE subscriptions where period has ended
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(now);

        if (expiredSubscriptions.isEmpty()) {
            log.info("No expired subscriptions found.");
            return;
        }

        log.info("Found {} expired subscriptions to process", expiredSubscriptions.size());

        int processed = 0;
        int failed = 0;
        for (Subscription subscription : expiredSubscriptions) {
            try {
                expireSubscription(subscription);
                processed++;
            } catch (Exception e) {
                failed++;
                log.error("Error expiring subscription {}: {}",
                        subscription.getId(), e.getMessage(), e);
            }
        }

        log.info("Subscription expiration check completed. Processed: {}, Failed: {}, Total: {}",
                processed, failed, expiredSubscriptions.size());
    }

    /**
     * Expire a single subscription and downgrade user to FREE plan.
     * 
     * @param subscription The subscription to expire
     */
    private void expireSubscription(Subscription subscription) {
        log.info("Expiring subscription for user {}. PeriodEnd was: {}",
                subscription.getUser().getId(), subscription.getCurrentPeriodEnd());

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setPlanType(PlanType.FREE);
        subscriptionRepository.save(subscription);

        // Sync AI quota to FREE limits
        subscriptionQuotaService.syncQuotaWithSubscription(
                subscription.getUser(), subscription);

        // Send subscription expired email
        paymentEmailService.sendSubscriptionExpiredEmail(subscription.getUser(), subscription);

        log.info("Subscription expired and user downgraded to FREE. SubscriptionId: {}",
                subscription.getId());
    }
}
