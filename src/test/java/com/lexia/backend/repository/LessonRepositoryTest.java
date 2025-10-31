package com.lexia.backend.repository;

import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Lesson.LessonType;
import com.lexia.backend.entity.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LessonRepository.
 * 
 * <p>
 * Tests CRUD operations, JSONB content handling, lesson type filtering, and
 * cross-section queries.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("LessonRepository Integration Tests")
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Course testCourse;
    private Section section1;
    private Section section2;
    private Lesson readingLesson;
    private Lesson listeningLesson;
    private Lesson quizLesson;
    private Lesson speakingLesson;

    @BeforeEach
    void setUp() {
        // Clear database
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();

        // Create test course
        testCourse = Course.builder()
                .title("Test Course")
                .cefrLevel("B1")
                .isPublished(true)
                .build();
        courseRepository.save(testCourse);

        // Create test sections
        section1 = Section.builder()
                .course(testCourse)
                .title("Grammar Basics")
                .orderIndex(1)
                .build();

        section2 = Section.builder()
                .course(testCourse)
                .title("Practice Exercises")
                .orderIndex(2)
                .build();

        sectionRepository.saveAll(List.of(section1, section2));

        // Create test lessons with different types and JSONB content
        readingLesson = Lesson.builder()
                .section(section1)
                .title("Reading Comprehension")
                .lessonType(LessonType.READING)
                .content(
                        "{\"passages\": [{\"text\": \"Sample passage\"}], \"questions\": [{\"question\": \"What is the main idea?\", \"options\": [\"A\", \"B\", \"C\"], \"correctAnswer\": 0}]}")
                .orderIndex(1)
                .durationMinutes(20)
                .build();

        listeningLesson = Lesson.builder()
                .section(section1)
                .title("Listening Practice")
                .lessonType(LessonType.LISTENING)
                .content(
                        "{\"audioUrl\": \"https://example.com/audio.mp3\", \"duration\": 180, \"transcript\": \"Sample transcript\", \"questions\": []}")
                .orderIndex(2)
                .durationMinutes(15)
                .build();

        quizLesson = Lesson.builder()
                .section(section2)
                .title("Grammar Quiz")
                .lessonType(LessonType.QUIZ)
                .content(
                        "{\"questions\": [{\"question\": \"Choose correct answer\", \"options\": [\"is\", \"are\"], \"correctAnswer\": 0, \"points\": 10}], \"passingScore\": 70}")
                .orderIndex(1)
                .durationMinutes(10)
                .build();

        speakingLesson = Lesson.builder()
                .section(section2)
                .title("Speaking Exercise")
                .lessonType(LessonType.SPEAKING)
                .content(
                        "{\"scenario\": \"Job interview\", \"difficulty\": \"INTERMEDIATE\", \"prompts\": [\"Introduce yourself\", \"Describe your experience\"], \"maxTurns\": 5}")
                .orderIndex(2)
                .durationMinutes(25)
                .build();

        lessonRepository.saveAll(List.of(readingLesson, listeningLesson, quizLesson, speakingLesson));
    }

    // ==================== CRUD Operation Tests ====================

    @Test
    @DisplayName("Should save a new lesson successfully")
    void testSaveLesson() {
        // Arrange
        Lesson newLesson = Lesson.builder()
                .section(section1)
                .title("Vocabulary Building")
                .lessonType(LessonType.READING)
                .content("{\"passages\": [], \"questions\": [], \"vocabulary\": []}")
                .orderIndex(3)
                .durationMinutes(15)
                .build();

        // Act
        Lesson saved = lessonRepository.save(newLesson);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Vocabulary Building");
        assertThat(saved.getLessonType()).isEqualTo(LessonType.READING);
        assertThat(saved.getContent()).contains("vocabulary");
        assertThat(saved.getDurationMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should find lesson by ID")
    void testFindById() {
        // Act
        Optional<Lesson> found = lessonRepository.findById(readingLesson.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Reading Comprehension");
        assertThat(found.get().getLessonType()).isEqualTo(LessonType.READING);
    }

    @Test
    @DisplayName("Should update lesson successfully")
    void testUpdateLesson() {
        // Arrange
        Lesson lesson = lessonRepository.findById(quizLesson.getId()).orElseThrow();
        String newTitle = "Advanced Grammar Quiz";
        String newContent = "{\"questions\": [], \"passingScore\": 80}";

        // Act
        lesson.setTitle(newTitle);
        lesson.setContent(newContent);
        lesson.setDurationMinutes(20);
        Lesson updated = lessonRepository.save(lesson);

        // Assert
        assertThat(updated.getTitle()).isEqualTo(newTitle);
        assertThat(updated.getContent()).contains("passingScore\": 80");
        assertThat(updated.getDurationMinutes()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should delete lesson successfully")
    void testDeleteLesson() {
        // Arrange
        Long lessonId = readingLesson.getId();

        // Act
        lessonRepository.deleteById(lessonId);

        // Assert
        assertThat(lessonRepository.findById(lessonId)).isEmpty();
    }

    // ==================== JSONB Content Tests ====================

    @Test
    @DisplayName("Should store and retrieve JSONB content correctly for READING lesson")
    void testJsonbContent_Reading() {
        // Act
        Lesson lesson = lessonRepository.findById(readingLesson.getId()).orElseThrow();

        // Assert
        assertThat(lesson.getContent()).isNotNull();
        assertThat(lesson.getContent()).contains("passages");
        assertThat(lesson.getContent()).contains("questions");
        assertThat(lesson.getContent()).contains("Sample passage");
        assertThat(lesson.getContent()).contains("correctAnswer");
    }

    @Test
    @DisplayName("Should store and retrieve JSONB content correctly for LISTENING lesson")
    void testJsonbContent_Listening() {
        // Act
        Lesson lesson = lessonRepository.findById(listeningLesson.getId()).orElseThrow();

        // Assert
        assertThat(lesson.getContent()).contains("audioUrl");
        assertThat(lesson.getContent()).contains("https://example.com/audio.mp3");
        assertThat(lesson.getContent()).contains("duration");
        assertThat(lesson.getContent()).contains("transcript");
    }

    @Test
    @DisplayName("Should store and retrieve JSONB content correctly for QUIZ lesson")
    void testJsonbContent_Quiz() {
        // Act
        Lesson lesson = lessonRepository.findById(quizLesson.getId()).orElseThrow();

        // Assert
        assertThat(lesson.getContent()).contains("questions");
        assertThat(lesson.getContent()).contains("passingScore");
        assertThat(lesson.getContent()).contains("points");
    }

    @Test
    @DisplayName("Should store and retrieve JSONB content correctly for SPEAKING lesson")
    void testJsonbContent_Speaking() {
        // Act
        Lesson lesson = lessonRepository.findById(speakingLesson.getId()).orElseThrow();

        // Assert
        assertThat(lesson.getContent()).contains("scenario");
        assertThat(lesson.getContent()).contains("Job interview");
        assertThat(lesson.getContent()).contains("difficulty");
        assertThat(lesson.getContent()).contains("prompts");
        assertThat(lesson.getContent()).contains("maxTurns");
    }

    // ==================== Section-Level Query Tests ====================

    @Test
    @DisplayName("Should find lessons by section ID ordered by order index")
    void testFindBySectionIdOrderByOrderIndexAsc() {
        // Act
        List<Lesson> section1Lessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section1.getId());

        // Assert
        assertThat(section1Lessons).hasSize(2);
        assertThat(section1Lessons.get(0).getTitle()).isEqualTo("Reading Comprehension");
        assertThat(section1Lessons.get(1).getTitle()).isEqualTo("Listening Practice");
        assertThat(section1Lessons).extracting("orderIndex").containsExactly(1, 2);
    }

    @Test
    @DisplayName("Should count lessons in a section")
    void testCountBySectionId() {
        // Act
        long section1Count = lessonRepository.countBySectionId(section1.getId());
        long section2Count = lessonRepository.countBySectionId(section2.getId());

        // Assert
        assertThat(section1Count).isEqualTo(2);
        assertThat(section2Count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if lesson exists at specific order index")
    void testExistsBySectionIdAndOrderIndex() {
        // Act & Assert - Existing lessons
        assertThat(lessonRepository.existsBySectionIdAndOrderIndex(section1.getId(), 1)).isTrue();
        assertThat(lessonRepository.existsBySectionIdAndOrderIndex(section1.getId(), 2)).isTrue();

        // Act & Assert - Non-existing order index
        assertThat(lessonRepository.existsBySectionIdAndOrderIndex(section1.getId(), 3)).isFalse();
    }

    @Test
    @DisplayName("Should find lesson by section ID and order index")
    void testFindBySectionIdAndOrderIndex() {
        // Act
        Optional<Lesson> found = lessonRepository.findBySectionIdAndOrderIndex(section2.getId(), 1);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Grammar Quiz");
        assertThat(found.get().getLessonType()).isEqualTo(LessonType.QUIZ);
    }

    @Test
    @DisplayName("Should find maximum order index for section")
    void testFindMaxOrderIndexBySectionId() {
        // Act
        Integer maxIndex = lessonRepository.findMaxOrderIndexBySectionId(section1.getId());

        // Assert
        assertThat(maxIndex).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return null when finding max order index for section with no lessons")
    void testFindMaxOrderIndexBySectionId_EmptySection() {
        // Arrange
        Section emptySection = Section.builder()
                .course(testCourse)
                .title("Empty Section")
                .orderIndex(3)
                .build();
        sectionRepository.save(emptySection);

        // Act
        Integer maxIndex = lessonRepository.findMaxOrderIndexBySectionId(emptySection.getId());

        // Assert
        assertThat(maxIndex).isNull();
    }

    // ==================== Lesson Type Filter Tests ====================

    @Test
    @DisplayName("Should find all lessons by type")
    void testFindByLessonType() {
        // Act
        List<Lesson> readingLessons = lessonRepository.findByLessonType(LessonType.READING);
        List<Lesson> listeningLessons = lessonRepository.findByLessonType(LessonType.LISTENING);
        List<Lesson> quizLessons = lessonRepository.findByLessonType(LessonType.QUIZ);
        List<Lesson> speakingLessons = lessonRepository.findByLessonType(LessonType.SPEAKING);

        // Assert
        assertThat(readingLessons).hasSize(1);
        assertThat(readingLessons.get(0).getTitle()).isEqualTo("Reading Comprehension");

        assertThat(listeningLessons).hasSize(1);
        assertThat(listeningLessons.get(0).getTitle()).isEqualTo("Listening Practice");

        assertThat(quizLessons).hasSize(1);
        assertThat(quizLessons.get(0).getTitle()).isEqualTo("Grammar Quiz");

        assertThat(speakingLessons).hasSize(1);
        assertThat(speakingLessons.get(0).getTitle()).isEqualTo("Speaking Exercise");
    }

    @Test
    @DisplayName("Should count lessons by type in a section")
    void testCountBySectionIdAndLessonType() {
        // Act
        long readingCount = lessonRepository.countBySectionIdAndLessonType(section1.getId(), LessonType.READING);
        long listeningCount = lessonRepository.countBySectionIdAndLessonType(section1.getId(), LessonType.LISTENING);
        long quizCount = lessonRepository.countBySectionIdAndLessonType(section2.getId(), LessonType.QUIZ);
        long speakingCount = lessonRepository.countBySectionIdAndLessonType(section2.getId(), LessonType.SPEAKING);

        // Assert
        assertThat(readingCount).isEqualTo(1);
        assertThat(listeningCount).isEqualTo(1);
        assertThat(quizCount).isEqualTo(1);
        assertThat(speakingCount).isEqualTo(1);
    }

    // ==================== Cross-Section (Course-Level) Query Tests
    // ====================

    @Test
    @DisplayName("Should find all lessons in a course ordered by section and lesson order")
    void testFindAllByCourseIdOrderBySectionAndLesson() {
        // Act
        List<Lesson> allLessons = lessonRepository.findAllByCourseIdOrderBySectionAndLesson(testCourse.getId());

        // Assert
        assertThat(allLessons).hasSize(4);
        // Should be ordered: section1 lessons (order 1, 2), then section2 lessons
        // (order 1, 2)
        assertThat(allLessons).extracting("title").containsExactly(
                "Reading Comprehension", // Section 1, Order 1
                "Listening Practice", // Section 1, Order 2
                "Grammar Quiz", // Section 2, Order 1
                "Speaking Exercise" // Section 2, Order 2
        );
    }

    @Test
    @DisplayName("Should count total lessons in a course")
    void testCountByCourseId() {
        // Act
        long totalLessons = lessonRepository.countByCourseId(testCourse.getId());

        // Assert
        assertThat(totalLessons).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find lessons by type in a course")
    void testFindByCourseIdAndLessonType() {
        // Arrange - Add another READING lesson to section2
        Lesson additionalReading = Lesson.builder()
                .section(section2)
                .title("Reading Exercise 2")
                .lessonType(LessonType.READING)
                .content("{\"passages\": [], \"questions\": []}")
                .orderIndex(3)
                .durationMinutes(15)
                .build();
        lessonRepository.save(additionalReading);

        // Act
        List<Lesson> readingLessons = lessonRepository.findByCourseIdAndLessonType(
                testCourse.getId(), LessonType.READING);

        // Assert
        assertThat(readingLessons).hasSize(2);
        assertThat(readingLessons).extracting("title")
                .containsExactly("Reading Comprehension", "Reading Exercise 2");
    }

    @Test
    @DisplayName("Should return empty list when course has no lessons")
    void testFindAllByCourseIdOrderBySectionAndLesson_EmptyCourse() {
        // Arrange
        Course emptyCourse = Course.builder()
                .title("Empty Course")
                .cefrLevel("A1")
                .isPublished(false)
                .build();
        courseRepository.save(emptyCourse);

        // Act
        List<Lesson> lessons = lessonRepository.findAllByCourseIdOrderBySectionAndLesson(emptyCourse.getId());

        // Assert
        assertThat(lessons).isEmpty();
    }

    // ==================== Delete Operations Tests ====================

    @Test
    @DisplayName("Should delete all lessons for a section")
    void testDeleteBySectionId() {
        // Arrange
        Long sectionId = section1.getId();

        // Act
        lessonRepository.deleteBySectionId(sectionId);

        // Assert
        List<Lesson> remainingLessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
        assertThat(remainingLessons).isEmpty();

        // Assert - Lessons from other sections are intact
        List<Lesson> section2Lessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section2.getId());
        assertThat(section2Lessons).hasSize(2);
    }

    // ==================== Duration Validation Tests ====================

    @Test
    @DisplayName("Should handle minimum duration value (1 minute)")
    void testMinimumDuration() {
        // Arrange
        Lesson shortLesson = Lesson.builder()
                .section(section1)
                .title("Quick Tip")
                .lessonType(LessonType.READING)
                .content("{\"passages\": [], \"questions\": []}")
                .orderIndex(10)
                .durationMinutes(1)
                .build();

        // Act
        Lesson saved = lessonRepository.save(shortLesson);

        // Assert
        assertThat(saved.getDurationMinutes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle maximum duration value (240 minutes)")
    void testMaximumDuration() {
        // Arrange
        Lesson longLesson = Lesson.builder()
                .section(section1)
                .title("Comprehensive Module")
                .lessonType(LessonType.READING)
                .content("{\"passages\": [], \"questions\": []}")
                .orderIndex(11)
                .durationMinutes(240)
                .build();

        // Act
        Lesson saved = lessonRepository.save(longLesson);

        // Assert
        assertThat(saved.getDurationMinutes()).isEqualTo(240);
    }

    // ==================== Order Management Tests ====================

    @Test
    @DisplayName("Should handle reordering lessons within a section")
    void testReorderLessons() {
        // Note: In production, reordering would be done transactionally with careful
        // handling
        // Here we test that lessons can be updated with new order indexes

        // Arrange
        Lesson reading = lessonRepository.findById(readingLesson.getId()).orElseThrow();

        // Act - Update order index
        int oldOrderIndex = reading.getOrderIndex();
        reading.setOrderIndex(10); // Change to a different value
        lessonRepository.save(reading);

        // Assert - Verify update was successful
        Lesson updated = lessonRepository.findById(reading.getId()).orElseThrow();
        assertThat(updated.getOrderIndex()).isEqualTo(10);
        assertThat(updated.getOrderIndex()).isNotEqualTo(oldOrderIndex);
    }

    @Test
    @DisplayName("Should calculate next order index correctly")
    void testCalculateNextOrderIndex() {
        // Act
        Integer maxIndex = lessonRepository.findMaxOrderIndexBySectionId(section1.getId());
        Integer nextIndex = (maxIndex != null) ? maxIndex + 1 : 1;

        // Assert
        assertThat(nextIndex).isEqualTo(3);

        // Arrange - Create lesson with next index
        Lesson newLesson = Lesson.builder()
                .section(section1)
                .title("New Lesson")
                .lessonType(LessonType.QUIZ)
                .content("{\"questions\": [], \"passingScore\": 70}")
                .orderIndex(nextIndex)
                .durationMinutes(10)
                .build();
        lessonRepository.save(newLesson);

        // Act - Verify new lesson is at correct position
        List<Lesson> orderedLessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section1.getId());

        // Assert
        assertThat(orderedLessons).hasSize(3);
        assertThat(orderedLessons.get(2).getTitle()).isEqualTo("New Lesson");
        assertThat(orderedLessons.get(2).getOrderIndex()).isEqualTo(3);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle lessons with same order index in different sections")
    void testDuplicateOrderIndexInDifferentSections() {
        // Both section1 and section2 have lessons at order index 1
        // Act
        Optional<Lesson> section1Lesson = lessonRepository.findBySectionIdAndOrderIndex(section1.getId(), 1);
        Optional<Lesson> section2Lesson = lessonRepository.findBySectionIdAndOrderIndex(section2.getId(), 1);

        // Assert
        assertThat(section1Lesson).isPresent();
        assertThat(section2Lesson).isPresent();
        assertThat(section1Lesson.get().getTitle()).isEqualTo("Reading Comprehension");
        assertThat(section2Lesson.get().getTitle()).isEqualTo("Grammar Quiz");
    }

    @Test
    @DisplayName("Should handle complex JSONB content")
    void testComplexJsonbContent() {
        // Arrange - Create lesson with nested JSONB structure
        String complexContent = """
                {
                    "passages": [
                        {
                            "text": "First passage",
                            "metadata": {"level": "B1", "wordCount": 150}
                        },
                        {
                            "text": "Second passage",
                            "metadata": {"level": "B2", "wordCount": 200}
                        }
                    ],
                    "questions": [
                        {
                            "question": "What is the theme?",
                            "options": ["Education", "Technology", "Health"],
                            "correctAnswer": 1,
                            "explanation": "The text focuses on technology"
                        }
                    ],
                    "vocabulary": [
                        {"word": "innovative", "definition": "introducing new ideas"}
                    ]
                }
                """;

        Lesson complexLesson = Lesson.builder()
                .section(section1)
                .title("Complex Reading")
                .lessonType(LessonType.READING)
                .content(complexContent)
                .orderIndex(20)
                .durationMinutes(30)
                .build();

        // Act
        Lesson saved = lessonRepository.save(complexLesson);
        Lesson retrieved = lessonRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getContent()).contains("passages");
        assertThat(retrieved.getContent()).contains("metadata");
        assertThat(retrieved.getContent()).contains("vocabulary");
        assertThat(retrieved.getContent()).contains("innovative");
        assertThat(retrieved.getContent()).contains("explanation");
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship with section")
    void testBidirectionalRelationshipWithSection() {
        // Act
        Lesson lesson = lessonRepository.findById(readingLesson.getId()).orElseThrow();

        // Assert - Lesson knows its section
        assertThat(lesson.getSection()).isNotNull();
        assertThat(lesson.getSection().getId()).isEqualTo(section1.getId());
        assertThat(lesson.getSection().getTitle()).isEqualTo("Grammar Basics");

        // Assert - Section knows its course
        assertThat(lesson.getSection().getCourse()).isNotNull();
        assertThat(lesson.getSection().getCourse().getId()).isEqualTo(testCourse.getId());
    }
}
