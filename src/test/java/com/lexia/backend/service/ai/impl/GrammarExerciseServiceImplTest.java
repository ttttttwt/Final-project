package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.GrammarExerciseSet;
import com.lexia.backend.entity.GrammarTopic;
import com.lexia.backend.entity.UserGrammarProgress;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.ai.AiRateLimitException;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.repository.GrammarExerciseSetRepository;
import com.lexia.backend.repository.GrammarTopicRepository;
import com.lexia.backend.repository.UserAiQuotaRepository;
import com.lexia.backend.repository.UserGrammarProgressRepository;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.service.ai.AiUsageTracker;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.service.ai.PromptTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GrammarExerciseServiceImpl.
 * Tests AI-powered grammar exercise generation, scoring, and progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GrammarExerciseServiceImpl Tests")
class GrammarExerciseServiceImplTest {

        @Mock
        private GeminiClientService geminiClientService;

        @Mock
        private PromptTemplateService promptTemplateService;

        @Mock
        private AiUsageTracker aiUsageTracker;

        @Mock
        private AIConfigService aiConfigService;

        @Mock
        private GrammarExerciseSetRepository exerciseSetRepository;

        @Mock
        private GrammarTopicRepository topicRepository;

        @Mock
        private UserGrammarProgressRepository progressRepository;

        @Mock
        private UserAiQuotaRepository userAiQuotaRepository;

        @Captor
        private ArgumentCaptor<GrammarExerciseSet> exerciseSetCaptor;

        @Captor
        private ArgumentCaptor<UserGrammarProgress> progressCaptor;

        @Captor
        private ArgumentCaptor<AiUsageTrackingRequest> trackingCaptor;

        private GrammarExerciseServiceImpl service;
        private ObjectMapper objectMapper;

        // Test data
        private static final UUID USER_ID = UUID.randomUUID();
        private static final UUID EXERCISE_SET_ID = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();

                // Mock default config behavior
                AIFeatureConfig featureConfig = new AIFeatureConfig();
                featureConfig.setEnabled(true);
                lenient().when(aiConfigService.getFeatureConfig("grammar")).thenReturn(featureConfig);

                service = new GrammarExerciseServiceImpl(
                                geminiClientService,
                                promptTemplateService,
                                aiUsageTracker,
                                exerciseSetRepository,
                                topicRepository,
                                progressRepository,
                                userAiQuotaRepository,
                                objectMapper,
                                aiConfigService);
        }

        // ========== Test Data Builders ==========

        private GrammarRequestDTO createRequest(String topic, String level) {
                return GrammarRequestDTO.builder()
                                .grammarTopic(topic)
                                .cefrLevel(level)
                                .exerciseCount(5)
                                .theme("workplace")
                                .timeLimitSeconds(600)
                                .build();
        }

        private GrammarTopic createTopic(String name) {
                return GrammarTopic.builder()
                                .id(1)
                                .name(name)
                                .category("Tenses")
                                .cefrLevels(new String[] { "A2", "B1", "B2" })
                                .description("Test topic")
                                .isActive(true)
                                .build();
        }

        private GrammarExerciseSet createExerciseSet(UUID userId) {
                Map<String, Object> content = createValidContent();
                return GrammarExerciseSet.builder()
                                .id(EXERCISE_SET_ID)
                                .userId(userId)
                                .grammarPoint("Present Simple")
                                .cefrLevel("B1")
                                .theme("workplace")
                                .content(content)
                                .exerciseCount(3)
                                .isFallback(false)
                                .createdAt(Instant.now())
                                .build();
        }

        private Map<String, Object> createValidContent() {
                Map<String, Object> content = new HashMap<>();

                Map<String, Object> explanation = new HashMap<>();
                explanation.put("rule", "Present Simple is used for habits and facts.");
                explanation.put("examples", List.of("I work every day.", "She goes to school."));
                explanation.put("commonMistakes", List.of("Using 'go' instead of 'goes' for third person."));
                content.put("explanation", explanation);

                List<Map<String, Object>> exercises = new ArrayList<>();
                exercises.add(createExercise(0, "She ___ to work every day.", "goes", "A"));
                exercises.add(createExercise(1, "They ___ English.", "speak", "B"));
                exercises.add(createExercise(2, "He ___ coffee.", "drinks", "C"));
                content.put("exercises", exercises);

                return content;
        }

        private Map<String, Object> createExercise(int index, String question, String answer, String correctOption) {
                Map<String, Object> exercise = new HashMap<>();
                exercise.put("type", "multiple_choice");
                exercise.put("instruction", "Choose the correct answer");
                exercise.put("question", question);
                exercise.put("options", List.of("goes", "speak", "drinks", "eats"));
                exercise.put("correctAnswer", correctOption);
                exercise.put("explanation", "This is the correct form for third person singular.");
                exercise.put("difficulty", "medium");
                return exercise;
        }

        private String createValidJsonResponse() {
                return """
                                {
                                  "explanation": {
                                    "rule": "Present Simple is used for habits.",
                                    "examples": ["I work."],
                                    "commonMistakes": ["Wrong form"]
                                  },
                                  "exercises": [
                                    {
                                      "type": "multiple_choice",
                                      "instruction": "Choose",
                                      "question": "She ___ to work.",
                                      "options": ["goes", "go", "going", "went"],
                                      "correctAnswer": "goes",
                                      "explanation": "Third person singular",
                                      "difficulty": "easy"
                                    }
                                  ]
                                }
                                """;
        }

        private GeminiResponseDTO createSuccessfulResponse() {
                return GeminiResponseDTO.success(
                                createValidJsonResponse(),
                                "gemini-2.0-flash-exp",
                                TokenUsageDTO.of(100, 200, 0.001),
                                500L,
                                "STOP");
        }

        private GrammarAnswerDTO createSubmission(List<GrammarAnswerDTO.AnswerItem> answers) {
                return GrammarAnswerDTO.builder()
                                .exerciseSetId(EXERCISE_SET_ID)
                                .answers(answers)
                                .totalTimeSeconds(120)
                                .build();
        }

        // ========== Generate Exercises Tests ==========

        @Nested
        @DisplayName("generateExercises()")
        class GenerateExercisesTests {

                @Test
                @DisplayName("Should throw exception when feature is disabled")
                void shouldThrowWhenFeatureIsDisabled() {
                        // Given
                        GrammarRequestDTO request = createRequest("Present Simple", "B1");

                        AIFeatureConfig disabledConfig = new AIFeatureConfig();
                        disabledConfig.setEnabled(false);
                        when(aiConfigService.getFeatureConfig("grammar")).thenReturn(disabledConfig);

                        // When & Then
                        assertThatThrownBy(() -> service.generateExercises(request, USER_ID))
                                        .isInstanceOf(AiServiceException.class)
                                        .hasMessageContaining("disabled");

                        verify(geminiClientService, never()).generateStructuredContent(anyString());
                }

                @Test
                @DisplayName("Should generate exercises successfully with AI")
                void shouldGenerateExercisesWithAI() {
                        // Given
                        GrammarRequestDTO request = createRequest("Present Simple", "B1");
                        GrammarTopic topic = createTopic("Present Simple");
                        GrammarExerciseSet savedSet = createExerciseSet(USER_ID);

                        when(topicRepository.findByNameIgnoreCase("Present Simple"))
                                        .thenReturn(Optional.of(topic));
                        when(aiUsageTracker.getDailyUsageCount(USER_ID, AiUsageTracker.CONTENT_TYPE_GRAMMAR))
                                        .thenReturn(5L);
                        when(promptTemplateService.getAndResolve(anyString(), anyMap()))
                                        .thenReturn(Optional.of("Generated prompt"));
                        when(geminiClientService.generateStructuredContent(anyString()))
                                        .thenReturn(createSuccessfulResponse());
                        when(exerciseSetRepository.save(any(GrammarExerciseSet.class)))
                                        .thenReturn(savedSet);
                        when(aiUsageTracker.trackUsageAsync(any()))
                                        .thenReturn(CompletableFuture.completedFuture(null));

                        // When
                        GrammarExerciseSetDTO result = service.generateExercises(request, USER_ID);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getGrammarPoint()).isEqualTo("Present Simple");
                        assertThat(result.getCefrLevel()).isEqualTo("B1");

                        verify(exerciseSetRepository).save(exerciseSetCaptor.capture());
                        GrammarExerciseSet captured = exerciseSetCaptor.getValue();
                        assertThat(captured.getUserId()).isEqualTo(USER_ID);
                        assertThat(captured.getGrammarPoint()).isEqualTo("Present Simple");

                        verify(aiUsageTracker).trackUsageAsync(any());
                }

                @Test
                @DisplayName("Should use fallback when explicitly requested")
                void shouldUseFallbackWhenRequested() {
                        // Given
                        GrammarRequestDTO request = GrammarRequestDTO.builder()
                                        .grammarTopic("Present Simple")
                                        .cefrLevel("B1")
                                        .useFallback(true)
                                        .exerciseCount(5)
                                        .build();

                        GrammarTopic topic = createTopic("Present Simple");
                        GrammarExerciseSet fallbackSet = createExerciseSet(null);
                        fallbackSet.setIsFallback(true);

                        when(topicRepository.findByNameIgnoreCase("Present Simple"))
                                        .thenReturn(Optional.of(topic));
                        when(exerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint("B1", "Present Simple"))
                                        .thenReturn(List.of(fallbackSet));

                        // When
                        GrammarExerciseSetDTO result = service.generateExercises(request, USER_ID);

                        // Then
                        assertThat(result).isNotNull();
                        verify(geminiClientService, never()).generateStructuredContent(anyString());
                }

                @Test
                @DisplayName("Should throw AiRateLimitException when quota exceeded")
                void shouldThrowWhenQuotaExceeded() {
                        // Given
                        GrammarRequestDTO request = createRequest("Present Simple", "B1");

                        when(topicRepository.findByNameIgnoreCase("Present Simple"))
                                        .thenReturn(Optional.empty());
                        when(aiUsageTracker.getDailyUsageCount(USER_ID, AiUsageTracker.CONTENT_TYPE_GRAMMAR))
                                        .thenReturn(51L); // Over quota

                        // When/Then
                        assertThatThrownBy(() -> service.generateExercises(request, USER_ID))
                                        .isInstanceOf(AiRateLimitException.class)
                                        .hasMessageContaining("Daily grammar exercise limit exceeded");
                }

                @Test
                @DisplayName("Should use fallback when AI fails")
                void shouldUseFallbackWhenAIFails() {
                        // Given
                        GrammarRequestDTO request = createRequest("Present Simple", "B1");
                        GrammarTopic topic = createTopic("Present Simple");
                        GrammarExerciseSet fallbackSet = createExerciseSet(null);
                        fallbackSet.setIsFallback(true);

                        when(topicRepository.findByNameIgnoreCase("Present Simple"))
                                        .thenReturn(Optional.of(topic));
                        when(aiUsageTracker.getDailyUsageCount(USER_ID, AiUsageTracker.CONTENT_TYPE_GRAMMAR))
                                        .thenReturn(5L);
                        when(promptTemplateService.getAndResolve(anyString(), anyMap()))
                                        .thenReturn(Optional.of("Prompt"));
                        when(geminiClientService.generateStructuredContent(anyString()))
                                        .thenThrow(new AiServiceException("API unavailable"));
                        when(exerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint("B1", "Present Simple"))
                                        .thenReturn(List.of(fallbackSet));

                        // When
                        GrammarExerciseSetDTO result = service.generateExercises(request, USER_ID);

                        // Then
                        assertThat(result).isNotNull();
                }

                @Test
                @DisplayName("Should create basic fallback when no fallback content exists")
                void shouldCreateBasicFallbackWhenNoneExists() {
                        // Given
                        GrammarRequestDTO request = createRequest("Present Simple", "B1");
                        GrammarTopic topic = createTopic("Present Simple");
                        GrammarExerciseSet savedSet = createExerciseSet(USER_ID);
                        savedSet.setIsFallback(true);

                        when(topicRepository.findByNameIgnoreCase("Present Simple"))
                                        .thenReturn(Optional.of(topic));
                        when(aiUsageTracker.getDailyUsageCount(USER_ID, AiUsageTracker.CONTENT_TYPE_GRAMMAR))
                                        .thenReturn(5L);
                        when(promptTemplateService.getAndResolve(anyString(), anyMap()))
                                        .thenReturn(Optional.of("Prompt"));
                        when(geminiClientService.generateStructuredContent(anyString()))
                                        .thenThrow(new AiServiceException("API unavailable"));
                        when(exerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint("B1", "Present Simple"))
                                        .thenReturn(Collections.emptyList());
                        when(exerciseSetRepository.save(any(GrammarExerciseSet.class)))
                                        .thenReturn(savedSet);

                        // When
                        GrammarExerciseSetDTO result = service.generateExercises(request, USER_ID);

                        // Then
                        assertThat(result).isNotNull();
                        verify(exerciseSetRepository).save(exerciseSetCaptor.capture());
                        assertThat(exerciseSetCaptor.getValue().getIsFallback()).isTrue();
                }
        }

        // ========== Topic Listing Tests ==========

        @Nested
        @DisplayName("Topic Listing")
        class TopicListingTests {

                @Test
                @DisplayName("Should get all active topics")
                void shouldGetAllTopics() {
                        // Given
                        List<GrammarTopic> topics = List.of(
                                        createTopic("Present Simple"),
                                        createTopic("Past Simple"));
                        when(topicRepository.findByIsActiveTrue()).thenReturn(topics);

                        // When
                        List<GrammarTopicDTO> result = service.getAllTopics();

                        // Then
                        assertThat(result).hasSize(2);
                        assertThat(result.get(0).name()).isEqualTo("Present Simple");
                }

                @Test
                @DisplayName("Should get topics by CEFR level")
                void shouldGetTopicsByLevel() {
                        // Given
                        GrammarTopic topic = createTopic("Present Simple");
                        when(topicRepository.findByCefrLevel("B1")).thenReturn(List.of(topic));

                        // When
                        List<GrammarTopicDTO> result = service.getTopicsByLevel("B1");

                        // Then
                        assertThat(result).hasSize(1);
                        assertThat(result.get(0).name()).isEqualTo("Present Simple");
                }

                @Test
                @DisplayName("Should get topics by category")
                void shouldGetTopicsByCategory() {
                        // Given
                        GrammarTopic topic = createTopic("Present Simple");
                        when(topicRepository.findByCategoryAndIsActiveTrue("Tenses")).thenReturn(List.of(topic));

                        // When
                        List<GrammarTopicDTO> result = service.getTopicsByCategory("Tenses");

                        // Then
                        assertThat(result).hasSize(1);
                        assertThat(result.get(0).category()).isEqualTo("Tenses");
                }

                @Test
                @DisplayName("Should get all categories")
                void shouldGetCategories() {
                        // Given
                        List<String> categories = List.of("Tenses", "Modals", "Articles");
                        when(topicRepository.findDistinctActiveCategories()).thenReturn(categories);

                        // When
                        List<String> result = service.getCategories();

                        // Then
                        assertThat(result).containsExactly("Tenses", "Modals", "Articles");
                }
        }

        // ========== Exercise Set Retrieval Tests ==========

        @Nested
        @DisplayName("Exercise Set Retrieval")
        class ExerciseSetRetrievalTests {

                @Test
                @DisplayName("Should get exercise set by ID")
                void shouldGetExerciseSetById() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));

                        // When
                        GrammarExerciseSetDTO result = service.getExerciseSet(EXERCISE_SET_ID);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getId()).isEqualTo(EXERCISE_SET_ID);
                }

                @Test
                @DisplayName("Should throw ResourceNotFoundException for missing exercise set")
                void shouldThrowForMissingExerciseSet() {
                        // Given
                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());

                        // When/Then
                        assertThatThrownBy(() -> service.getExerciseSet(EXERCISE_SET_ID))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Exercise set not found");
                }

                @Test
                @DisplayName("Should get user exercise sets with pagination")
                void shouldGetUserExerciseSets() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        Page<GrammarExerciseSet> page = new PageImpl<>(List.of(exerciseSet));
                        Pageable pageable = PageRequest.of(0, 10);

                        when(exerciseSetRepository.findByUserId(USER_ID, pageable)).thenReturn(page);

                        // When
                        Page<GrammarExerciseSetDTO> result = service.getUserExerciseSets(USER_ID, pageable);

                        // Then
                        assertThat(result.getContent()).hasSize(1);
                        assertThat(result.getContent().get(0).getId()).isEqualTo(EXERCISE_SET_ID);
                }
        }

        // ========== Answer Submission Tests ==========

        @Nested
        @DisplayName("submitAnswers()")
        class SubmitAnswersTests {

                @Test
                @DisplayName("Should score all correct answers")
                void shouldScoreAllCorrectAnswers() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(1).answer("B").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(2).answer("C").timeMs(1000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        GrammarResultDTO result = service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then
                        assertThat(result.getScore()).isEqualTo(3);
                        assertThat(result.getMaxScore()).isEqualTo(3);
                        assertThat(result.getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                        assertThat(result.getPassed()).isTrue();
                        assertThat(result.getFeedback()).hasSize(3);
                        assertThat(result.getFeedback()).allMatch(GrammarResultDTO.QuestionFeedback::getCorrect);
                }

                @Test
                @DisplayName("Should score partial correct answers")
                void shouldScorePartialCorrectAnswers() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(1).answer("D").timeMs(1000L)
                                                        .build(), // Wrong
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(2).answer("C").timeMs(1000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        GrammarResultDTO result = service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then
                        assertThat(result.getScore()).isEqualTo(2);
                        assertThat(result.getMaxScore()).isEqualTo(3);
                        assertThat(result.getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(66.67));
                        assertThat(result.getPassed()).isFalse(); // Below 70%
                }

                @Test
                @DisplayName("Should throw IllegalStateException when already submitted")
                void shouldThrowWhenAlreadySubmitted() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        UserGrammarProgress existingProgress = UserGrammarProgress.builder()
                                        .userId(USER_ID)
                                        .exerciseSet(exerciseSet)
                                        .completedAt(Instant.now())
                                        .build();
                        GrammarAnswerDTO submission = createSubmission(List.of());

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(existingProgress));

                        // When/Then
                        assertThatThrownBy(() -> service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission))
                                        .isInstanceOf(IllegalStateException.class)
                                        .hasMessageContaining("already submitted");
                }

                @Test
                @DisplayName("Should throw ResourceNotFoundException for missing exercise set")
                void shouldThrowForMissingExerciseSetOnSubmit() {
                        // Given
                        GrammarAnswerDTO submission = createSubmission(List.of());

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());

                        // When/Then
                        assertThatThrownBy(() -> service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw IllegalArgumentException when accessing another user's exercise set")
                void shouldThrowWhenAccessingOtherUsersExerciseSet() {
                        // Given
                        UUID otherUserId = UUID.randomUUID();
                        GrammarExerciseSet exerciseSet = createExerciseSet(otherUserId); // Different owner
                        GrammarAnswerDTO submission = createSubmission(List.of());

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));

                        // When/Then
                        assertThatThrownBy(() -> service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("do not have permission");
                }

                @Test
                @DisplayName("Should allow submitting to public/fallback exercise sets (null userId)")
                void shouldAllowSubmittingToPublicExerciseSets() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(null); // Public/fallback set
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(1000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        GrammarResultDTO result = service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then
                        assertThat(result).isNotNull();
                        verify(progressRepository).save(any(UserGrammarProgress.class));
                }

                @Test
                @DisplayName("Should handle out-of-order answer submissions")
                void shouldHandleOutOfOrderAnswers() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        // Submit answers in reverse order using questionIndex
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(2).answer("C").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(1).answer("B").timeMs(1000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        GrammarResultDTO result = service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then - All answers should be correct even though submitted out of order
                        assertThat(result.getScore()).isEqualTo(3);
                        assertThat(result.getMaxScore()).isEqualTo(3);
                        assertThat(result.getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                }

                @Test
                @DisplayName("Should save progress correctly")
                void shouldSaveProgressCorrectly() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(5000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then
                        verify(progressRepository).save(progressCaptor.capture());
                        UserGrammarProgress savedProgress = progressCaptor.getValue();
                        assertThat(savedProgress.getUserId()).isEqualTo(USER_ID);
                        assertThat(savedProgress.getScore()).isEqualTo(1);
                        assertThat(savedProgress.getCompletedAt()).isNotNull();
                }

                @Test
                @DisplayName("Should generate encouraging feedback for high scores")
                void shouldGenerateEncouragingFeedback() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        List<GrammarAnswerDTO.AnswerItem> answers = List.of(
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(0).answer("A").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(1).answer("B").timeMs(1000L)
                                                        .build(),
                                        GrammarAnswerDTO.AnswerItem.builder().questionIndex(2).answer("C").timeMs(1000L)
                                                        .build());
                        GrammarAnswerDTO submission = createSubmission(answers);

                        when(exerciseSetRepository.findById(EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(exerciseSet));
                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.empty());
                        when(progressRepository.save(any(UserGrammarProgress.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // When
                        GrammarResultDTO result = service.submitAnswers(EXERCISE_SET_ID, USER_ID, submission);

                        // Then
                        assertThat(result.getEncouragement()).contains("Excellent");
                }
        }

        // ========== History and Statistics Tests ==========

        @Nested
        @DisplayName("History and Statistics")
        class HistoryAndStatisticsTests {

                @Test
                @DisplayName("Should get user history with pagination")
                void shouldGetHistory() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        UserGrammarProgress progress = UserGrammarProgress.builder()
                                        .id(1L)
                                        .userId(USER_ID)
                                        .exerciseSet(exerciseSet)
                                        .score(4)
                                        .maxScore(5)
                                        .percentage(BigDecimal.valueOf(80))
                                        .completedAt(Instant.now())
                                        .build();

                        Page<UserGrammarProgress> page = new PageImpl<>(List.of(progress));
                        Pageable pageable = PageRequest.of(0, 10);

                        when(progressRepository.findByUserId(USER_ID, pageable)).thenReturn(page);

                        // When
                        Page<GrammarProgressDTO> result = service.getHistory(USER_ID, pageable);

                        // Then
                        assertThat(result.getContent()).hasSize(1);
                        assertThat(result.getContent().get(0).getScore()).isEqualTo(4);
                }

                @Test
                @DisplayName("Should calculate statistics correctly")
                void shouldCalculateStatistics() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        List<UserGrammarProgress> progressList = List.of(
                                        UserGrammarProgress.builder()
                                                        .userId(USER_ID)
                                                        .exerciseSet(exerciseSet)
                                                        .score(4)
                                                        .maxScore(5)
                                                        .percentage(BigDecimal.valueOf(80))
                                                        .timeSpentSeconds(120)
                                                        .completedAt(Instant.now())
                                                        .build(),
                                        UserGrammarProgress.builder()
                                                        .userId(USER_ID)
                                                        .exerciseSet(exerciseSet)
                                                        .score(3)
                                                        .maxScore(5)
                                                        .percentage(BigDecimal.valueOf(60))
                                                        .timeSpentSeconds(180)
                                                        .completedAt(Instant.now())
                                                        .build());

                        when(progressRepository.findByUserIdAndCompletedAtIsNotNull(USER_ID))
                                        .thenReturn(progressList);

                        // When
                        GrammarStatsDTO result = service.getStatistics(USER_ID);

                        // Then
                        assertThat(result.getTotalCompleted()).isEqualTo(2);
                        assertThat(result.getTotalPassed()).isEqualTo(1); // Only 80% passed
                        assertThat(result.getTotalTimeSpentSeconds()).isEqualTo(300);
                        assertThat(result.getTotalQuestionsAnswered()).isEqualTo(10);
                        assertThat(result.getTotalCorrectAnswers()).isEqualTo(7);
                }

                @Test
                @DisplayName("Should return empty stats for user with no progress")
                void shouldReturnEmptyStatsForNewUser() {
                        // Given
                        when(progressRepository.findByUserIdAndCompletedAtIsNotNull(USER_ID))
                                        .thenReturn(Collections.emptyList());

                        // When
                        GrammarStatsDTO result = service.getStatistics(USER_ID);

                        // Then
                        assertThat(result.getTotalCompleted()).isEqualTo(0);
                        assertThat(result.getAverageScore()).isEqualByComparingTo(BigDecimal.ZERO);
                }

                @Test
                @DisplayName("Should get progress for specific exercise set")
                void shouldGetProgress() {
                        // Given
                        GrammarExerciseSet exerciseSet = createExerciseSet(USER_ID);
                        UserGrammarProgress progress = UserGrammarProgress.builder()
                                        .userId(USER_ID)
                                        .exerciseSet(exerciseSet)
                                        .score(4)
                                        .maxScore(5)
                                        .completedAt(Instant.now())
                                        .build();

                        when(progressRepository.findByUserIdAndExerciseSet_Id(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(Optional.of(progress));

                        // When
                        GrammarProgressDTO result = service.getProgress(EXERCISE_SET_ID, USER_ID);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getScore()).isEqualTo(4);
                }

                @Test
                @DisplayName("Should check if user has submitted")
                void shouldCheckIfSubmitted() {
                        // Given
                        when(progressRepository.existsCompletedByUserIdAndExerciseSetId(USER_ID, EXERCISE_SET_ID))
                                        .thenReturn(true);

                        // When
                        boolean result = service.hasSubmitted(EXERCISE_SET_ID, USER_ID);

                        // Then
                        assertThat(result).isTrue();
                }
        }

        // ========== Fallback Content Tests ==========

        @Nested
        @DisplayName("Fallback Content")
        class FallbackContentTests {

                @Test
                @DisplayName("Should get fallback exercises when available")
                void shouldGetFallbackExercises() {
                        // Given
                        GrammarExerciseSet fallbackSet = createExerciseSet(null);
                        fallbackSet.setIsFallback(true);

                        when(exerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint("B1", "Present Simple"))
                                        .thenReturn(List.of(fallbackSet));

                        // When
                        GrammarExerciseSetDTO result = service.getFallbackExercises("Present Simple", "B1");

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getIsFallback()).isTrue();
                }

                @Test
                @DisplayName("Should return null when no fallback available")
                void shouldReturnNullWhenNoFallback() {
                        // Given
                        when(exerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint("C2", "Subjunctive"))
                                        .thenReturn(Collections.emptyList());

                        // When
                        GrammarExerciseSetDTO result = service.getFallbackExercises("Subjunctive", "C2");

                        // Then
                        assertThat(result).isNull();
                }
        }
}
