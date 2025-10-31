package com.lexia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.entity.Lesson.LessonType;
import com.lexia.backend.exception.InvalidLessonContentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for LessonContentValidator.
 * 
 * <p>
 * Tests all validation rules for each lesson type:
 * </p>
 * <ul>
 * <li>READING: passages[], questions[], vocabulary[]</li>
 * <li>LISTENING: audioUrl, duration, transcript, questions[]</li>
 * <li>QUIZ: questions[], passingScore, points, timeLimit</li>
 * <li>SPEAKING: scenario, difficulty, prompts[], turns</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DisplayName("LessonContentValidator Tests")
class LessonContentValidatorTest {

    private LessonContentValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = new LessonContentValidator(objectMapper);
    }

    // ==================== General Validation Tests ====================

    @Nested
    @DisplayName("General Validation")
    class GeneralValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject null or empty content string")
        void shouldRejectNullOrEmptyContentString(String content) {
            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, content))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("Should reject invalid JSON format")
        void shouldRejectInvalidJsonFormat() {
            // Arrange
            String invalidJson = "{ invalid json }";

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidJson))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("Invalid JSON format");
        }

        @Test
        @DisplayName("Should reject null JsonNode")
        void shouldRejectNullJsonNode() {
            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, (JsonNode) null))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    // ==================== READING Lesson Tests ====================

    @Nested
    @DisplayName("READING Lesson Validation")
    class ReadingLessonValidation {

        @Test
        @DisplayName("Should accept valid READING content with all fields")
        void shouldAcceptValidReadingContentWithAllFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "passages": [
                        {
                          "title": "Working from Home",
                          "text": "Remote work has become increasingly popular in recent years."
                        }
                      ],
                      "questions": [
                        {
                          "question": "What is the main topic?",
                          "type": "multiple_choice",
                          "options": ["Remote work", "Office", "Policy", "Benefits"],
                          "correctAnswer": 0,
                          "explanation": "The passage focuses on remote work."
                        }
                      ],
                      "vocabulary": [
                        {
                          "word": "flexible",
                          "definition": "capable of bending easily",
                          "example": "She has a flexible schedule.",
                          "partOfSpeech": "adjective"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.READING, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept valid READING content without optional vocabulary")
        void shouldAcceptValidReadingContentWithoutVocabulary() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "passages": [
                        {
                          "text": "Remote work is popular."
                        }
                      ],
                      "questions": [
                        {
                          "question": "What is remote work?",
                          "type": "short_answer",
                          "correctAnswer": "Working from home"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.READING, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject READING content without passages")
        void shouldRejectReadingContentWithoutPassages() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "What is the topic?",
                          "type": "multiple_choice",
                          "options": ["A", "B"],
                          "correctAnswer": 0
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'passages' array");
        }

        @Test
        @DisplayName("Should reject READING content with empty passages array")
        void shouldRejectReadingContentWithEmptyPassagesArray() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passages": [],
                      "questions": [
                        {
                          "question": "What is the topic?",
                          "type": "multiple_choice",
                          "options": ["A", "B"],
                          "correctAnswer": 0
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have at least 1 passage");
        }

        @Test
        @DisplayName("Should reject READING content with passage missing text")
        void shouldRejectReadingContentWithPassageMissingText() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passages": [
                        {
                          "title": "Title without text"
                        }
                      ],
                      "questions": [
                        {
                          "question": "What is the topic?",
                          "type": "multiple_choice",
                          "options": ["A", "B"],
                          "correctAnswer": 0
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have non-empty 'text' field");
        }

        @Test
        @DisplayName("Should reject READING content without questions")
        void shouldRejectReadingContentWithoutQuestions() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passages": [
                        {
                          "text": "Some text here."
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'questions' array");
        }

        @Test
        @DisplayName("Should reject READING content with invalid vocabulary structure")
        void shouldRejectReadingContentWithInvalidVocabulary() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passages": [
                        {
                          "text": "Some text here."
                        }
                      ],
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ],
                      "vocabulary": [
                        {
                          "word": "test"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have non-empty 'definition' field");
        }
    }

    // ==================== LISTENING Lesson Tests ====================

    @Nested
    @DisplayName("LISTENING Lesson Validation")
    class ListeningLessonValidation {

        @Test
        @DisplayName("Should accept valid LISTENING content with all fields")
        void shouldAcceptValidListeningContentWithAllFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/lesson-15.mp3",
                      "duration": 120,
                      "transcript": "A: Good morning! B: Hello!",
                      "showTranscript": false,
                      "questions": [
                        {
                          "question": "What greeting was used?",
                          "type": "multiple_choice",
                          "options": ["Good morning", "Hello", "Both", "None"],
                          "correctAnswer": 2,
                          "timestamp": 5.2
                        }
                      ],
                      "vocabulary": [
                        {
                          "word": "greeting",
                          "definition": "a polite word or sign of welcome",
                          "timestamp": 3.5
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.LISTENING, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject LISTENING content without audioUrl")
        void shouldRejectListeningContentWithoutAudioUrl() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "duration": 120,
                      "transcript": "Some transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'audioUrl' field");
        }

        @Test
        @DisplayName("Should reject LISTENING content with invalid audioUrl")
        void shouldRejectListeningContentWithInvalidAudioUrl() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "not-a-valid-url",
                      "duration": 120,
                      "transcript": "Some transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must be a valid URL");
        }

        @Test
        @DisplayName("Should reject LISTENING content without duration")
        void shouldRejectListeningContentWithoutDuration() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/test.mp3",
                      "transcript": "Some transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'duration' field");
        }

        @Test
        @DisplayName("Should reject LISTENING content with zero or negative duration")
        void shouldRejectListeningContentWithInvalidDuration() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/test.mp3",
                      "duration": 0,
                      "transcript": "Some transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must be greater than 0");
        }

        @Test
        @DisplayName("Should reject LISTENING content without transcript")
        void shouldRejectListeningContentWithoutTranscript() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/test.mp3",
                      "duration": 120,
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'transcript' field");
        }

        @Test
        @DisplayName("Should reject LISTENING content with timestamp exceeding duration")
        void shouldRejectListeningContentWithTimestampExceedingDuration() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/test.mp3",
                      "duration": 60,
                      "transcript": "Transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true",
                          "timestamp": 90
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("exceeds audio duration");
        }

        @Test
        @DisplayName("Should reject LISTENING content with vocabulary timestamp exceeding duration")
        void shouldRejectListeningContentWithVocabTimestampExceedingDuration() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "audioUrl": "https://cdn.lexia.com/audio/test.mp3",
                      "duration": 60,
                      "transcript": "Transcript",
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ],
                      "vocabulary": [
                        {
                          "word": "test",
                          "definition": "a test",
                          "timestamp": 90
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.LISTENING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("exceeds audio duration");
        }
    }

    // ==================== QUIZ Lesson Tests ====================

    @Nested
    @DisplayName("QUIZ Lesson Validation")
    class QuizLessonValidation {

        @Test
        @DisplayName("Should accept valid QUIZ content with all fields")
        void shouldAcceptValidQuizContentWithAllFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "title": "Present Perfect Tense Quiz",
                      "instructions": "Choose the correct form.",
                      "timeLimit": 600,
                      "passingScore": 80,
                      "questions": [
                        {
                          "question": "I ___ to Paris three times.",
                          "type": "multiple_choice",
                          "options": ["have been", "was", "have gone", "went"],
                          "correctAnswer": 0,
                          "points": 2,
                          "explanation": "Use 'have been'",
                          "hint": "Think about present perfect"
                        },
                        {
                          "question": "She has lived in London since 2010.",
                          "type": "true_false",
                          "correctAnswer": "true",
                          "points": 1
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.QUIZ, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept valid QUIZ content without optional fields")
        void shouldAcceptValidQuizContentWithoutOptionalFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "questions": [
                        {
                          "question": "What is 2+2?",
                          "type": "multiple_choice",
                          "options": ["3", "4", "5"],
                          "correctAnswer": 1
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.QUIZ, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject QUIZ content without questions")
        void shouldRejectQuizContentWithoutQuestions() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "title": "Quiz without questions",
                      "passingScore": 70
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'questions' array");
        }

        @Test
        @DisplayName("Should reject QUIZ content with invalid passingScore")
        void shouldRejectQuizContentWithInvalidPassingScore() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passingScore": 150,
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must be between 0 and 100");
        }

        @Test
        @DisplayName("Should reject QUIZ content with negative points")
        void shouldRejectQuizContentWithNegativePoints() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true",
                          "points": -1
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have positive 'points' value");
        }

        @Test
        @DisplayName("Should reject QUIZ content with invalid timeLimit")
        void shouldRejectQuizContentWithInvalidTimeLimit() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "timeLimit": 0,
                      "questions": [
                        {
                          "question": "Question?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must be greater than 0");
        }
    }

    // ==================== SPEAKING Lesson Tests ====================

    @Nested
    @DisplayName("SPEAKING Lesson Validation")
    class SpeakingLessonValidation {

        @Test
        @DisplayName("Should accept valid SPEAKING content with all fields")
        void shouldAcceptValidSpeakingContentWithAllFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "scenario": "Ordering food at a restaurant",
                      "difficulty": "beginner",
                      "prompts": [
                        {
                          "prompt": "Greet the waiter and ask for a menu",
                          "context": "You just sat down",
                          "sampleAnswers": [
                            "Hello! Could I see the menu, please?",
                            "Good evening. May I have a menu?"
                          ],
                          "targetGrammar": ["modal verbs", "polite requests"],
                          "targetVocabulary": ["menu", "order", "waiter"]
                        }
                      ],
                      "rolePlaySettings": {
                        "aiPersona": "friendly waiter",
                        "turns": 6,
                        "enableFeedback": true
                      }
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.SPEAKING, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept valid SPEAKING content with minimal fields")
        void shouldAcceptValidSpeakingContentWithMinimalFields() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "scenario": "Simple conversation",
                      "difficulty": "intermediate",
                      "prompts": [
                        {
                          "prompt": "Introduce yourself"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.SPEAKING, validContent))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject SPEAKING content without scenario")
        void shouldRejectSpeakingContentWithoutScenario() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "difficulty": "beginner",
                      "prompts": [
                        {
                          "prompt": "Say something"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'scenario' field");
        }

        @Test
        @DisplayName("Should reject SPEAKING content without difficulty")
        void shouldRejectSpeakingContentWithoutDifficulty() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "scenario": "Some scenario",
                      "prompts": [
                        {
                          "prompt": "Say something"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'difficulty' field");
        }

        @ParameterizedTest
        @ValueSource(strings = { "easy", "hard", "expert", "INVALID" })
        @DisplayName("Should reject SPEAKING content with invalid difficulty")
        void shouldRejectSpeakingContentWithInvalidDifficulty(String invalidDifficulty) throws Exception {
            // Arrange
            String invalidContent = String.format("""
                    {
                      "scenario": "Some scenario",
                      "difficulty": "%s",
                      "prompts": [
                        {
                          "prompt": "Say something"
                        }
                      ]
                    }
                    """, invalidDifficulty);

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must be one of: beginner, intermediate, advanced");
        }

        @Test
        @DisplayName("Should reject SPEAKING content without prompts")
        void shouldRejectSpeakingContentWithoutPrompts() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "scenario": "Some scenario",
                      "difficulty": "beginner"
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'prompts' array");
        }

        @Test
        @DisplayName("Should reject SPEAKING content with empty prompts array")
        void shouldRejectSpeakingContentWithEmptyPromptsArray() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "scenario": "Some scenario",
                      "difficulty": "beginner",
                      "prompts": []
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have at least 1 prompt");
        }

        @Test
        @DisplayName("Should reject SPEAKING content with prompt missing prompt field")
        void shouldRejectSpeakingContentWithPromptMissingPromptField() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "scenario": "Some scenario",
                      "difficulty": "beginner",
                      "prompts": [
                        {
                          "context": "Some context"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have non-empty 'prompt' field");
        }

        @Test
        @DisplayName("Should reject SPEAKING content with empty sampleAnswers array")
        void shouldRejectSpeakingContentWithEmptySampleAnswers() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "scenario": "Some scenario",
                      "difficulty": "beginner",
                      "prompts": [
                        {
                          "prompt": "Say something",
                          "sampleAnswers": []
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("'sampleAnswers' must be a non-empty array");
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, 21, 100 })
        @DisplayName("Should reject SPEAKING content with invalid turns")
        void shouldRejectSpeakingContentWithInvalidTurns(int invalidTurns) throws Exception {
            // Arrange
            String invalidContent = String.format("""
                    {
                      "scenario": "Some scenario",
                      "difficulty": "beginner",
                      "prompts": [
                        {
                          "prompt": "Say something"
                        }
                      ],
                      "rolePlaySettings": {
                        "turns": %d
                      }
                    }
                    """, invalidTurns);

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.SPEAKING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("'turns' must be between 1 and 20");
        }
    }

    // ==================== Common Questions Validation Tests ====================

    @Nested
    @DisplayName("Common Questions Validation")
    class CommonQuestionsValidation {

        @Test
        @DisplayName("Should reject content with empty questions array")
        void shouldRejectContentWithEmptyQuestionsArray() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "passages": [
                        {
                          "text": "Some text"
                        }
                      ],
                      "questions": []
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.READING, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have at least 1 question");
        }

        @Test
        @DisplayName("Should reject question without question text")
        void shouldRejectQuestionWithoutQuestionText() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "type": "true_false",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have non-empty 'question' field");
        }

        @Test
        @DisplayName("Should reject question without type")
        void shouldRejectQuestionWithoutType() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "correctAnswer": "true"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'type' field");
        }

        @Test
        @DisplayName("Should reject question with invalid type")
        void shouldRejectQuestionWithInvalidType() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "type": "invalid_type",
                          "correctAnswer": "answer"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("has invalid type");
        }

        @Test
        @DisplayName("Should reject multiple_choice question without options")
        void shouldRejectMultipleChoiceQuestionWithoutOptions() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "type": "multiple_choice",
                          "correctAnswer": 0
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'options' array");
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, 7, 10 })
        @DisplayName("Should reject multiple_choice question with invalid number of options")
        void shouldRejectMultipleChoiceQuestionWithInvalidOptions(int optionCount) throws Exception {
            // Arrange
            StringBuilder options = new StringBuilder("[");
            for (int i = 0; i < optionCount; i++) {
                if (i > 0)
                    options.append(", ");
                options.append("\"Option ").append(i + 1).append("\"");
            }
            options.append("]");

            String invalidContent = String.format("""
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "type": "multiple_choice",
                          "options": %s,
                          "correctAnswer": 0
                        }
                      ]
                    }
                    """, options);

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 2-6 options");
        }

        @Test
        @DisplayName("Should reject question without correctAnswer")
        void shouldRejectQuestionWithoutCorrectAnswer() throws Exception {
            // Arrange
            String invalidContent = """
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "type": "true_false"
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("must have 'correctAnswer' field");
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 4, 10 })
        @DisplayName("Should reject multiple_choice with invalid correctAnswer index")
        void shouldRejectMultipleChoiceWithInvalidCorrectAnswerIndex(int invalidIndex) throws Exception {
            // Arrange
            String invalidContent = String.format("""
                    {
                      "questions": [
                        {
                          "question": "What is this?",
                          "type": "multiple_choice",
                          "options": ["A", "B", "C", "D"],
                          "correctAnswer": %d
                        }
                      ]
                    }
                    """, invalidIndex);

            // Act & Assert
            assertThatThrownBy(() -> validator.validate(LessonType.QUIZ, invalidContent))
                    .isInstanceOf(InvalidLessonContentException.class)
                    .hasMessageContaining("has invalid 'correctAnswer' index");
        }

        @Test
        @DisplayName("Should accept all valid question types")
        void shouldAcceptAllValidQuestionTypes() throws Exception {
            // Arrange
            String validContent = """
                    {
                      "questions": [
                        {
                          "question": "Multiple choice?",
                          "type": "multiple_choice",
                          "options": ["A", "B"],
                          "correctAnswer": 0
                        },
                        {
                          "question": "True or false?",
                          "type": "true_false",
                          "correctAnswer": "true"
                        },
                        {
                          "question": "Short answer?",
                          "type": "short_answer",
                          "correctAnswer": "answer"
                        },
                        {
                          "question": "Fill blank?",
                          "type": "fill_blank",
                          "correctAnswer": "word"
                        },
                        {
                          "question": "Matching?",
                          "type": "matching",
                          "correctAnswer": ["match1", "match2"]
                        }
                      ]
                    }
                    """;

            // Act & Assert
            assertThatCode(() -> validator.validate(LessonType.QUIZ, validContent))
                    .doesNotThrowAnyException();
        }
    }
}
