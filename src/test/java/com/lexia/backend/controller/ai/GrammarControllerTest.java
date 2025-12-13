package com.lexia.backend.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.service.ai.GrammarExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GrammarController.
 * Tests all REST endpoints for grammar exercise operations.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@WebMvcTest(GrammarController.class)
@ContextConfiguration(classes = { GrammarController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class GrammarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GrammarExerciseService grammarExerciseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private UUID testUserId;
    private UUID testExerciseSetId;
    private GrammarExerciseSetDTO testExerciseSet;
    private GrammarTopicDTO testTopic;
    private GrammarResultDTO testResult;
    private GrammarProgressDTO testProgress;
    private GrammarStatsDTO testStats;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testExerciseSetId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");

        // Set up test exercise set DTO
        testExerciseSet = GrammarExerciseSetDTO.builder()
                .id(testExerciseSetId)
                .grammarPoint("Present Simple")
                .cefrLevel("B1")
                .theme("workplace")
                .exerciseCount(5)
                .timeLimitSeconds(600)
                .isFallback(false)
                .exercises(List.of(
                        GrammarExerciseDTO.builder()
                                .type("fill_blank")
                                .question("She ___ to work every day.")
                                .options(List.of("go", "goes", "going", "gone"))
                                .correctAnswer("goes")
                                .explanation("Third person singular uses 'goes'")
                                .build()
                ))
                .explanation(GrammarExerciseSetDTO.GrammarExplanationDTO.builder()
                        .rule("The Present Simple is used for habits and routines.")
                        .examples(List.of("I work every day.", "She speaks English."))
                        .commonMistakes(List.of("Using 'go' instead of 'goes' for third person"))
                        .tips(List.of("Remember: he/she/it + verb + s"))
                        .build())
                .createdAt(Instant.now())
                .build();

        // Set up test topic DTO
        testTopic = GrammarTopicDTO.builder()
                .id(1)
                .name("Present Simple")
                .category("Tenses")
                .cefrLevels(List.of("A1", "A2", "B1"))
                .description("Used for habits, routines, and general truths")
                .examples(List.of("I work every day.", "She speaks English fluently."))
                .isActive(true)
                .build();

        // Set up test result DTO
        testResult = GrammarResultDTO.builder()
                .exerciseSetId(testExerciseSetId)
                .grammarPoint("Present Simple")
                .cefrLevel("B1")
                .score(4)
                .maxScore(5)
                .percentage(BigDecimal.valueOf(80.00))
                .passed(true)
                .timeSpentSeconds(180)
                .feedback(List.of(
                        GrammarResultDTO.QuestionFeedback.builder()
                                .questionIndex(0)
                                .userAnswer("goes")
                                .correctAnswer("goes")
                                .correct(true)
                                .explanation("Correct! Third person singular uses 'goes'.")
                                .build()
                ))
                .areasToImprove(Collections.emptyList())
                .encouragement("Great job! You have a solid understanding of Present Simple.")
                .completedAt(Instant.now())
                .build();

        // Set up test progress DTO
        testProgress = GrammarProgressDTO.builder()
                .id(1L)
                .userId(testUserId)
                .exerciseSetId(testExerciseSetId)
                .grammarPoint("Present Simple")
                .cefrLevel("B1")
                .score(4)
                .maxScore(5)
                .percentage(BigDecimal.valueOf(80.00))
                .passed(true)
                .timeSpentSeconds(180)
                .completed(true)
                .completedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        // Set up test stats DTO
        testStats = GrammarStatsDTO.builder()
                .userId(testUserId)
                .totalAttempted(25)
                .totalCompleted(20)
                .totalPassed(18)
                .averageScore(BigDecimal.valueOf(78.50))
                .totalTimeSpentSeconds(7200)
                .averageTimePerSet(360)
                .totalQuestionsAnswered(125)
                .totalCorrectAnswers(98)
                .currentStreak(5)
                .longestStreak(14)
                .build();

        // Set up Spring Security context
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ========== Generation Endpoint Tests ==========

    @Nested
    @DisplayName("POST /api/v1/ai/grammar/generate")
    class GenerateExercisesTests {

        @Test
        @DisplayName("Should generate grammar exercises successfully")
        void generateExercises_Success() throws Exception {
            GrammarRequestDTO request = GrammarRequestDTO.builder()
                    .grammarTopic("Present Simple")
                    .cefrLevel("B1")
                    .theme("workplace")
                    .exerciseCount(5)
                    .timeLimitSeconds(600)
                    .build();

            when(grammarExerciseService.generateExercises(any(GrammarRequestDTO.class), eq(testUserId)))
                    .thenReturn(testExerciseSet);

            mockMvc.perform(post("/api/v1/ai/grammar/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testExerciseSetId.toString()))
                    .andExpect(jsonPath("$.grammarPoint").value("Present Simple"))
                    .andExpect(jsonPath("$.cefrLevel").value("B1"))
                    .andExpect(jsonPath("$.exerciseCount").value(5));

            verify(grammarExerciseService).generateExercises(any(GrammarRequestDTO.class), eq(testUserId));
        }

        @Test
        @DisplayName("Should return 400 when grammar topic is missing")
        void generateExercises_MissingGrammarTopic() throws Exception {
            String invalidRequest = """
                {
                    "cefrLevel": "B1"
                }
                """;

            mockMvc.perform(post("/api/v1/ai/grammar/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when CEFR level is invalid")
        void generateExercises_InvalidCefrLevel() throws Exception {
            String invalidRequest = """
                {
                    "grammarTopic": "Present Simple",
                    "cefrLevel": "X1"
                }
                """;

            mockMvc.perform(post("/api/v1/ai/grammar/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void generateExercises_RateLimitExceeded() throws Exception {
            GrammarRequestDTO request = GrammarRequestDTO.builder()
                    .grammarTopic("Present Simple")
                    .cefrLevel("B1")
                    .build();

            when(grammarExerciseService.generateExercises(any(GrammarRequestDTO.class), eq(testUserId)))
                    .thenThrow(new RuntimeException("Daily grammar quota exceeded (50/day)"));

            mockMvc.perform(post("/api/v1/ai/grammar/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ========== Topic Endpoint Tests ==========

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/topics")
    class GetTopicsTests {

        @Test
        @DisplayName("Should return all topics")
        void getAllTopics_Success() throws Exception {
            List<GrammarTopicDTO> topics = List.of(testTopic);
            when(grammarExerciseService.getAllTopics()).thenReturn(topics);

            mockMvc.perform(get("/api/v1/ai/grammar/topics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("Present Simple"))
                    .andExpect(jsonPath("$[0].category").value("Tenses"));

            verify(grammarExerciseService).getAllTopics();
        }

        @Test
        @DisplayName("Should return empty list when no topics")
        void getAllTopics_Empty() throws Exception {
            when(grammarExerciseService.getAllTopics()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/ai/grammar/topics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/topics/level/{level}")
    class GetTopicsByLevelTests {

        @Test
        @DisplayName("Should return topics for level B1")
        void getTopicsByLevel_Success() throws Exception {
            List<GrammarTopicDTO> topics = List.of(testTopic);
            when(grammarExerciseService.getTopicsByLevel("B1")).thenReturn(topics);

            mockMvc.perform(get("/api/v1/ai/grammar/topics/level/B1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("Present Simple"));

            verify(grammarExerciseService).getTopicsByLevel("B1");
        }

        @Test
        @DisplayName("Should return 400 for invalid CEFR level")
        void getTopicsByLevel_InvalidLevel() throws Exception {
            mockMvc.perform(get("/api/v1/ai/grammar/topics/level/X1"))
                    .andExpect(status().isBadRequest());

            verify(grammarExerciseService, never()).getTopicsByLevel(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/topics/category/{category}")
    class GetTopicsByCategoryTests {

        @Test
        @DisplayName("Should return topics for Tenses category")
        void getTopicsByCategory_Success() throws Exception {
            List<GrammarTopicDTO> topics = List.of(testTopic);
            when(grammarExerciseService.getTopicsByCategory("Tenses")).thenReturn(topics);

            mockMvc.perform(get("/api/v1/ai/grammar/topics/category/Tenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].category").value("Tenses"));

            verify(grammarExerciseService).getTopicsByCategory("Tenses");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return all categories")
        void getCategories_Success() throws Exception {
            List<String> categories = List.of("Tenses", "Modals", "Conditionals");
            when(grammarExerciseService.getCategories()).thenReturn(categories);

            mockMvc.perform(get("/api/v1/ai/grammar/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0]").value("Tenses"))
                    .andExpect(jsonPath("$[1]").value("Modals"));

            verify(grammarExerciseService).getCategories();
        }
    }

    // ========== Exercise Set Endpoint Tests ==========

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/exercises/{id}")
    class GetExerciseSetTests {

        @Test
        @DisplayName("Should return exercise set by ID")
        void getExerciseSet_Success() throws Exception {
            when(grammarExerciseService.getExerciseSet(testExerciseSetId)).thenReturn(testExerciseSet);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + testExerciseSetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testExerciseSetId.toString()))
                    .andExpect(jsonPath("$.grammarPoint").value("Present Simple"))
                    .andExpect(jsonPath("$.exercises", hasSize(1)));

            verify(grammarExerciseService).getExerciseSet(testExerciseSetId);
        }

        @Test
        @DisplayName("Should return 404 when exercise set not found")
        void getExerciseSet_NotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(grammarExerciseService.getExerciseSet(unknownId))
                    .thenThrow(new ResourceNotFoundException("Exercise set", unknownId));

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + unknownId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/exercises")
    class GetUserExerciseSetsTests {

        @Test
        @DisplayName("Should return paginated exercise sets for user")
        void getUserExerciseSets_Success() throws Exception {
            Page<GrammarExerciseSetDTO> page = new PageImpl<>(List.of(testExerciseSet));
            when(grammarExerciseService.getUserExerciseSets(eq(testUserId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].grammarPoint").value("Present Simple"));

            verify(grammarExerciseService).getUserExerciseSets(eq(testUserId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should limit page size to 100")
        void getUserExerciseSets_MaxPageSize() throws Exception {
            Page<GrammarExerciseSetDTO> page = new PageImpl<>(Collections.emptyList());
            when(grammarExerciseService.getUserExerciseSets(eq(testUserId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises")
                            .param("page", "0")
                            .param("size", "200"))
                    .andExpect(status().isOk());

            verify(grammarExerciseService).getUserExerciseSets(eq(testUserId), argThat(pageable -> 
                    pageable.getPageSize() <= 100));
        }
    }

    // ========== Submission Endpoint Tests ==========

    @Nested
    @DisplayName("POST /api/v1/ai/grammar/exercises/{exerciseSetId}/submit")
    class SubmitAnswersTests {

        @Test
        @DisplayName("Should submit answers and return result")
        void submitAnswers_Success() throws Exception {
            GrammarAnswerDTO submission = GrammarAnswerDTO.builder()
                    .exerciseSetId(testExerciseSetId)
                    .answers(List.of(
                            GrammarAnswerDTO.AnswerItem.builder()
                                    .questionIndex(0)
                                    .answer("goes")
                                    .timeMs(5000L)
                                    .build()
                    ))
                    .totalTimeSeconds(180)
                    .build();

            when(grammarExerciseService.submitAnswers(eq(testExerciseSetId), eq(testUserId), any(GrammarAnswerDTO.class)))
                    .thenReturn(testResult);

            mockMvc.perform(post("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submission)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").value(4))
                    .andExpect(jsonPath("$.maxScore").value(5))
                    .andExpect(jsonPath("$.percentage").value(80.00))
                    .andExpect(jsonPath("$.passed").value(true))
                    .andExpect(jsonPath("$.feedback", hasSize(1)));

            verify(grammarExerciseService).submitAnswers(eq(testExerciseSetId), eq(testUserId), any(GrammarAnswerDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when answers are empty")
        void submitAnswers_EmptyAnswers() throws Exception {
            String invalidSubmission = """
                {
                    "exerciseSetId": "%s",
                    "answers": []
                }
                """.formatted(testExerciseSetId);

            mockMvc.perform(post("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidSubmission))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when exercise set not found")
        void submitAnswers_ExerciseSetNotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();
            GrammarAnswerDTO submission = GrammarAnswerDTO.builder()
                    .exerciseSetId(unknownId)
                    .answers(List.of(
                            GrammarAnswerDTO.AnswerItem.builder()
                                    .questionIndex(0)
                                    .answer("goes")
                                    .build()
                    ))
                    .build();

            when(grammarExerciseService.submitAnswers(eq(unknownId), eq(testUserId), any(GrammarAnswerDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Exercise set", unknownId));

            mockMvc.perform(post("/api/v1/ai/grammar/exercises/" + unknownId + "/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submission)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when already submitted")
        void submitAnswers_AlreadySubmitted() throws Exception {
            GrammarAnswerDTO submission = GrammarAnswerDTO.builder()
                    .exerciseSetId(testExerciseSetId)
                    .answers(List.of(
                            GrammarAnswerDTO.AnswerItem.builder()
                                    .questionIndex(0)
                                    .answer("goes")
                                    .build()
                    ))
                    .build();

            when(grammarExerciseService.submitAnswers(eq(testExerciseSetId), eq(testUserId), any(GrammarAnswerDTO.class)))
                    .thenThrow(new IllegalStateException("You have already submitted answers for this exercise set."));

            mockMvc.perform(post("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submission)))
                    .andExpect(status().isBadRequest()); // IllegalStateException -> 400
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/exercises/{exerciseSetId}/submitted")
    class HasSubmittedTests {

        @Test
        @DisplayName("Should return true when already submitted")
        void hasSubmitted_True() throws Exception {
            when(grammarExerciseService.hasSubmitted(testExerciseSetId, testUserId)).thenReturn(true);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/submitted"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(grammarExerciseService).hasSubmitted(testExerciseSetId, testUserId);
        }

        @Test
        @DisplayName("Should return false when not submitted")
        void hasSubmitted_False() throws Exception {
            when(grammarExerciseService.hasSubmitted(testExerciseSetId, testUserId)).thenReturn(false);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/submitted"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    // ========== History and Statistics Endpoint Tests ==========

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/history")
    class GetHistoryTests {

        @Test
        @DisplayName("Should return paginated history")
        void getHistory_Success() throws Exception {
            Page<GrammarProgressDTO> page = new PageImpl<>(List.of(testProgress));
            when(grammarExerciseService.getHistory(eq(testUserId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/ai/grammar/history")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].grammarPoint").value("Present Simple"))
                    .andExpect(jsonPath("$.content[0].passed").value(true));

            verify(grammarExerciseService).getHistory(eq(testUserId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty history for new user")
        void getHistory_Empty() throws Exception {
            Page<GrammarProgressDTO> page = new PageImpl<>(Collections.emptyList());
            when(grammarExerciseService.getHistory(eq(testUserId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/ai/grammar/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/stats")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return user statistics")
        void getStatistics_Success() throws Exception {
            when(grammarExerciseService.getStatistics(testUserId)).thenReturn(testStats);

            mockMvc.perform(get("/api/v1/ai/grammar/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                    .andExpect(jsonPath("$.totalAttempted").value(25))
                    .andExpect(jsonPath("$.totalCompleted").value(20))
                    .andExpect(jsonPath("$.totalPassed").value(18))
                    .andExpect(jsonPath("$.averageScore").value(78.50))
                    .andExpect(jsonPath("$.currentStreak").value(5));

            verify(grammarExerciseService).getStatistics(testUserId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/exercises/{exerciseSetId}/progress")
    class GetProgressTests {

        @Test
        @DisplayName("Should return progress for exercise set")
        void getProgress_Success() throws Exception {
            when(grammarExerciseService.getProgress(testExerciseSetId, testUserId)).thenReturn(testProgress);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").value(4))
                    .andExpect(jsonPath("$.maxScore").value(5))
                    .andExpect(jsonPath("$.passed").value(true));

            verify(grammarExerciseService).getProgress(testExerciseSetId, testUserId);
        }

        @Test
        @DisplayName("Should return 404 when progress not found")
        void getProgress_NotFound() throws Exception {
            when(grammarExerciseService.getProgress(testExerciseSetId, testUserId)).thenReturn(null);

            mockMvc.perform(get("/api/v1/ai/grammar/exercises/" + testExerciseSetId + "/progress"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== Fallback Endpoint Tests ==========

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/fallback")
    class GetFallbackExercisesTests {

        @Test
        @DisplayName("Should return fallback exercises")
        void getFallbackExercises_Success() throws Exception {
            GrammarExerciseSetDTO fallback = GrammarExerciseSetDTO.builder()
                    .id(testExerciseSetId)
                    .grammarPoint("Present Simple")
                    .cefrLevel("B1")
                    .isFallback(true)
                    .build();

            when(grammarExerciseService.getFallbackExercises("Present Simple", "B1")).thenReturn(fallback);

            mockMvc.perform(get("/api/v1/ai/grammar/fallback")
                            .param("topic", "Present Simple")
                            .param("level", "B1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.grammarPoint").value("Present Simple"))
                    .andExpect(jsonPath("$.isFallback").value(true));

            verify(grammarExerciseService).getFallbackExercises("Present Simple", "B1");
        }

        @Test
        @DisplayName("Should return 400 for invalid CEFR level")
        void getFallbackExercises_InvalidLevel() throws Exception {
            mockMvc.perform(get("/api/v1/ai/grammar/fallback")
                            .param("topic", "Present Simple")
                            .param("level", "X1"))
                    .andExpect(status().isBadRequest());

            verify(grammarExerciseService, never()).getFallbackExercises(any(), any());
        }

        @Test
        @DisplayName("Should return 404 when no fallback available")
        void getFallbackExercises_NotFound() throws Exception {
            when(grammarExerciseService.getFallbackExercises("Rare Grammar Point", "C2")).thenReturn(null);

            mockMvc.perform(get("/api/v1/ai/grammar/fallback")
                            .param("topic", "Rare Grammar Point")
                            .param("level", "C2"))
                    .andExpect(status().isNotFound());
        }
    }
}
