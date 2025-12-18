package com.lexia.backend.aspect;

import com.lexia.backend.annotation.QuotaCheck;
import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.exception.QuotaExceededException;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.SubscriptionQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Aspect for enforcing AI quota limits on annotated endpoints.
 * Intercepts methods annotated with @QuotaCheck and validates
 * that the user has sufficient quota before proceeding.
 * 
 * @see com.lexia.backend.annotation.QuotaCheck
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class QuotaCheckAspect {

    private final UserAiQuotaRepository quotaRepository;
    private final QuotaLimitsConfig quotaLimitsConfig;
    private final SubscriptionQuotaService subscriptionQuotaService;

    /**
     * Before advice that checks quota before allowing the method to proceed.
     * 
     * @param joinPoint  The join point
     * @param quotaCheck The annotation with quota check configuration
     * @throws QuotaExceededException if quota is exceeded
     */
    @Before("@annotation(quotaCheck)")
    @Transactional
    public void checkQuota(JoinPoint joinPoint, QuotaCheck quotaCheck) {
        User user = getCurrentUser();
        if (user == null) {
            log.warn("QuotaCheck: No authenticated user found");
            return; // Let security handle unauthenticated requests
        }

        UUID userId = user.getId();
        String contentType = quotaCheck.contentType();

        // Check and reset quota if needed
        subscriptionQuotaService.checkAndResetQuotaIfNeeded(userId);

        // Get user quota
        UserAiQuota quota = quotaRepository.findById(userId).orElse(null);
        if (quota == null) {
            quota = createDefaultQuota(userId);
        }

        // Check if suspended
        if (Boolean.TRUE.equals(quota.getSuspended())) {
            log.warn("QuotaCheck: User {} is suspended", userId);
            throw new QuotaExceededException("ai_access", 0, 0, quota.getPlanType());
        }

        // Get limits for user's plan
        QuotaLimitsConfig.QuotaLimits limits = quotaLimitsConfig.getForPlan(quota.getPlanType());

        // Check session-specific limits
        if (quotaCheck.incrementSession()) {
            checkSessionLimit(quota, limits, quotaCheck.sessionType());
        }

        // Check total monthly requests
        if (quota.getMonthlyUsed() >= limits.getTotalRequests()) {
            log.info("QuotaCheck: User {} exceeded total monthly requests ({}/{})",
                    userId, quota.getMonthlyUsed(), limits.getTotalRequests());
            throw new QuotaExceededException(
                    "total_requests",
                    limits.getTotalRequests(),
                    quota.getMonthlyUsed(),
                    quota.getPlanType());
        }

        log.debug("QuotaCheck PASSED: user={}, contentType={}, monthlyUsed={}/{}",
                userId, contentType, quota.getMonthlyUsed(), limits.getTotalRequests());
    }

    private void checkSessionLimit(UserAiQuota quota, QuotaLimitsConfig.QuotaLimits limits, String sessionType) {
        switch (sessionType) {
            case "roleplay":
                if (quota.getRoleplaySessionsUsed() >= limits.getRoleplaySessions()) {
                    throw new QuotaExceededException(
                            "roleplay_sessions",
                            limits.getRoleplaySessions(),
                            quota.getRoleplaySessionsUsed(),
                            quota.getPlanType());
                }
                break;
            case "flashcard":
                if (quota.getFlashcardDecksUsed() >= limits.getFlashcardDecks()) {
                    throw new QuotaExceededException(
                            "flashcard_decks",
                            limits.getFlashcardDecks(),
                            quota.getFlashcardDecksUsed(),
                            quota.getPlanType());
                }
                break;
            case "grammar":
                if (quota.getGrammarExercisesUsed() >= limits.getGrammarExercises()) {
                    throw new QuotaExceededException(
                            "grammar_exercises",
                            limits.getGrammarExercises(),
                            quota.getGrammarExercisesUsed(),
                            quota.getPlanType());
                }
                break;
            default:
                log.warn("Unknown session type: {}", sessionType);
        }
    }

    /**
     * Increments the appropriate session counter after a successful operation.
     * Should be called after the AI operation completes successfully.
     */
    @Transactional
    public void incrementSessionCounter(UUID userId, String sessionType) {
        UserAiQuota quota = quotaRepository.findById(userId).orElse(null);
        if (quota == null)
            return;

        switch (sessionType) {
            case "roleplay":
                quota.setRoleplaySessionsUsed(quota.getRoleplaySessionsUsed() + 1);
                break;
            case "flashcard":
                quota.setFlashcardDecksUsed(quota.getFlashcardDecksUsed() + 1);
                break;
            case "grammar":
                quota.setGrammarExercisesUsed(quota.getGrammarExercisesUsed() + 1);
                break;
        }

        quotaRepository.save(quota);
        log.debug("Incremented {} counter for user {}", sessionType, userId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    private UserAiQuota createDefaultQuota(UUID userId) {
        UserAiQuota quota = UserAiQuota.createForUser(userId);
        return quotaRepository.save(quota);
    }
}
