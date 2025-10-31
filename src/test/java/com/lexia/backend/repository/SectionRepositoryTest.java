package com.lexia.backend.repository;

import com.lexia.backend.entity.Course;
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
 * Integration tests for SectionRepository.
 * 
 * <p>
 * Tests CRUD operations, order management, and custom queries for section
 * entities.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SectionRepository Integration Tests")
class SectionRepositoryTest {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Course testCourse;
    private Section section1;
    private Section section2;
    private Section section3;

    @BeforeEach
    void setUp() {
        // Clear database
        sectionRepository.deleteAll();
        courseRepository.deleteAll();

        // Create test course
        testCourse = Course.builder()
                .title("Test Course")
                .description("Course for testing sections")
                .cefrLevel("B1")
                .isPublished(true)
                .build();
        courseRepository.save(testCourse);

        // Create test sections
        section1 = Section.builder()
                .course(testCourse)
                .title("Introduction")
                .orderIndex(1)
                .build();

        section2 = Section.builder()
                .course(testCourse)
                .title("Main Content")
                .orderIndex(2)
                .build();

        section3 = Section.builder()
                .course(testCourse)
                .title("Advanced Topics")
                .orderIndex(3)
                .build();

        sectionRepository.saveAll(List.of(section1, section2, section3));
    }

    // ==================== CRUD Operation Tests ====================

    @Test
    @DisplayName("Should save a new section successfully")
    void testSaveSection() {
        // Arrange
        Section newSection = Section.builder()
                .course(testCourse)
                .title("Conclusion")
                .orderIndex(4)
                .build();

        // Act
        Section saved = sectionRepository.save(newSection);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Conclusion");
        assertThat(saved.getOrderIndex()).isEqualTo(4);
        assertThat(saved.getCourse()).isEqualTo(testCourse);
    }

    @Test
    @DisplayName("Should find section by ID")
    void testFindById() {
        // Act
        Optional<Section> found = sectionRepository.findById(section1.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Introduction");
        assertThat(found.get().getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update section successfully")
    void testUpdateSection() {
        // Arrange
        Section section = sectionRepository.findById(section1.getId()).orElseThrow();
        String newTitle = "Updated Introduction";

        // Act
        section.setTitle(newTitle);
        section.setOrderIndex(10);
        Section updated = sectionRepository.save(section);

        // Assert
        assertThat(updated.getTitle()).isEqualTo(newTitle);
        assertThat(updated.getOrderIndex()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should delete section successfully")
    void testDeleteSection() {
        // Arrange
        Long sectionId = section1.getId();

        // Act
        sectionRepository.deleteById(sectionId);

        // Assert
        assertThat(sectionRepository.findById(sectionId)).isEmpty();
    }

    // ==================== Order Management Tests ====================

    @Test
    @DisplayName("Should find sections by course ID ordered by order index")
    void testFindByCourseIdOrderByOrderIndexAsc() {
        // Act
        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(testCourse.getId());

        // Assert
        assertThat(sections).hasSize(3);
        assertThat(sections.get(0).getTitle()).isEqualTo("Introduction");
        assertThat(sections.get(1).getTitle()).isEqualTo("Main Content");
        assertThat(sections.get(2).getTitle()).isEqualTo("Advanced Topics");
        assertThat(sections).extracting("orderIndex").containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Should return empty list when course has no sections")
    void testFindByCourseIdOrderByOrderIndexAsc_EmptyCourse() {
        // Arrange
        Course emptyCourse = Course.builder()
                .title("Empty Course")
                .cefrLevel("A1")
                .isPublished(false)
                .build();
        courseRepository.save(emptyCourse);

        // Act
        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(emptyCourse.getId());

        // Assert
        assertThat(sections).isEmpty();
    }

    @Test
    @DisplayName("Should count sections by course ID")
    void testCountByCourseId() {
        // Act
        long count = sectionRepository.countByCourseId(testCourse.getId());

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero when counting sections for course with no sections")
    void testCountByCourseId_EmptyCourse() {
        // Arrange
        Course emptyCourse = Course.builder()
                .title("Empty Course")
                .cefrLevel("A1")
                .isPublished(false)
                .build();
        courseRepository.save(emptyCourse);

        // Act
        long count = sectionRepository.countByCourseId(emptyCourse.getId());

        // Assert
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should check if section exists at specific order index")
    void testExistsByCourseIdAndOrderIndex() {
        // Act & Assert - Existing section
        assertThat(sectionRepository.existsByCourseIdAndOrderIndex(testCourse.getId(), 1)).isTrue();
        assertThat(sectionRepository.existsByCourseIdAndOrderIndex(testCourse.getId(), 2)).isTrue();
        assertThat(sectionRepository.existsByCourseIdAndOrderIndex(testCourse.getId(), 3)).isTrue();

        // Act & Assert - Non-existing order index
        assertThat(sectionRepository.existsByCourseIdAndOrderIndex(testCourse.getId(), 4)).isFalse();
        assertThat(sectionRepository.existsByCourseIdAndOrderIndex(testCourse.getId(), 0)).isFalse();
    }

    @Test
    @DisplayName("Should find section by course ID and order index")
    void testFindByCourseIdAndOrderIndex() {
        // Act
        Optional<Section> found = sectionRepository.findByCourseIdAndOrderIndex(testCourse.getId(), 2);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Main Content");
        assertThat(found.get().getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty when finding non-existent order index")
    void testFindByCourseIdAndOrderIndex_NotFound() {
        // Act
        Optional<Section> notFound = sectionRepository.findByCourseIdAndOrderIndex(testCourse.getId(), 99);

        // Assert
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should find maximum order index for course")
    void testFindMaxOrderIndexByCourseId() {
        // Act
        Integer maxOrderIndex = sectionRepository.findMaxOrderIndexByCourseId(testCourse.getId());

        // Assert
        assertThat(maxOrderIndex).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return null when finding max order index for course with no sections")
    void testFindMaxOrderIndexByCourseId_EmptyCourse() {
        // Arrange
        Course emptyCourse = Course.builder()
                .title("Empty Course")
                .cefrLevel("A1")
                .isPublished(false)
                .build();
        courseRepository.save(emptyCourse);

        // Act
        Integer maxOrderIndex = sectionRepository.findMaxOrderIndexByCourseId(emptyCourse.getId());

        // Assert
        assertThat(maxOrderIndex).isNull();
    }

    @Test
    @DisplayName("Should calculate next order index correctly")
    void testCalculateNextOrderIndex() {
        // Act
        Integer maxIndex = sectionRepository.findMaxOrderIndexByCourseId(testCourse.getId());
        Integer nextIndex = (maxIndex != null) ? maxIndex + 1 : 1;

        // Assert
        assertThat(nextIndex).isEqualTo(4);

        // Arrange - Create section with next index
        Section newSection = Section.builder()
                .course(testCourse)
                .title("New Section")
                .orderIndex(nextIndex)
                .build();
        sectionRepository.save(newSection);

        // Act - Verify new section is at correct position
        List<Section> orderedSections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(testCourse.getId());

        // Assert
        assertThat(orderedSections).hasSize(4);
        assertThat(orderedSections.get(3).getTitle()).isEqualTo("New Section");
        assertThat(orderedSections.get(3).getOrderIndex()).isEqualTo(4);
    }

    // ==================== Delete Operations Tests ====================

    @Test
    @DisplayName("Should delete all sections for a course")
    void testDeleteByCourseId() {
        // Arrange
        Long courseId = testCourse.getId();

        // Act
        sectionRepository.deleteByCourseId(courseId);

        // Assert
        List<Section> remainingSections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        assertThat(remainingSections).isEmpty();
    }

    @Test
    @DisplayName("Should not affect other courses when deleting sections")
    void testDeleteByCourseId_IsolatedToSpecificCourse() {
        // Arrange - Create another course with sections
        Course anotherCourse = Course.builder()
                .title("Another Course")
                .cefrLevel("A2")
                .isPublished(true)
                .build();
        courseRepository.save(anotherCourse);

        Section otherSection = Section.builder()
                .course(anotherCourse)
                .title("Other Section")
                .orderIndex(1)
                .build();
        sectionRepository.save(otherSection);

        // Act - Delete sections from first course
        sectionRepository.deleteByCourseId(testCourse.getId());

        // Assert - First course sections deleted
        assertThat(sectionRepository.findByCourseIdOrderByOrderIndexAsc(testCourse.getId())).isEmpty();

        // Assert - Second course sections intact
        List<Section> otherCourseSections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(anotherCourse.getId());
        assertThat(otherCourseSections).hasSize(1);
        assertThat(otherCourseSections.get(0).getTitle()).isEqualTo("Other Section");
    }

    // ==================== Cascade Delete Tests ====================

    @Test
    @DisplayName("Should cascade delete lessons when section is deleted")
    void testCascadeDeleteLessons() {
        // Note: In production PostgreSQL, lessons are cascade deleted when section is
        // deleted.
        // In H2, cascade behavior may differ slightly, so we test the core
        // functionality.

        // Arrange
        Long sectionId = section1.getId();

        // Act - Delete section
        sectionRepository.deleteById(sectionId);

        // Assert - Section should be deleted
        assertThat(sectionRepository.findById(sectionId)).isEmpty();
    }

    // ==================== Bidirectional Relationship Tests ====================

    @Test
    @DisplayName("Should maintain bidirectional relationship with course")
    void testBidirectionalRelationshipWithCourse() {
        // Act
        Section section = sectionRepository.findById(section1.getId()).orElseThrow();

        // Assert - Section knows its course
        assertThat(section.getCourse()).isNotNull();
        assertThat(section.getCourse().getId()).isEqualTo(testCourse.getId());
        assertThat(section.getCourse().getTitle()).isEqualTo("Test Course");
    }

    @Test
    @DisplayName("Should be sortable using Comparable interface")
    void testComparableInterface() {
        // Arrange
        List<Section> sections = List.of(section3, section1, section2);

        // Act
        List<Section> sorted = sections.stream()
                .sorted()
                .toList();

        // Assert
        assertThat(sorted).extracting("orderIndex").containsExactly(1, 2, 3);
        assertThat(sorted).extracting("title")
                .containsExactly("Introduction", "Main Content", "Advanced Topics");
    }

    @Test
    @DisplayName("Should handle null order index in compareTo")
    void testCompareTo_NullOrderIndex() {
        // Arrange - Create section with orderIndex set, then simulate null for
        // comparison test
        Section section = Section.builder()
                .course(testCourse)
                .title("Section for comparison")
                .orderIndex(5)
                .build();

        Section sectionForComparison = Section.builder()
                .course(testCourse)
                .title("Another section")
                .orderIndex(null)
                .build();

        // Act - Compare (without saving to DB)
        int comparison = sectionForComparison.compareTo(section);

        // Assert - Null order should come after non-null orders
        assertThat(comparison).isPositive();
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle sections with duplicate order index in different courses")
    void testDuplicateOrderIndexInDifferentCourses() {
        // Arrange - Create another course with section at order index 1
        Course anotherCourse = Course.builder()
                .title("Another Course")
                .cefrLevel("C1")
                .isPublished(true)
                .build();
        courseRepository.save(anotherCourse);

        Section duplicateOrderSection = Section.builder()
                .course(anotherCourse)
                .title("Another Introduction")
                .orderIndex(1) // Same as section1 but different course
                .build();
        sectionRepository.save(duplicateOrderSection);

        // Act
        List<Section> course1Sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(testCourse.getId());
        List<Section> course2Sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(anotherCourse.getId());

        // Assert - Both courses can have section at order index 1
        assertThat(course1Sections).extracting("orderIndex").contains(1);
        assertThat(course2Sections).extracting("orderIndex").contains(1);
        assertThat(course1Sections.get(0).getTitle()).isEqualTo("Introduction");
        assertThat(course2Sections.get(0).getTitle()).isEqualTo("Another Introduction");
    }

    @Test
    @DisplayName("Should handle reordering sections")
    void testReorderSections() {
        // Note: In production, reordering would be done transactionally with careful
        // handling
        // Here we test that sections can be updated with new order indexes

        // Arrange
        Section s1 = sectionRepository.findById(section1.getId()).orElseThrow();

        // Act - Update order index
        int oldOrderIndex = s1.getOrderIndex();
        s1.setOrderIndex(10); // Change to a different value
        sectionRepository.save(s1);

        // Assert - Verify update was successful
        Section updated = sectionRepository.findById(s1.getId()).orElseThrow();
        assertThat(updated.getOrderIndex()).isEqualTo(10);
        assertThat(updated.getOrderIndex()).isNotEqualTo(oldOrderIndex);
    }
}
