package com.lexia.backend.controller.ai;

import com.lexia.backend.dto.ai.AiQuotaResponse;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.service.ai.AIQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI General API", description = "General AI operations including quota management")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private static final Logger LOG = LoggerFactory.getLogger(AiController.class);

    private final AIQuotaService aiQuotaService;

    public AiController(AIQuotaService aiQuotaService) {
        this.aiQuotaService = aiQuotaService;
    }

    @GetMapping("/quota")
    @Operation(summary = "Get AI usage quota", description = "Retrieves current AI usage and limits for the user. Can be filtered by feature.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quota retrieved successfully", 
            content = @Content(schema = @Schema(implementation = AiQuotaResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AiQuotaResponse> getQuota(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Optional feature name (roleplay, flashcards, grammar)") 
            @RequestParam(required = false) String feature) {
        
        LOG.debug("Fetching AI quota for user {} (feature={})", user.getEmail(), feature);
        
        UserAiQuota userQuota = aiQuotaService.getQuotaByUserId(user.getId());
        
        AiQuotaResponse response = new AiQuotaResponse();
        
        // Calculate reset times (UTC)
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime dailyReset = now.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        ZonedDateTime monthlyReset = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).plusMonths(1);
        
        response.setDailyResetAt(dailyReset.toInstant().toString());
        response.setMonthlyResetAt(monthlyReset.toInstant().toString());

        if (feature != null && !feature.isEmpty()) {
            // Feature-specific quota
            Map<String, Map<String, Integer>> featureLimits = userQuota.getFeatureLimits();
            Map<String, Map<String, Integer>> featureUsage = userQuota.getFeatureUsage();
            
            Map<String, Integer> limits = featureLimits != null ? featureLimits.getOrDefault(feature, Map.of()) : Map.of();
            Map<String, Integer> usage = featureUsage != null ? featureUsage.getOrDefault(feature, Map.of()) : Map.of();
            
            // If feature specific limits are not set, fallback to global limits or 0?
            // Usually if feature limit is not set, it might mean it uses global limit or has no specific limit.
            // For now, let's return what's in the map, defaulting to 0 if not found, 
            // but maybe we should fallback to global if 0? 
            // The frontend logic might handle 0 as "no limit" or "blocked".
            // Let's assume if feature limit is missing, we return global limit as a safe fallback for now, 
            // or just 0. Given the user report, returning 0 might block them if they rely on this.
            // However, the entity has "createDefaultFeatureLimits" so it should be populated.
            
            response.setDailyLimit(limits.getOrDefault("daily", userQuota.getDailyLimit()));
            response.setMonthlyLimit(limits.getOrDefault("monthly", userQuota.getMonthlyLimit()));
            response.setDailyUsed(usage.getOrDefault("daily", 0));
            response.setMonthlyUsed(usage.getOrDefault("monthly", 0));
        } else {
            // Global quota
            response.setDailyLimit(userQuota.getDailyLimit());
            response.setMonthlyLimit(userQuota.getMonthlyLimit());
            response.setDailyUsed(userQuota.getDailyUsed());
            response.setMonthlyUsed(userQuota.getMonthlyUsed());
        }
        
        return ResponseEntity.ok(response);
    }
}
