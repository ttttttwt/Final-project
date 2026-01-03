package com.lexia.backend.controller;

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.dto.ai.QuotaSummaryStatsDTO;
import com.lexia.backend.dto.ai.UserAiQuotaDTO;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.service.ai.AIQuotaService;
import com.lexia.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/ai-quotas")
@RequiredArgsConstructor
@Tag(name = "Admin AI Quotas", description = "Manage user AI quotas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIQuotaController {

    private final AIQuotaService quotaService;
    private final UserRepository userRepository;
    private final QuotaLimitsConfig quotaLimitsConfig;
    private final com.lexia.backend.repository.UserCustomMaterialRepository customMaterialRepository;

    @GetMapping
    @Operation(summary = "Get all quotas", description = "List all user quotas with optional search, plan filter, and sorting")
    public ResponseEntity<Page<UserAiQuotaDTO>> getAllQuotas(
            @Parameter(description = "Search by email or name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by plan type (FREE, PRO, ALL)") @RequestParam(required = false) String planType,
            @Parameter(description = "Field to sort by") @RequestParam(required = false, defaultValue = "userId") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "10") int size) {
        
        // Build sort
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, mapSortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(quotaService.getAllQuotas(search, planType, pageable).map(this::mapToDTO));
    }
    
    /**
     * Maps frontend sort field names to entity field names.
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null) return "userId";
        return switch (sortBy) {
            case "userEmail" -> "userId"; // Will sort by userId since email is in User table
            case "planType" -> "planType";
            case "roleplaySessionsUsed" -> "roleplaySessionsUsed";
            case "grammarExercisesUsed" -> "grammarExercisesUsed";
            case "flashcardDecksUsed" -> "flashcardDecksUsed";
            case "customMaterialsUsed" -> "customMaterialsUsed";
            case "daysUntilReset" -> "quotaResetDate";
            default -> "userId";
        };
    }

    @GetMapping("/summary-stats")
    @Operation(summary = "Get quota summary statistics", description = "Returns aggregate statistics for all user quotas including total, pro, free, exceeded counts")
    public ResponseEntity<QuotaSummaryStatsDTO> getQuotaSummaryStats() {
        return ResponseEntity.ok(quotaService.getQuotaSummaryStats());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserAiQuotaDTO> getQuota(@PathVariable UUID userId) {
        return ResponseEntity.ok(mapToDTO(quotaService.getQuotaByUserId(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserAiQuotaDTO> updateQuota(@PathVariable UUID userId,
            @RequestBody com.lexia.backend.dto.ai.UpdateQuotaRequest request) {
        return ResponseEntity.ok(mapToDTO(quotaService.updateQuota(userId, request)));
    }

    @PostMapping("/{userId}/reset")
    public ResponseEntity<Void> resetQuota(@PathVariable UUID userId) {
        quotaService.resetQuota(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<java.util.Map<String, Object>> bulkUpdateQuotas(
            @RequestBody com.lexia.backend.dto.ai.BulkUpdateQuotaRequest request) {
        quotaService.bulkUpdateQuotas(request.getUserIds(), request.getQuotaDetails());
        return ResponseEntity.ok(java.util.Map.of("updated", request.getUserIds().size(), "failed", 0));
    }

    @PatchMapping("/{userId}/unlimited")
    public ResponseEntity<UserAiQuotaDTO> setUnlimited(@PathVariable UUID userId,
            @RequestBody java.util.Map<String, Boolean> body) {
        return ResponseEntity.ok(mapToDTO(quotaService.setUnlimited(userId, body.get("isUnlimited"))));
    }

    private UserAiQuotaDTO mapToDTO(UserAiQuota entity) {
        UserAiQuotaDTO dto = new UserAiQuotaDTO();
        dto.setUserId(entity.getUserId());

        // Fetch user details
        userRepository.findById(entity.getUserId()).ifPresent(user -> {
            dto.setUserEmail(user.getEmail());
            if (user.getProfile() != null) {
                dto.setUserFullName(user.getProfile().getFullName());
            }
        });

        // ========== Subscription-Based Quota (NEW) ==========

        // Plan type
        dto.setPlanType(entity.getPlanType() != null ? entity.getPlanType().name() : "FREE");

        // Quota reset date and days until reset
        LocalDate resetDate = entity.getQuotaResetDate();
        dto.setQuotaResetDate(resetDate);
        if (resetDate != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), resetDate);
            dto.setDaysUntilReset((int) Math.max(0, daysUntil));
        }

        // Get plan-specific limits
        QuotaLimitsConfig.QuotaLimits limits = quotaLimitsConfig.getForPlan(entity.getPlanType());

        // Session/deck/exercise counters (monthly)
        dto.setRoleplaySessionsUsed(entity.getRoleplaySessionsUsed());
        dto.setRoleplaySessionsLimit(limits.getRoleplaySessions());

        dto.setFlashcardDecksUsed(entity.getFlashcardDecksUsed());
        dto.setFlashcardDecksLimit(limits.getFlashcardDecks());

        dto.setGrammarExercisesUsed(entity.getGrammarExercisesUsed());
        dto.setGrammarExercisesLimit(limits.getGrammarExercises());

        dto.setTotalRequestsUsed(entity.getMonthlyUsed());
        dto.setTotalRequestsLimit(limits.getTotalRequests());

        // Custom Materials quota (count from repository)
        Instant monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long customMaterialsUsed = customMaterialRepository.countByUserIdThisMonth(entity.getUserId(), monthStart);
        dto.setCustomMaterialsUsed((int) customMaterialsUsed);

        // Use individual limit if set, otherwise plan default
        Integer customLimit = entity.getCustomMaterialsMonthlyLimit() != null ? entity.getCustomMaterialsMonthlyLimit()
                : limits.getCustomMaterialsLimit();
        dto.setCustomMaterialsLimit(customLimit != null ? customLimit : 10);

        // Warning flags
        double warningThreshold = quotaLimitsConfig.getWarningThreshold();
        double criticalThreshold = quotaLimitsConfig.getCriticalThreshold();
        
        // Get custom materials effective limit for threshold calculation
        int effectiveCustomLimit = customLimit != null ? customLimit : 10;

        boolean hasWarning = (limits.getRoleplaySessions() > 0
                && (double) entity.getRoleplaySessionsUsed() / limits.getRoleplaySessions() >= warningThreshold) ||
                (limits.getFlashcardDecks() > 0
                        && (double) entity.getFlashcardDecksUsed() / limits.getFlashcardDecks() >= warningThreshold)
                ||
                (limits.getGrammarExercises() > 0 && (double) entity.getGrammarExercisesUsed()
                        / limits.getGrammarExercises() >= warningThreshold) ||
                (effectiveCustomLimit > 0 && (double) customMaterialsUsed / effectiveCustomLimit >= warningThreshold);

        boolean hasCritical = (limits.getRoleplaySessions() > 0
                && (double) entity.getRoleplaySessionsUsed() / limits.getRoleplaySessions() >= criticalThreshold) ||
                (limits.getFlashcardDecks() > 0
                        && (double) entity.getFlashcardDecksUsed() / limits.getFlashcardDecks() >= criticalThreshold)
                ||
                (limits.getGrammarExercises() > 0 && (double) entity.getGrammarExercisesUsed()
                        / limits.getGrammarExercises() >= criticalThreshold) ||
                (effectiveCustomLimit > 0 && (double) customMaterialsUsed / effectiveCustomLimit >= criticalThreshold);

        dto.setQuotaWarning(hasWarning);
        dto.setQuotaCritical(hasCritical);

        // ========== Legacy Fields (for backward compatibility) ==========

        dto.setDailyLimit(entity.getDailyLimit());
        dto.setDailyUsed(entity.getDailyUsed());
        dto.setLastResetDaily(entity.getLastResetDaily());
        dto.setMonthlyLimit(entity.getMonthlyLimit());
        dto.setMonthlyUsed(entity.getMonthlyUsed());
        dto.setLastResetMonthly(entity.getLastResetMonthly());
        dto.setIsPremium(entity.getIsPremium());
        dto.setSuspended(entity.getSuspended());

        // Set isUnlimited - only when explicitly set, NOT for Pro users automatically
        // Pro users have higher limits but are still subject to quota limits
        dto.setIsUnlimited(entity.getDailyLimit() == Integer.MAX_VALUE);

        // Legacy feature specific (daily)
        dto.setRolePlayDailyLimit(entity.getFeatureDailyLimit("roleplay"));
        dto.setRolePlayUsedToday(entity.getFeatureDailyUsage("roleplay"));

        dto.setGrammarDailyLimit(entity.getFeatureDailyLimit("grammar"));
        dto.setGrammarUsedToday(entity.getFeatureDailyUsage("grammar"));

        dto.setFlashcardDailyLimit(entity.getFeatureDailyLimit("flashcard"));
        dto.setFlashcardUsedToday(entity.getFeatureDailyUsage("flashcard"));

        dto.setTotalUsedToday(entity.getDailyUsed());
        dto.setTotalDailyLimit(entity.getDailyLimit());

        return dto;
    }
}
