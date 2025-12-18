package com.lexia.backend.controller;

import com.lexia.backend.dto.ai.UserAiQuotaDTO;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ai.AIQuotaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/quota")
@RequiredArgsConstructor
@Tag(name = "User AI Quota", description = "Get AI quota for current user")
public class UserAIQuotaController {

    private final AIQuotaService quotaService;

    @GetMapping("/me")
    public ResponseEntity<UserAiQuotaDTO> getMyQuota(@AuthenticationPrincipal User user) {
        UserAiQuota quota = quotaService.getQuotaByUserId(user.getId());
        return ResponseEntity.ok(mapToDTO(quota));
    }

    private UserAiQuotaDTO mapToDTO(UserAiQuota entity) {
        UserAiQuotaDTO dto = new UserAiQuotaDTO();
        dto.setUserId(entity.getUserId());
        
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

        // Feature specific
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
}
