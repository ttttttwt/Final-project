package com.lexia.backend.service.ai.impl;

import com.lexia.backend.entity.AIConfig;
import com.lexia.backend.repository.AIConfigRepository;
import com.lexia.backend.service.ai.AIConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lexia.backend.dto.ai.AIFeatureConfig;
import com.lexia.backend.dto.ai.AIGlobalSettings;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AIConfigServiceImpl implements AIConfigService {

    private final AIConfigRepository configRepository;

    @Override
    public List<AIConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    @Override
    public AIConfig getConfig(String key) {
        return configRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));
    }

    @Override
    @Transactional
    public AIConfig updateConfig(String key, String value) {
        AIConfig config = configRepository.findById(key)
                .orElse(AIConfig.builder().configKey(key).build());
        config.setConfigValue(value);
        return configRepository.save(config);
    }

    @Override
    public Map<String, String> getPublicConfigs() {
        return configRepository.findAll().stream()
                .filter(c -> !c.getIsEncrypted())
                .collect(Collectors.toMap(AIConfig::getConfigKey, AIConfig::getConfigValue));
    }

    @Override
    public AIFeatureConfig getFeatureConfig(String featureName) {
        // Fallback to single fetch if needed, but getSettings uses bulk fetch
        return getFeatureConfigFromMap(getPublicConfigs(), featureName);
    }

    @Override
    public AIGlobalSettings getSettings() {
        Map<String, String> configMap = configRepository.findAll().stream()
                .collect(Collectors.toMap(AIConfig::getConfigKey, AIConfig::getConfigValue));
        
        AIGlobalSettings settings = new AIGlobalSettings();
        
        // New fields
        settings.setGlobalEnabled(Boolean.parseBoolean(configMap.getOrDefault("global.enabled", "true")));
        settings.setMonthlyBudgetLimit(Double.parseDouble(configMap.getOrDefault("global.monthlyBudgetLimit", "100.0")));
        settings.setAlertThresholdPercentage(Integer.parseInt(configMap.getOrDefault("global.alertThresholdPercentage", "80")));
        settings.setFallbackEnabled(Boolean.parseBoolean(configMap.getOrDefault("global.fallbackEnabled", "false")));
        settings.setRateLimitEnabled(Boolean.parseBoolean(configMap.getOrDefault("global.rateLimitEnabled", "true")));
        settings.setCostPerInputToken(Double.parseDouble(configMap.getOrDefault("global.costPerInputToken", "0.000001")));
        settings.setCostPerOutputToken(Double.parseDouble(configMap.getOrDefault("global.costPerOutputToken", "0.000002")));

        // Legacy fields
        settings.setDefaultDailyLimit(Integer.parseInt(configMap.getOrDefault("global.defaultDailyLimit", "100")));
        settings.setDefaultMonthlyLimit(Integer.parseInt(configMap.getOrDefault("global.defaultMonthlyLimit", "3000")));
        settings.setCostPerToken(Double.parseDouble(configMap.getOrDefault("global.costPerToken", "0.00001")));
        
        settings.setFeatures(List.of(
            getFeatureConfigFromMap(configMap, "roleplay"),
            getFeatureConfigFromMap(configMap, "grammar"),
            getFeatureConfigFromMap(configMap, "flashcards")
        ));
        return settings;
    }

    @Override
    @Transactional
    public AIGlobalSettings updateSettings(AIGlobalSettings settings) {
        Map<String, AIConfig> existingConfigs = configRepository.findAll().stream()
                .collect(Collectors.toMap(AIConfig::getConfigKey, c -> c));
        
        List<AIConfig> toSave = new ArrayList<>();
        
        // New fields
        updateList(toSave, existingConfigs, "global.enabled", String.valueOf(settings.isGlobalEnabled()));
        updateList(toSave, existingConfigs, "global.monthlyBudgetLimit", String.valueOf(settings.getMonthlyBudgetLimit()));
        updateList(toSave, existingConfigs, "global.alertThresholdPercentage", String.valueOf(settings.getAlertThresholdPercentage()));
        updateList(toSave, existingConfigs, "global.fallbackEnabled", String.valueOf(settings.isFallbackEnabled()));
        updateList(toSave, existingConfigs, "global.rateLimitEnabled", String.valueOf(settings.isRateLimitEnabled()));
        updateList(toSave, existingConfigs, "global.costPerInputToken", String.valueOf(settings.getCostPerInputToken()));
        updateList(toSave, existingConfigs, "global.costPerOutputToken", String.valueOf(settings.getCostPerOutputToken()));

        // Legacy fields
        updateList(toSave, existingConfigs, "global.defaultDailyLimit", String.valueOf(settings.getDefaultDailyLimit()));
        updateList(toSave, existingConfigs, "global.defaultMonthlyLimit", String.valueOf(settings.getDefaultMonthlyLimit()));
        updateList(toSave, existingConfigs, "global.costPerToken", String.valueOf(settings.getCostPerToken()));
        
        if (settings.getFeatures() != null) {
            for (AIFeatureConfig fc : settings.getFeatures()) {
                updateFeatureConfigInternal(toSave, existingConfigs, fc);
            }
        }
        configRepository.saveAll(toSave);
        return getSettings();
    }

    @Override
    @Transactional
    public AIFeatureConfig updateFeatureConfig(String featureName, AIFeatureConfig config) {
        config.setFeatureId(featureName); // Ensure ID matches
        Map<String, AIConfig> existingConfigs = configRepository.findAll().stream()
                .collect(Collectors.toMap(AIConfig::getConfigKey, c -> c));
        List<AIConfig> toSave = new ArrayList<>();
        updateFeatureConfigInternal(toSave, existingConfigs, config);
        configRepository.saveAll(toSave);
        return getFeatureConfig(featureName);
    }

    @Override
    @Transactional
    public AIFeatureConfig toggleFeature(String featureName, boolean isEnabled) {
        updateConfig("feature." + featureName + ".enabled", String.valueOf(isEnabled));
        return getFeatureConfig(featureName);
    }

    private void updateFeatureConfigInternal(List<AIConfig> toSave, Map<String, AIConfig> existingConfigs, AIFeatureConfig fc) {
        String prefix = "feature." + fc.getFeatureId() + ".";
        updateList(toSave, existingConfigs, prefix + "enabled", String.valueOf(fc.isEnabled()));
        updateList(toSave, existingConfigs, prefix + "model", fc.getModelId());
        updateList(toSave, existingConfigs, prefix + "maxTokens", String.valueOf(fc.getMaxTokens()));
        updateList(toSave, existingConfigs, prefix + "temperature", String.valueOf(fc.getTemperature()));
        updateList(toSave, existingConfigs, prefix + "dailyLimit", String.valueOf(fc.getDailyLimit()));
    }

    private void updateList(List<AIConfig> toSave, Map<String, AIConfig> existing, String key, String value) {
        AIConfig config = existing.getOrDefault(key, AIConfig.builder().configKey(key).build());
        config.setConfigValue(value);
        toSave.add(config);
    }

    private AIFeatureConfig getFeatureConfigFromMap(Map<String, String> configMap, String featureName) {
        AIFeatureConfig config = new AIFeatureConfig();
        config.setFeatureId(featureName);
        config.setName(featureName);
        config.setEnabled(Boolean.parseBoolean(configMap.getOrDefault("feature." + featureName + ".enabled", "true")));
        config.setModelId(configMap.getOrDefault("feature." + featureName + ".model", "gemini-pro"));
        config.setMaxTokens(Integer.parseInt(configMap.getOrDefault("feature." + featureName + ".maxTokens", "1000")));
        config.setTemperature(Double.parseDouble(configMap.getOrDefault("feature." + featureName + ".temperature", "0.7")));
        config.setDailyLimit(Integer.parseInt(configMap.getOrDefault("feature." + featureName + ".dailyLimit", "50")));
        return config;
    }

    private String getConfigValue(String key, String defaultValue) {
        return configRepository.findById(key).map(AIConfig::getConfigValue).orElse(defaultValue);
    }
}
