package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.GrammarExerciseSet;
import com.lexia.backend.entity.GrammarTopic;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.entity.UserGrammarProgress;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.ai.AiRateLimitException;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.mapper.GrammarExerciseMapper;
import com.lexia.backend.repository.GrammarExerciseSetRepository;
import com.lexia.backend.repository.GrammarTopicRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.repository.UserGrammarProgressRepository;
import com.lexia.backend.service.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of GrammarExerciseService with AI-powered generation.
 * 
 * <p>
 * Provides grammar exercise generation using Google Gemini API with:
 * </p>
 * <ul>
 * <li>CEFR-level appropriate exercises</li>
 * <li>Multiple exercise types (MCQ, fill-in-blank, etc.)</li>
 * <li>Fallback content support when AI is unavailable</li>
 * <li>Usage tracking and rate limiting</li>
 * <li>Progress tracking and scoring</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class GrammarExerciseServiceImpl implements GrammarExerciseService {

    /** Default passing percentage threshold */
    private static final double PASSING_THRESHOLD = 70.0;

    /** Prompt template key for grammar exercise generation */
    private static final String GRAMMAR_TEMPLATE_KEY = "grammar_exercise_v1";

    private final GeminiClientService geminiClientService;
    private final PromptTemplateService promptTemplateService;
    private final AiUsageTracker aiUsageTracker;
    private final GrammarExerciseSetRepository exerciseSetRepository;
    private final GrammarTopicRepository topicRepository;
    private final UserGrammarProgressRepository progressRepository;
    private final UserAiQuotaRepository userAiQuotaRepository;
    private final ObjectMapper objectMapper;

    public GrammarExerciseServiceImpl(
            GeminiClientService geminiClientService,
            PromptTemplateService promptTemplateService,
            AiUsageTracker aiUsageTracker,
            GrammarExerciseSetRepository exerciseSetRepository,
            GrammarTopicRepository topicRepository,
            UserGrammarProgressRepository progressRepository,
            UserAiQuotaRepository userAiQuotaRepository,
            ObjectMapper objectMapper) {
        this.geminiClientService = geminiClientService;
        this.promptTemplateService = promptTemplateService;
        this.aiUsageTracker = aiUsageTracker;
        this.exerciseSetRepository = exerciseSetRepository;
        this.topicRepository = topicRepository;
        this.progressRepository = progressRepository;
        this.userAiQuotaRepository = userAiQuotaRepository;
        this.objectMapper = objectMapper;

        log.info("GrammarExerciseService initialized");
    }

    @Override
    @Transactional
    public GrammarExerciseSetDTO generateExercises(GrammarRequestDTO request, UUID userId) {
        log.info("Generating grammar exercises for user: {}, topic: {}, level: {}",
                userId, request.getGrammarTopic(), request.getCefrLevel());

        // Validate topic exists
        GrammarTopic topic = topicRepository.findByNameIgnoreCase(request.getGrammarTopic())
                .orElse(null);

        // Check if user explicitly requested fallback
        if (Boolean.TRUE.equals(request.getUseFallback())) {
            log.info("User requested fallback content");
            return getFallbackOrGenerate(request, userId, topic);
        }

        // Check daily quota
        long dailyUsage = aiUsageTracker.getDailyUsageCount(userId, AiUsageTracker.CONTENT_TYPE_GRAMMAR);
        if (dailyUsage >= 50) { // Default daily limit
            log.warn("User {} exceeded daily grammar quota: {}", userId, dailyUsage);
            throw new AiRateLimitException("Daily grammar exercise limit exceeded. Please try again tomorrow.");
        }

        // Try AI generation
        try {
            return generateWithAI(request, userId, topic);
        } catch (AiServiceException e) {
            log.warn("AI generation failed, using fallback: {}", e.getMessage());
            return getFallbackOrGenerate(request, userId, topic);
        }
    }

    /**
     * Generates exercises using AI.
     */
    private GrammarExerciseSetDTO generateWithAI(GrammarRequestDTO request, UUID userId, GrammarTopic topic) {
        long startTime = System.currentTimeMillis();

        // Build prompt from template
        Map<String, Object> variables = buildPromptVariables(request);
        String prompt = promptTemplateService.getAndResolve(GRAMMAR_TEMPLATE_KEY, variables)
                .orElseGet(() -> buildDefaultPrompt(request));

        // Call Gemini API with structured output (JSON mode)
        GeminiResponseDTO response = geminiClientService.generateStructuredContent(prompt);

        log.info("Raw Gemini response content: {}", response.content());

        long responseTimeMs = System.currentTimeMillis() - startTime;

        // Track usage
        trackUsage(userId, response, responseTimeMs, true);

        // Parse response and create entity
        Map<String, Object> content = parseAIResponse(response.content());

        if (content == null) {
            log.warn("Failed to parse AI response, using fallback");
            return getFallbackOrGenerate(request, userId, topic);
        }

        // Create and save exercise set
        GrammarExerciseSet exerciseSet = GrammarExerciseMapper.toExerciseSetEntity(request, content, userId, topic);
        exerciseSet = exerciseSetRepository.save(exerciseSet);

        log.info("Successfully generated {} exercises for user {} in {}ms",
                exerciseSet.getExerciseCount(), userId, responseTimeMs);

        // Increment grammar exercise counter
        incrementGrammarExerciseCounter(userId);

        return GrammarExerciseMapper.toExerciseSetDTO(exerciseSet);
    }

    /**
     * Increments the grammar exercise counter for a user.
     * Called when generating a new exercise set.
     */
    private void incrementGrammarExerciseCounter(UUID userId) {
        try {
            UserAiQuota quota = userAiQuotaRepository.findByUserId(userId).orElse(null);
            if (quota != null) {
                int newCount = (quota.getGrammarExercisesUsed() != null ? quota.getGrammarExercisesUsed() : 0) + 1;
                quota.setGrammarExercisesUsed(newCount);
                userAiQuotaRepository.save(quota);
                log.info("Incremented grammar exercise counter for user {} to {}", userId, newCount);
            } else {
                log.warn("No quota record found for user {} when incrementing grammar counter", userId);
            }
        } catch (Exception e) {
            log.error("Failed to increment grammar exercise counter for user {}: {}", userId, e.getMessage());
            // Don't fail the exercise generation if quota increment fails
        }
    }

    /**
     * Gets fallback content or generates basic exercises.
     */
    private GrammarExerciseSetDTO getFallbackOrGenerate(GrammarRequestDTO request, UUID userId, GrammarTopic topic) {
        // Try to find existing fallback content
        List<GrammarExerciseSet> fallbacks = exerciseSetRepository
                .findFallbackByCefrLevelAndGrammarPoint(request.getCefrLevel(), request.getGrammarTopic());

        if (!fallbacks.isEmpty()) {
            // Return random fallback
            GrammarExerciseSet fallback = fallbacks.get(new Random().nextInt(fallbacks.size()));
            log.info("Using fallback content: {}", fallback.getId());
            GrammarExerciseSetDTO dto = GrammarExerciseMapper.toExerciseSetDTO(fallback);
            return dto;
        }

        // No fallback available - create minimal exercise set
        log.warn("No fallback content available for {} at level {}",
                request.getGrammarTopic(), request.getCefrLevel());

        Map<String, Object> content = createBasicExerciseContent(request);
        GrammarExerciseSet exerciseSet = GrammarExerciseMapper.toExerciseSetEntity(request, content, userId, topic);
        exerciseSet.setIsFallback(true);
        exerciseSet = exerciseSetRepository.save(exerciseSet);

        return GrammarExerciseMapper.toExerciseSetDTO(exerciseSet);
    }

    /**
     * Creates basic fallback exercise content when no pre-seeded content is
     * available.
     */
    private Map<String, Object> createBasicExerciseContent(GrammarRequestDTO request) {
        Map<String, Object> content = new HashMap<>();

        // Create basic explanation
        Map<String, Object> explanation = new HashMap<>();
        explanation.put("rule", "Practice exercises for " + request.getGrammarTopic());
        explanation.put("examples", List.of("Example sentences will be added soon."));
        explanation.put("commonMistakes", List.of("Content is being updated."));
        content.put("explanation", explanation);

        // Create placeholder exercises
        List<Map<String, Object>> exercises = new ArrayList<>();
        for (int i = 0; i < request.getExerciseCount(); i++) {
            Map<String, Object> exercise = new HashMap<>();
            exercise.put("type", "multiple_choice");
            exercise.put("instruction", "Select the correct answer.");
            exercise.put("question",
                    "This is a placeholder question for " + request.getGrammarTopic() + " - Question " + (i + 1));
            exercise.put("options", List.of("Option A", "Option B", "Option C", "Option D"));
            exercise.put("correctAnswer", "A");
            exercise.put("explanation", "Fallback content - detailed explanation pending.");
            exercise.put("difficulty", "medium");
            exercises.add(exercise);
        }
        content.put("exercises", exercises);

        return content;
    }

    /**
     * Parses the AI response JSON into a content map.
     */
    private Map<String, Object> parseAIResponse(String responseText) {
        if (responseText == null || responseText.isBlank()) {
            return null;
        }

        try {
            // Clean response - extract JSON if wrapped in markdown code blocks
            String cleaned = responseText.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            Map<String, Object> parsed = objectMapper.readValue(cleaned, new TypeReference<Map<String, Object>>() {
            });

            // Unwrap "exerciseSet" wrapper if present (as requested in prompt)
            if (parsed.containsKey("exerciseSet") && parsed.get("exerciseSet") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inner = (Map<String, Object>) parsed.get("exerciseSet");
                return inner;
            }

            return parsed;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Builds prompt variables for template resolution.
     */
    private Map<String, Object> buildPromptVariables(GrammarRequestDTO request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("grammar_topic", request.getGrammarTopic());
        variables.put("cefr_level", request.getCefrLevel());
        variables.put("theme", request.getTheme() != null ? request.getTheme() : "general");
        variables.put("count", request.getExerciseCount());
        return variables;
    }

    /**
     * Builds a default prompt when template is not available.
     */
    private String buildDefaultPrompt(GrammarRequestDTO request) {
        return String.format("""
                You are an expert ESL grammar instructor creating exercises for learners.

                Create %d grammar exercises for the topic "%s" at CEFR level %s.
                Theme: %s

                Return a JSON object with this structure:
                {
                  "explanation": {
                    "rule": "Explanation of the grammar rule",
                    "examples": ["Example 1", "Example 2"],
                    "commonMistakes": ["Common mistake 1"]
                  },
                  "exercises": [
                    {
                      "type": "multiple_choice",
                      "instruction": "Choose the correct answer",
                      "question": "Question text with ___",
                      "options": ["A", "B", "C", "D"],
                      "correctAnswer": "A",
                      "explanation": "Why A is correct",
                      "difficulty": "medium"
                    }
                  ]
                }

                Include a mix of exercise types: multiple_choice, fill_blank, transformation, error_correction.
                Make the content appropriate for %s level learners.
                """,
                request.getExerciseCount(),
                request.getGrammarTopic(),
                request.getCefrLevel(),
                request.getTheme() != null ? request.getTheme() : "general",
                request.getCefrLevel());
    }

    /**
     * Tracks AI usage for billing and quota management.
     */
    private void trackUsage(UUID userId, GeminiResponseDTO response, long responseTimeMs, boolean success) {
        AiUsageTrackingRequest trackingRequest = AiUsageTrackingRequest.builder()
                .userId(userId)
                .contentType(AiUsageTracker.CONTENT_TYPE_GRAMMAR)
                .modelId(response.model())
                .inputTokens(response.tokenUsage() != null ? response.tokenUsage().inputTokens() : 0)
                .outputTokens(response.tokenUsage() != null ? response.tokenUsage().outputTokens() : 0)
                .responseTimeMs((int) responseTimeMs)
                .success(success)
                .promptVersion(GRAMMAR_TEMPLATE_KEY)
                .build();

        aiUsageTracker.trackUsageAsync(trackingRequest);
    }

    @Override
    public List<GrammarTopicDTO> getAllTopics() {
        List<GrammarTopic> topics = topicRepository.findByIsActiveTrue();
        return GrammarExerciseMapper.toTopicDTOList(topics);
    }

    @Override
    public List<GrammarTopicDTO> getTopicsByLevel(String cefrLevel) {
        List<GrammarTopic> topics = topicRepository.findByCefrLevel(cefrLevel);
        return GrammarExerciseMapper.toTopicDTOList(topics);
    }

    @Override
    public List<GrammarTopicDTO> getTopicsByCategory(String category) {
        List<GrammarTopic> topics = topicRepository.findByCategoryAndIsActiveTrue(category);
        return GrammarExerciseMapper.toTopicDTOList(topics);
    }

    @Override
    public List<String> getCategories() {
        return topicRepository.findDistinctActiveCategories();
    }

    @Override
    public GrammarExerciseSetDTO getExerciseSet(UUID exerciseSetId) {
        GrammarExerciseSet exerciseSet = exerciseSetRepository.findById(exerciseSetId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise set not found: " + exerciseSetId));
        return GrammarExerciseMapper.toExerciseSetDTO(exerciseSet);
    }

    @Override
    public Page<GrammarExerciseSetDTO> getUserExerciseSets(UUID userId, Pageable pageable) {
        Page<GrammarExerciseSet> page = exerciseSetRepository.findByUserId(userId, pageable);
        return page.map(GrammarExerciseMapper::toExerciseSetDTO);
    }

    @Override
    @Transactional
    public GrammarResultDTO submitAnswers(UUID exerciseSetId, UUID userId, GrammarAnswerDTO submission) {
        log.info("Processing answer submission for exercise set: {}, user: {}", exerciseSetId, userId);

        // Get exercise set
        GrammarExerciseSet exerciseSet = exerciseSetRepository.findById(exerciseSetId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise set not found: " + exerciseSetId));

        // Security: Verify ownership - exercise sets with null userId are
        // public/fallback
        if (exerciseSet.getUserId() != null && !exerciseSet.getUserId().equals(userId)) {
            log.warn("User {} attempted to access exercise set {} owned by {}",
                    userId, exerciseSetId, exerciseSet.getUserId());
            throw new IllegalArgumentException("You do not have permission to access this exercise set.");
        }

        // Check if already submitted
        Optional<UserGrammarProgress> existingProgress = progressRepository
                .findByUserIdAndExerciseSet_Id(userId, exerciseSetId);

        if (existingProgress.isPresent() && existingProgress.get().isCompleted()) {
            log.warn("User {} already completed exercise set {}", userId, exerciseSetId);
            throw new IllegalStateException("You have already submitted answers for this exercise set.");
        }

        // Extract correct answers from exercise set content
        List<Map<String, Object>> exercises = extractExercises(exerciseSet.getContent());
        int maxScore = exercises.size();

        // Get answers from submission and map by question index for robust matching
        List<GrammarAnswerDTO.AnswerItem> answers = submission.getAnswers();
        if (answers == null) {
            answers = Collections.emptyList();
        }

        // Map answers by question index for O(1) lookup and handling out-of-order
        // submissions
        Map<Integer, GrammarAnswerDTO.AnswerItem> answerMap = new HashMap<>();
        for (GrammarAnswerDTO.AnswerItem answer : answers) {
            int index = answer.getQuestionIndex() != null ? answer.getQuestionIndex() : answerMap.size();
            answerMap.put(index, answer);
        }

        // Validate and score answers
        List<GrammarResultDTO.QuestionFeedback> feedback = new ArrayList<>();
        int score = 0;
        int totalTimeMs = 0;

        for (int i = 0; i < exercises.size(); i++) {
            Map<String, Object> exercise = exercises.get(i);
            GrammarAnswerDTO.AnswerItem answer = answerMap.get(i);

            String correctAnswer = String.valueOf(exercise.get("correctAnswer"));
            String userAnswer = "";
            Long timeMs = null;

            if (answer != null) {
                userAnswer = answer.getAnswer() != null ? String.valueOf(answer.getAnswer()) : "";
                timeMs = answer.getTimeMs();
                if (timeMs != null) {
                    totalTimeMs += timeMs;
                }
            }

            boolean isCorrect = correctAnswer.equalsIgnoreCase(userAnswer.trim());
            if (isCorrect) {
                score++;
            }

            feedback.add(GrammarResultDTO.QuestionFeedback.builder()
                    .questionIndex(i)
                    .userAnswer(userAnswer.isEmpty() ? null : userAnswer)
                    .correctAnswer(correctAnswer)
                    .correct(isCorrect)
                    .explanation((String) exercise.get("explanation"))
                    .timeMs(timeMs)
                    .build());
        }

        // Calculate percentage
        BigDecimal percentage = maxScore > 0
                ? BigDecimal.valueOf((score * 100.0) / maxScore).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean passed = percentage.doubleValue() >= PASSING_THRESHOLD;

        // Use total time from submission if provided, otherwise sum from individual
        // answers
        int totalTimeSeconds = submission.getTotalTimeSeconds() != null
                ? submission.getTotalTimeSeconds()
                : totalTimeMs / 1000;

        // Create or update progress
        UserGrammarProgress progress = existingProgress.orElse(UserGrammarProgress.builder()
                .userId(userId)
                .exerciseSet(exerciseSet)
                .build());

        // Convert feedback to answer format for storage
        List<Map<String, Object>> answerMaps = feedback.stream()
                .map(this::feedbackToMap)
                .collect(Collectors.toList());

        progress.setAnswers(answerMaps);
        progress.setScore(score);
        progress.setMaxScore(maxScore);
        progress.setPercentage(percentage);
        progress.setTimeSpentSeconds(totalTimeSeconds);
        progress.setCompletedAt(Instant.now());

        progressRepository.save(progress);

        log.info("User {} scored {}/{} ({}%) on exercise set {}",
                userId, score, maxScore, percentage, exerciseSetId);

        // Build result DTO
        List<String> areasToImprove = identifyAreasToImprove(feedback, exercises);
        String encouragement = generateEncouragement(percentage.doubleValue(), passed);

        return GrammarResultDTO.builder()
                .exerciseSetId(exerciseSetId)
                .grammarPoint(exerciseSet.getGrammarPoint())
                .cefrLevel(exerciseSet.getCefrLevel())
                .score(score)
                .maxScore(maxScore)
                .percentage(percentage)
                .passed(passed)
                .timeSpentSeconds(totalTimeSeconds)
                .feedback(feedback)
                .areasToImprove(areasToImprove)
                .encouragement(encouragement)
                .completedAt(progress.getCompletedAt())
                .build();
    }

    /**
     * Extracts exercises list from JSONB content.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractExercises(Map<String, Object> content) {
        if (content == null || !content.containsKey("exercises")) {
            return Collections.emptyList();
        }
        Object exercises = content.get("exercises");
        if (exercises instanceof List) {
            return (List<Map<String, Object>>) exercises;
        }
        return Collections.emptyList();
    }

    /**
     * Converts feedback to a map for storage.
     */
    private Map<String, Object> feedbackToMap(GrammarResultDTO.QuestionFeedback feedback) {
        Map<String, Object> map = new HashMap<>();
        map.put("questionIndex", feedback.getQuestionIndex());
        map.put("answer", feedback.getUserAnswer());
        map.put("correct", feedback.getCorrect());
        map.put("timeMs", feedback.getTimeMs());
        return map;
    }

    /**
     * Identifies areas where the user needs improvement.
     */
    private List<String> identifyAreasToImprove(List<GrammarResultDTO.QuestionFeedback> feedback,
            List<Map<String, Object>> exercises) {
        List<String> areas = new ArrayList<>();

        for (int i = 0; i < feedback.size(); i++) {
            if (!Boolean.TRUE.equals(feedback.get(i).getCorrect()) && i < exercises.size()) {
                String type = (String) exercises.get(i).get("type");
                if (type != null && !areas.contains(type)) {
                    areas.add(type + " exercises");
                }
            }
        }

        return areas;
    }

    /**
     * Generates an encouraging message based on the score.
     */
    private String generateEncouragement(double percentage, boolean passed) {
        if (percentage >= 90) {
            return "Excellent work! You've mastered this grammar topic!";
        } else if (percentage >= 80) {
            return "Great job! You have a strong understanding of this topic.";
        } else if (passed) {
            return "Good work! You passed. Keep practicing to improve further.";
        } else if (percentage >= 50) {
            return "Nice effort! Review the explanations and try again.";
        } else {
            return "Keep practicing! Review the grammar rules and try again.";
        }
    }

    @Override
    public Page<GrammarProgressDTO> getHistory(UUID userId, Pageable pageable) {
        Page<UserGrammarProgress> page = progressRepository.findByUserId(userId, pageable);
        return page.map(GrammarExerciseMapper::toProgressDTO);
    }

    @Override
    public GrammarStatsDTO getStatistics(UUID userId) {
        List<UserGrammarProgress> progressList = progressRepository.findByUserIdAndCompletedAtIsNotNull(userId);

        if (progressList.isEmpty()) {
            return GrammarStatsDTO.empty(userId);
        }

        int totalCompleted = progressList.size();
        int totalPassed = 0;
        int totalScore = 0;
        int totalMaxScore = 0;
        int totalTime = 0;

        for (UserGrammarProgress progress : progressList) {
            if (progress.isPassed(PASSING_THRESHOLD)) {
                totalPassed++;
            }
            totalScore += progress.getScore() != null ? progress.getScore() : 0;
            totalMaxScore += progress.getMaxScore() != null ? progress.getMaxScore() : 0;
            totalTime += progress.getTimeSpentSeconds() != null ? progress.getTimeSpentSeconds() : 0;
        }

        BigDecimal avgScore = totalMaxScore > 0
                ? BigDecimal.valueOf((totalScore * 100.0) / totalMaxScore).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int avgTime = totalCompleted > 0 ? totalTime / totalCompleted : 0;

        return GrammarStatsDTO.builder()
                .userId(userId)
                .totalAttempted(progressList.size())
                .totalCompleted(totalCompleted)
                .totalPassed(totalPassed)
                .averageScore(avgScore)
                .totalTimeSpentSeconds(totalTime)
                .averageTimePerSet(avgTime)
                .totalQuestionsAnswered(totalMaxScore)
                .totalCorrectAnswers(totalScore)
                .build();
    }

    @Override
    public GrammarProgressDTO getProgress(UUID exerciseSetId, UUID userId) {
        return progressRepository.findByUserIdAndExerciseSet_Id(userId, exerciseSetId)
                .map(GrammarExerciseMapper::toProgressDTO)
                .orElse(null);
    }

    @Override
    public boolean hasSubmitted(UUID exerciseSetId, UUID userId) {
        return progressRepository.existsCompletedByUserIdAndExerciseSetId(userId, exerciseSetId);
    }

    @Override
    public GrammarExerciseSetDTO getFallbackExercises(String grammarTopic, String cefrLevel) {
        List<GrammarExerciseSet> fallbacks = exerciseSetRepository
                .findFallbackByCefrLevelAndGrammarPoint(cefrLevel, grammarTopic);

        if (fallbacks.isEmpty()) {
            return null;
        }

        GrammarExerciseSet fallback = fallbacks.get(new Random().nextInt(fallbacks.size()));
        return GrammarExerciseMapper.toExerciseSetDTO(fallback);
    }

    @Override
    @Transactional
    public void resetProgress(UUID exerciseSetId, UUID userId) {
        log.info("Resetting progress for exercise set: {} by user: {}", exerciseSetId, userId);

        // Verify exercise set exists
        if (!exerciseSetRepository.existsById(exerciseSetId)) {
            throw new ResourceNotFoundException("Exercise set not found: " + exerciseSetId);
        }

        // Check if progress exists
        if (!progressRepository.existsByUserIdAndExerciseSet_Id(userId, exerciseSetId)) {
            throw new ResourceNotFoundException("No progress found for this exercise set");
        }

        // Delete the progress record
        progressRepository.deleteByUserIdAndExerciseSet_Id(userId, exerciseSetId);

        log.info("Successfully reset progress for exercise set: {} by user: {}", exerciseSetId, userId);
    }
}
