package com.lexia.backend.aspect;

import com.lexia.backend.annotation.RequirePremium;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.SubscriptionRequiredException;
import com.lexia.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect to enforce Premium subscription requirement.
 * 
 * <p>
 * Intercepts methods annotated with @RequirePremium and verifies
 * that the authenticated user has an active Pro subscription.
 * </p>
 *
 * @since Sprint 5
 * @see RequirePremium
 */
@Slf4j
@Aspect
@Component
@Order(2) // After security
@RequiredArgsConstructor
public class PremiumCheckAspect {

    private final SubscriptionRepository subscriptionRepository;

    @Around("@annotation(requirePremium)")
    public Object checkPremiumSubscription(ProceedingJoinPoint joinPoint, RequirePremium requirePremium)
            throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            log.warn("Premium check failed: No authenticated user");
            throw new SubscriptionRequiredException("Authentication required", "custom_materials");
        }

        User user = null;
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else {
            log.warn("Premium check failed: Principal is not User type");
            throw new SubscriptionRequiredException("Invalid authentication", "custom_materials");
        }

        // Check subscription
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElse(null);

        boolean isPro = subscription != null
                && subscription.getPlanType() != null
                && subscription.getPlanType().isPro();

        if (!isPro) {
            String message = requirePremium.message();
            log.info("Premium feature access denied for user {}: {}", user.getId(), message);
            throw new SubscriptionRequiredException(message, "custom_materials");
        }

        log.debug("Premium check passed for user {}", user.getId());
        return joinPoint.proceed();
    }
}
