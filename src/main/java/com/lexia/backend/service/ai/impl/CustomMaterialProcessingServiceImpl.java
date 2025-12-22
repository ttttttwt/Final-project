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
import com.lexia.backend.service.ai.FlashcardService;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.dto.ai.CreateFlashcardDeckDTO;
import com.lexia.backend.dto.ai.FlashcardCardDTO;
import com.lexia.backend.dto.ai.FlashcardBackDTO;
import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import com.lexia.backend.validation.PromptSanitizer;
import com.lexia.backend.notification.event.MaterialProcessingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final ApplicationEventPublisher eventPublisher;
    private final FlashcardService flashcardService;
    private final com.lexia.backend.service.ai.AiUsageTracker usageTracker;
    private final ApplicationContext applicationContext;

    @Override
    @Async("aiProcessingExecutor")
    @Transactional
    public void processMaterial(UUID materialId) {
        log.info("Starting async processing for material {}", materialId);

        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        UserCustomMaterial material = materialRepository.findById(materialId).orElse(null);

        if (material == null) {
            log.error("Material {} not found in processMaterial", materialId);
            return;
        }

        if (job == null) {
            log.error("Job not found for material {} in processMaterial", materialId);
            updateMaterialStatus(material, CustomMaterialStatus.FAILED, "Processing job not found");
            return;
        }

        log.info("Found job {} for material {}, status: {}", job.getId(), materialId, job.getStatus());

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
            Map<String, Object> generatedContent = generateContent(material, sanitizedContent, targetOptions);
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

            // Step 6: Sync vocabulary to SRS if enabled
            if (settings != null && Boolean.TRUE.equals(settings.getSyncVocabToSrs())) {
                syncVocabularyToSrs(material, generatedContent, null); // Uses default CEFR level
            }

            // Publish success event for notification
            publishCompletedEvent(material, true, null);

        } catch (ContentExtractionException e) {
            log.error("Content extraction failed for material {}: {}", materialId, e.getMessage());
            log.error("Stack trace:", e);
            failMaterial(material, job, "Content extraction failed: " + e.getReason());
            publishCompletedEvent(material, false, e.getReason());
        } catch (Exception e) {
            log.error("Processing failed for material {}: {}", materialId, e.getMessage());
            log.error("Full stack trace for processing failure:", e);
            failMaterial(material, job, "Processing failed: " + e.getMessage());
            publishCompletedEvent(material, false, e.getMessage());
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

        // Trigger async processing after commit
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("Transaction (retry) committed, triggering async processing for material {}", materialId);
                    try {
                        applicationContext.getBean(CustomMaterialProcessingService.class).processMaterial(materialId);
                    } catch (Exception e) {
                        log.error("Failed to trigger async retry for material {}", materialId, e);
                    }
                }
            });
        } else {
            // Should not happen with @Transactional, but fallback
            applicationContext.getBean(CustomMaterialProcessingService.class).processMaterial(materialId);
        }
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

    private Map<String, Object> generateContent(UserCustomMaterial material, String content,
            List<String> targetOptions) {
        Map<String, Object> result = new HashMap<>();

        // Build combined prompt
        Map<String, Object> variables = Map.of(
                "content", content,
                "cefr_level", DEFAULT_CEFR_LEVEL,
                "target_options", String.join(", ", targetOptions));

        String prompt = CustomMaterialPrompts.resolve(CustomMaterialPrompts.COMBINED_PROMPT, variables);

        try {
            // Call Gemini
            long startTime = System.currentTimeMillis();
            GeminiResponseDTO response = geminiClient.generateStructuredContent(prompt);
            long responseTimeMs = System.currentTimeMillis() - startTime;
            String responseText = response.content();
            log.info("Raw AI response for material {}: {}", material.getId(), responseText);

            // Track usage with granular content type
            trackContentGeneration(material, response, responseTimeMs, true, null);

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
                    result = generateIndividually(material, content, targetOptions);
                }
            }

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            // Track failure
            trackContentGeneration(material, null, 0, false, e.getMessage());
            throw new RuntimeException("AI content generation failed: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Tracks AI usage for custom material content generation.
     */
    private void trackContentGeneration(UserCustomMaterial material, GeminiResponseDTO response,
            long responseTimeMs, boolean success, String errorMessage) {
        try {
            // Determine content type based on source type
            String contentType = switch (material.getSourceType()) {
                case PDF -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_PDF_EXTRACTION;
                case DOCX -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_DOCX_EXTRACTION;
                case IMAGE -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_IMAGE_OCR;
                case YOUTUBE -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_YOUTUBE_TRANSCRIPT;
                case WEBSITE -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_WEBSITE_EXTRACTION;
                case TEXT -> com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_TEXT_INPUT;
            };

            int inputTokens = 0;
            int outputTokens = 0;
            String modelId = "gemini-1.5-pro";

            if (response != null && response.tokenUsage() != null) {
                inputTokens = response.tokenUsage().inputTokens();
                outputTokens = response.tokenUsage().outputTokens();
                modelId = response.model() != null ? response.model() : modelId;
            }

            // Track content generation
            usageTracker.trackUsageAsync(AiUsageTrackingRequest.builder()
                    .userId(material.getUserId())
                    .contentType(com.lexia.backend.service.ai.AiUsageTracker.CONTENT_TYPE_CM_CONTENT_GENERATION)
                    .modelId(modelId)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .responseTimeMs((int) responseTimeMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build()
                    .withMetadata("materialId", material.getId().toString())
                    .withMetadata("sourceType", contentType));

        } catch (Exception e) {
            log.warn("Failed to track usage: {}", e.getMessage());
        }
    }

    private Map<String, Object> generateIndividually(UserCustomMaterial material, String content,
            List<String> targetOptions) {
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
                    log.info("Raw AI response for material {} (fallback option {}): {}", material.getId(), option,
                            response.content());
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

    private void publishCompletedEvent(UserCustomMaterial material, boolean success, String errorMessage) {
        try {
            MaterialProcessingCompletedEvent event = new MaterialProcessingCompletedEvent(
                    this,
                    material.getUserId(),
                    material.getId(),
                    material.getTitle(),
                    success,
                    errorMessage);
            eventPublisher.publishEvent(event);
            log.debug("Published MaterialProcessingCompletedEvent for material {}", material.getId());
        } catch (Exception e) {
            log.warn("Failed to publish completion event: {}", e.getMessage());
        }
    }

    /**
     * Syncs vocabulary from generated content to the Flashcard SRS system.
     * Creates a new Flashcard deck with the extracted vocabulary items.
     */
    @SuppressWarnings("unchecked")
    private void syncVocabularyToSrs(UserCustomMaterial material, Map<String, Object> generatedContent,
            String cefrLevel) {
        log.info("Syncing vocabulary to SRS for material {}", material.getId());

        try {
            // Extract vocabulary from generated content
            List<Map<String, Object>> vocabulary = null;
            Object vocabObj = generatedContent.get("vocabulary");
            if (vocabObj instanceof List<?>) {
                vocabulary = (List<Map<String, Object>>) vocabObj;
            }

            if (vocabulary == null || vocabulary.isEmpty()) {
                log.info("No vocabulary found to sync for material {}", material.getId());
                return;
            }

            // Convert to FlashcardCardDTO list
            List<FlashcardCardDTO> cards = vocabulary.stream()
                    .map(this::convertToFlashcardCard)
                    .filter(card -> card != null)
                    .limit(100) // Limit to 100 cards per deck
                    .toList();

            if (cards.isEmpty()) {
                log.info("No valid vocabulary cards to sync for material {}", material.getId());
                return;
            }

            // Create deck request
            CreateFlashcardDeckDTO deckRequest = CreateFlashcardDeckDTO.builder()
                    .title("Vocabulary: " + material.getTitle())
                    .description("Auto-generated from Custom Material: " + material.getTitle())
                    .sourceType("USER_CREATED") // Using USER_CREATED as there's no CUSTOM_MATERIAL type
                    .cefrLevel(cefrLevel != null ? cefrLevel : DEFAULT_CEFR_LEVEL)
                    .cards(cards)
                    .build();

            // Create the deck
            flashcardService.createDeck(deckRequest, material.getUserId());
            log.info("Successfully synced {} vocabulary items to SRS for material {}",
                    cards.size(), material.getId());

        } catch (Exception e) {
            log.warn("Failed to sync vocabulary to SRS for material {}: {}",
                    material.getId(), e.getMessage());
            // Don't fail the material processing if SRS sync fails
        }
    }

    /**
     * Converts a vocabulary map from generated content to FlashcardCardDTO.
     */
    @SuppressWarnings("unchecked")
    private FlashcardCardDTO convertToFlashcardCard(Map<String, Object> vocabItem) {
        try {
            String word = String.valueOf(vocabItem.get("word"));
            String definition = String.valueOf(vocabItem.get("definition"));

            if (word == null || word.isBlank() || "null".equals(word)) {
                return null;
            }

            FlashcardBackDTO back = FlashcardBackDTO.builder()
                    .definition(definition)
                    .partOfSpeech((String) vocabItem.get("pos"))
                    .pronunciation((String) vocabItem.get("pronunciation"))
                    .exampleSentence((String) vocabItem.get("example"))
                    .build();

            // Handle synonyms if present
            if (vocabItem.get("synonyms") instanceof List<?> synonymsList) {
                back.setSynonyms(synonymsList.stream()
                        .map(Object::toString)
                        .limit(5)
                        .toList());
            }

            return FlashcardCardDTO.builder()
                    .front(word)
                    .back(back)
                    .difficulty(3) // Default difficulty
                    .build();

        } catch (Exception e) {
            log.debug("Failed to convert vocabulary item: {}", e.getMessage());
            return null;
        }
    }
}
