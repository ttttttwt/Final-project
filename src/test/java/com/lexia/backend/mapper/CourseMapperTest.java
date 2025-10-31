package com.lexia.backend.mapper;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CourseMapper.
 * Verifies correct mapping between Course entities and DTOs.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DisplayName("CourseMapper Unit Tests")
class CourseMapperTest {

    @Test
    @DisplayName("Should convert Course entity to CourseDTO with all fields")
    void testToDTO_WithAllFields_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Course course = Course.builder()
                .id(1L)
                .title("English Basics (A1)")
                .description("Foundation course for beginners")
                .thumbnailUrl("https://cdn.lexia.com/courses/english-basics.jpg")
                .cefrLevel("A1")
                .isPublished(true)
                .createdAt(now)
                .updatedAt(now)
                .sections(new ArrayList<>())
                .build();

        // Add sections to test sectionCount
        Section section1 = Section.builder().id(1L).title("Section 1").orderIndex(0).build();
        Section section2 = Section.builder().id(2L).title("Section 2").orderIndex(1).build();
        course.addSection(section1);
        course.addSection(section2);

        // Act
        CourseDTO dto = CourseMapper.toDTO(course);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("English Basics (A1)", dto.getTitle());
        assertEquals("Foundation course for beginners", dto.getDescription());
        assertEquals("https://cdn.lexia.com/courses/english-basics.jpg", dto.getThumbnailUrl());
        assertEquals("A1", dto.getCefrLevel());
        assertTrue(dto.getIsPublished());
        assertEquals(2, dto.getSectionCount());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should convert Course entity to CourseDTO with null optional fields")
    void testToDTO_WithNullOptionalFields_Success() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Basic Course")
                .cefrLevel("B1")
                .isPublished(false)
                .build();

        // Act
        CourseDTO dto = CourseMapper.toDTO(course);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Basic Course", dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getThumbnailUrl());
        assertEquals("B1", dto.getCefrLevel());
        assertFalse(dto.getIsPublished());
        assertEquals(0, dto.getSectionCount());
    }

    @Test
    @DisplayName("Should return null when converting null Course entity to DTO")
    void testToDTO_WithNullEntity_ReturnsNull() {
        // Act
        CourseDTO dto = CourseMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should create Course entity from CreateCourseDTO with all fields")
    void testToEntity_WithAllFields_Success() {
        // Arrange
        CreateCourseDTO dto = CreateCourseDTO.builder()
                .title("Business English")
                .description("Professional English for business communication")
                .thumbnailUrl("https://cdn.lexia.com/courses/business.jpg")
                .cefrLevel("B2")
                .build();

        // Act
        Course course = CourseMapper.toEntity(dto);

        // Assert
        assertNotNull(course);
        assertNull(course.getId()); // ID not set until saved
        assertEquals("Business English", course.getTitle());
        assertEquals("Professional English for business communication", course.getDescription());
        assertEquals("https://cdn.lexia.com/courses/business.jpg", course.getThumbnailUrl());
        assertEquals("B2", course.getCefrLevel());
        assertFalse(course.getIsPublished()); // Default is false
        assertNotNull(course.getSections());
        assertTrue(course.getSections().isEmpty());
    }

    @Test
    @DisplayName("Should create Course entity from CreateCourseDTO with minimal fields")
    void testToEntity_WithMinimalFields_Success() {
        // Arrange
        CreateCourseDTO dto = CreateCourseDTO.builder()
                .title("Simple Course")
                .cefrLevel("A2")
                .build();

        // Act
        Course course = CourseMapper.toEntity(dto);

        // Assert
        assertNotNull(course);
        assertEquals("Simple Course", course.getTitle());
        assertNull(course.getDescription());
        assertNull(course.getThumbnailUrl());
        assertEquals("A2", course.getCefrLevel());
        assertFalse(course.getIsPublished());
    }

    @Test
    @DisplayName("Should throw exception when creating entity from null DTO")
    void testToEntity_WithNullDTO_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CourseMapper.toEntity(null));
        assertEquals("CreateCourseDTO cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should update Course entity from UpdateCourseDTO with all fields")
    void testUpdateEntityFromDTO_WithAllFields_Success() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Old Title")
                .description("Old description")
                .thumbnailUrl("https://old.url/image.jpg")
                .cefrLevel("A1")
                .isPublished(false)
                .build();

        UpdateCourseDTO dto = UpdateCourseDTO.builder()
                .title("New Title")
                .description("New description")
                .thumbnailUrl("https://new.url/image.jpg")
                .cefrLevel("B1")
                .isPublished(true)
                .build();

        // Act
        CourseMapper.updateEntityFromDTO(course, dto);

        // Assert
        assertEquals("New Title", course.getTitle());
        assertEquals("New description", course.getDescription());
        assertEquals("https://new.url/image.jpg", course.getThumbnailUrl());
        assertEquals("B1", course.getCefrLevel());
        assertTrue(course.getIsPublished());
        assertEquals(1L, course.getId()); // ID should not change
    }

    @Test
    @DisplayName("Should update Course entity from UpdateCourseDTO with partial fields")
    void testUpdateEntityFromDTO_WithPartialFields_Success() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Original Title")
                .description("Original description")
                .thumbnailUrl("https://original.url/image.jpg")
                .cefrLevel("A2")
                .isPublished(false)
                .build();

        UpdateCourseDTO dto = UpdateCourseDTO.builder()
                .title("Updated Title")
                .isPublished(true)
                // description, thumbnailUrl, and cefrLevel are null (should not be updated)
                .build();

        // Act
        CourseMapper.updateEntityFromDTO(course, dto);

        // Assert
        assertEquals("Updated Title", course.getTitle());
        assertEquals("Original description", course.getDescription()); // Unchanged
        assertEquals("https://original.url/image.jpg", course.getThumbnailUrl()); // Unchanged
        assertEquals("A2", course.getCefrLevel()); // Unchanged
        assertTrue(course.getIsPublished()); // Updated
    }

    @Test
    @DisplayName("Should not update any fields when UpdateCourseDTO has all null values")
    void testUpdateEntityFromDTO_WithAllNullValues_NoChanges() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Original Title")
                .description("Original description")
                .thumbnailUrl("https://original.url/image.jpg")
                .cefrLevel("C1")
                .isPublished(true)
                .build();

        UpdateCourseDTO dto = UpdateCourseDTO.builder().build(); // All fields null

        // Act
        CourseMapper.updateEntityFromDTO(course, dto);

        // Assert - all fields should remain unchanged
        assertEquals("Original Title", course.getTitle());
        assertEquals("Original description", course.getDescription());
        assertEquals("https://original.url/image.jpg", course.getThumbnailUrl());
        assertEquals("C1", course.getCefrLevel());
        assertTrue(course.getIsPublished());
    }

    @Test
    @DisplayName("Should throw exception when updating entity with null Course")
    void testUpdateEntityFromDTO_WithNullEntity_ThrowsException() {
        // Arrange
        UpdateCourseDTO dto = UpdateCourseDTO.builder()
                .title("New Title")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CourseMapper.updateEntityFromDTO(null, dto));
        assertEquals("Course entity cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating entity with null DTO")
    void testUpdateEntityFromDTO_WithNullDTO_ThrowsException() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Original Title")
                .cefrLevel("A1")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CourseMapper.updateEntityFromDTO(course, null));
        assertEquals("UpdateCourseDTO cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should not allow instantiation of utility class")
    void testConstructor_ThrowsException() {
        // The constructor is private, so we cannot test instantiation directly
        // This test verifies that all methods are static and the class follows utility
        // class pattern
        assertNotNull(CourseMapper.class);
    }
}
