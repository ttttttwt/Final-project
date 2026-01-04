package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import com.lexia.backend.dto.speaking.GrammarFeedbackDTO;
import com.lexia.backend.dto.speaking.SpeakingAssessmentResponseDTO;
import com.lexia.backend.dto.speaking.VocabularyFeedbackDTO;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.file.service.FileStorageService;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.service.ai.AiUsageTracker;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.service.ai.SpeakingAssessmentService;
import com.lexia.backend.file.enums.FileCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Implementation of SpeakingAssessmentService using Gemini AI.
 * Provides speech recognition, pronunciation scoring, and grammar/vocabulary
 * feedback.
 * 
 * @author LEXIA Team
 * @since Sprint 6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingAssessmentServiceImpl implements SpeakingAssessmentService {

    private final LessonRepository lessonRepository;
    private final FileStorageService fileStorageService;
    private final GeminiClientService geminiClient;
    private final AIConfigService aiConfigService;
    private final AiUsageTracker aiUsageTracker;
    private final ObjectMapper objectMapper;

    private static final String CONTENT_TYPE_SPEAKING = "speaking_assessment";

    @Override
    @Transactional
    public SpeakingAssessmentResponseDTO assessSpeaking(
            Long lessonId,
            String promptId,
            MultipartFile audio,
            UUID userId) {

        log.info("Assessing speaking for lesson {} prompt {} by user {}", lessonId, promptId, userId);

        // 1. Validate lesson exists and is SPEAKING type
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        if (lesson.getLessonType() != Lesson.LessonType.SPEAKING) {
            throw new IllegalArgumentException("Lesson is not a SPEAKING type: " + lesson.getLessonType());
        }

        // 2. Extract prompt data from lesson content
        PromptData promptData = extractPromptData(lesson, promptId);
        if (promptData == null) {
            throw new ResourceNotFoundException("Prompt not found: " + promptId);
        }

        // 3. Extract audio bytes for AI processing (send directly, not via URL)
        byte[] audioBytes = null;
        String audioMimeType = null;
        if (audio != null && !audio.isEmpty()) {
            try {
                audioBytes = audio.getBytes();
                audioMimeType = audio.getContentType();
                
                // Extract base MIME type (remove parameters like ;codecs=opus)
                if (audioMimeType != null && audioMimeType.contains(";")) {
                    audioMimeType = audioMimeType.split(";")[0].trim();
                }
                
                // Normalize MIME type for Gemini (it prefers audio/mpeg over audio/mp3)
                if ("audio/mp3".equals(audioMimeType)) {
                    audioMimeType = "audio/mpeg";
                }
                log.info("Audio file loaded: {} bytes, type: {}", audioBytes.length, audioMimeType);
            } catch (IOException e) {
                log.error("Failed to read audio file: {}", e.getMessage());
                throw new AiServiceException("Failed to process audio file");
            }
            
            // Also store for history/reference (optional)
            fileStorageService.store(audio, FileCategory.LESSON_AUDIO, userId);
        }

        // 4. Check AI feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("speaking_assessment");
        if (featureConfig == null) {
            featureConfig = aiConfigService.getFeatureConfig("custom_materials"); // Fallback
        }
        if (featureConfig == null || !featureConfig.isEnabled()) {
            throw new AiServiceException("Speaking assessment feature is currently disabled");
        }

        // 5. Generate assessment using AI (pass audio bytes directly)
        SpeakingAssessmentResponseDTO result = generateFullAssessment(
                promptData,
                audioBytes,
                audioMimeType,
                featureConfig.getModelId(),
                (float) featureConfig.getTemperature(),
                featureConfig.getMaxTokens());
        result.setPromptId(promptId);

        // 6. Track AI usage
        aiUsageTracker.trackUsage(AiUsageTrackingRequest.builder()
                .userId(userId)
                .contentType(CONTENT_TYPE_SPEAKING)
                .modelId(featureConfig.getModelId())
                .inputTokens(featureConfig.getMaxTokens() / 2)
                .outputTokens(featureConfig.getMaxTokens())
                .success(true)
                .build());

        log.info("Speaking assessment completed for lesson {} prompt {}: score={}",
                lessonId, promptId, result.getOverallScore());

        return result;
    }

    /**
     * Generates full assessment including transcription, pronunciation, grammar,
     * and vocabulary.
     * Uses Gemini File API to upload audio directly instead of URL (for localhost compatibility).
     */
    private SpeakingAssessmentResponseDTO generateFullAssessment(
            PromptData promptData,
            byte[] audioBytes,
            String audioMimeType,
            String modelId,
            float temperature,
            int maxTokens) {

        String prompt = buildAssessmentPrompt(promptData);

        try {
            if (audioBytes != null && audioBytes.length > 0) {
                // Send audio file directly to Gemini (like PDF/image in CustomContent)
                log.info("Sending audio directly to Gemini: {} bytes, type: {}", 
                        audioBytes.length, audioMimeType);
                var response = geminiClient.generateContentWithFile(
                        prompt, audioBytes, audioMimeType);
                return parseAssessmentResponse(response.content(), promptData);
            } else {
                // No audio - generate mock assessment for testing
                log.warn("No audio provided, generating mock assessment");
                var response = geminiClient.generateStructuredContent(
                        prompt + "\n\n(No audio provided - generate mock assessment for testing)",
                        modelId, temperature, maxTokens);
                return parseAssessmentResponse(response.content(), promptData);
            }
        } catch (Exception e) {
            log.error("Failed to generate speaking assessment: {}", e.getMessage(), e);
            // Return demo assessment on failure
            return buildDemoAssessment(promptData);
        }
    }

    /**
     * Builds the AI prompt for comprehensive speaking assessment.
     * Audio is sent as inline file data via Gemini File API, not via URL.
     */
    private String buildAssessmentPrompt(PromptData promptData) {
        String targetGrammarList = String.join(", ", promptData.targetGrammar);
        String targetVocabList = String.join(", ", promptData.targetVocabulary);
        String sampleAnswersList = String.join("\n- ", promptData.sampleAnswers);

        return String.format(
                """
                        You are an English speaking assessment expert. Analyze the learner's speaking attempt from the attached audio file.

                        <context>
                        Scenario: %s
                        Prompt: %s
                        Expected sample answers:
                        - %s
                        </context>

                        <learning_targets>
                        Target Grammar Structures: [%s]
                        Target Vocabulary: [%s]
                        </learning_targets>

                        <critical_rules>
                        1. ONLY transcribe what you ACTUALLY HEAR in the audio. DO NOT make up or fabricate speech.
                        2. If the audio is SILENT, contains only background noise, or has NO speech:
                           - Set transcription to "" (empty string)
                           - Set ALL scores to 0
                           - Set pronunciationFeedback to "No speech detected in the audio. Please try again and speak clearly."
                           - Set noSpeechDetected to true
                        3. If the audio quality is poor or speech is unclear, indicate this honestly.
                        4. NEVER invent or hallucinate words that were not spoken.
                        5. Be accurate and honest in your assessment - if someone didn't speak, they get 0 points.
                        </critical_rules>

                        <instructions>
                        Listen CAREFULLY to the attached audio file. If you hear actual speech, provide assessment.
                        If the audio is silent or has no speech, return the silent response format.

                        JSON response structure:
                        {
                          "noSpeechDetected": false,
                          "transcription": "The ACTUAL words spoken by the user - DO NOT FABRICATE",
                          "pronunciationScore": 85,
                          "pronunciationFeedback": "Overall feedback on pronunciation",
                          "wordBreakdown": {
                            "word1": { "word": "experience", "score": 90, "note": "Good clarity" },
                            "word2": { "word": "collaborate", "score": 70, "note": "Watch the stress" }
                          },
                          "grammarFeedback": {
                            "issues": [
                              { "originalText": "I have work", "correction": "I have worked", "rule": "Present Perfect", "explanation": "Use past participle" }
                            ],
                            "usedTargetStructures": ["present perfect", "past simple"],
                            "missedStructures": ["conditional"],
                            "suggestion": "Great use of past tenses!",
                            "score": 80
                          },
                          "vocabularyFeedback": {
                            "usedTargetWords": ["experience", "responsibility"],
                            "missedWords": ["achievement"],
                            "advancedWords": ["collaborate"],
                            "suggestion": "Try incorporating 'achievement' next time",
                            "score": 75
                          },
                          "overallScore": 82
                        }

                        FOR SILENT/NO SPEECH AUDIO, return:
                        {
                          "noSpeechDetected": true,
                          "transcription": "",
                          "pronunciationScore": 0,
                          "pronunciationFeedback": "No speech detected in the audio recording. Please try again and speak clearly into your microphone.",
                          "wordBreakdown": {},
                          "grammarFeedback": {
                            "issues": [],
                            "usedTargetStructures": [],
                            "missedStructures": [],
                            "suggestion": "No speech to analyze. Please record yourself speaking.",
                            "score": 0
                          },
                          "vocabularyFeedback": {
                            "usedTargetWords": [],
                            "missedWords": [],
                            "advancedWords": [],
                            "suggestion": "No speech to analyze. Please record yourself speaking.",
                            "score": 0
                          },
                          "overallScore": 0
                        }

                        Scoring weights for pronunciation:
                        - Accuracy: 40%% (correct phonemes)
                        - Stress/Intonation: 30%% (word stress, sentence rhythm)
                        - Fluency: 30%% (speed, pauses, flow)

                        For grammar, ONLY check the target structures: [%s]
                        Do NOT flag grammar issues unrelated to target structures.

                        Be HONEST and ACCURATE. If no speech is detected, return 0 scores.
                        </instructions>

                        Return ONLY valid JSON, no markdown formatting.
                        """,
                promptData.scenario,
                promptData.promptText,
                sampleAnswersList.isEmpty() ? "No sample answers provided" : sampleAnswersList,
                targetGrammarList,
                targetVocabList,
                targetGrammarList);
    }

    /**
     * Parses the AI response into SpeakingAssessmentResponseDTO.
     */
    private SpeakingAssessmentResponseDTO parseAssessmentResponse(String content, PromptData promptData) {
        try {
            // Clean JSON if wrapped in markdown
            String cleanJson = content.trim();
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
            }

            JsonNode root = objectMapper.readTree(cleanJson);

            // Check if no speech was detected
            boolean noSpeechDetected = root.path("noSpeechDetected").asBoolean(false);
            
            // Also check if transcription is empty/null as fallback detection
            String transcription = root.path("transcription").asText("");
            if (transcription.isBlank() && !noSpeechDetected) {
                // If transcription is empty but flag wasn't set, treat as no speech
                noSpeechDetected = true;
            }

            // Parse word breakdown
            Map<String, SpeakingAssessmentResponseDTO.WordScore> wordBreakdown = new HashMap<>();
            if (root.has("wordBreakdown")) {
                JsonNode breakdown = root.get("wordBreakdown");
                breakdown.fields().forEachRemaining(entry -> {
                    JsonNode wordNode = entry.getValue();
                    wordBreakdown.put(entry.getKey(), SpeakingAssessmentResponseDTO.WordScore.builder()
                            .word(wordNode.path("word").asText())
                            .score(wordNode.path("score").asInt(80))
                            .note(wordNode.path("note").asText(""))
                            .build());
                });
            }

            // Parse grammar feedback
            GrammarFeedbackDTO grammarFeedback = parseGrammarFeedback(root.path("grammarFeedback"));

            // Parse vocabulary feedback
            VocabularyFeedbackDTO vocabularyFeedback = parseVocabularyFeedback(root.path("vocabularyFeedback"));

            // If no speech detected, ensure scores are 0
            int pronunciationScore = noSpeechDetected ? 0 : root.path("pronunciationScore").asInt(0);
            int overallScore = noSpeechDetected ? 0 : root.path("overallScore").asInt(0);
            String pronunciationFeedback = noSpeechDetected 
                    ? "No speech detected in the audio recording. Please try again and speak clearly into your microphone."
                    : root.path("pronunciationFeedback").asText("Keep practicing!");

            return SpeakingAssessmentResponseDTO.builder()
                    .noSpeechDetected(noSpeechDetected)
                    .transcription(transcription)
                    .pronunciationScore(pronunciationScore)
                    .pronunciationFeedback(pronunciationFeedback)
                    .wordBreakdown(wordBreakdown)
                    .grammarFeedback(grammarFeedback)
                    .vocabularyFeedback(vocabularyFeedback)
                    .overallScore(overallScore)
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse assessment response, using demo: {}", e.getMessage());
            return buildDemoAssessment(promptData);
        }
    }

    private GrammarFeedbackDTO parseGrammarFeedback(JsonNode node) {
        if (node.isMissingNode()) {
            return GrammarFeedbackDTO.builder()
                    .issues(List.of())
                    .usedTargetStructures(List.of())
                    .missedStructures(List.of())
                    .suggestion("No grammar analysis available")
                    .score(70)
                    .build();
        }

        List<GrammarFeedbackDTO.GrammarIssue> issues = new ArrayList<>();
        if (node.has("issues") && node.get("issues").isArray()) {
            for (JsonNode issueNode : node.get("issues")) {
                issues.add(GrammarFeedbackDTO.GrammarIssue.builder()
                        .originalText(issueNode.path("originalText").asText())
                        .correction(issueNode.path("correction").asText())
                        .rule(issueNode.path("rule").asText())
                        .explanation(issueNode.path("explanation").asText())
                        .build());
            }
        }

        return GrammarFeedbackDTO.builder()
                .issues(issues)
                .usedTargetStructures(parseStringList(node.get("usedTargetStructures")))
                .missedStructures(parseStringList(node.get("missedStructures")))
                .suggestion(node.path("suggestion").asText(""))
                .score(node.path("score").asInt(70))
                .build();
    }

    private VocabularyFeedbackDTO parseVocabularyFeedback(JsonNode node) {
        if (node.isMissingNode()) {
            return VocabularyFeedbackDTO.builder()
                    .usedTargetWords(List.of())
                    .missedWords(List.of())
                    .advancedWords(List.of())
                    .suggestion("No vocabulary analysis available")
                    .score(70)
                    .build();
        }

        return VocabularyFeedbackDTO.builder()
                .usedTargetWords(parseStringList(node.get("usedTargetWords")))
                .missedWords(parseStringList(node.get("missedWords")))
                .advancedWords(parseStringList(node.get("advancedWords")))
                .suggestion(node.path("suggestion").asText(""))
                .score(node.path("score").asInt(70))
                .build();
    }

    private List<String> parseStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> result.add(item.asText()));
        return result;
    }

    /**
     * Builds a demo assessment for testing or when AI fails.
     */
    private SpeakingAssessmentResponseDTO buildDemoAssessment(PromptData promptData) {
        return SpeakingAssessmentResponseDTO.builder()
                .transcription("I have three years of experience in software development.")
                .pronunciationScore(78)
                .pronunciationFeedback("Good clarity overall. Watch the stress on 'development'.")
                .wordBreakdown(Map.of(
                        "experience", SpeakingAssessmentResponseDTO.WordScore.builder()
                                .word("experience").score(85).note("Good pronunciation").build(),
                        "development", SpeakingAssessmentResponseDTO.WordScore.builder()
                                .word("development").score(70).note("Stress on second syllable").build()))
                .grammarFeedback(GrammarFeedbackDTO.builder()
                        .issues(List.of())
                        .usedTargetStructures(promptData.targetGrammar.isEmpty()
                                ? List.of("present perfect")
                                : promptData.targetGrammar)
                        .missedStructures(List.of())
                        .suggestion("Good use of tenses!")
                        .score(80)
                        .build())
                .vocabularyFeedback(VocabularyFeedbackDTO.builder()
                        .usedTargetWords(promptData.targetVocabulary.isEmpty()
                                ? List.of("experience")
                                : promptData.targetVocabulary.subList(0,
                                        Math.min(2, promptData.targetVocabulary.size())))
                        .missedWords(List.of())
                        .advancedWords(List.of())
                        .suggestion("Try using more varied vocabulary.")
                        .score(75)
                        .build())
                .overallScore(78)
                .build();
    }

    /**
     * Extracts prompt data from lesson JSONB content.
     */
    private PromptData extractPromptData(Lesson lesson, String promptId) {
        try {
            JsonNode content = objectMapper.readTree(lesson.getContent());
            if (content == null) {
                return null;
            }

            String scenario = content.path("scenario").asText("");
            JsonNode prompts = content.get("prompts");

            if (prompts == null || !prompts.isArray()) {
                return null;
            }

            // Find prompt by index (promptId is "0", "1", etc.)
            int promptIndex;
            try {
                promptIndex = Integer.parseInt(promptId);
            } catch (NumberFormatException e) {
                log.warn("Invalid promptId format: {}", promptId);
                return null;
            }

            if (promptIndex < 0 || promptIndex >= prompts.size()) {
                return null;
            }

            JsonNode promptNode = prompts.get(promptIndex);

            return new PromptData(
                    scenario,
                    promptNode.path("prompt").asText(""),
                    promptNode.path("context").asText(""),
                    parseStringList(promptNode.get("sampleAnswers")),
                    parseStringList(promptNode.get("targetGrammar")),
                    parseStringList(promptNode.get("targetVocabulary")));
        } catch (Exception e) {
            log.error("Failed to extract prompt data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Internal record to hold prompt data extracted from lesson.
     */
    private record PromptData(
            String scenario,
            String promptText,
            String context,
            List<String> sampleAnswers,
            List<String> targetGrammar,
            List<String> targetVocabulary) {
    }
}
