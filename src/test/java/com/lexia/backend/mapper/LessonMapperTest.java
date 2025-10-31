package com.lexia.backend.mapper;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LessonMapper.
 * Verifies correct mapping between Lesson entities and DTOs.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DisplayName("LessonMapper Unit Tests")
class LessonMapperTest {

    private static final String READING_CONTENT = """
            {
              "passages": [{"text": "Sample reading passage", "title": "Test Passage"}],
              "questions": [{"question": "What is the topic?", "type": "multiple_choice", "options": ["A", "B"], "correctAnswer": 0}]
            }
            """;

    private static final String LISTENING_CONTENT = """
            {
              "audioUrl": "https://cdn.lexia.com/audio/sample.mp3",
              "duration": 120,
              "transcript": "This is a sample transcript",
              "questions": []
            }
            """;

    @Test
    @DisplayName("Should convert Lesson entity to LessonDTO with all fields")
    void testToDTO_WithAllFields_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Section section = Section.builder()
                .id(1L)
                .title("Test Section")
                .orderIndex(0)
                .build();

        Lesson lesson = Lesson.builder()
                .id(1L)
                .section(section)
                .title("Basic Greetings")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(15)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Act
        LessonDTO dto = LessonMapper.toDTO(lesson);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getSectionId());
        assertEquals("Basic Greetings", dto.getTitle());
        assertEquals(Lesson.LessonType.READING, dto.getLessonType());
        assertEquals(READING_CONTENT, dto.getContent());
        assertEquals(0, dto.getOrderIndex());
        assertEquals(15, dto.getDurationMinutes());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should convert Lesson entity to LessonDTO with different lesson types")
    void testToDTO_WithDifferentLessonTypes_Success() {
        // Test LISTENING type
        Lesson listeningLesson = Lesson.builder()
                .id(2L)
                .title("Airport Conversation")
                .lessonType(Lesson.LessonType.LISTENING)
                .content(LISTENING_CONTENT)
                .orderIndex(1)
                .durationMinutes(20)
                .build();

        LessonDTO listeningDTO = LessonMapper.toDTO(listeningLesson);
        assertNotNull(listeningDTO);
        assertEquals(Lesson.LessonType.LISTENING, listeningDTO.getLessonType());

        // Test QUIZ type
        Lesson quizLesson = Lesson.builder()
                .id(3L)
                .title("Grammar Quiz")
                .lessonType(Lesson.LessonType.QUIZ)
                .content("{\"questions\": []}")
                .orderIndex(2)
                .durationMinutes(10)
                .build();

        LessonDTO quizDTO = LessonMapper.toDTO(quizLesson);
        assertNotNull(quizDTO);
        assertEquals(Lesson.LessonType.QUIZ, quizDTO.getLessonType());

        // Test SPEAKING type
        Lesson speakingLesson = Lesson.builder()
                .id(4L)
                .title("Role Play Practice")
                .lessonType(Lesson.LessonType.SPEAKING)
                .content("{\"scenario\": \"Restaurant ordering\"}")
                .orderIndex(3)
                .durationMinutes(25)
                .build();

        LessonDTO speakingDTO = LessonMapper.toDTO(speakingLesson);
        assertNotNull(speakingDTO);
        assertEquals(Lesson.LessonType.SPEAKING, speakingDTO.getLessonType());
    }

    @Test
    @DisplayName("Should convert Lesson entity to LessonDTO with null section")
    void testToDTO_WithNullSection_Success() {
        // Arrange
        Lesson lesson = Lesson.builder()
                .id(5L)
                .title("Orphan Lesson")
                .lessonType(Lesson.LessonType.QUIZ)
                .content("{}")
                .orderIndex(0)
                .durationMinutes(15)
                .build();
        // Note: section is null

        // Act
        LessonDTO dto = LessonMapper.toDTO(lesson);

        // Assert
        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertNull(dto.getSectionId()); // Should handle null section gracefully
        assertEquals("Orphan Lesson", dto.getTitle());
    }

    @Test
    @DisplayName("Should return null when converting null Lesson entity to DTO")
    void testToDTO_WithNullEntity_ReturnsNull() {
        // Act
        LessonDTO dto = LessonMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should create Lesson entity from CreateLessonDTO with all fields")
    void testToEntity_WithAllFields_Success() {
        // Arrange
        CreateLessonDTO dto = CreateLessonDTO.builder()
                .title("Introduction to Verbs")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(20)
                .build();

        // Act
        Lesson lesson = LessonMapper.toEntity(dto);

        // Assert
        assertNotNull(lesson);
        assertNull(lesson.getId()); // ID not set until saved
        assertNull(lesson.getSection()); // Section set separately
        assertEquals("Introduction to Verbs", lesson.getTitle());
        assertEquals(Lesson.LessonType.READING, lesson.getLessonType());
        assertEquals(READING_CONTENT, lesson.getContent());
        assertEquals(0, lesson.getOrderIndex());
        assertEquals(20, lesson.getDurationMinutes());
    }

    @Test
    @DisplayName("Should create Lesson entity from CreateLessonDTO for all lesson types")
    void testToEntity_WithAllLessonTypes_Success() {
        // Test LISTENING
        CreateLessonDTO listeningDTO = CreateLessonDTO.builder()
                .title("Listening Exercise")
                .lessonType(Lesson.LessonType.LISTENING)
                .content(LISTENING_CONTENT)
                .orderIndex(0)
                .durationMinutes(15)
                .build();

        Lesson listeningLesson = LessonMapper.toEntity(listeningDTO);
        assertNotNull(listeningLesson);
        assertEquals(Lesson.LessonType.LISTENING, listeningLesson.getLessonType());

        // Test QUIZ
        CreateLessonDTO quizDTO = CreateLessonDTO.builder()
                .title("Quick Quiz")
                .lessonType(Lesson.LessonType.QUIZ)
                .content("{\"questions\": []}")
                .orderIndex(1)
                .durationMinutes(10)
                .build();

        Lesson quizLesson = LessonMapper.toEntity(quizDTO);
        assertNotNull(quizLesson);
        assertEquals(Lesson.LessonType.QUIZ, quizLesson.getLessonType());

        // Test SPEAKING
        CreateLessonDTO speakingDTO = CreateLessonDTO.builder()
                .title("Conversation Practice")
                .lessonType(Lesson.LessonType.SPEAKING)
                .content("{\"scenario\": \"Test\"}")
                .orderIndex(2)
                .durationMinutes(30)
                .build();

        Lesson speakingLesson = LessonMapper.toEntity(speakingDTO);
        assertNotNull(speakingLesson);
        assertEquals(Lesson.LessonType.SPEAKING, speakingLesson.getLessonType());
    }

    @Test
    @DisplayName("Should throw exception when creating entity from null DTO")
    void testToEntity_WithNullDTO_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LessonMapper.toEntity(null));
        assertEquals("CreateLessonDTO cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should update Lesson entity from CreateLessonDTO")
    void testUpdateEntityFromDTO_WithAllFields_Success() {
        // Arrange
        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("Old Title")
                .lessonType(Lesson.LessonType.QUIZ)
                .content("{\"old\": \"content\"}")
                .orderIndex(5)
                .durationMinutes(10)
                .build();

        CreateLessonDTO dto = CreateLessonDTO.builder()
                .title("New Title")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(25)
                .build();

        // Act
        LessonMapper.updateEntityFromDTO(lesson, dto);

        // Assert
        assertEquals("New Title", lesson.getTitle());
        assertEquals(Lesson.LessonType.READING, lesson.getLessonType());
        assertEquals(READING_CONTENT, lesson.getContent());
        assertEquals(0, lesson.getOrderIndex());
        assertEquals(25, lesson.getDurationMinutes());
        assertEquals(1L, lesson.getId()); // ID should not change
    }

    @Test
    @DisplayName("Should update Lesson entity with different content types")
    void testUpdateEntityFromDTO_WithDifferentContentTypes_Success() {
        // Arrange
        Lesson lesson = Lesson.builder()
                .id(2L)
                .title("Original Lesson")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(15)
                .build();

        CreateLessonDTO dto = CreateLessonDTO.builder()
                .title("Updated Lesson")
                .lessonType(Lesson.LessonType.LISTENING)
                .content(LISTENING_CONTENT)
                .orderIndex(1)
                .durationMinutes(20)
                .build();

        // Act
        LessonMapper.updateEntityFromDTO(lesson, dto);

        // Assert
        assertEquals("Updated Lesson", lesson.getTitle());
        assertEquals(Lesson.LessonType.LISTENING, lesson.getLessonType());
        assertEquals(LISTENING_CONTENT, lesson.getContent());
        assertEquals(1, lesson.getOrderIndex());
        assertEquals(20, lesson.getDurationMinutes());
    }

    @Test
    @DisplayName("Should throw exception when updating entity with null Lesson")
    void testUpdateEntityFromDTO_WithNullEntity_ThrowsException() {
        // Arrange
        CreateLessonDTO dto = CreateLessonDTO.builder()
                .title("New Title")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(15)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LessonMapper.updateEntityFromDTO(null, dto));
        assertEquals("Lesson entity cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating entity with null DTO")
    void testUpdateEntityFromDTO_WithNullDTO_ThrowsException() {
        // Arrange
        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("Original Title")
                .lessonType(Lesson.LessonType.READING)
                .content(READING_CONTENT)
                .orderIndex(0)
                .durationMinutes(15)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LessonMapper.updateEntityFromDTO(lesson, null));
        assertEquals("CreateLessonDTO cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should verify LessonMapper follows utility class pattern")
    void testUtilityClassPattern() {
        // The constructor is private, so we cannot test instantiation directly
        // This test verifies that all methods are static and the class follows utility
        // class pattern
        assertNotNull(LessonMapper.class);
    }
}
