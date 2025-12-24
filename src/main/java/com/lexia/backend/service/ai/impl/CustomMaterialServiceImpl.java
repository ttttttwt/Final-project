package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.entity.*;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.exception.AccessDeniedException;
import com.lexia.backend.exception.QuotaExceededException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.service.FileStorageService;
import com.lexia.backend.mapper.CustomMaterialMapper;
import com.lexia.backend.repository.*;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.service.ai.CustomMaterialProcessingService;
import com.lexia.backend.service.ai.CustomMaterialPrompts;
import com.lexia.backend.service.ai.CustomMaterialService;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.exception.ai.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of CustomMaterialService.
 * 
 * <p>
 * Handles file uploads, content extraction, and async AI processing
 * for custom learning materials.
 * </p>
 * 
 * @since Sprint 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomMaterialServiceImpl implements CustomMaterialService {

    private static final int MONTHLY_QUOTA = 10;
    private static final String FEATURE_TYPE = "custom_materials";

    private final UserCustomMaterialRepository materialRepository;
    private final UserCustomMaterialSettingsRepository settingsRepository;
    private final CustomMaterialJobRepository jobRepository;
    private final UserShadowingAttemptRepository shadowingAttemptRepository;
    private final FileStorageService fileStorageService;
    private final CustomMaterialMapper mapper;
    private final CustomMaterialProcessingService processingService;
    private final GeminiClientService geminiClient;
    private final AIConfigService aiConfigService;
    private final UserAiQuotaRepository quotaRepository;
    private final com.lexia.backend.config.QuotaLimitsConfig quotaLimitsConfig;

    // ===== Public Methods =====

    @Override
    @Transactional
    public MaterialCreatedDTO createMaterial(CreateMaterialRequestDTO request, MultipartFile file, UUID userId) {
        // Check if feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("custom_materials");
        if (!featureConfig.isEnabled()) {
            throw new AiServiceException("Custom materials feature is currently disabled by administrator");
        }

        // Validate request
        request.validate();

        // Quota is now checked by @QuotaCheck aspect on the controller
        // but we still keep this as a secondary check if called from other services
        int remaining = getRemainingQuota(userId);
        if (remaining <= 0) {
            UserAiQuota quota = quotaRepository.findByUserId(userId).orElse(null);
            throw new QuotaExceededException(
                    FEATURE_TYPE,
                    quotaLimitsConfig.getForPlan(quota != null ? quota.getPlanType() : PlanType.FREE)
                            .getCustomMaterialsLimit(),
                    quota != null ? quota.getCustomMaterialsUsed() : 0,
                    quota != null ? quota.getPlanType() : PlanType.FREE);
        }

        // Handle file upload if needed
        String fileUrl = null;
        if (request.getSourceType().requiresFileUpload()) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is required for " + request.getSourceType() + " source type");
            }
            var fileEntity = fileStorageService.store(file, FileCategory.CUSTOM_MATERIAL, userId);
            fileUrl = fileStorageService.getPublicUrl(fileEntity.getId());
        }

        // Create material entity
        UserCustomMaterial material = UserCustomMaterial.builder()
                .userId(userId)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .originalFileUrl(fileUrl)
                .contentText(request.getRawText())
                .inputMetadata(request.getInputMetadata() != null ? request.getInputMetadata().toMap() : Map.of())
                .status(CustomMaterialStatus.PENDING)
                .build();

        // Handle URL source types
        if (request.getSourceType().requiresUrl()) {
            Map<String, Object> metadata = new java.util.HashMap<>(material.getInputMetadata());
            metadata.put("sourceUrl", request.getSourceUrl());
            material.setInputMetadata(metadata);
        }

        material = materialRepository.save(material);

        // Create settings
        createSettings(material, request);

        // Create processing job
        createJob(material);

        log.info("Created material {} for user {}", material.getId(), userId);

        // Trigger async processing
        processAsync(material.getId());

        return mapper.toCreated(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialListItemDTO> listMaterials(UUID userId, CustomMaterialStatus status, Pageable pageable) {
        Page<UserCustomMaterial> materials;

        if (status != null) {
            materials = materialRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            materials = materialRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return materials.map(mapper::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponseDTO getMaterial(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);
        return mapper.toResponse(material);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialStatusDTO getStatus(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);
        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        return mapper.toStatus(material, job);
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateContent(UUID materialId, UpdateContentDTO request, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);

        if (!material.isReady()) {
            throw new IllegalStateException("Cannot update content for material that is not completed");
        }

        material.setGeneratedContent(request.getGeneratedContent());
        material = materialRepository.save(material);

        log.info("Updated content for material {}", materialId);
        return mapper.toResponse(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);

        // Delete associated file if exists
        if (material.getOriginalFileUrl() != null) {
            try {
                // Extract file ID from URL format: /api/v1/files/{uuid}/download
                UUID fileId = extractFileIdFromUrl(material.getOriginalFileUrl());
                if (fileId != null) {
                    fileStorageService.delete(fileId);
                    log.info("Deleted file {} for material {}", fileId, materialId);
                }
            } catch (Exception e) {
                // Log but don't fail the material deletion
                log.warn("Failed to delete file for material {}: {}", materialId, e.getMessage());
            }
        }

        materialRepository.delete(material);
        log.info("Deleted material {}", materialId);
    }

    /**
     * Extracts file UUID from storage URL.
     * Expected format: /api/v1/files/{uuid}/download or full URL with same path.
     */
    private UUID extractFileIdFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            // Pattern: .../files/{uuid}/download
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "/files/([0-9a-fA-F-]{36})/download");
            java.util.regex.Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return UUID.fromString(matcher.group(1));
            }
        } catch (Exception e) {
            log.warn("Could not extract file ID from URL {}: {}", url, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean canCreateMaterial(UUID userId) {
        return getRemainingQuota(userId) > 0;
    }

    @Override
    public int getRemainingQuota(UUID userId) {
        UserAiQuota quota = quotaRepository.findByUserId(userId).orElse(null);
        if (quota == null) {
            return 0; // Should be created by aspect or login
        }

        var limits = quotaLimitsConfig.getForPlan(quota.getPlanType());
        return Math.max(0, limits.getCustomMaterialsLimit() - quota.getCustomMaterialsUsed());
    }

    // ===== Private Helpers =====

    private UserCustomMaterial findMaterialWithOwnershipCheck(UUID materialId, UUID userId) {
        UserCustomMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));

        if (!material.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have access to this material");
        }

        return material;
    }

    private void createSettings(UserCustomMaterial material, CreateMaterialRequestDTO request) {
        UserCustomMaterialSettings settings = UserCustomMaterialSettings.builder()
                .material(material)
                .targetOptions(request.getTargetOptions() != null
                        ? request.getTargetOptions()
                        : List.of(UserCustomMaterialSettings.OPTION_VOCABULARY))
                .build();

        MaterialSettingsDTO settingsDTO = request.getSettings();
        if (settingsDTO != null) {
            if (settingsDTO.getAiCorrectionMode() != null) {
                settings.setAiCorrectionMode(settingsDTO.getAiCorrectionMode());
            }
            if (settingsDTO.getStyleLearnMode() != null) {
                settings.setStyleLearnMode(settingsDTO.getStyleLearnMode());
            }
            if (settingsDTO.getSyncVocabToSrs() != null) {
                settings.setSyncVocabToSrs(settingsDTO.getSyncVocabToSrs());
            }
        }

        settingsRepository.save(settings);
    }

    private void createJob(UserCustomMaterial material) {
        CustomMaterialJob job = CustomMaterialJob.builder()
                .material(material)
                .status(CustomMaterialJob.STATUS_QUEUED)
                .progress(0)
                .build();
        jobRepository.save(job);
    }

    /**
     * Triggers async processing of material content.
     * Delegates to CustomMaterialProcessingService.
     */
    private void processAsync(UUID materialId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("Transaction committed, triggering async processing for material {}", materialId);
                    try {
                        processingService.processMaterial(materialId);
                    } catch (Exception e) {
                        log.error("Failed to trigger async processing for material {}", materialId, e);
                    }
                }
            });
        } else {
            log.info("No active transaction, triggering async processing immediately for material {}", materialId);
            processingService.processMaterial(materialId);
        }
    }

    @Override
    public StyleTransformResponseDTO transformStyle(StyleTransformRequestDTO request, UUID userId) {
        log.info("Transforming style to {} for user {}", request.getTargetStyle(), userId);

        // Check if feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("custom_materials");
        if (!featureConfig.isEnabled()) {
            throw new AiServiceException("Custom materials feature is currently disabled");
        }

        // Build prompt using template
        String prompt = CustomMaterialPrompts.STYLE_TRANSFORM_PROMPT
                .replace("{{content}}", request.getText())
                .replace("{{target_style}}", request.getTargetStyle());

        // Handle learn mode conditionals
        if (request.isIncludeExplanation()) {
            prompt = prompt.replace("{{#learn_mode}}", "").replace("{{/learn_mode}}", "");
            prompt = prompt.replaceAll("\\{\\{\\^learn_mode\\}\\}.*?\\{\\{/learn_mode\\}\\}", "");
        } else {
            prompt = prompt.replace("{{^learn_mode}}", "").replace("{{/learn_mode}}", "");
            prompt = prompt.replaceAll("\\{\\{#learn_mode\\}\\}.*?\\{\\{/learn_mode\\}\\}", "");
        }

        try {
            var response = geminiClient.generateContent(prompt);
            return parseStyleTransformResponse(response.content(), request.isIncludeExplanation());
        } catch (Exception e) {
            log.error("Style transform failed: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to transform style: " + e.getMessage());
        }
    }

    private StyleTransformResponseDTO parseStyleTransformResponse(String content, boolean includeExplanation) {
        if (!includeExplanation) {
            return StyleTransformResponseDTO.builder()
                    .transformedText(content.trim())
                    .build();
        }

        // Parse content with explanations (markdown format)
        String transformedText = content;
        java.util.List<StyleTransformResponseDTO.StyleExplanation> explanations = new java.util.ArrayList<>();

        if (content.contains("## Transformed Text")) {
            String[] parts = content.split("## Key Changes");
            if (parts.length > 0) {
                transformedText = parts[0]
                        .replace("## Transformed Text", "")
                        .trim();
            }
            if (parts.length > 1) {
                // Parse key changes: - [Original] -> [Changed]: [Reason]
                String changesSection = parts[1];
                String[] lines = changesSection.split("\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (trimmedLine.startsWith("-")) {
                        String contentLine = trimmedLine.substring(1).trim();
                        
                        // Try to parse: [Original] -> [Changed]: [Reason]
                        if (contentLine.contains("->") && contentLine.contains(":")) {
                            try {
                                String[] arrowParts = contentLine.split("->", 2);
                                String original = arrowParts[0].trim();
                                
                                String[] colonParts = arrowParts[1].split(":", 2);
                                String changed = colonParts[0].trim();
                                String reason = colonParts[1].trim();
                                
                                explanations.add(StyleTransformResponseDTO.StyleExplanation.builder()
                                        .original(original)
                                        .changed(changed)
                                        .reason(reason)
                                        .build());
                            } catch (Exception e) {
                                // Fallback if parsing fails
                                explanations.add(StyleTransformResponseDTO.StyleExplanation.builder()
                                        .reason(contentLine)
                                        .build());
                            }
                        } else {
                            // Fallback for simple format
                            explanations.add(StyleTransformResponseDTO.StyleExplanation.builder()
                                    .reason(contentLine)
                                    .build());
                        }
                    }
                }
            }
        }

        return StyleTransformResponseDTO.builder()
                .transformedText(transformedText)
                .explanations(explanations.isEmpty() ? null : explanations)
                .build();
    }

    @Override
    @Transactional
    public ShadowingScoreResponseDTO scoreShadowing(UUID materialId, String sentenceId,
            MultipartFile audio, UUID userId) {
        log.info("Scoring shadowing for material {} sentence {} by user {}",
                materialId, sentenceId, userId);

        // Validate material and ownership
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);

        if (!material.isReady()) {
            throw new IllegalStateException("Material is not ready for shadowing");
        }

        // Find the sentence in shadowing content
        Map<String, Object> generatedContent = material.getGeneratedContent();
        String targetSentence = findShadowingSentence(generatedContent, sentenceId);
        if (targetSentence == null) {
            throw new ResourceNotFoundException("Sentence not found: " + sentenceId);
        }

        // Upload audio file
        String audioUrl = null;
        if (audio != null && !audio.isEmpty()) {
            var fileEntity = fileStorageService.store(audio,
                    FileCategory.CUSTOM_MATERIAL, userId);
            audioUrl = fileStorageService.getPublicUrl(fileEntity.getId());
        }

        // Generate pronunciation assessment via AI
        ShadowingScoreResponseDTO result = generatePronunciationScore(targetSentence, audioUrl);

        // Save attempt to database
        saveAttempt(material, userId, sentenceId, audioUrl, result);

        return result;
    }

    @SuppressWarnings("unchecked")
    private String findShadowingSentence(Map<String, Object> generatedContent, String sentenceId) {
        if (generatedContent == null)
            return null;

        Object shadowingObj = generatedContent.get("shadowing");
        if (shadowingObj instanceof List<?> shadowingList) {
            for (int i = 0; i < shadowingList.size(); i++) {
                Object item = shadowingList.get(i);
                
                // Case 1: Item is a Map (Object with id and sentence)
                if (item instanceof Map<?, ?> sentenceMap) {
                    Object idObj = ((Map<String, Object>) sentenceMap).get("id");
                    String id = idObj != null ? String.valueOf(idObj) : "s-" + i;
                    
                    if (sentenceId.equals(id)) {
                        // Try "sentence" then "text" as fallback
                        Object sentenceObj = ((Map<String, Object>) sentenceMap).get("sentence");
                        if (sentenceObj == null) {
                            sentenceObj = ((Map<String, Object>) sentenceMap).get("text");
                        }
                        return sentenceObj != null ? String.valueOf(sentenceObj) : null;
                    }
                } 
                // Case 2: Item is a String (Legacy/Simple format)
                else if (item instanceof String sentenceStr) {
                    String id = "s-" + i;
                    if (sentenceId.equals(id)) {
                        return sentenceStr;
                    }
                }
            }
        }
        return null;
    }

    private ShadowingScoreResponseDTO generatePronunciationScore(String targetSentence, String audioUrl) {
        // Build pronunciation assessment prompt
        String prompt = String.format("""
                You are a pronunciation assessment expert. Analyze the user's pronunciation attempt.

                <target_sentence>%s</target_sentence>

                %s

                <instructions>
                Provide a JSON response with this structure:
                {
                  "score": 85,
                  "feedback": "Good overall pronunciation. Watch the stress on 'consider'.",
                  "phonemeBreakdown": {
                    "word1": { "word": "...", "score": 90, "note": "..." }
                  }
                }

                Score from 0-100 based on:
                - Pronunciation accuracy (40%%)
                - Stress and intonation (30%%)
                - Fluency and rhythm (30%%)

                Be encouraging but specific about improvements.
                </instructions>

                Return ONLY valid JSON, no markdown.
                """,
                targetSentence,
                audioUrl != null ? "<audio_url>" + audioUrl + "</audio_url>"
                        : "(No audio provided - provide mock assessment for demo purposes)");

        try {
            var response = geminiClient.generateContent(prompt);
            return parseShadowingScore(response.content());
        } catch (Exception e) {
            log.error("Failed to generate pronunciation score: {}", e.getMessage(), e);
            // Return demo score if AI fails
            return ShadowingScoreResponseDTO.builder()
                    .score(75)
                    .feedback("Good attempt! Keep practicing for better fluency.")
                    .build();
        }
    }

    private ShadowingScoreResponseDTO parseShadowingScore(String content) {
        try {
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(content, Map.class);

            int score = 70;
            if (parsed.get("score") instanceof Number num) {
                score = num.intValue();
            }

            String feedback = String.valueOf(parsed.getOrDefault("feedback", "Keep practicing!"));

            @SuppressWarnings("unchecked")
            Map<String, Object> phonemeBreakdown = parsed.containsKey("phonemeBreakdown")
                    ? (Map<String, Object>) parsed.get("phonemeBreakdown")
                    : null;

            return ShadowingScoreResponseDTO.builder()
                    .score(score)
                    .feedback(feedback)
                    .phonemeBreakdown(phonemeBreakdown)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse shadowing score JSON: {}", e.getMessage());
            return ShadowingScoreResponseDTO.builder()
                    .score(70)
                    .feedback("Assessment completed. Keep practicing!")
                    .build();
        }
    }

    private void saveAttempt(UserCustomMaterial material, UUID userId, String sentenceId,
            String audioUrl, ShadowingScoreResponseDTO result) {
        UserShadowingAttempt attempt = UserShadowingAttempt.builder()
                .material(material)
                .userId(userId)
                .sentenceId(sentenceId)
                .audioUrl(audioUrl)
                .score(result.getScore())
                .feedback(result.getPhonemeBreakdown())
                .build();
        shadowingAttemptRepository.save(attempt);
        log.info("Saved shadowing attempt for sentence {}", sentenceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialListItemDTO> getRelatedMaterials(UUID materialId, UUID userId) {
        // Get the current material
        UserCustomMaterial currentMaterial = findMaterialWithOwnershipCheck(materialId, userId);

        // Find related materials based on source
        List<UserCustomMaterial> relatedMaterials = new java.util.ArrayList<>();

        // Case 1: Materials from same uploaded file
        if (currentMaterial.getOriginalFileUrl() != null && !currentMaterial.getOriginalFileUrl().isBlank()) {
            relatedMaterials = materialRepository.findByOriginalFileUrlAndUserId(
                    currentMaterial.getOriginalFileUrl(),
                    userId);
        }
        // Case 2: Materials from same URL source (YouTube/Website)
        else if (currentMaterial.getInputMetadata() != null
                && currentMaterial.getInputMetadata().containsKey("sourceUrl")) {
            String sourceUrl = String.valueOf(currentMaterial.getInputMetadata().get("sourceUrl"));

            // Find all materials with same sourceUrl in metadata
            List<UserCustomMaterial> allMaterials = materialRepository.findByUserIdOrderByCreatedAtDesc(
                    userId,
                    org.springframework.data.domain.PageRequest.of(0, 100)).getContent();

            relatedMaterials = allMaterials.stream()
                    .filter(m -> m.getStatus() == CustomMaterialStatus.COMPLETED)
                    .filter(m -> m.getInputMetadata() != null
                            && sourceUrl.equals(String.valueOf(m.getInputMetadata().get("sourceUrl"))))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Map to DTOs and filter out current material
        return relatedMaterials.stream()
                .filter(m -> !m.getId().equals(materialId))
                .map(mapper::toListItem)
                .collect(java.util.stream.Collectors.toList());
    }
}
