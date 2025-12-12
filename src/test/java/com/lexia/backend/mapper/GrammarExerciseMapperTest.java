package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.GrammarExerciseSet;
import com.lexia.backend.entity.GrammarTopic;
import com.lexia.backend.entity.UserGrammarProgress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GrammarExerciseMapper.
 * Verifies correct mapping between Grammar entities and DTOs.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@DisplayName("GrammarExerciseMapper Unit Tests")
class GrammarExerciseMapperTest {

    // ========== GrammarTopic Mapping Tests ==========

    @Nested
    @DisplayName("GrammarTopic to GrammarTopicDTO")
    class GrammarTopicMappingTests {

        @Test
        @DisplayName("Should convert GrammarTopic entity to DTO with all fields")
        void testToTopicDTO_WithAllFields_Success() {
            // Arrange
            GrammarTopic topic = GrammarTopic.builder()
                    .id(1)
                    .name("Present Simple")
                    .category("Tenses")
                    .cefrLevels(new String[]{"A1", "A2"})
                    .description("Used for habits, routines, and general truths")
                    .examples(new String[]{"I work every day.", "She speaks English."})
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            // Act
            GrammarTopicDTO dto = GrammarExerciseMapper.toTopicDTO(topic);

            // Assert
            assertNotNull(dto);
            assertEquals(1, dto.id());
            assertEquals("Present Simple", dto.name());
            assertEquals("Tenses", dto.category());
            assertEquals(Arrays.asList("A1", "A2"), dto.cefrLevels());
            assertEquals("Used for habits, routines, and general truths", dto.description());
            assertEquals(Arrays.asList("I work every day.", "She speaks English."), dto.examples());
            assertTrue(dto.isActive());
        }

        @Test
        @DisplayName("Should return null when converting null GrammarTopic")
        void testToTopicDTO_WithNullEntity_ReturnsNull() {
            // Act
            GrammarTopicDTO dto = GrammarExerciseMapper.toTopicDTO(null);

            // Assert
            assertNull(dto);
        }

        @Test
        @DisplayName("Should handle topic with null arrays")
        void testToTopicDTO_WithNullArrays_Success() {
            // Arrange
            GrammarTopic topic = GrammarTopic.builder()
                    .id(1)
                    .name("Test Topic")
                    .category("Test")
                    .cefrLevels(null)
                    .examples(null)
                    .isActive(true)
                    .build();

            // Act
            GrammarTopicDTO dto = GrammarExerciseMapper.toTopicDTO(topic);

            // Assert
            assertNotNull(dto);
            assertTrue(dto.cefrLevels().isEmpty());
            assertTrue(dto.examples().isEmpty());
        }

        @Test
        @DisplayName("Should convert list of GrammarTopic entities to DTOs")
        void testToTopicDTOList_Success() {
            // Arrange
            List<GrammarTopic> topics = Arrays.asList(
                    GrammarTopic.builder()
                            .id(1)
                            .name("Present Simple")
                            .category("Tenses")
                            .cefrLevels(new String[]{"A1"})
                            .isActive(true)
                            .build(),
                    GrammarTopic.builder()
                            .id(2)
                            .name("Past Simple")
                            .category("Tenses")
                            .cefrLevels(new String[]{"A2"})
                            .isActive(true)
                            .build()
            );

            // Act
            List<GrammarTopicDTO> dtos = GrammarExerciseMapper.toTopicDTOList(topics);

            // Assert
            assertNotNull(dtos);
            assertEquals(2, dtos.size());
            assertEquals("Present Simple", dtos.get(0).name());
            assertEquals("Past Simple", dtos.get(1).name());
        }

        @Test
        @DisplayName("Should return empty list for null input")
        void testToTopicDTOList_WithNullInput_ReturnsEmptyList() {
            // Act
            List<GrammarTopicDTO> dtos = GrammarExerciseMapper.toTopicDTOList(null);

            // Assert
            assertNotNull(dtos);
            assertTrue(dtos.isEmpty());
        }
    }

    // ========== GrammarExerciseSet Mapping Tests ==========

    @Nested
    @DisplayName("GrammarExerciseSet to GrammarExerciseSetDTO")
    class GrammarExerciseSetMappingTests {

        @Test
        @DisplayName("Should convert GrammarExerciseSet entity to DTO with all fields")
        void testToExerciseSetDTO_WithAllFields_Success() {
            // Arrange
            Map<String, Object> content = createSampleContent();
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(id)
                    .grammarPoint("Present Simple")
                    .cefrLevel("B1")
                    .theme("workplace")
                    .content(content)
                    .exerciseCount(5)
                    .timeLimitSeconds(600)
                    .isFallback(false)
                    .createdAt(now)
                    .build();

            // Act
            GrammarExerciseSetDTO dto = GrammarExerciseMapper.toExerciseSetDTO(exerciseSet);

            // Assert
            assertNotNull(dto);
            assertEquals(id, dto.getId());
            assertEquals("Present Simple", dto.getGrammarPoint());
            assertEquals("B1", dto.getCefrLevel());
            assertEquals("workplace", dto.getTheme());
            assertEquals(5, dto.getExerciseCount());
            assertEquals(600, dto.getTimeLimitSeconds());
            assertFalse(dto.getIsFallback());
            assertEquals(now, dto.getCreatedAt());
            
            // Verify explanation was parsed
            assertNotNull(dto.getExplanation());
            assertEquals("Present Simple is used for habits and routines.", dto.getExplanation().getRule());
            
            // Verify exercises were parsed
            assertNotNull(dto.getExercises());
            assertEquals(1, dto.getExercises().size());
            assertEquals("multiple_choice", dto.getExercises().get(0).type());
        }

        @Test
        @DisplayName("Should return null when converting null GrammarExerciseSet")
        void testToExerciseSetDTO_WithNullEntity_ReturnsNull() {
            // Act
            GrammarExerciseSetDTO dto = GrammarExerciseMapper.toExerciseSetDTO(null);

            // Assert
            assertNull(dto);
        }

        @Test
        @DisplayName("Should handle exercise set with null content")
        void testToExerciseSetDTO_WithNullContent_Success() {
            // Arrange
            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(UUID.randomUUID())
                    .grammarPoint("Present Simple")
                    .cefrLevel("A1")
                    .content(null)
                    .build();

            // Act
            GrammarExerciseSetDTO dto = GrammarExerciseMapper.toExerciseSetDTO(exerciseSet);

            // Assert
            assertNotNull(dto);
            assertNull(dto.getExplanation());
            assertNull(dto.getExercises());
        }

        @Test
        @DisplayName("Should create GrammarExerciseSet entity from request DTO")
        void testToExerciseSetEntity_Success() {
            // Arrange
            GrammarRequestDTO request = GrammarRequestDTO.builder()
                    .grammarTopic("Present Simple")
                    .cefrLevel("B1")
                    .theme("workplace")
                    .exerciseCount(5)
                    .timeLimitSeconds(600)
                    .useFallback(false)
                    .build();

            Map<String, Object> content = createSampleContent();
            UUID userId = UUID.randomUUID();

            // Act
            GrammarExerciseSet entity = GrammarExerciseMapper.toExerciseSetEntity(
                    request, content, userId, null);

            // Assert
            assertNotNull(entity);
            assertEquals("Present Simple", entity.getGrammarPoint());
            assertEquals("B1", entity.getCefrLevel());
            assertEquals("workplace", entity.getTheme());
            assertEquals(600, entity.getTimeLimitSeconds());
            assertFalse(entity.getIsFallback());
            assertEquals(userId, entity.getUserId());
            assertEquals(1, entity.getExerciseCount()); // Parsed from content
        }

        @Test
        @DisplayName("Should throw exception when request DTO is null")
        void testToExerciseSetEntity_WithNullRequest_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                    () -> GrammarExerciseMapper.toExerciseSetEntity(null, null, null, null));
        }
    }

    // ========== UserGrammarProgress Mapping Tests ==========

    @Nested
    @DisplayName("UserGrammarProgress to GrammarProgressDTO")
    class UserGrammarProgressMappingTests {

        @Test
        @DisplayName("Should convert UserGrammarProgress entity to DTO")
        void testToProgressDTO_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID exerciseSetId = UUID.randomUUID();
            Instant now = Instant.now();

            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(exerciseSetId)
                    .grammarPoint("Present Simple")
                    .cefrLevel("B1")
                    .build();

            UserGrammarProgress progress = UserGrammarProgress.builder()
                    .id(1L)
                    .userId(userId)
                    .exerciseSet(exerciseSet)
                    .score(4)
                    .maxScore(5)
                    .percentage(new BigDecimal("80.00"))
                    .timeSpentSeconds(180)
                    .completedAt(now)
                    .createdAt(now)
                    .build();

            // Act
            GrammarProgressDTO dto = GrammarExerciseMapper.toProgressDTO(progress);

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals(userId, dto.getUserId());
            assertEquals(exerciseSetId, dto.getExerciseSetId());
            assertEquals("Present Simple", dto.getGrammarPoint());
            assertEquals("B1", dto.getCefrLevel());
            assertEquals(4, dto.getScore());
            assertEquals(5, dto.getMaxScore());
            assertEquals(new BigDecimal("80.00"), dto.getPercentage());
            assertTrue(dto.getPassed());
            assertEquals(180, dto.getTimeSpentSeconds());
            assertTrue(dto.getCompleted());
        }

        @Test
        @DisplayName("Should return null when converting null progress")
        void testToProgressDTO_WithNullEntity_ReturnsNull() {
            // Act
            GrammarProgressDTO dto = GrammarExerciseMapper.toProgressDTO(null);

            // Assert
            assertNull(dto);
        }

        @Test
        @DisplayName("Should create UserGrammarProgress entity from answer DTO")
        void testToProgressEntity_Success() {
            // Arrange
            UUID exerciseSetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(exerciseSetId)
                    .grammarPoint("Present Simple")
                    .cefrLevel("B1")
                    .build();

            List<GrammarAnswerDTO.AnswerItem> answers = Arrays.asList(
                    GrammarAnswerDTO.AnswerItem.builder()
                            .questionIndex(0)
                            .answer("goes")
                            .timeMs(5000L)
                            .build(),
                    GrammarAnswerDTO.AnswerItem.builder()
                            .questionIndex(1)
                            .answer("is working")
                            .timeMs(8000L)
                            .build()
            );

            GrammarAnswerDTO answerDTO = GrammarAnswerDTO.builder()
                    .exerciseSetId(exerciseSetId)
                    .answers(answers)
                    .totalTimeSeconds(180)
                    .build();

            // Act
            UserGrammarProgress entity = GrammarExerciseMapper.toProgressEntity(
                    answerDTO, exerciseSet, userId);

            // Assert
            assertNotNull(entity);
            assertEquals(userId, entity.getUserId());
            assertEquals(exerciseSet, entity.getExerciseSet());
            assertEquals(180, entity.getTimeSpentSeconds());
            assertNotNull(entity.getAnswers());
            assertEquals(2, entity.getAnswers().size());
        }

        @Test
        @DisplayName("Should throw exception when required parameters are null")
        void testToProgressEntity_WithNullParams_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                    () -> GrammarExerciseMapper.toProgressEntity(null, null, null));
        }
    }

    // ========== GrammarResult Mapping Tests ==========

    @Nested
    @DisplayName("GrammarResult Mapping")
    class GrammarResultMappingTests {

        @Test
        @DisplayName("Should create GrammarResultDTO from progress with passing score")
        void testToResultDTO_WithPassingScore_Success() {
            // Arrange
            UUID exerciseSetId = UUID.randomUUID();
            Instant now = Instant.now();

            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(exerciseSetId)
                    .grammarPoint("Present Simple")
                    .cefrLevel("B1")
                    .build();

            UserGrammarProgress progress = UserGrammarProgress.builder()
                    .id(1L)
                    .userId(UUID.randomUUID())
                    .exerciseSet(exerciseSet)
                    .score(4)
                    .maxScore(5)
                    .percentage(new BigDecimal("80.00"))
                    .timeSpentSeconds(180)
                    .completedAt(now)
                    .build();

            List<GrammarResultDTO.QuestionFeedback> feedback = Arrays.asList(
                    GrammarResultDTO.QuestionFeedback.builder()
                            .questionIndex(0)
                            .userAnswer("goes")
                            .correctAnswer("goes")
                            .correct(true)
                            .explanation("Correct!")
                            .build()
            );

            // Act
            GrammarResultDTO result = GrammarExerciseMapper.toResultDTO(
                    progress, feedback, Collections.emptyList());

            // Assert
            assertNotNull(result);
            assertEquals(exerciseSetId, result.getExerciseSetId());
            assertEquals("Present Simple", result.getGrammarPoint());
            assertEquals("B1", result.getCefrLevel());
            assertEquals(4, result.getScore());
            assertEquals(5, result.getMaxScore());
            assertEquals(new BigDecimal("80.00"), result.getPercentage());
            assertTrue(result.getPassed());
            assertNotNull(result.getEncouragement());
            assertTrue(result.getAreasToImprove().isEmpty());
        }

        @Test
        @DisplayName("Should create GrammarResultDTO from progress with failing score")
        void testToResultDTO_WithFailingScore_Success() {
            // Arrange
            UUID exerciseSetId = UUID.randomUUID();

            GrammarExerciseSet exerciseSet = GrammarExerciseSet.builder()
                    .id(exerciseSetId)
                    .grammarPoint("Past Perfect")
                    .cefrLevel("B2")
                    .build();

            UserGrammarProgress progress = UserGrammarProgress.builder()
                    .id(1L)
                    .userId(UUID.randomUUID())
                    .exerciseSet(exerciseSet)
                    .score(2)
                    .maxScore(5)
                    .percentage(new BigDecimal("40.00"))
                    .timeSpentSeconds(200)
                    .completedAt(Instant.now())
                    .build();

            List<String> areasToImprove = Arrays.asList(
                    "Review past perfect formation",
                    "Practice with timeline exercises"
            );

            // Act
            GrammarResultDTO result = GrammarExerciseMapper.toResultDTO(
                    progress, Collections.emptyList(), areasToImprove);

            // Assert
            assertNotNull(result);
            assertFalse(result.getPassed());
            assertEquals(2, result.getAreasToImprove().size());
            assertNotNull(result.getEncouragement());
        }
    }

    // ========== Content Parsing Tests ==========

    @Nested
    @DisplayName("Content Parsing Helper Methods")
    class ContentParsingTests {

        @Test
        @DisplayName("Should extract exercises from content map")
        void testExtractExercisesFromContent_Success() {
            // Arrange
            Map<String, Object> content = createSampleContent();

            // Act
            List<Map<String, Object>> exercises = 
                    GrammarExerciseMapper.extractExercisesFromContent(content);

            // Assert
            assertNotNull(exercises);
            assertEquals(1, exercises.size());
            assertEquals("multiple_choice", exercises.get(0).get("type"));
        }

        @Test
        @DisplayName("Should return empty list for null content")
        void testExtractExercisesFromContent_WithNullContent_ReturnsEmptyList() {
            // Act
            List<Map<String, Object>> exercises = 
                    GrammarExerciseMapper.extractExercisesFromContent(null);

            // Assert
            assertNotNull(exercises);
            assertTrue(exercises.isEmpty());
        }

        @Test
        @DisplayName("Should create content map from explanation and exercises")
        void testCreateContentMap_Success() {
            // Arrange
            GrammarExerciseSetDTO.GrammarExplanationDTO explanation = 
                    GrammarExerciseSetDTO.GrammarExplanationDTO.builder()
                            .rule("Test rule")
                            .examples(Arrays.asList("Example 1", "Example 2"))
                            .commonMistakes(Arrays.asList("Mistake 1"))
                            .build();

            List<GrammarExerciseDTO> exercises = Arrays.asList(
                    GrammarExerciseDTO.multipleChoice(
                            "Choose the correct answer",
                            "She ___ to work.",
                            Arrays.asList("goes", "go", "going", "went"),
                            "goes",
                            "Third person singular uses -es"
                    )
            );

            // Act
            Map<String, Object> content = 
                    GrammarExerciseMapper.createContentMap(explanation, exercises);

            // Assert
            assertNotNull(content);
            assertTrue(content.containsKey("explanation"));
            assertTrue(content.containsKey("exercises"));

            @SuppressWarnings("unchecked")
            Map<String, Object> explanationMap = (Map<String, Object>) content.get("explanation");
            assertEquals("Test rule", explanationMap.get("rule"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exercisesList = 
                    (List<Map<String, Object>>) content.get("exercises");
            assertEquals(1, exercisesList.size());
            assertEquals("multiple_choice", exercisesList.get(0).get("type"));
        }
    }

    // ========== Statistics Mapping Tests ==========

    @Nested
    @DisplayName("Statistics Summary Mapping")
    class StatisticsMappingTests {

        @Test
        @DisplayName("Should create statistics summary DTO")
        void testCreateStatsSummary_Success() {
            // Arrange
            List<Object[]> bestScores = Arrays.asList(
                    new Object[]{"Present Simple", new BigDecimal("90.00")},
                    new Object[]{"Past Simple", new BigDecimal("85.00")}
            );

            List<Object[]> progressByLevel = Arrays.asList(
                    new Object[]{"A1", 5L},
                    new Object[]{"A2", 3L},
                    new Object[]{"B1", 2L}
            );

            // Act
            GrammarProgressDTO.GrammarStatsSummaryDTO stats = 
                    GrammarExerciseMapper.createStatsSummary(
                            10L, 8L, 6L,
                            new BigDecimal("75.50"),
                            150, 3600,
                            bestScores, progressByLevel
                    );

            // Assert
            assertNotNull(stats);
            assertEquals(10L, stats.getTotalAttempted());
            assertEquals(8L, stats.getTotalCompleted());
            assertEquals(6L, stats.getTotalPassed());
            assertEquals(new BigDecimal("75.50"), stats.getAveragePercentage());
            assertEquals(150, stats.getTotalScore());
            assertEquals(3600, stats.getTotalTimeSeconds());
            
            assertEquals(2, stats.getBestScores().size());
            assertEquals("Present Simple", stats.getBestScores().get(0).getGrammarPoint());
            
            assertEquals(3, stats.getProgressByLevel().size());
            assertEquals("A1", stats.getProgressByLevel().get(0).getCefrLevel());
            assertEquals(5L, stats.getProgressByLevel().get(0).getCompletedCount());
        }
    }

    // ========== Helper Methods ==========

    /**
     * Creates sample content map for testing.
     */
    private Map<String, Object> createSampleContent() {
        Map<String, Object> content = new HashMap<>();

        // Create explanation
        Map<String, Object> explanation = new HashMap<>();
        explanation.put("rule", "Present Simple is used for habits and routines.");
        explanation.put("examples", Arrays.asList("I work every day.", "She speaks English."));
        explanation.put("commonMistakes", Arrays.asList("Forgetting -s for third person"));
        content.put("explanation", explanation);

        // Create exercises
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("type", "multiple_choice");
        exercise.put("instruction", "Choose the correct form.");
        exercise.put("question", "She ___ to work every day.");
        exercise.put("options", Arrays.asList("goes", "go", "going", "went"));
        exercise.put("correctAnswer", "goes");
        exercise.put("explanation", "Third person singular uses -es.");
        exercise.put("difficulty", "easy");
        content.put("exercises", Arrays.asList(exercise));

        return content;
    }
}
