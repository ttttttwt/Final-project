package com.lexia.backend.service;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CourseSearchDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.DuplicateCourseException;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService.
 * Tests all CRUD operations, business rules, and exception cases.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CreateCourseDTO validCreateDTO;
    private UpdateCourseDTO validUpdateDTO;
    private Course validCourse;
    private Course publishedCourse;

    @BeforeEach
    void setUp() {
        // Setup valid CreateCourseDTO
        validCreateDTO = CreateCourseDTO.builder()
                .title("English Basics")
                .description("Learn the fundamentals of English")
                .thumbnailUrl("https://example.com/thumbnail.jpg")
                .cefrLevel("A1")
                .build();

        // Setup valid UpdateCourseDTO
        validUpdateDTO = UpdateCourseDTO.builder()
                .title("Updated English Basics")
                .description("Updated description")
                .build();

        // Setup valid Course entity
        validCourse = Course.builder()
                .id(1L)
                .title("English Basics")
                .description("Learn the fundamentals of English")
                .thumbnailUrl("https://example.com/thumbnail.jpg")
                .cefrLevel("A1")
                .isPublished(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sections(new ArrayList<>())
                .build();

        // Setup published course with sections and lessons
        publishedCourse = Course.builder()
                .id(2L)
                .title("Advanced English")
                .description("Advanced English course")
                .cefrLevel("C1")
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sections(new ArrayList<>())
                .build();

        Section section = Section.builder()
                .id(1L)
                .title("Getting Started")
                .orderIndex(0)
                .course(publishedCourse)
                .lessons(new ArrayList<>())
                .build();

        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("Introduction")
                .lessonType(Lesson.LessonType.READING)
                .content("{\"passages\": [], \"questions\": []}")
                .orderIndex(0)
                .section(section)
                .build();

        section.getLessons().add(lesson);
        publishedCourse.getSections().add(section);
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("Create course with valid data succeeds")
    void testCreate_WithValidData_Success() {
        // Arrange
        when(courseRepository.existsByTitle(anyString())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(validCourse);

        // Act
        CourseDTO result = courseService.create(validCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("English Basics", result.getTitle());
        assertEquals("Learn the fundamentals of English", result.getDescription());
        assertEquals("A1", result.getCefrLevel());
        assertFalse(result.getIsPublished());

        verify(courseRepository).existsByTitle("English Basics");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Create course with duplicate title throws DuplicateCourseException")
    void testCreate_WithDuplicateTitle_ThrowsException() {
        // Arrange
        when(courseRepository.existsByTitle("English Basics")).thenReturn(true);

        // Act & Assert
        DuplicateCourseException exception = assertThrows(
                DuplicateCourseException.class,
                () -> courseService.create(validCreateDTO));

        assertEquals("Course with title 'English Basics' already exists", exception.getMessage());
        verify(courseRepository).existsByTitle("English Basics");
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Create course with all CEFR levels succeeds")
    void testCreate_WithAllCefrLevels_Success() {
        // Test all valid CEFR levels
        String[] cefrLevels = { "A1", "A2", "B1", "B2", "C1", "C2" };

        for (String level : cefrLevels) {
            // Arrange
            CreateCourseDTO dto = CreateCourseDTO.builder()
                    .title("Course " + level)
                    .description("Description for " + level)
                    .cefrLevel(level)
                    .build();

            Course course = Course.builder()
                    .id(1L)
                    .title(dto.getTitle())
                    .cefrLevel(level)
                    .isPublished(false)
                    .sections(new ArrayList<>())
                    .build();

            when(courseRepository.existsByTitle(dto.getTitle())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            // Act
            CourseDTO result = courseService.create(dto);

            // Assert
            assertEquals(level, result.getCefrLevel());
            verify(courseRepository).existsByTitle(dto.getTitle());
        }
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Update course with valid data succeeds")
    void testUpdate_WithValidData_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
        when(courseRepository.existsByTitleAndIdNot("Updated English Basics", 1L)).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(validCourse);

        // Act
        CourseDTO result = courseService.update(1L, validUpdateDTO);

        // Assert
        assertNotNull(result);
        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByTitleAndIdNot("Updated English Basics", 1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Update non-existent course throws CourseNotFoundException")
    void testUpdate_WithNonExistentCourse_ThrowsException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.update(999L, validUpdateDTO));

        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository).findById(999L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Update course title to existing title throws DuplicateCourseException")
    void testUpdate_WithDuplicateTitle_ThrowsException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
        when(courseRepository.existsByTitleAndIdNot("Updated English Basics", 1L)).thenReturn(true);

        // Act & Assert
        DuplicateCourseException exception = assertThrows(
                DuplicateCourseException.class,
                () -> courseService.update(1L, validUpdateDTO));

        assertEquals("Course with title 'Updated English Basics' already exists", exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByTitleAndIdNot("Updated English Basics", 1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Update course with partial data succeeds")
    void testUpdate_WithPartialData_Success() {
        // Arrange
        UpdateCourseDTO partialDTO = UpdateCourseDTO.builder()
                .description("Only updating description")
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(validCourse);

        // Act
        CourseDTO result = courseService.update(1L, partialDTO);

        // Assert
        assertNotNull(result);
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
        verify(courseRepository, never()).existsByTitleAndIdNot(anyString(), anyLong());
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("Get course by ID succeeds")
    void testGetById_WithValidId_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));

        // Act
        CourseDTO result = courseService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("English Basics", result.getTitle());
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Get non-existent course throws CourseNotFoundException")
    void testGetById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.getById(999L));

        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository).findById(999L);
    }

    @Test
    @DisplayName("Get course by ID with sections succeeds")
    void testGetByIdWithSections_WithValidId_Success() {
        // Arrange
        when(courseRepository.findByIdWithSections(2L)).thenReturn(Optional.of(publishedCourse));

        // Act
        CourseDTO result = courseService.getByIdWithSections(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(courseRepository).findByIdWithSections(2L);
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Delete unpublished course succeeds")
    void testDelete_WithUnpublishedCourse_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
        doNothing().when(courseRepository).delete(any(Course.class));

        // Act
        courseService.delete(1L);

        // Assert
        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(validCourse);
    }

    @Test
    @DisplayName("Delete published course throws IllegalStateException")
    void testDelete_WithPublishedCourse_ThrowsException() {
        // Arrange
        when(courseRepository.findById(2L)).thenReturn(Optional.of(publishedCourse));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> courseService.delete(2L));

        assertEquals("Cannot delete published course. Unpublish first.", exception.getMessage());
        verify(courseRepository).findById(2L);
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    @DisplayName("Delete non-existent course throws CourseNotFoundException")
    void testDelete_WithNonExistentCourse_ThrowsException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.delete(999L));

        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository).findById(999L);
        verify(courseRepository, never()).delete(any(Course.class));
    }

    // ========== PUBLISH TESTS ==========

    @Test
    @DisplayName("Publish course with content succeeds")
    void testPublish_WithContent_Success() {
        // Arrange
        when(courseRepository.findByIdWithSections(2L)).thenReturn(Optional.of(publishedCourse));
        publishedCourse.setIsPublished(false); // Set to unpublished first
        when(courseRepository.save(any(Course.class))).thenReturn(publishedCourse);

        // Act
        CourseDTO result = courseService.publish(2L);

        // Assert
        assertNotNull(result);
        verify(courseRepository).findByIdWithSections(2L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Publish course without sections throws IllegalStateException")
    void testPublish_WithoutSections_ThrowsException() {
        // Arrange
        when(courseRepository.findByIdWithSections(1L)).thenReturn(Optional.of(validCourse));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> courseService.publish(1L));

        assertEquals("Cannot publish course without sections", exception.getMessage());
        verify(courseRepository).findByIdWithSections(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Publish course without lessons throws IllegalStateException")
    void testPublish_WithoutLessons_ThrowsException() {
        // Arrange
        Course courseWithEmptySection = Course.builder()
                .id(3L)
                .title("Test Course")
                .cefrLevel("A1")
                .isPublished(false)
                .sections(new ArrayList<>())
                .build();

        Section emptySection = Section.builder()
                .id(1L)
                .title("Empty Section")
                .orderIndex(0)
                .lessons(new ArrayList<>()) // No lessons
                .build();

        courseWithEmptySection.getSections().add(emptySection);

        when(courseRepository.findByIdWithSections(3L)).thenReturn(Optional.of(courseWithEmptySection));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> courseService.publish(3L));

        assertEquals("Cannot publish course without lessons", exception.getMessage());
        verify(courseRepository).findByIdWithSections(3L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Publish non-existent course throws CourseNotFoundException")
    void testPublish_WithNonExistentCourse_ThrowsException() {
        // Arrange
        when(courseRepository.findByIdWithSections(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.publish(999L));

        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository).findByIdWithSections(999L);
    }

    // ========== UNPUBLISH TESTS ==========

    @Test
    @DisplayName("Unpublish course succeeds")
    void testUnpublish_WithPublishedCourse_Success() {
        // Arrange
        when(courseRepository.findById(2L)).thenReturn(Optional.of(publishedCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(publishedCourse);

        // Act
        CourseDTO result = courseService.unpublish(2L);

        // Assert
        assertNotNull(result);
        verify(courseRepository).findById(2L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Unpublish non-existent course throws CourseNotFoundException")
    void testUnpublish_WithNonExistentCourse_ThrowsException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.unpublish(999L));

        assertEquals("Course not found with ID: 999", exception.getMessage());
        verify(courseRepository).findById(999L);
    }

    // ========== SEARCH TESTS ==========

    @Test
    @DisplayName("Search courses with criteria succeeds")
    void testSearch_WithCriteria_Success() {
        // Arrange
        CourseSearchDTO searchDTO = CourseSearchDTO.builder()
                .title("English")
                .cefrLevel("A1")
                .isPublished(true)
                .page(0)
                .size(10)
                .build();

        List<Course> courses = List.of(validCourse, publishedCourse);
        Page<Course> coursePage = new PageImpl<>(courses, PageRequest.of(0, 10), courses.size());

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.search(searchDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Search courses with pagination succeeds")
    void testSearch_WithPagination_Success() {
        // Arrange
        CourseSearchDTO searchDTO = CourseSearchDTO.builder()
                .page(1)
                .size(5)
                .build();

        Page<Course> coursePage = new PageImpl<>(new ArrayList<>(), PageRequest.of(1, 5), 0);

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.search(searchDTO);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Search courses with max size limit enforced")
    void testSearch_WithLargeSize_EnforcesMaxLimit() {
        // Arrange
        CourseSearchDTO searchDTO = CourseSearchDTO.builder()
                .size(500) // Requesting more than max (100)
                .build();

        Page<Course> coursePage = new PageImpl<>(new ArrayList<>());

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.search(searchDTO);

        // Assert
        assertNotNull(result);
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== GET ALL PUBLISHED TESTS ==========

    @Test
    @DisplayName("Get all published courses succeeds")
    void testGetAllPublished_Success() {
        // Arrange
        List<Course> courses = List.of(publishedCourse);
        Page<Course> coursePage = new PageImpl<>(courses, PageRequest.of(0, 10), courses.size());

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.getAllPublished(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== GET ALL TESTS ==========

    @Test
    @DisplayName("Get all courses succeeds")
    void testGetAll_Success() {
        // Arrange
        List<Course> courses = List.of(validCourse, publishedCourse);
        Page<Course> coursePage = new PageImpl<>(courses, PageRequest.of(0, 10), courses.size());

        when(courseRepository.findAll(any(Pageable.class))).thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.getAll(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(courseRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Get all courses with custom page size succeeds")
    void testGetAll_WithCustomPageSize_Success() {
        // Arrange
        List<Course> courses = List.of(validCourse);
        Page<Course> coursePage = new PageImpl<>(courses, PageRequest.of(0, 5), 1);

        when(courseRepository.findAll(any(Pageable.class))).thenReturn(coursePage);

        // Act
        Page<CourseDTO> result = courseService.getAll(PageRequest.of(0, 5));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(5, result.getSize());
        verify(courseRepository).findAll(any(Pageable.class));
    }
}
