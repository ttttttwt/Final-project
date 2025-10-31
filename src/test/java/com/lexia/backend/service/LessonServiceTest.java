package com.lexia.backend.service;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import com.lexia.backend.exception.InvalidLessonContentException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.SectionRepository;
import com.lexia.backend.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LessonService.
 * Tests all CRUD operations, JSONB validation, and exception cases.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LessonService Tests")
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private LessonContentValidator contentValidator;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Section validSection;
    private Course validCourse;
    private Lesson validLesson;
    private CreateLessonDTO validCreateDTO;
    private UpdateLessonDTO validUpdateDTO;

    private String validReadingContent;
    private String validListeningContent;
    private String validQuizContent;
    private String validSpeakingContent;

    @BeforeEach
    void setUp() {
        // Setup valid course
        validCourse = Course.builder()
                .id(1L)
                .title("English Basics")
                .cefrLevel("A1")
                .isPublished(false)
                .sections(new ArrayList<>())
                .build();

        // Setup valid section
        validSection = Section.builder()
                .id(1L)
                .title("Getting Started")
                .orderIndex(0)
                .course(validCourse)
                .lessons(new ArrayList<>())
                .build();

        // Setup valid lesson content
        validReadingContent = """
                {
                    "passages": [
                        {"text": "Hello world", "audioUrl": "https://example.com/audio.mp3"}
                    ],
                    "questions": [
                        {
                            "id": "q1",
                            "questionText": "What is the greeting?",
                            "options": ["Hello", "Goodbye"],
                            "correctAnswer": 0,
                            "explanation": "Hello is a greeting"
                        }
                    ]
                }
                """;

        validListeningContent = """
                {
                    "audioUrl": "https://example.com/audio.mp3",
                    "duration": 120,
                    "transcript": "This is a transcript",
                    "questions": [
                        {
                            "id": "q1",
                            "questionText": "What did you hear?",
                            "options": ["Option A", "Option B"],
                            "correctAnswer": 0,
                            "timestamp": 30
                        }
                    ]
                }
                """;

        validQuizContent = """
                {
                    "questions": [
                        {
                            "id": "q1",
                            "questionText": "What is 2+2?",
                            "options": ["3", "4"],
                            "correctAnswer": 1,
                            "explanation": "2+2=4",
                            "points": 10
                        }
                    ],
                    "passingScore": 70,
                    "timeLimit": 300
                }
                """;

        validSpeakingContent = """
                {
                    "scenario": "Order food at a restaurant",
                    "difficulty": "BEGINNER",
                    "prompts": [
                        {
                            "order": 1,
                            "speaker": "AI",
                            "text": "Welcome! What would you like to order?"
                        }
                    ],
                    "suggestedAnswers": ["I'd like a burger", "Can I have pizza?"],
                    "evaluationCriteria": ["pronunciation", "grammar"],
                    "maxTurns": 5
                }
                """;

        // Setup CreateLessonDTO
        validCreateDTO = CreateLessonDTO.builder()
                .title("Introduction to English")
                .lessonType(Lesson.LessonType.READING)
                .content(validReadingContent)
                .durationMinutes(15)
                .build();

        // Setup UpdateLessonDTO
        validUpdateDTO = UpdateLessonDTO.builder()
                .title("Updated Introduction")
                .durationMinutes(20)
                .build();

        // Setup valid lesson entity
        validLesson = Lesson.builder()
                .id(1L)
                .title("Introduction to English")
                .lessonType(Lesson.LessonType.READING)
                .content(validReadingContent)
                .orderIndex(0)
                .durationMinutes(15)
                .section(validSection)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("Create lesson with valid data succeeds")
    void testCreate_WithValidData_Success() {
        // Arrange
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(validSection));
        doNothing().when(contentValidator).validate(any(), anyString());
        when(lessonRepository.findMaxOrderIndexBySectionId(1L)).thenReturn(null);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        LessonDTO result = lessonService.create(1L, validCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Introduction to English", result.getTitle());
        assertEquals(Lesson.LessonType.READING, result.getLessonType());
        assertEquals(15, result.getDurationMinutes());

        verify(sectionRepository).findById(1L);
        verify(contentValidator).validate(Lesson.LessonType.READING, validReadingContent);
        verify(lessonRepository).findMaxOrderIndexBySectionId(1L);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Create lesson with auto-calculated order index succeeds")
    void testCreate_WithAutoOrderIndex_Success() {
        // Arrange
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(validSection));
        doNothing().when(contentValidator).validate(any(), anyString());
        when(lessonRepository.findMaxOrderIndexBySectionId(1L)).thenReturn(2); // Existing lessons
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            assertEquals(3, lesson.getOrderIndex()); // Should be max + 1
            return lesson;
        });

        // Act
        lessonService.create(1L, validCreateDTO);

        // Assert
        verify(lessonRepository).findMaxOrderIndexBySectionId(1L);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Create lesson with explicit order index succeeds")
    void testCreate_WithExplicitOrderIndex_Success() {
        // Arrange
        CreateLessonDTO dtoWithOrder = CreateLessonDTO.builder()
                .title("Lesson with Order")
                .lessonType(Lesson.LessonType.READING)
                .content(validReadingContent)
                .orderIndex(5)
                .build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(validSection));
        doNothing().when(contentValidator).validate(any(), anyString());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            assertEquals(5, lesson.getOrderIndex());
            return lesson;
        });

        // Act
        lessonService.create(1L, dtoWithOrder);

        // Assert
        verify(lessonRepository, never()).findMaxOrderIndexBySectionId(anyLong());
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Create lesson with non-existent section throws SectionNotFoundException")
    void testCreate_WithNonExistentSection_ThrowsException() {
        // Arrange
        when(sectionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        SectionNotFoundException exception = assertThrows(
                SectionNotFoundException.class,
                () -> lessonService.create(999L, validCreateDTO));

        assertEquals("Section not found with ID: 999", exception.getMessage());
        verify(sectionRepository).findById(999L);
        verify(contentValidator, never()).validate(any(), anyString());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Create lesson with invalid content throws InvalidLessonContentException")
    void testCreate_WithInvalidContent_ThrowsException() {
        // Arrange
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(validSection));
        doThrow(new InvalidLessonContentException("Invalid reading content: missing passages"))
                .when(contentValidator).validate(any(), anyString());

        // Act & Assert
        InvalidLessonContentException exception = assertThrows(
                InvalidLessonContentException.class,
                () -> lessonService.create(1L, validCreateDTO));

        assertEquals("Invalid reading content: missing passages", exception.getMessage());
        verify(contentValidator).validate(Lesson.LessonType.READING, validReadingContent);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Create lesson validates all lesson types correctly")
    void testCreate_ValidatesAllLessonTypes() {
        // Test READING
        testLessonTypeCreation(Lesson.LessonType.READING, validReadingContent);

        // Test LISTENING
        testLessonTypeCreation(Lesson.LessonType.LISTENING, validListeningContent);

        // Test QUIZ
        testLessonTypeCreation(Lesson.LessonType.QUIZ, validQuizContent);

        // Test SPEAKING
        testLessonTypeCreation(Lesson.LessonType.SPEAKING, validSpeakingContent);
    }

    private void testLessonTypeCreation(Lesson.LessonType type, String content) {
        // Arrange
        CreateLessonDTO dto = CreateLessonDTO.builder()
                .title("Test " + type)
                .lessonType(type)
                .content(content)
                .build();

        Lesson lesson = Lesson.builder()
                .id(1L)
                .title(dto.getTitle())
                .lessonType(type)
                .content(content)
                .section(validSection)
                .build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(validSection));
        doNothing().when(contentValidator).validate(type, content);
        when(lessonRepository.findMaxOrderIndexBySectionId(1L)).thenReturn(null);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        // Act
        LessonDTO result = lessonService.create(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(type, result.getLessonType());
        verify(contentValidator).validate(type, content);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Update lesson with valid data succeeds")
    void testUpdate_WithValidData_Success() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        LessonDTO result = lessonService.update(1L, validUpdateDTO);

        // Assert
        assertNotNull(result);
        verify(lessonRepository).findById(1L);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Update lesson with content validates correctly")
    void testUpdate_WithContent_ValidatesContent() {
        // Arrange
        UpdateLessonDTO dtoWithContent = UpdateLessonDTO.builder()
                .content(validReadingContent)
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        doNothing().when(contentValidator).validate(Lesson.LessonType.READING, validReadingContent);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        lessonService.update(1L, dtoWithContent);

        // Assert
        verify(contentValidator).validate(Lesson.LessonType.READING, validReadingContent);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Update lesson with new lesson type validates with new type")
    void testUpdate_WithNewLessonType_ValidatesWithNewType() {
        // Arrange
        UpdateLessonDTO dtoWithTypeChange = UpdateLessonDTO.builder()
                .lessonType(Lesson.LessonType.LISTENING)
                .content(validListeningContent)
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        doNothing().when(contentValidator).validate(Lesson.LessonType.LISTENING, validListeningContent);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        lessonService.update(1L, dtoWithTypeChange);

        // Assert
        verify(contentValidator).validate(Lesson.LessonType.LISTENING, validListeningContent);
    }

    @Test
    @DisplayName("Update lesson with invalid content throws InvalidLessonContentException")
    void testUpdate_WithInvalidContent_ThrowsException() {
        // Arrange
        UpdateLessonDTO dtoWithInvalidContent = UpdateLessonDTO.builder()
                .content("{\"invalid\": \"content\"}")
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        doThrow(new InvalidLessonContentException("Invalid content"))
                .when(contentValidator).validate(any(), anyString());

        // Act & Assert
        InvalidLessonContentException exception = assertThrows(
                InvalidLessonContentException.class,
                () -> lessonService.update(1L, dtoWithInvalidContent));

        assertEquals("Invalid content", exception.getMessage());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Update non-existent lesson throws LessonNotFoundException")
    void testUpdate_WithNonExistentLesson_ThrowsException() {
        // Arrange
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LessonNotFoundException exception = assertThrows(
                LessonNotFoundException.class,
                () -> lessonService.update(999L, validUpdateDTO));

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
        verify(lessonRepository).findById(999L);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Update lesson with partial data succeeds")
    void testUpdate_WithPartialData_Success() {
        // Arrange
        UpdateLessonDTO partialDTO = UpdateLessonDTO.builder()
                .durationMinutes(30)
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        LessonDTO result = lessonService.update(1L, partialDTO);

        // Assert
        assertNotNull(result);
        verify(contentValidator, never()).validate(any(), anyString());
        verify(lessonRepository).save(any(Lesson.class));
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("Get lesson by ID succeeds")
    void testGetById_WithValidId_Success() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));

        // Act
        LessonDTO result = lessonService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Introduction to English", result.getTitle());
        verify(lessonRepository).findById(1L);
    }

    @Test
    @DisplayName("Get non-existent lesson throws LessonNotFoundException")
    void testGetById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LessonNotFoundException exception = assertThrows(
                LessonNotFoundException.class,
                () -> lessonService.getById(999L));

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
        verify(lessonRepository).findById(999L);
    }

    // ========== GET ALL BY SECTION TESTS ==========

    @Test
    @DisplayName("Get all lessons by section ID succeeds")
    void testGetAllBySectionId_WithValidSection_Success() {
        // Arrange
        List<Lesson> lessons = List.of(validLesson);
        when(sectionRepository.existsById(1L)).thenReturn(true);
        when(lessonRepository.findBySectionIdOrderByOrderIndexAsc(1L)).thenReturn(lessons);

        // Act
        List<LessonDTO> result = lessonService.getAllBySectionId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Introduction to English", result.get(0).getTitle());
        verify(sectionRepository).existsById(1L);
        verify(lessonRepository).findBySectionIdOrderByOrderIndexAsc(1L);
    }

    @Test
    @DisplayName("Get all lessons by non-existent section throws SectionNotFoundException")
    void testGetAllBySectionId_WithNonExistentSection_ThrowsException() {
        // Arrange
        when(sectionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        SectionNotFoundException exception = assertThrows(
                SectionNotFoundException.class,
                () -> lessonService.getAllBySectionId(999L));

        assertEquals("Section not found with ID: 999", exception.getMessage());
        verify(sectionRepository).existsById(999L);
        verify(lessonRepository, never()).findBySectionIdOrderByOrderIndexAsc(anyLong());
    }

    @Test
    @DisplayName("Get all lessons by section returns ordered list")
    void testGetAllBySectionId_ReturnsOrderedList() {
        // Arrange
        Lesson lesson1 = Lesson.builder().id(1L).orderIndex(0).section(validSection).build();
        Lesson lesson2 = Lesson.builder().id(2L).orderIndex(1).section(validSection).build();
        Lesson lesson3 = Lesson.builder().id(3L).orderIndex(2).section(validSection).build();

        List<Lesson> lessons = List.of(lesson1, lesson2, lesson3);
        when(sectionRepository.existsById(1L)).thenReturn(true);
        when(lessonRepository.findBySectionIdOrderByOrderIndexAsc(1L)).thenReturn(lessons);

        // Act
        List<LessonDTO> result = lessonService.getAllBySectionId(1L);

        // Assert
        assertEquals(3, result.size());
        verify(lessonRepository).findBySectionIdOrderByOrderIndexAsc(1L);
    }

    // ========== GET ALL BY COURSE TESTS ==========

    @Test
    @DisplayName("Get all lessons by course ID succeeds")
    void testGetAllByCourseId_WithValidCourse_Success() {
        // Arrange
        List<Lesson> lessons = List.of(validLesson);
        when(lessonRepository.findAllByCourseIdOrderBySectionAndLesson(1L)).thenReturn(lessons);

        // Act
        List<LessonDTO> result = lessonService.getAllByCourseId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(lessonRepository).findAllByCourseIdOrderBySectionAndLesson(1L);
    }

    @Test
    @DisplayName("Get all lessons by course returns empty list for no lessons")
    void testGetAllByCourseId_WithNoLessons_ReturnsEmptyList() {
        // Arrange
        when(lessonRepository.findAllByCourseIdOrderBySectionAndLesson(1L)).thenReturn(new ArrayList<>());

        // Act
        List<LessonDTO> result = lessonService.getAllByCourseId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Delete lesson succeeds")
    void testDelete_WithValidId_Success() {
        // Arrange
        when(lessonRepository.existsById(1L)).thenReturn(true);
        doNothing().when(lessonRepository).deleteById(1L);

        // Act
        lessonService.delete(1L);

        // Assert
        verify(lessonRepository).existsById(1L);
        verify(lessonRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete non-existent lesson throws LessonNotFoundException")
    void testDelete_WithNonExistentLesson_ThrowsException() {
        // Arrange
        when(lessonRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        LessonNotFoundException exception = assertThrows(
                LessonNotFoundException.class,
                () -> lessonService.delete(999L));

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
        verify(lessonRepository).existsById(999L);
        verify(lessonRepository, never()).deleteById(anyLong());
    }

    // ========== REORDER TESTS ==========

    @Test
    @DisplayName("Reorder lesson succeeds")
    void testReorder_WithValidData_Success() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(validLesson);

        // Act
        LessonDTO result = lessonService.reorder(1L, 5);

        // Assert
        assertNotNull(result);
        verify(lessonRepository).findById(1L);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Reorder non-existent lesson throws LessonNotFoundException")
    void testReorder_WithNonExistentLesson_ThrowsException() {
        // Arrange
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        LessonNotFoundException exception = assertThrows(
                LessonNotFoundException.class,
                () -> lessonService.reorder(999L, 5));

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
        verify(lessonRepository).findById(999L);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Reorder lesson updates order index correctly")
    void testReorder_UpdatesOrderIndexCorrectly() {
        // Arrange
        Integer newPosition = 10;
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(validLesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            assertEquals(newPosition, lesson.getOrderIndex());
            return lesson;
        });

        // Act
        lessonService.reorder(1L, newPosition);

        // Assert
        verify(lessonRepository).save(any(Lesson.class));
    }
}
