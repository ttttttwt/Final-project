package com.lexia.backend.controller;

import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.dto.ai.PlanLimitsDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lexia.backend.dto.ai.AIFeatureConfig;
import com.lexia.backend.dto.ai.AIGlobalSettings;

@RestController
@RequestMapping("/api/v1/admin/ai-config")
@RequiredArgsConstructor
@Tag(name = "Admin AI Config", description = "Global AI Configuration")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIConfigController {

    private final AIConfigService configService;

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
        return ResponseEntity.ok(configService.getPlanLimits());
    }

    @PutMapping("/plan-limits")
    public ResponseEntity<PlanLimitsDTO> updatePlanLimits(@RequestBody PlanLimitsDTO dto) {
        return ResponseEntity.ok(configService.updatePlanLimits(dto));
    }
}
