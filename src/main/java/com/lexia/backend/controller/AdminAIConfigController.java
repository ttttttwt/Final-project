package com.lexia.backend.controller;

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.entity.AIConfig;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.dto.ai.PlanLimitsDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.lexia.backend.dto.ai.AIFeatureConfig;
import com.lexia.backend.dto.ai.AIGlobalSettings;

@RestController
@RequestMapping("/api/v1/admin/ai-config")
@RequiredArgsConstructor
@Tag(name = "Admin AI Config", description = "Global AI Configuration")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIConfigController {

    private final AIConfigService configService;
    private final QuotaLimitsConfig quotaLimitsConfig;

    @GetMapping
    public ResponseEntity<AIGlobalSettings> getSettings() {
        return ResponseEntity.ok(configService.getSettings());
    }

    @PutMapping
    public ResponseEntity<AIGlobalSettings> updateSettings(@RequestBody AIGlobalSettings settings) {
        return ResponseEntity.ok(configService.updateSettings(settings));
    }

    @GetMapping("/features/{featureName}")
    public ResponseEntity<AIFeatureConfig> getFeatureConfig(@PathVariable String featureName) {
        return ResponseEntity.ok(configService.getFeatureConfig(featureName));
    }

    @PutMapping("/features/{featureName}")
    public ResponseEntity<AIFeatureConfig> updateFeatureConfig(@PathVariable String featureName,
            @RequestBody AIFeatureConfig config) {
        return ResponseEntity.ok(configService.updateFeatureConfig(featureName, config));
    }

    @PatchMapping("/features/{featureName}/toggle")
    public ResponseEntity<AIFeatureConfig> toggleFeature(@PathVariable String featureName,
            @RequestBody java.util.Map<String, Boolean> body) {
        return ResponseEntity.ok(configService.toggleFeature(featureName, body.get("isEnabled")));
    }

    // ========== Plan Limits Endpoints ==========

    @GetMapping("/plan-limits")
    public ResponseEntity<PlanLimitsDTO> getPlanLimits() {
        PlanLimitsDTO dto = PlanLimitsDTO.builder()
                .freeRoleplaySessions(quotaLimitsConfig.getFreeRoleplaySessions())
                .freeFlashcardDecks(quotaLimitsConfig.getFreeFlashcardDecks())
                .freeGrammarExercises(quotaLimitsConfig.getFreeGrammarExercises())
                .freeTotalRequests(quotaLimitsConfig.getFreeTotalRequests())
                .proRoleplaySessions(quotaLimitsConfig.getProRoleplaySessions())
                .proFlashcardDecks(quotaLimitsConfig.getProFlashcardDecks())
                .proGrammarExercises(quotaLimitsConfig.getProGrammarExercises())
                .proTotalRequests(quotaLimitsConfig.getProTotalRequests())
                .warningThresholdPercent((int) (quotaLimitsConfig.getWarningThreshold() * 100))
                .criticalThresholdPercent((int) (quotaLimitsConfig.getCriticalThreshold() * 100))
                .build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/plan-limits")
    public ResponseEntity<PlanLimitsDTO> updatePlanLimits(@RequestBody PlanLimitsDTO dto) {
        // Update config values (these are runtime changes, not persisted to yaml)
        quotaLimitsConfig.setFreeRoleplaySessions(dto.getFreeRoleplaySessions());
        quotaLimitsConfig.setFreeFlashcardDecks(dto.getFreeFlashcardDecks());
        quotaLimitsConfig.setFreeGrammarExercises(dto.getFreeGrammarExercises());
        quotaLimitsConfig.setFreeTotalRequests(dto.getFreeTotalRequests());
        quotaLimitsConfig.setProRoleplaySessions(dto.getProRoleplaySessions());
        quotaLimitsConfig.setProFlashcardDecks(dto.getProFlashcardDecks());
        quotaLimitsConfig.setProGrammarExercises(dto.getProGrammarExercises());
        quotaLimitsConfig.setProTotalRequests(dto.getProTotalRequests());
        quotaLimitsConfig.setWarningThreshold(dto.getWarningThresholdPercent() / 100.0);
        quotaLimitsConfig.setCriticalThreshold(dto.getCriticalThresholdPercent() / 100.0);

        return ResponseEntity.ok(dto);
    }
}
