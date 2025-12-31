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
import com.lexia.backend.service.ai.FlashcardService;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.validation.PromptSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomMaterialProcessingServiceImplTest {

        @Mock
        private UserCustomMaterialRepository materialRepository;
        @Mock
        private UserCustomMaterialSettingsRepository settingsRepository;
        @Mock
        private CustomMaterialJobRepository jobRepository;
        @Mock
        private ContentExtractorService contentExtractor;
        @Mock
        private GeminiClientService geminiClient;
        @Mock
        private PromptSanitizer promptSanitizer;
        @Mock
        private ApplicationContext applicationContext;
        @Mock
        private ApplicationEventPublisher eventPublisher;
        @Mock
        private FlashcardService flashcardService;
        @Mock
        private com.lexia.backend.service.ai.AiUsageTracker usageTracker;
        @Mock
        private com.lexia.backend.service.ai.AIConfigService aiConfigService;
        @Spy
        private ObjectMapper objectMapper = new ObjectMapper();

        @InjectMocks
        private CustomMaterialProcessingServiceImpl processingService;

        private UserCustomMaterial material;
        private CustomMaterialJob job;
        private UUID materialId;

        @BeforeEach
        void setUp() {
                materialId = UUID.randomUUID();

                material = UserCustomMaterial.builder()
                                .id(materialId)
                                .userId(UUID.randomUUID())
                                .status(CustomMaterialStatus.PENDING)
                                .build();

                job = CustomMaterialJob.builder()
                                .id(UUID.randomUUID())
                                .material(material)
                                .status(CustomMaterialJob.STATUS_QUEUED)
                                .progress(0)
                                .retryCount(0)
                                .maxRetries(3)
                                .build();
        }

        @Test
        void processMaterial_Success() throws JsonProcessingException {
                // Setup mocks
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(contentExtractor.extractContent(material)).thenReturn("Extracted content");
                when(promptSanitizer.sanitize(anyString(), anyInt())).thenReturn("Sanitized content");
                when(settingsRepository.findByMaterialId(materialId)).thenReturn(Optional.empty()); // Default options

                com.lexia.backend.dto.ai.AIFeatureConfig config = new com.lexia.backend.dto.ai.AIFeatureConfig();
                config.setEnabled(true);
                config.setModelId("model");
                config.setTemperature(1.0);
                config.setMaxTokens(100);
                when(aiConfigService.getFeatureConfig("custom_materials")).thenReturn(config);

                // Mock Gemini response
                String jsonResponse = """
                                ```json
                                {
                                  "vocabulary": [{"term": "test", "definition": "test def"}]
                                }
                                ```
                                """;
                GeminiResponseDTO geminiResponse = new GeminiResponseDTO(jsonResponse, "model", null, Instant.now(),
                                100, false,
                                "STOP");
                when(geminiClient.generateStructuredContent(anyString(), anyString(), anyFloat(), anyInt())).thenReturn(geminiResponse);

                // Execute
                processingService.processMaterial(materialId);

                // Verify
                verify(jobRepository, atLeastOnce()).save(job);
                verify(materialRepository, atLeastOnce()).save(material);

                assertEquals(100, job.getProgress(),
                                "Job should be completed (100% implicit via complete status logic usually, or explicitly set)");
                // Note: The service implementation calls job.complete() which usually sets
                // status to COMPLETED. Progress might not be explicitly set to 100 in
                // complete(),
                // but let's check status.
                assertEquals(CustomMaterialJob.STATUS_COMPLETED, job.getStatus());
                assertEquals(CustomMaterialStatus.COMPLETED, material.getStatus());
                assertNotNull(material.getGeneratedContent());
                assertTrue(material.getGeneratedContent().containsKey("vocabulary"));
        }

        @Test
        void processMaterial_ExtractionFailed() {
                // Setup mocks
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(contentExtractor.extractContent(material))
                                .thenThrow(new ContentExtractionException("Extraction error", "TEST"));

                // Execute
                processingService.processMaterial(materialId);

                // Verify
                assertEquals(CustomMaterialJob.STATUS_FAILED, job.getStatus());
                assertEquals(CustomMaterialStatus.FAILED, material.getStatus());
                assertTrue(material.getErrorMessage().contains("Content extraction failed"));
        }

        @Test
        void processMaterial_GeminiFailed() {
                // Setup mocks
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(contentExtractor.extractContent(material)).thenReturn("Content");
                when(promptSanitizer.sanitize(anyString(), anyInt())).thenReturn("Content");

                com.lexia.backend.dto.ai.AIFeatureConfig config = new com.lexia.backend.dto.ai.AIFeatureConfig();
                config.setEnabled(true);
                config.setModelId("model");
                config.setTemperature(1.0);
                config.setMaxTokens(100);
                when(aiConfigService.getFeatureConfig("custom_materials")).thenReturn(config);

                when(geminiClient.generateStructuredContent(anyString(), anyString(), anyFloat(), anyInt())).thenThrow(new RuntimeException("API Error"));

                // Execute
                processingService.processMaterial(materialId);

                // Verify
                assertEquals(CustomMaterialJob.STATUS_FAILED, job.getStatus());
                assertEquals(CustomMaterialStatus.FAILED, material.getStatus());
                assertTrue(material.getErrorMessage().contains("Processing failed"));
        }

        @Test
        void processMaterial_FallbackToIndividualGeneration_WhenJsonParseFails() throws JsonProcessingException {
                // Setup mocks
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(contentExtractor.extractContent(material)).thenReturn("Content");
                when(promptSanitizer.sanitize(anyString(), anyInt())).thenReturn("Content");

                com.lexia.backend.dto.ai.AIFeatureConfig config = new com.lexia.backend.dto.ai.AIFeatureConfig();
                config.setEnabled(true);
                config.setModelId("model");
                config.setTemperature(1.0);
                config.setMaxTokens(100);
                when(aiConfigService.getFeatureConfig("custom_materials")).thenReturn(config);

                // Mock Settings to request specific options
                UserCustomMaterialSettings settings = new UserCustomMaterialSettings();
                settings.setTargetOptions(List.of("VOCABULARY"));
                when(settingsRepository.findByMaterialId(materialId)).thenReturn(Optional.of(settings));

                // First call fails JSON parsing (Combined prompt)
                GeminiResponseDTO invalidResponse = new GeminiResponseDTO("Invalid JSON", "model", null, Instant.now(),
                                100, false, "STOP");
                // Fallback call succeeds (Individual prompt)
                String validVocab = "[{\"term\":\"fallback\"}]";
                GeminiResponseDTO vocabResponse = new GeminiResponseDTO(validVocab, "model", null, Instant.now(), 100,
                                false, "STOP");

                // Mock sequential calls: 1. Combined (fails parsing), 2. Vocabulary (succeeds)
                when(geminiClient.generateStructuredContent(anyString(), anyString(), anyFloat(), anyInt()))
                                .thenReturn(invalidResponse)
                                .thenReturn(vocabResponse);

                // Execute
                processingService.processMaterial(materialId);

                // Verify
                assertEquals(CustomMaterialStatus.COMPLETED, material.getStatus());
                Map<String, Object> content = material.getGeneratedContent();
                assertNotNull(content);
                assertTrue(content.containsKey("vocabulary"));
        }

        @Test
        void retryProcessing_Success() {
                job.setStatus(CustomMaterialJob.STATUS_FAILED);
                job.setRetryCount(0);

                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(applicationContext.getBean(CustomMaterialProcessingService.class)).thenReturn(processingService);
                // We also need to mock the subsequent processMaterial calls
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
                when(contentExtractor.extractContent(material)).thenReturn("Content");
                when(promptSanitizer.sanitize(anyString(), anyInt())).thenReturn("Sanitized");

                com.lexia.backend.dto.ai.AIFeatureConfig config = new com.lexia.backend.dto.ai.AIFeatureConfig();
                config.setEnabled(true);
                config.setModelId("model");
                config.setTemperature(1.0);
                config.setMaxTokens(100);
                when(aiConfigService.getFeatureConfig("custom_materials")).thenReturn(config);

                when(geminiClient.generateStructuredContent(anyString(), anyString(), anyFloat(), anyInt()))
                                .thenReturn(new GeminiResponseDTO("{}", "model", null, Instant.now(), 100, false,
                                                "STOP"));

                boolean result = processingService.retryProcessing(materialId);

                assertTrue(result);
                // Since processMaterial runs synchronously in test environment, it completes
                // immediately
                assertEquals(CustomMaterialJob.STATUS_COMPLETED, job.getStatus());
                verify(jobRepository, atLeastOnce()).save(job);
                // implicit verification that processMaterial run
                verify(contentExtractor).extractContent(material);
        }

        @Test
        void cancelProcessing_Success() {
                job.setStatus(CustomMaterialJob.STATUS_PROCESSING);
                material.setStatus(CustomMaterialStatus.PROCESSING);

                when(jobRepository.findByMaterialId(materialId)).thenReturn(Optional.of(job));
                when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

                processingService.cancelProcessing(materialId);

                assertEquals(CustomMaterialJob.STATUS_FAILED, job.getStatus());
                assertEquals("Cancelled by user", job.getLastError());
                assertEquals(CustomMaterialStatus.FAILED, material.getStatus());
                assertEquals("Cancelled by user", material.getErrorMessage());
        }
}
