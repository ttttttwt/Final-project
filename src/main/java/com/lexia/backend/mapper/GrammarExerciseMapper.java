package com.lexia.backend.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.GrammarExerciseSet;
import com.lexia.backend.entity.GrammarTopic;
import com.lexia.backend.entity.UserGrammarProgress;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between Grammar entities and DTOs.
 * Provides centralized mapping logic for grammar data transformations.
 * 
 * <p>Handles:</p>
 * <ul>
 *   <li>GrammarTopic entity to/from GrammarTopicDTO</li>
 *   <li>GrammarExerciseSet entity to/from GrammarExerciseSetDTO</li>
 *   <li>UserGrammarProgress entity to/from GrammarProgressDTO</li>
 *   <li>JSONB content parsing for exercises</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Slf4j
public class GrammarExerciseMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GrammarExerciseMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== GrammarTopic Mapping ==========

    /**
     * Converts a GrammarTopic entity to GrammarTopicDTO.
     *
     * @param topic the grammar topic entity
     * @return GrammarTopicDTO representation, or null if topic is null
     */
    public static GrammarTopicDTO toTopicDTO(GrammarTopic topic) {
        if (topic == null) {
            return null;
        }

        return GrammarTopicDTO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .category(topic.getCategory())
                .cefrLevels(topic.getCefrLevels() != null 
                        ? Arrays.asList(topic.getCefrLevels()) 
                        : Collections.emptyList())
                .description(topic.getDescription())
                .examples(topic.getExamples() != null 
                        ? Arrays.asList(topic.getExamples()) 
                        : Collections.emptyList())
                .isActive(topic.getIsActive())
                .build();
    }

    /**
     * Converts a list of GrammarTopic entities to DTOs.
     *
     * @param topics list of grammar topic entities
     * @return list of GrammarTopicDTO representations
     */
    public static List<GrammarTopicDTO> toTopicDTOList(List<GrammarTopic> topics) {
        if (topics == null) {
            return Collections.emptyList();
        }
        return topics.stream()
                .map(GrammarExerciseMapper::toTopicDTO)
                .collect(Collectors.toList());
    }

    // ========== GrammarExerciseSet Mapping ==========

    /**
     * Converts a GrammarExerciseSet entity to GrammarExerciseSetDTO.
     * Parses JSONB content to extract exercises and explanation.
     *
     * @param exerciseSet the grammar exercise set entity
     * @return GrammarExerciseSetDTO representation, or null if exerciseSet is null
     */
    public static GrammarExerciseSetDTO toExerciseSetDTO(GrammarExerciseSet exerciseSet) {
        if (exerciseSet == null) {
            return null;
        }

        GrammarExerciseSetDTO dto = GrammarExerciseSetDTO.builder()
                .id(exerciseSet.getId())
                .grammarPoint(exerciseSet.getGrammarPoint())
                .cefrLevel(exerciseSet.getCefrLevel())
                .theme(exerciseSet.getTheme())
                .exerciseCount(exerciseSet.getExerciseCount())
                .timeLimitSeconds(exerciseSet.getTimeLimitSeconds())
                .isFallback(exerciseSet.getIsFallback())
                .createdAt(exerciseSet.getCreatedAt())
                .build();

        // Parse JSONB content
        if (exerciseSet.getContent() != null) {
            parseContent(exerciseSet.getContent(), dto);
        }

        return dto;
    }

    /**
     * Converts a list of GrammarExerciseSet entities to DTOs.
     *
     * @param exerciseSets list of grammar exercise set entities
     * @return list of GrammarExerciseSetDTO representations
     */
    public static List<GrammarExerciseSetDTO> toExerciseSetDTOList(List<GrammarExerciseSet> exerciseSets) {
        if (exerciseSets == null) {
            return Collections.emptyList();
        }
        return exerciseSets.stream()
                .map(GrammarExerciseMapper::toExerciseSetDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a GrammarExerciseSet entity from request DTO and JSONB content.
     *
     * @param request the grammar request DTO
     * @param content the JSONB content map
     * @param userId optional user ID
     * @param topic optional grammar topic entity
     * @return new GrammarExerciseSet entity
     */
    public static GrammarExerciseSet toExerciseSetEntity(GrammarRequestDTO request, 
            Map<String, Object> content, UUID userId, GrammarTopic topic) {
        if (request == null) {
            throw new IllegalArgumentException("GrammarRequestDTO cannot be null");
        }

        int exerciseCount = 0;
        if (content != null && content.containsKey("exercises")) {
            Object exercises = content.get("exercises");
            if (exercises instanceof List) {
                exerciseCount = ((List<?>) exercises).size();
            }
        }

        return GrammarExerciseSet.builder()
                .userId(userId)
                .topic(topic)
                .grammarPoint(request.getGrammarTopic())
                .cefrLevel(request.getCefrLevel())
                .theme(request.getTheme())
                .content(content)
                .exerciseCount(exerciseCount)
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .isFallback(Boolean.TRUE.equals(request.getUseFallback()))
                .build();
    }

    // ========== UserGrammarProgress Mapping ==========

    /**
     * Converts a UserGrammarProgress entity to GrammarProgressDTO.
     *
     * @param progress the user grammar progress entity
     * @return GrammarProgressDTO representation, or null if progress is null
     */
    public static GrammarProgressDTO toProgressDTO(UserGrammarProgress progress) {
        if (progress == null) {
            return null;
        }

        GrammarExerciseSet exerciseSet = progress.getExerciseSet();

        return GrammarProgressDTO.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .exerciseSetId(progress.getExerciseSetId())
                .grammarPoint(exerciseSet != null ? exerciseSet.getGrammarPoint() : null)
                .cefrLevel(exerciseSet != null ? exerciseSet.getCefrLevel() : null)
                .score(progress.getScore())
                .maxScore(progress.getMaxScore())
                .percentage(progress.getPercentage())
                .passed(progress.isPassed())
                .timeSpentSeconds(progress.getTimeSpentSeconds())
                .completed(progress.isCompleted())
                .completedAt(progress.getCompletedAt())
                .createdAt(progress.getCreatedAt())
                .build();
    }

    /**
     * Converts a list of UserGrammarProgress entities to DTOs.
     *
     * @param progressList list of user grammar progress entities
     * @return list of GrammarProgressDTO representations
     */
    public static List<GrammarProgressDTO> toProgressDTOList(List<UserGrammarProgress> progressList) {
        if (progressList == null) {
            return Collections.emptyList();
        }
        return progressList.stream()
                .map(GrammarExerciseMapper::toProgressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a UserGrammarProgress entity from answer submission.
     *
     * @param answerDTO the grammar answer DTO
     * @param exerciseSet the exercise set entity
     * @param userId the user ID
     * @return new UserGrammarProgress entity
     */
    public static UserGrammarProgress toProgressEntity(GrammarAnswerDTO answerDTO,
            GrammarExerciseSet exerciseSet, UUID userId) {
        if (answerDTO == null || exerciseSet == null || userId == null) {
            throw new IllegalArgumentException("Answer DTO, exercise set, and user ID are required");
        }

        List<Map<String, Object>> answers = convertAnswersToMap(answerDTO.getAnswers());

        return UserGrammarProgress.builder()
                .userId(userId)
                .exerciseSet(exerciseSet)
                .answers(answers)
                .timeSpentSeconds(answerDTO.getTotalTimeSeconds())
                .build();
    }

    // ========== GrammarResult Mapping ==========

    /**
     * Creates a GrammarResultDTO from progress entity with feedback.
     *
     * @param progress the user grammar progress entity
     * @param feedback list of question feedback
     * @param areasToImprove list of areas needing improvement
     * @return GrammarResultDTO with complete result information
     */
    public static GrammarResultDTO toResultDTO(UserGrammarProgress progress,
            List<GrammarResultDTO.QuestionFeedback> feedback, List<String> areasToImprove) {
        if (progress == null) {
            return null;
        }

        GrammarExerciseSet exerciseSet = progress.getExerciseSet();
        boolean passed = progress.isPassed();

        return GrammarResultDTO.builder()
                .exerciseSetId(progress.getExerciseSetId())
                .grammarPoint(exerciseSet != null ? exerciseSet.getGrammarPoint() : null)
                .cefrLevel(exerciseSet != null ? exerciseSet.getCefrLevel() : null)
                .score(progress.getScore())
                .maxScore(progress.getMaxScore())
                .percentage(progress.getPercentage())
                .passed(passed)
                .timeSpentSeconds(progress.getTimeSpentSeconds())
                .feedback(feedback)
                .areasToImprove(passed ? Collections.emptyList() : areasToImprove)
                .encouragement(passed 
                        ? "Great job! You've demonstrated a solid understanding!" 
                        : "Keep practicing! Review the explanations and try again.")
                .completedAt(progress.getCompletedAt())
                .build();
    }

    // ========== Helper Methods ==========

    /**
     * Parses JSONB content map and populates the DTO with exercises and explanation.
     */
    @SuppressWarnings("unchecked")
    private static void parseContent(Map<String, Object> content, GrammarExerciseSetDTO dto) {
        try {
            // Parse explanation
            if (content.containsKey("explanation")) {
                Object explanationObj = content.get("explanation");
                if (explanationObj instanceof Map) {
                    Map<String, Object> explanationMap = (Map<String, Object>) explanationObj;
                    GrammarExerciseSetDTO.GrammarExplanationDTO explanation = 
                            GrammarExerciseSetDTO.GrammarExplanationDTO.builder()
                                    .rule((String) explanationMap.get("rule"))
                                    .examples(toStringList(explanationMap.get("examples")))
                                    .commonMistakes(toStringList(explanationMap.get("commonMistakes")))
                                    .tips(toStringList(explanationMap.get("tips")))
                                    .build();
                    dto.setExplanation(explanation);
                }
            }

            // Parse exercises
            if (content.containsKey("exercises")) {
                Object exercisesObj = content.get("exercises");
                if (exercisesObj instanceof List) {
                    List<Map<String, Object>> exercisesList = (List<Map<String, Object>>) exercisesObj;
                    List<GrammarExerciseDTO> exercises = exercisesList.stream()
                            .map(GrammarExerciseMapper::parseExercise)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    dto.setExercises(exercises);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse grammar exercise content: {}", e.getMessage());
        }
    }

    /**
     * Parses a single exercise from a map.
     */
    @SuppressWarnings("unchecked")
    private static GrammarExerciseDTO parseExercise(Map<String, Object> exerciseMap) {
        if (exerciseMap == null) {
            return null;
        }

        return GrammarExerciseDTO.builder()
                .type((String) exerciseMap.get("type"))
                .instruction((String) exerciseMap.get("instruction"))
                .question((String) exerciseMap.get("question"))
                .options(toStringList(exerciseMap.get("options")))
                .blanks(toStringList(exerciseMap.get("blanks")))
                .correctAnswer(exerciseMap.get("correctAnswer"))
                .hint((String) exerciseMap.get("hint"))
                .explanation((String) exerciseMap.get("explanation"))
                .difficulty((String) exerciseMap.get("difficulty"))
                .build();
    }

    /**
     * Converts an object to a List of Strings.
     */
    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof List) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Converts AnswerItem list to Map list for JSONB storage.
     */
    private static List<Map<String, Object>> convertAnswersToMap(List<GrammarAnswerDTO.AnswerItem> answers) {
        if (answers == null) {
            return Collections.emptyList();
        }
        return answers.stream()
                .map(answer -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("questionIndex", answer.getQuestionIndex());
                    map.put("answer", answer.getAnswer());
                    map.put("timeMs", answer.getTimeMs());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Converts JSONB content map to exercises list for validation.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extractExercisesFromContent(Map<String, Object> content) {
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
     * Creates content map from exercises and explanation for entity storage.
     */
    public static Map<String, Object> createContentMap(
            GrammarExerciseSetDTO.GrammarExplanationDTO explanation,
            List<GrammarExerciseDTO> exercises) {
        
        Map<String, Object> content = new HashMap<>();

        if (explanation != null) {
            Map<String, Object> explanationMap = new HashMap<>();
            explanationMap.put("rule", explanation.getRule());
            explanationMap.put("examples", explanation.getExamples());
            explanationMap.put("commonMistakes", explanation.getCommonMistakes());
            explanationMap.put("tips", explanation.getTips());
            content.put("explanation", explanationMap);
        }

        if (exercises != null) {
            List<Map<String, Object>> exercisesList = exercises.stream()
                    .map(exercise -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", exercise.type());
                        map.put("instruction", exercise.instruction());
                        map.put("question", exercise.question());
                        map.put("options", exercise.options());
                        map.put("blanks", exercise.blanks());
                        map.put("correctAnswer", exercise.correctAnswer());
                        map.put("hint", exercise.hint());
                        map.put("explanation", exercise.explanation());
                        map.put("difficulty", exercise.difficulty());
                        return map;
                    })
                    .collect(Collectors.toList());
            content.put("exercises", exercisesList);
        }

        return content;
    }

    /**
     * Creates statistics summary DTO from aggregated data.
     */
    public static GrammarProgressDTO.GrammarStatsSummaryDTO createStatsSummary(
            long totalAttempted, long totalCompleted, long totalPassed,
            BigDecimal averagePercentage, int totalScore, int totalTimeSeconds,
            List<Object[]> bestScoresByPoint, List<Object[]> progressByLevel) {

        // Convert best scores
        List<GrammarProgressDTO.GrammarPointScore> bestScores = bestScoresByPoint.stream()
                .map(row -> GrammarProgressDTO.GrammarPointScore.builder()
                        .grammarPoint((String) row[0])
                        .bestPercentage((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());

        // Convert progress by level
        List<GrammarProgressDTO.CefrLevelProgress> levelProgress = progressByLevel.stream()
                .map(row -> GrammarProgressDTO.CefrLevelProgress.builder()
                        .cefrLevel((String) row[0])
                        .completedCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return GrammarProgressDTO.GrammarStatsSummaryDTO.builder()
                .totalAttempted(totalAttempted)
                .totalCompleted(totalCompleted)
                .totalPassed(totalPassed)
                .averagePercentage(averagePercentage)
                .totalScore(totalScore)
                .totalTimeSeconds(totalTimeSeconds)
                .bestScores(bestScores)
                .progressByLevel(levelProgress)
                .build();
    }
}
