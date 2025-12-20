package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.GeminiResponseDTO;
import com.lexia.backend.entity.CustomMaterialJob;
import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.entity.UserCustomMaterialSettings;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.exception.ContentExtractionException;
import com.lexia.backend.repository.CustomMaterialJobRepository;
import com.lexia.backend.repository.UserCustomMaterialRepository;
import com.lexia.backend.repository.UserCustomMaterialSettingsRepository;
import com.lexia.backend.service.ai.ContentExtractorService;
import com.lexia.backend.service.ai.CustomMaterialProcessingService;
import com.lexia.backend.service.ai.CustomMaterialPrompts;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.validation.PromptSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of CustomMaterialProcessingService.
 * 
 * <p>
 * Handles the async processing pipeline:
 * </p>
 * <ol>
 * <li>Content extraction from source</li>
 * <li>Prompt construction with security (XML tagging)</li>
 * <li>Gemini API call for content generation</li>
 * <li>Response parsing and validation</li>
 * <li>Storage of generated content</li>
 * </ol>
 * 
 * @since Sprint 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomMaterialProcessingServiceImpl implements CustomMaterialProcessingService {

    private static final String DEFAULT_CEFR_LEVEL = "B1";

    private final UserCustomMaterialRepository materialRepository;
    private final UserCustomMaterialSettingsRepository settingsRepository;
    private final CustomMaterialJobRepository jobRepository;
    private final ContentExtractorService contentExtractor;
    private final GeminiClientService geminiClient;
    private final PromptSanitizer promptSanitizer;
    private final ObjectMapper objectMapper;

    @Override
    @Async("aiProcessingExecutor")
    @Transactional
    public void processMaterial(UUID materialId) {
        log.info("Starting async processing for material {}", materialId);

        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        UserCustomMaterial material = materialRepository.findById(materialId).orElse(null);

        if (material == null) {
            log.error("Material {} not found", materialId);
            return;
        }

        if (job == null) {
            log.error("Job not found for material {}", materialId);
            updateMaterialStatus(material, CustomMaterialStatus.FAILED, "Processing job not found");
            return;
        }

        try {
            // Update status to processing
            job.startProcessing();
            jobRepository.save(job);
            updateMaterialStatus(material, CustomMaterialStatus.PROCESSING, null);

            // Step 1: Extract content (20% progress)
            log.info("Step 1: Extracting content for material {}", materialId);
            updateProgress(job, 10);

            String extractedContent = contentExtractor.extractContent(material);
            material.setContentText(extractedContent);
            materialRepository.save(material);
            updateProgress(job, 20);

            // Step 2: Sanitize content for AI
            log.info("Step 2: Sanitizing content for material {}", materialId);
            String sanitizedContent = promptSanitizer.sanitize(extractedContent, 50000);
            updateProgress(job, 25);

            // Step 3: Get settings and determine what to generate
            UserCustomMaterialSettings settings = settingsRepository.findByMaterialId(materialId)
                    .orElse(null);
            List<String> targetOptions = settings != null ? settings.getTargetOptions()
                    : List.of(UserCustomMaterialSettings.OPTION_VOCABULARY);

            // Step 4: Generate content with Gemini (60% of work)
            log.info("Step 3: Generating content for material {} with options {}", materialId, targetOptions);
            Map<String, Object> generatedContent = generateContent(sanitizedContent, targetOptions);
            updateProgress(job, 85);

            // Step 5: Save generated content
            log.info("Step 4: Saving generated content for material {}", materialId);
            generatedContent.put("schemaVersion", 1);
            material.setGeneratedContent(generatedContent);
            material.setStatus(CustomMaterialStatus.COMPLETED);
            materialRepository.save(material);

            // Complete job
            job.complete();
            jobRepository.save(job);
            log.info("Successfully completed processing for material {}", materialId);

        } catch (ContentExtractionException e) {
            log.error("Content extraction failed for material {}: {}", materialId, e.getMessage());
            failMaterial(material, job, "Content extraction failed: " + e.getReason());
        } catch (Exception e) {
            log.error("Processing failed for material {}: {}", materialId, e.getMessage(), e);
            failMaterial(material, job, "Processing failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean retryProcessing(UUID materialId) {
        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        if (job == null) {
            log.warn("No job found for material {} to retry", materialId);
            return false;
        }

        if (!job.canRetry()) {
            log.warn("Max retries exceeded for material {}", materialId);
            return false;
        }

        job.retry();
        jobRepository.save(job);

        // Trigger async processing
        processMaterial(materialId);
        return true;
    }

    @Override
    @Transactional
    public void cancelProcessing(UUID materialId) {
        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        if (job != null && !job.isTerminal()) {
            job.fail("Cancelled by user");
            jobRepository.save(job);
        }

        UserCustomMaterial material = materialRepository.findById(materialId).orElse(null);
        if (material != null && material.isProcessing()) {
            material.setStatus(CustomMaterialStatus.FAILED);
            material.setErrorMessage("Cancelled by user");
            materialRepository.save(material);
        }
    }

    // ===== Private methods =====

    private Map<String, Object> generateContent(String content, List<String> targetOptions) {
        Map<String, Object> result = new HashMap<>();

        // Build combined prompt
        Map<String, Object> variables = Map.of(
                "content", content,
                "cefr_level", DEFAULT_CEFR_LEVEL,
                "target_options", String.join(", ", targetOptions));

        String prompt = CustomMaterialPrompts.resolve(CustomMaterialPrompts.COMBINED_PROMPT, variables);

        try {
            // Call Gemini
            GeminiResponseDTO response = geminiClient.generateStructuredContent(prompt);
            String responseText = response.content();

            // Parse JSON response
            if (responseText != null && !responseText.isBlank()) {
                // Clean up response (remove markdown code blocks if present)
                responseText = cleanJsonResponse(responseText);

                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = objectMapper.readValue(responseText, Map.class);
                    result.putAll(parsed);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse JSON response, generating individually: {}", e.getMessage());
                    // Fallback: generate each type individually
                    result = generateIndividually(content, targetOptions);
                }
            }

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI content generation failed: " + e.getMessage(), e);
        }

        return result;
    }

    private Map<String, Object> generateIndividually(String content, List<String> targetOptions) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> variables = Map.of(
                "content", content,
                "cefr_level", DEFAULT_CEFR_LEVEL);

        for (String option : targetOptions) {
            try {
                String prompt = switch (option) {
                    case "VOCABULARY" ->
                        CustomMaterialPrompts.resolve(CustomMaterialPrompts.VOCABULARY_PROMPT, variables);
                    case "QUIZ" -> CustomMaterialPrompts.resolve(CustomMaterialPrompts.QUIZ_PROMPT, variables);
                    case "SUMMARY" -> CustomMaterialPrompts.resolve(CustomMaterialPrompts.SUMMARY_PROMPT, variables);
                    case "ROLE_PLAY" -> CustomMaterialPrompts.resolve(CustomMaterialPrompts.ROLEPLAY_PROMPT, variables);
                    case "SHADOWING" ->
                        CustomMaterialPrompts.resolve(CustomMaterialPrompts.SHADOWING_PROMPT, variables);
                    default -> null;
                };

                if (prompt != null) {
                    GeminiResponseDTO response = geminiClient.generateStructuredContent(prompt);
                    String responseText = cleanJsonResponse(response.content());

                    String key = option.toLowerCase().replace("_", "");
                    if (option.equals("ROLE_PLAY"))
                        key = "roleplay";

                    if (option.equals("SUMMARY")) {
                        result.put(key, responseText); // Summary is plain text
                    } else {
                        result.put(key, objectMapper.readValue(responseText, Object.class));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to generate {}: {}", option, e.getMessage());
            }
        }

        return result;
    }

    private String cleanJsonResponse(String response) {
        if (response == null)
            return "";

        // Remove markdown code blocks
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }

        return response.trim();
    }

    private void updateProgress(CustomMaterialJob job, int progress) {
        job.updateProgress(progress);
        jobRepository.save(job);
    }

    private void updateMaterialStatus(UserCustomMaterial material, CustomMaterialStatus status, String error) {
        material.setStatus(status);
        if (error != null) {
            material.setErrorMessage(error);
        }
        materialRepository.save(material);
    }

    private void failMaterial(UserCustomMaterial material, CustomMaterialJob job, String error) {
        if (job != null) {
            job.fail(error);
            jobRepository.save(job);
        }
        updateMaterialStatus(material, CustomMaterialStatus.FAILED, error);
    }
}
