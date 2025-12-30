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

import com.lexia.backend.config.QuotaLimitsConfig;
import com.lexia.backend.config.GeminiConfig;
import com.lexia.backend.dto.ai.PlanLimitsDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class AIConfigServiceImpl implements AIConfigService {

        private final AIConfigRepository configRepository;
        private final QuotaLimitsConfig quotaLimitsConfig;
        private final GeminiConfig geminiConfig;

        @PostConstruct
        public void init() {
                // Load plan limits from DB to memory on startup
                PlanLimitsDTO limits = getPlanLimits();
                updateQuotaLimitsConfig(limits);

                // Register custom_materials feature if not exists
                registerFeatureIfNotExists("custom_materials");

                // Sync outdated model configs to use the configured default model
                syncOutdatedModelConfigs();
        }

        /**
         * Syncs outdated model configurations to the current default model.
         * This fixes issues where old models (e.g., gemini-1.5-pro) are stored in the
         * DB
         * but are no longer valid in the API.
         */
        @Transactional
        public void syncOutdatedModelConfigs() {
                if (geminiConfig == null) {
                        return;
                }
                String currentDefaultModel = geminiConfig.getDefaultModel();
                List<String> outdatedModels = List.of("gemini-1.5-pro", "gemini-pro");

                List<AIConfig> configs = configRepository.findAll();
                List<AIConfig> toUpdate = new ArrayList<>();

                for (AIConfig config : configs) {
                        if (config.getConfigKey().endsWith(".model")
                                        && outdatedModels.contains(config.getConfigValue())) {
                                config.setConfigValue(currentDefaultModel);
                                toUpdate.add(config);
                        }
                }

                if (!toUpdate.isEmpty()) {
                        configRepository.saveAll(toUpdate);
                        log.info("Synced {} outdated model configs to {}", toUpdate.size(), currentDefaultModel);
                }
        }

        /**
         * Registers a new feature with default configuration if it doesn't exist.
         * Called during startup and can be called dynamically by Admin.
         * 
         * @param featureName The feature name to register
         */
        @Transactional
        public void registerFeatureIfNotExists(String featureName) {
                String enabledKey = "feature." + featureName + ".enabled";
                if (!configRepository.existsById(enabledKey)) {
                        List<AIConfig> configs = new ArrayList<>();
                        configs.add(AIConfig.builder().configKey(enabledKey).configValue("true").isEncrypted(false)
                                        .build());
                        // Use model from application.properties via GeminiConfig
                        String defaultModel = geminiConfig != null ? geminiConfig.getDefaultModel()
                                        : "gemini-2.5-flash-lite";
                        configs.add(AIConfig.builder().configKey("feature." + featureName + ".model")
                                        .configValue(defaultModel)
                                        .isEncrypted(false).build());
                        configs.add(AIConfig.builder().configKey("feature." + featureName + ".maxTokens")
                                        .configValue("8000")
                                        .isEncrypted(false).build());
                        configs.add(AIConfig.builder().configKey("feature." + featureName + ".temperature")
                                        .configValue("0.7")
                                        .isEncrypted(false).build());
                        configs.add(AIConfig.builder().configKey("feature." + featureName + ".dailyLimit")
                                        .configValue("10")
                                        .isEncrypted(false).build());

                        // Custom Materials specific configs
                        if ("custom_materials".equals(featureName)) {
                                configs.add(AIConfig.builder().configKey("feature." + featureName + ".max_file_size")
                                                .configValue("10485760").isEncrypted(false).build());
                                configs.add(AIConfig.builder().configKey("feature." + featureName + ".max_pages")
                                                .configValue("20")
                                                .isEncrypted(false).build());
                                configs.add(AIConfig.builder().configKey("feature." + featureName + ".monthly_limit")
                                                .configValue("10")
                                                .isEncrypted(false).build());
                        }

                        configRepository.saveAll(configs);
                }
        }

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
                settings.setMonthlyBudgetLimit(
                                Double.parseDouble(configMap.getOrDefault("global.monthlyBudgetLimit", "100.0")));
                settings.setAlertThresholdPercentage(
                                Integer.parseInt(configMap.getOrDefault("global.alertThresholdPercentage", "80")));
                settings.setFallbackEnabled(
                                Boolean.parseBoolean(configMap.getOrDefault("global.fallbackEnabled", "false")));
                settings.setRateLimitEnabled(
                                Boolean.parseBoolean(configMap.getOrDefault("global.rateLimitEnabled", "true")));
                settings.setCostPerInputToken(
                                Double.parseDouble(configMap.getOrDefault("global.costPerInputToken", "0.000001")));
                settings.setCostPerOutputToken(
                                Double.parseDouble(configMap.getOrDefault("global.costPerOutputToken", "0.000002")));

                // Legacy fields
                settings.setDefaultDailyLimit(
                                Integer.parseInt(configMap.getOrDefault("global.defaultDailyLimit", "100")));
                settings.setDefaultMonthlyLimit(
                                Integer.parseInt(configMap.getOrDefault("global.defaultMonthlyLimit", "3000")));
                settings.setCostPerToken(Double.parseDouble(configMap.getOrDefault("global.costPerToken", "0.00001")));

                settings.setFeatures(List.of(
                                getFeatureConfigFromMap(configMap, "roleplay"),
                                getFeatureConfigFromMap(configMap, "grammar"),
                                getFeatureConfigFromMap(configMap, "flashcards"),
                                getFeatureConfigFromMap(configMap, "custom_materials")));
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
                updateList(toSave, existingConfigs, "global.monthlyBudgetLimit",
                                String.valueOf(settings.getMonthlyBudgetLimit()));
                updateList(toSave, existingConfigs, "global.alertThresholdPercentage",
                                String.valueOf(settings.getAlertThresholdPercentage()));
                updateList(toSave, existingConfigs, "global.fallbackEnabled",
                                String.valueOf(settings.isFallbackEnabled()));
                updateList(toSave, existingConfigs, "global.rateLimitEnabled",
                                String.valueOf(settings.isRateLimitEnabled()));
                updateList(toSave, existingConfigs, "global.costPerInputToken",
                                String.valueOf(settings.getCostPerInputToken()));
                updateList(toSave, existingConfigs, "global.costPerOutputToken",
                                String.valueOf(settings.getCostPerOutputToken()));

                // Legacy fields
                updateList(toSave, existingConfigs, "global.defaultDailyLimit",
                                String.valueOf(settings.getDefaultDailyLimit()));
                updateList(toSave, existingConfigs, "global.defaultMonthlyLimit",
                                String.valueOf(settings.getDefaultMonthlyLimit()));
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

        @Override
        public PlanLimitsDTO getPlanLimits() {
                Map<String, String> configMap = configRepository.findAll().stream()
                                .collect(Collectors.toMap(AIConfig::getConfigKey, AIConfig::getConfigValue));

                return PlanLimitsDTO.builder()
                                .freeRoleplaySessions(Integer.parseInt(configMap.getOrDefault(
                                                "quota.free.roleplaySessions",
                                                String.valueOf(quotaLimitsConfig.getFreeRoleplaySessions()))))
                                .freeFlashcardDecks(Integer.parseInt(configMap.getOrDefault("quota.free.flashcardDecks",
                                                String.valueOf(quotaLimitsConfig.getFreeFlashcardDecks()))))
                                .freeGrammarExercises(Integer.parseInt(configMap.getOrDefault(
                                                "quota.free.grammarExercises",
                                                String.valueOf(quotaLimitsConfig.getFreeGrammarExercises()))))
                                .freeTotalRequests(Integer.parseInt(configMap.getOrDefault("quota.free.totalRequests",
                                                String.valueOf(quotaLimitsConfig.getFreeTotalRequests()))))
                                .proRoleplaySessions(Integer.parseInt(configMap.getOrDefault(
                                                "quota.pro.roleplaySessions",
                                                String.valueOf(quotaLimitsConfig.getProRoleplaySessions()))))
                                .proFlashcardDecks(Integer.parseInt(configMap.getOrDefault("quota.pro.flashcardDecks",
                                                String.valueOf(quotaLimitsConfig.getProFlashcardDecks()))))
                                .proGrammarExercises(Integer.parseInt(configMap.getOrDefault(
                                                "quota.pro.grammarExercises",
                                                String.valueOf(quotaLimitsConfig.getProGrammarExercises()))))
                                .proTotalRequests(Integer.parseInt(configMap.getOrDefault("quota.pro.totalRequests",
                                                String.valueOf(quotaLimitsConfig.getProTotalRequests()))))
                                .warningThresholdPercent((int) (Double.parseDouble(configMap.getOrDefault(
                                                "quota.warningThreshold",
                                                String.valueOf(quotaLimitsConfig.getWarningThreshold()))) * 100))
                                .criticalThresholdPercent((int) (Double.parseDouble(configMap.getOrDefault(
                                                "quota.criticalThreshold",
                                                String.valueOf(quotaLimitsConfig.getCriticalThreshold()))) * 100))
                                .build();
        }

        @Override
        @Transactional
        public PlanLimitsDTO updatePlanLimits(PlanLimitsDTO dto) {
                Map<String, AIConfig> existingConfigs = configRepository.findAll().stream()
                                .collect(Collectors.toMap(AIConfig::getConfigKey, c -> c));
                List<AIConfig> toSave = new ArrayList<>();

                updateList(toSave, existingConfigs, "quota.free.roleplaySessions",
                                String.valueOf(dto.getFreeRoleplaySessions()));
                updateList(toSave, existingConfigs, "quota.free.flashcardDecks",
                                String.valueOf(dto.getFreeFlashcardDecks()));
                updateList(toSave, existingConfigs, "quota.free.grammarExercises",
                                String.valueOf(dto.getFreeGrammarExercises()));
                updateList(toSave, existingConfigs, "quota.free.totalRequests",
                                String.valueOf(dto.getFreeTotalRequests()));

                updateList(toSave, existingConfigs, "quota.pro.roleplaySessions",
                                String.valueOf(dto.getProRoleplaySessions()));
                updateList(toSave, existingConfigs, "quota.pro.flashcardDecks",
                                String.valueOf(dto.getProFlashcardDecks()));
                updateList(toSave, existingConfigs, "quota.pro.grammarExercises",
                                String.valueOf(dto.getProGrammarExercises()));
                updateList(toSave, existingConfigs, "quota.pro.totalRequests",
                                String.valueOf(dto.getProTotalRequests()));

                updateList(toSave, existingConfigs, "quota.warningThreshold",
                                String.valueOf(dto.getWarningThresholdPercent() / 100.0));
                updateList(toSave, existingConfigs, "quota.criticalThreshold",
                                String.valueOf(dto.getCriticalThresholdPercent() / 100.0));

                configRepository.saveAll(toSave);

                // Update in-memory config
                updateQuotaLimitsConfig(dto);

                return dto;
        }

        private void updateQuotaLimitsConfig(PlanLimitsDTO dto) {
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
        }

        private void updateFeatureConfigInternal(List<AIConfig> toSave, Map<String, AIConfig> existingConfigs,
                        AIFeatureConfig fc) {
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
                // Get fallback model from application.properties
                String fallbackModel = geminiConfig != null ? geminiConfig.getDefaultModel() : "gemini-2.5-flash-lite";

                AIFeatureConfig config = new AIFeatureConfig();
                config.setFeatureId(featureName);
                config.setName(featureName);
                config.setEnabled(Boolean
                                .parseBoolean(configMap.getOrDefault("feature." + featureName + ".enabled", "true")));

                // Use DB model if set. Fallback to application.properties model
                String dbModel = configMap.get("feature." + featureName + ".model");
                config.setModelId(dbModel != null && !dbModel.isBlank() ? dbModel : fallbackModel);

                config.setMaxTokens(Integer
                                .parseInt(configMap.getOrDefault("feature." + featureName + ".maxTokens", "4000")));
                config.setTemperature(
                                Double.parseDouble(configMap.getOrDefault("feature." + featureName + ".temperature",
                                                "0.7")));
                config.setDailyLimit(Integer
                                .parseInt(configMap.getOrDefault("feature." + featureName + ".dailyLimit", "50")));
                return config;
        }
}
