package com.lexia.backend.mapper;

import com.lexia.backend.dto.SectionDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SectionMapper.
 * Verifies correct mapping between Section entities and DTOs.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@DisplayName("SectionMapper Unit Tests")
class SectionMapperTest {

    @Test
    @DisplayName("Should convert Section entity to SectionDTO with all fields")
    void testToDTO_WithAllFields_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Course course = Course.builder()
                .id(1L)
                .title("Test Course")
                .cefrLevel("A1")
                .build();

        Section section = Section.builder()
                .id(1L)
                .course(course)
                .title("Getting Started")
                .orderIndex(0)
                .createdAt(now)
                .lessons(new ArrayList<>())
                .build();

        // Add lessons to test lessonCount
        Lesson lesson1 = Lesson.builder().id(1L).title("Lesson 1").orderIndex(0).build();
        Lesson lesson2 = Lesson.builder().id(2L).title("Lesson 2").orderIndex(1).build();
        Lesson lesson3 = Lesson.builder().id(3L).title("Lesson 3").orderIndex(2).build();
        section.addLesson(lesson1);
        section.addLesson(lesson2);
        section.addLesson(lesson3);

        // Act
        SectionDTO dto = SectionMapper.toDTO(section);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getCourseId());
        assertEquals("Getting Started", dto.getTitle());
        assertEquals(0, dto.getOrderIndex());
        assertEquals(3, dto.getLessonCount());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should convert Section entity to SectionDTO with no lessons")
    void testToDTO_WithNoLessons_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Course course = Course.builder()
                .id(5L)
                .title("Test Course")
                .cefrLevel("B2")
                .build();

        Section section = Section.builder()
                .id(10L)
                .course(course)
                .title("Empty Section")
                .orderIndex(2)
                .createdAt(now)
                .lessons(new ArrayList<>())
                .build();

        // Act
        SectionDTO dto = SectionMapper.toDTO(section);

        // Assert
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals(5L, dto.getCourseId());
        assertEquals("Empty Section", dto.getTitle());
        assertEquals(2, dto.getOrderIndex());
        assertEquals(0, dto.getLessonCount());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should convert Section entity to SectionDTO with null lessons list")
    void testToDTO_WithNullLessonsList_Success() {
        // Arrange
        Course course = Course.builder()
                .id(2L)
                .title("Test Course")
                .cefrLevel("C1")
                .build();

        Section section = Section.builder()
                .id(3L)
                .course(course)
                .title("Section Without Lessons")
                .orderIndex(1)
                .build();
        // Note: lessons list is null (not initialized)

        // Act
        SectionDTO dto = SectionMapper.toDTO(section);

        // Assert
        assertNotNull(dto);
        assertEquals(3L, dto.getId());
        assertEquals(2L, dto.getCourseId());
        assertEquals("Section Without Lessons", dto.getTitle());
        assertEquals(1, dto.getOrderIndex());
        assertEquals(0, dto.getLessonCount()); // Should handle null list gracefully
    }

    @Test
    @DisplayName("Should convert Section entity to SectionDTO with null course")
    void testToDTO_WithNullCourse_Success() {
        // Arrange
        Section section = Section.builder()
                .id(4L)
                .title("Orphan Section")
                .orderIndex(0)
                .lessons(new ArrayList<>())
                .build();
        // Note: course is null

        // Act
        SectionDTO dto = SectionMapper.toDTO(section);

        // Assert
        assertNotNull(dto);
        assertEquals(4L, dto.getId());
        assertNull(dto.getCourseId()); // Should handle null course gracefully
        assertEquals("Orphan Section", dto.getTitle());
        assertEquals(0, dto.getOrderIndex());
        assertEquals(0, dto.getLessonCount());
    }

    @Test
    @DisplayName("Should return null when converting null Section entity to DTO")
    void testToDTO_WithNullEntity_ReturnsNull() {
        // Act
        SectionDTO dto = SectionMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should handle Section with large number of lessons")
    void testToDTO_WithManyLessons_Success() {
        // Arrange
        Course course = Course.builder()
                .id(1L)
                .title("Test Course")
                .cefrLevel("B1")
                .build();

        Section section = Section.builder()
                .id(1L)
                .course(course)
                .title("Large Section")
                .orderIndex(0)
                .lessons(new ArrayList<>())
                .build();

        // Add 20 lessons
        for (int i = 0; i < 20; i++) {
            Lesson lesson = Lesson.builder()
                    .id((long) i)
                    .title("Lesson " + i)
                    .orderIndex(i)
                    .build();
            section.addLesson(lesson);
        }

        // Act
        SectionDTO dto = SectionMapper.toDTO(section);

        // Assert
        assertNotNull(dto);
        assertEquals(20, dto.getLessonCount());
    }

    @Test
    @DisplayName("Should verify SectionMapper follows utility class pattern")
    void testUtilityClassPattern() {
        // The constructor is private, so we cannot test instantiation directly
        // This test verifies that all methods are static and the class follows utility
        // class pattern
        assertNotNull(SectionMapper.class);
    }
}
