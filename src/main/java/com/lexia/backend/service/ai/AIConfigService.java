package com.lexia.backend.service.ai;

import com.lexia.backend.entity.AIConfig;
import java.util.List;
import java.util.Map;

public interface AIConfigService {
    List<AIConfig> getAllConfigs();
    AIConfig getConfig(String key);
    AIConfig updateConfig(String key, String value);
    Map<String, String> getPublicConfigs();
    com.lexia.backend.dto.ai.AIFeatureConfig getFeatureConfig(String featureName);
    com.lexia.backend.dto.ai.AIGlobalSettings getSettings();
    com.lexia.backend.dto.ai.AIGlobalSettings updateSettings(com.lexia.backend.dto.ai.AIGlobalSettings settings);
    com.lexia.backend.dto.ai.AIFeatureConfig updateFeatureConfig(String featureName, com.lexia.backend.dto.ai.AIFeatureConfig config);
    com.lexia.backend.dto.ai.AIFeatureConfig toggleFeature(String featureName, boolean isEnabled);
    com.lexia.backend.dto.ai.PlanLimitsDTO getPlanLimits();
    com.lexia.backend.dto.ai.PlanLimitsDTO updatePlanLimits(com.lexia.backend.dto.ai.PlanLimitsDTO planLimits);
}
