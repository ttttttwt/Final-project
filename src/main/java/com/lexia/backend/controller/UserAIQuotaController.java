package com.lexia.backend.controller;

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.dto.ai.UserAiQuotaDTO;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.SubscriptionQuotaService;
import com.lexia.backend.service.ai.AIQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/ai/quota")
@RequiredArgsConstructor
@Tag(name = "User AI Quota", description = "Get AI quota for current user")
public class UserAIQuotaController {

    private final AIQuotaService quotaService;
    private final QuotaLimitsConfig quotaLimitsConfig;
    private final SubscriptionQuotaService subscriptionQuotaService;

    private static final double WARNING_THRESHOLD = 0.80;
    private static final double CRITICAL_THRESHOLD = 0.95;

    @GetMapping("/me")
    @Operation(summary = "Get current user's AI quota", description = "Returns quota usage and limits based on subscription plan")
    public ResponseEntity<UserAiQuotaDTO> getMyQuota(@AuthenticationPrincipal User user) {
        // Check and reset quota if needed
        subscriptionQuotaService.checkAndResetQuotaIfNeeded(user.getId());

        UserAiQuota quota = quotaService.getQuotaByUserId(user.getId());
        QuotaLimitsConfig.QuotaLimits limits = quotaLimitsConfig.getForPlan(quota.getPlanType());

        return ResponseEntity.ok(mapToDTO(quota, limits));
    }

    private UserAiQuotaDTO mapToDTO(UserAiQuota entity, QuotaLimitsConfig.QuotaLimits limits) {
        UserAiQuotaDTO dto = new UserAiQuotaDTO();
        dto.setUserId(entity.getUserId());

        // Legacy daily/monthly fields
        dto.setDailyLimit(entity.getDailyLimit());
        dto.setDailyUsed(entity.getDailyUsed());
        dto.setLastResetDaily(entity.getLastResetDaily());
        dto.setMonthlyLimit(entity.getMonthlyLimit());
        dto.setMonthlyUsed(entity.getMonthlyUsed());
        dto.setLastResetMonthly(entity.getLastResetMonthly());
        dto.setIsPremium(entity.getIsPremium());
        dto.setSuspended(entity.getSuspended());

        // Set isUnlimited
        dto.setIsUnlimited(entity.getDailyLimit() == Integer.MAX_VALUE);

        // ========== Subscription-Based Quota ==========
        dto.setPlanType(entity.getPlanType() != null ? entity.getPlanType().name() : "FREE");
        dto.setQuotaResetDate(entity.getQuotaResetDate());

        // Calculate days until reset
        if (entity.getQuotaResetDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), entity.getQuotaResetDate());
            dto.setDaysUntilReset((int) Math.max(0, days));
        } else {
            dto.setDaysUntilReset(30);
        }

        // Session/Deck/Exercise counters with limits from config
        dto.setRoleplaySessionsUsed(entity.getRoleplaySessionsUsed());
        dto.setRoleplaySessionsLimit(limits.getRoleplaySessions());

        dto.setFlashcardDecksUsed(entity.getFlashcardDecksUsed());
        dto.setFlashcardDecksLimit(limits.getFlashcardDecks());

        dto.setGrammarExercisesUsed(entity.getGrammarExercisesUsed());
        dto.setGrammarExercisesLimit(limits.getGrammarExercises());

        dto.setTotalRequestsUsed(entity.getMonthlyUsed());
        dto.setTotalRequestsLimit(limits.getTotalRequests());

        // Warning flags
        boolean hasWarning = isAboveThreshold(entity.getRoleplaySessionsUsed(), limits.getRoleplaySessions(),
                WARNING_THRESHOLD)
                || isAboveThreshold(entity.getFlashcardDecksUsed(), limits.getFlashcardDecks(), WARNING_THRESHOLD)
                || isAboveThreshold(entity.getGrammarExercisesUsed(), limits.getGrammarExercises(), WARNING_THRESHOLD)
                || isAboveThreshold(entity.getMonthlyUsed(), limits.getTotalRequests(), WARNING_THRESHOLD);

        boolean hasCritical = isAboveThreshold(entity.getRoleplaySessionsUsed(), limits.getRoleplaySessions(),
                CRITICAL_THRESHOLD)
                || isAboveThreshold(entity.getFlashcardDecksUsed(), limits.getFlashcardDecks(), CRITICAL_THRESHOLD)
                || isAboveThreshold(entity.getGrammarExercisesUsed(), limits.getGrammarExercises(), CRITICAL_THRESHOLD)
                || isAboveThreshold(entity.getMonthlyUsed(), limits.getTotalRequests(), CRITICAL_THRESHOLD);

        dto.setQuotaWarning(hasWarning);
        dto.setQuotaCritical(hasCritical);

        // Legacy feature-specific fields
        dto.setRolePlayDailyLimit(entity.getFeatureDailyLimit("roleplay"));
        dto.setRolePlayUsedToday(entity.getFeatureDailyUsage("roleplay"));
        dto.setRolePlayMonthlyLimit(entity.getFeatureMonthlyLimit("roleplay"));
        dto.setRolePlayUsedMonth(entity.getFeatureMonthlyUsage("roleplay"));

        dto.setGrammarDailyLimit(entity.getFeatureDailyLimit("grammar"));
        dto.setGrammarUsedToday(entity.getFeatureDailyUsage("grammar"));
        dto.setGrammarMonthlyLimit(entity.getFeatureMonthlyLimit("grammar"));
        dto.setGrammarUsedMonth(entity.getFeatureMonthlyUsage("grammar"));

        dto.setFlashcardDailyLimit(entity.getFeatureDailyLimit("flashcard"));
        dto.setFlashcardUsedToday(entity.getFeatureDailyUsage("flashcard"));
        dto.setFlashcardMonthlyLimit(entity.getFeatureMonthlyLimit("flashcard"));
        dto.setFlashcardUsedMonth(entity.getFeatureMonthlyUsage("flashcard"));

        dto.setTotalUsedToday(entity.getDailyUsed());
        dto.setTotalDailyLimit(entity.getDailyLimit());

        return dto;
    }

    private boolean isAboveThreshold(Integer used, int limit, double threshold) {
        if (used == null || limit == 0)
            return false;
        return (double) used / limit >= threshold;
    }
}
