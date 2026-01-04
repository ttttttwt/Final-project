package com.lexia.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.entity.Lesson.LessonType;
import com.lexia.backend.exception.InvalidLessonContentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for validating lesson content against type-specific JSONB schemas.
 * 
 * <p>
 * This validator ensures that lesson content conforms to the required structure
 * documented in DATABASE-SCHEMA.md section 2.3 before persisting to the
 * database.
 * </p>
 * 
 * <p>
 * <strong>Validation Rules by Lesson Type:</strong>
 * </p>
 * <ul>
 * <li>READING: passages[], questions[], optional vocabulary[]</li>
 * <li>LISTENING: audioUrl, duration, transcript, questions[]</li>
 * <li>QUIZ: questions[], passingScore (0-100), positive points</li>
 * <li>SPEAKING: scenario, difficulty (enum), prompts[], turns (1-20)</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonContentValidator {

    private final ObjectMapper objectMapper;

    private static final Set<String> VALID_DIFFICULTIES = new HashSet<>(
            Arrays.asList("beginner", "intermediate", "advanced"));

    private static final Set<String> VALID_QUESTION_TYPES = new HashSet<>(
            Arrays.asList("multiple_choice", "true_false", "short_answer", "fill_blank", "matching"));

    /**
     * Validates lesson content against the appropriate schema for the given lesson
     * type.
     * 
     * @param type    the lesson type (READING, LISTENING, QUIZ, SPEAKING)
     * @param content the JSONB content as a string
     * @throws InvalidLessonContentException if validation fails
     */
    public void validate(LessonType type, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidLessonContentException("Lesson content cannot be null or empty");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            validate(type, jsonNode);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse lesson content as JSON: {}", e.getMessage());
            throw new InvalidLessonContentException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    /**
     * Validates lesson content against the appropriate schema for the given lesson
     * type.
     * 
     * @param type    the lesson type (READING, LISTENING, QUIZ, SPEAKING)
     * @param content the JSONB content as JsonNode
     * @throws InvalidLessonContentException if validation fails
     */
    public void validate(LessonType type, JsonNode content) {
        if (content == null || content.isNull()) {
            throw new InvalidLessonContentException("Lesson content cannot be null");
        }

        log.debug("Validating {} lesson content", type);

        switch (type) {
            case READING -> validateReadingContent(content);
            case LISTENING -> validateListeningContent(content);
            case QUIZ -> validateQuizContent(content);
            case SPEAKING -> validateSpeakingContent(content);
            default -> throw new IllegalArgumentException("Unknown lesson type: " + type);
        }

        log.debug("Successfully validated {} lesson content", type);
    }

    /**
     * Validates READING lesson content.
     * 
     * <p>
     * Required fields:
     * </p>
     * <ul>
     * <li>passages[] - array with at least 1 passage, each must have 'text'</li>
     * <li>questions[] - array with at least 1 question</li>
     * </ul>
     * 
     * @param content the JSONB content as JsonNode
     * @throws InvalidLessonContentException if validation fails
     */
    private void validateReadingContent(JsonNode content) {
        // Validate passages
        if (!content.has("passages")) {
            throw new InvalidLessonContentException("READING lesson must have 'passages' array");
        }

        JsonNode passages = content.get("passages");
        if (!passages.isArray() || passages.size() == 0) {
            throw new InvalidLessonContentException("READING lesson must have at least 1 passage");
        }

        // Validate each passage has required 'text' field
        for (int i = 0; i < passages.size(); i++) {
            JsonNode passage = passages.get(i);
            if (!passage.has("text") || passage.get("text").asText().trim().isEmpty()) {
                throw new InvalidLessonContentException(
                        "Passage at index " + i + " must have non-empty 'text' field");
            }
        }

        // Validate questions
        validateQuestionsArray(content, "READING");

        // Validate vocabulary if present (optional)
        if (content.has("vocabulary")) {
            JsonNode vocabulary = content.get("vocabulary");
            if (!vocabulary.isArray()) {
                throw new InvalidLessonContentException("'vocabulary' must be an array");
            }

            for (int i = 0; i < vocabulary.size(); i++) {
                JsonNode vocab = vocabulary.get(i);
                if (!vocab.has("word") || vocab.get("word").asText().trim().isEmpty()) {
                    throw new InvalidLessonContentException(
                            "Vocabulary at index " + i + " must have non-empty 'word' field");
                }
                if (!vocab.has("definition") || vocab.get("definition").asText().trim().isEmpty()) {
                    throw new InvalidLessonContentException(
                            "Vocabulary at index " + i + " must have non-empty 'definition' field");
                }
            }
        }
    }

    /**
     * Validates LISTENING lesson content.
     * 
     * <p>
     * Required fields:
     * </p>
     * <ul>
     * <li>audioUrl - valid URL (optional during creation, will be set after file upload)</li>
     * <li>duration - positive number (seconds)</li>
     * <li>transcript - non-empty string</li>
     * <li>questions[] - array with at least 1 question</li>
     * </ul>
     * 
     * @param content the JSONB content as JsonNode
     * @throws InvalidLessonContentException if validation fails
     */
    private void validateListeningContent(JsonNode content) {
        // Validate audioUrl - optional during creation (file will be uploaded after lesson is created)
        // If audioUrl is provided, it must be a valid URL or relative path
        if (content.has("audioUrl")) {
            String audioUrl = content.get("audioUrl").asText();
            if (audioUrl != null && !audioUrl.trim().isEmpty()) {
                // Accept both full URLs (http/https) and relative paths (/api/...)
                if (!audioUrl.startsWith("/")) {
                    try {
                        new URL(audioUrl);
                    } catch (MalformedURLException e) {
                        throw new InvalidLessonContentException("'audioUrl' must be a valid URL or relative path: " + audioUrl);
                    }
                }
                // Relative paths starting with "/" are valid
            }
        }

        // Validate duration
        if (!content.has("duration")) {
            throw new InvalidLessonContentException("LISTENING lesson must have 'duration' field");
        }

        int duration = content.get("duration").asInt(0);
        if (duration <= 0) {
            throw new InvalidLessonContentException("'duration' must be greater than 0");
        }

        // Validate transcript (accessibility requirement)
        if (!content.has("transcript")) {
            throw new InvalidLessonContentException("LISTENING lesson must have 'transcript' field");
        }

        String transcript = content.get("transcript").asText();
        if (transcript == null || transcript.trim().isEmpty()) {
            throw new InvalidLessonContentException("'transcript' cannot be empty");
        }

        // Validate questions
        validateQuestionsArray(content, "LISTENING");

        // Validate timestamps if present
        JsonNode questions = content.get("questions");
        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);
            if (question.has("timestamp")) {
                double timestamp = question.get("timestamp").asDouble(-1);
                if (timestamp < 0) {
                    throw new InvalidLessonContentException(
                            "Question at index " + i + " has invalid timestamp");
                }
                if (timestamp > duration) {
                    throw new InvalidLessonContentException(
                            "Question at index " + i + " timestamp (" + timestamp
                                    + ") exceeds audio duration (" + duration + ")");
                }
            }
        }

        // Validate vocabulary timestamps if present
        if (content.has("vocabulary")) {
            JsonNode vocabulary = content.get("vocabulary");
            if (vocabulary.isArray()) {
                for (int i = 0; i < vocabulary.size(); i++) {
                    JsonNode vocab = vocabulary.get(i);
                    if (!vocab.has("word") || vocab.get("word").asText().trim().isEmpty()) {
                        throw new InvalidLessonContentException(
                                "Vocabulary at index " + i + " must have non-empty 'word' field");
                    }
                    if (!vocab.has("definition") || vocab.get("definition").asText().trim().isEmpty()) {
                        throw new InvalidLessonContentException(
                                "Vocabulary at index " + i + " must have non-empty 'definition' field");
                    }
                    if (vocab.has("timestamp")) {
                        double timestamp = vocab.get("timestamp").asDouble(-1);
                        if (timestamp > duration) {
                            throw new InvalidLessonContentException(
                                    "Vocabulary at index " + i + " timestamp (" + timestamp
                                            + ") exceeds audio duration (" + duration + ")");
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates QUIZ lesson content.
     * 
     * <p>
     * Required fields:
     * </p>
     * <ul>
     * <li>questions[] - array with at least 1 question</li>
     * <li>passingScore - 0-100 (optional, default 70)</li>
     * <li>points - positive number for each question</li>
     * </ul>
     * 
     * @param content the JSONB content as JsonNode
     * @throws InvalidLessonContentException if validation fails
     */
    private void validateQuizContent(JsonNode content) {
        // Validate questions
        validateQuestionsArray(content, "QUIZ");

        // Validate passingScore if present
        if (content.has("passingScore")) {
            int passingScore = content.get("passingScore").asInt(-1);
            if (passingScore < 0 || passingScore > 100) {
                throw new InvalidLessonContentException("'passingScore' must be between 0 and 100");
            }
        }

        // Validate points for each question
        JsonNode questions = content.get("questions");
        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);
            if (question.has("points")) {
                double points = question.get("points").asDouble(-1);
                if (points <= 0) {
                    throw new InvalidLessonContentException(
                            "Question at index " + i + " must have positive 'points' value");
                }
            }
        }

        // Validate timeLimit if present
        if (content.has("timeLimit")) {
            int timeLimit = content.get("timeLimit").asInt(-1);
            if (timeLimit <= 0) {
                throw new InvalidLessonContentException("'timeLimit' must be greater than 0");
            }
        }
    }

    /**
     * Validates SPEAKING lesson content.
     * 
     * <p>
     * Required fields:
     * </p>
     * <ul>
     * <li>scenario - non-empty string</li>
     * <li>difficulty - beginner | intermediate | advanced</li>
     * <li>prompts[] - array with at least 1 prompt</li>
     * <li>turns - 1-20 (if provided)</li>
     * </ul>
     * 
     * @param content the JSONB content as JsonNode
     * @throws InvalidLessonContentException if validation fails
     */
    private void validateSpeakingContent(JsonNode content) {
        // Validate scenario
        if (!content.has("scenario")) {
            throw new InvalidLessonContentException("SPEAKING lesson must have 'scenario' field");
        }

        String scenario = content.get("scenario").asText();
        if (scenario == null || scenario.trim().isEmpty()) {
            throw new InvalidLessonContentException("'scenario' cannot be empty");
        }

        // Validate difficulty
        if (!content.has("difficulty")) {
            throw new InvalidLessonContentException("SPEAKING lesson must have 'difficulty' field");
        }

        String difficulty = content.get("difficulty").asText().toLowerCase();
        if (!VALID_DIFFICULTIES.contains(difficulty)) {
            throw new InvalidLessonContentException(
                    "'difficulty' must be one of: beginner, intermediate, advanced");
        }

        // Validate prompts
        if (!content.has("prompts")) {
            throw new InvalidLessonContentException("SPEAKING lesson must have 'prompts' array");
        }

        JsonNode prompts = content.get("prompts");
        if (!prompts.isArray() || prompts.size() == 0) {
            throw new InvalidLessonContentException("SPEAKING lesson must have at least 1 prompt");
        }

        // Validate each prompt
        for (int i = 0; i < prompts.size(); i++) {
            JsonNode prompt = prompts.get(i);
            if (!prompt.has("prompt") || prompt.get("prompt").asText().trim().isEmpty()) {
                throw new InvalidLessonContentException(
                        "Prompt at index " + i + " must have non-empty 'prompt' field");
            }

            // Validate sampleAnswers if present (minimum 1 if provided)
            if (prompt.has("sampleAnswers")) {
                JsonNode sampleAnswers = prompt.get("sampleAnswers");
                if (!sampleAnswers.isArray() || sampleAnswers.size() == 0) {
                    throw new InvalidLessonContentException(
                            "Prompt at index " + i + " 'sampleAnswers' must be a non-empty array");
                }
            }
        }

        // Validate rolePlaySettings if present
        if (content.has("rolePlaySettings")) {
            JsonNode settings = content.get("rolePlaySettings");

            // Validate turns
            if (settings.has("turns")) {
                int turns = settings.get("turns").asInt(-1);
                if (turns < 1 || turns > 20) {
                    throw new InvalidLessonContentException("'turns' must be between 1 and 20");
                }
            }
        }
    }

    /**
     * Validates the questions array common to multiple lesson types.
     * 
     * @param content    the JSONB content as JsonNode
     * @param lessonType the lesson type for error messages
     * @throws InvalidLessonContentException if validation fails
     */
    private void validateQuestionsArray(JsonNode content, String lessonType) {
        if (!content.has("questions")) {
            throw new InvalidLessonContentException(lessonType + " lesson must have 'questions' array");
        }

        JsonNode questions = content.get("questions");
        if (!questions.isArray() || questions.size() == 0) {
            throw new InvalidLessonContentException(lessonType + " lesson must have at least 1 question");
        }

        // Validate each question
        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);

            // Validate question text
            if (!question.has("question") || question.get("question").asText().trim().isEmpty()) {
                throw new InvalidLessonContentException(
                        "Question at index " + i + " must have non-empty 'question' field");
            }

            // Validate question type
            if (!question.has("type")) {
                throw new InvalidLessonContentException(
                        "Question at index " + i + " must have 'type' field");
            }

            String type = question.get("type").asText();
            if (!VALID_QUESTION_TYPES.contains(type)) {
                throw new InvalidLessonContentException(
                        "Question at index " + i + " has invalid type: " + type);
            }

            // Validate options for multiple_choice
            if ("multiple_choice".equals(type)) {
                if (!question.has("options")) {
                    throw new InvalidLessonContentException(
                            "Multiple choice question at index " + i + " must have 'options' array");
                }

                JsonNode options = question.get("options");
                if (!options.isArray() || options.size() < 2 || options.size() > 6) {
                    throw new InvalidLessonContentException(
                            "Multiple choice question at index " + i + " must have 2-6 options");
                }
            }

            // Validate correctAnswer
            if (!question.has("correctAnswer")) {
                throw new InvalidLessonContentException(
                        "Question at index " + i + " must have 'correctAnswer' field");
            }

            // Validate correctAnswer index for multiple_choice
            if ("multiple_choice".equals(type)) {
                int correctAnswer = question.get("correctAnswer").asInt(-1);
                int optionsSize = question.get("options").size();
                if (correctAnswer < 0 || correctAnswer >= optionsSize) {
                    throw new InvalidLessonContentException(
                            "Question at index " + i + " has invalid 'correctAnswer' index: " + correctAnswer
                                    + " (must be 0-" + (optionsSize - 1) + ")");
                }
            }
        }
    }
}
