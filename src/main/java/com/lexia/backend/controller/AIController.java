package com.lexia.backend.controller;

import com.lexia.backend.dto.ai.AiQuotaResponse;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.exception.UserNotFoundException;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.ai.AIQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Operations", description = "General AI operations including quota management")
public class AIController {

    private final AIQuotaService quotaService;
    private final UserRepository userRepository;

    @GetMapping("/quota")
    @Operation(summary = "Get current user's AI quota")
    public ResponseEntity<AiQuotaResponse> getQuota(@RequestParam(required = false) String feature) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserAiQuota quota = quotaService.getQuotaByUserId(user.getId());

        AiQuotaResponse response = AiQuotaResponse.builder()
                .dailyUsed(quota.getDailyUsed())
                .dailyLimit(quota.getDailyLimit())
                .monthlyUsed(quota.getMonthlyUsed())
                .monthlyLimit(quota.getMonthlyLimit())
                .dailyResetAt(quota.getLastResetDaily() != null ? quota.getLastResetDaily().toString() : null)
                .monthlyResetAt(quota.getLastResetMonthly() != null ? quota.getLastResetMonthly().toString() : null)
                .build();

        if (feature != null) {
            String featureKey = feature.toLowerCase();
            if (featureKey.equals("role_play")) featureKey = "roleplay";
            if (featureKey.equals("magic_flashcard")) featureKey = "flashcard";
            if (featureKey.equals("grammar_sandbox")) featureKey = "grammar";
            
            // Use helper methods from UserAiQuota entity
            response.setDailyUsed(quota.getFeatureDailyUsage(featureKey));
            response.setDailyLimit(quota.getFeatureDailyLimit(featureKey));
            response.setMonthlyUsed(quota.getFeatureMonthlyUsage(featureKey));
            response.setMonthlyLimit(quota.getFeatureMonthlyLimit(featureKey));
        }

        return ResponseEntity.ok(response);
    }
}
