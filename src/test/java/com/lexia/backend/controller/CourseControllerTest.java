package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.DuplicateCourseException;
import com.lexia.backend.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CourseController.
 * Tests all course management endpoints with various scenarios.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@WebMvcTest(CourseController.class)
@ContextConfiguration(classes = { CourseController.class, GlobalExceptionHandler.class })
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CourseDTO mockCourseDTO;
    private CreateCourseDTO createCourseDTO;
    private UpdateCourseDTO updateCourseDTO;

    @BeforeEach
    void setUp() {
        // Setup mock course DTO
        mockCourseDTO = CourseDTO.builder()
                .id(1L)
                .title("English Basics (A1)")
                .description("Foundation course for beginners")
                .thumbnailUrl("https://cdn.lexia.com/courses/a1-basics.jpg")
                .cefrLevel("A1")
                .isPublished(true)
                .sectionCount(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup create DTO
        createCourseDTO = CreateCourseDTO.builder()
                .title("Business English for Professionals")
                .description("Master business English for office communication")
                .thumbnailUrl("https://cdn.lexia.com/courses/business-english.jpg")
                .cefrLevel("B1")
                .build();

        // Setup update DTO
        updateCourseDTO = UpdateCourseDTO.builder()
                .title("Advanced Business English")
                .description("Master advanced business English for executive-level communication")
                .cefrLevel("C1")
                .build();
    }

    // ========== GET /api/v1/courses - Get All Published Courses Tests ==========

    @Test
    @WithMockUser
    void getAllPublishedCourses_WithDefaultPagination_ReturnsOk() throws Exception {
        // Given
        List<CourseDTO> courses = Arrays.asList(mockCourseDTO);
        Page<CourseDTO> page = new PageImpl<>(courses, PageRequest.of(0, 10), 1);
        when(courseService.getAllPublished(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("English Basics (A1)"))
                .andExpect(jsonPath("$.content[0].cefrLevel").value("A1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(courseService, times(1)).getAllPublished(any());
    }

    @Test
    @WithMockUser
    void getAllPublishedCourses_WithCustomPagination_ReturnsOk() throws Exception {
        // Given
        Page<CourseDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 5), 0);
        when(courseService.getAllPublished(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses")
                .param("page", "1")
                .param("size", "5")
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(courseService, times(1)).getAllPublished(any());
    }

    @Test
    @WithMockUser
    void getAllPublishedCourses_EnforcesMaxPageSize_ReturnsOk() throws Exception {
        // Given
        Page<CourseDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 100), 0);
        when(courseService.getAllPublished(any())).thenReturn(page);

        // When & Then - Request size > 100, should be capped at 100
        mockMvc.perform(get("/api/v1/courses")
                .param("size", "200"))
                .andExpect(status().isOk());

        verify(courseService, times(1)).getAllPublished(any());
    }

    @Test
    void getAllPublishedCourses_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isUnauthorized());

        verify(courseService, never()).getAllPublished(any());
    }

    // ========== GET /api/v1/courses/{id} - Get Course By ID Tests ==========

    @Test
    @WithMockUser
    void getCourseById_WithValidId_ReturnsOk() throws Exception {
        // Given
        when(courseService.getById(1L)).thenReturn(mockCourseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("English Basics (A1)"))
                .andExpect(jsonPath("$.cefrLevel").value("A1"))
                .andExpect(jsonPath("$.isPublished").value(true))
                .andExpect(jsonPath("$.sectionCount").value(3));

        verify(courseService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser
    void getCourseById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(courseService.getById(999L))
                .thenThrow(new CourseNotFoundException("Course not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/courses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Course Not Found"))
                .andExpect(jsonPath("$.message").value("Course not found with id: 999"));

        verify(courseService, times(1)).getById(999L);
    }

    @Test
    void getCourseById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/courses/1"))
                .andExpect(status().isUnauthorized());

        verify(courseService, never()).getById(any());
    }

    // ========== GET /api/v1/courses/search - Search Courses Tests ==========

    @Test
    @WithMockUser
    void searchCourses_WithTitleFilter_ReturnsOk() throws Exception {
        // Given
        List<CourseDTO> courses = Arrays.asList(mockCourseDTO);
        Page<CourseDTO> page = new PageImpl<>(courses, PageRequest.of(0, 10), 1);
        when(courseService.search(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses/search")
                .param("title", "business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(courseService, times(1)).search(any());
    }

    @Test
    @WithMockUser
    void searchCourses_WithCefrLevelFilter_ReturnsOk() throws Exception {
        // Given
        Page<CourseDTO> page = new PageImpl<>(Arrays.asList(mockCourseDTO), PageRequest.of(0, 10), 1);
        when(courseService.search(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses/search")
                .param("cefrLevel", "A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cefrLevel").value("A1"));

        verify(courseService, times(1)).search(any());
    }

    @Test
    @WithMockUser
    void searchCourses_WithPublishedFilter_ReturnsOk() throws Exception {
        // Given
        Page<CourseDTO> page = new PageImpl<>(Arrays.asList(mockCourseDTO), PageRequest.of(0, 10), 1);
        when(courseService.search(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses/search")
                .param("isPublished", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].isPublished").value(true));

        verify(courseService, times(1)).search(any());
    }

    @Test
    @WithMockUser
    void searchCourses_WithMultipleFilters_ReturnsOk() throws Exception {
        // Given
        Page<CourseDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(courseService.search(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/courses/search")
                .param("title", "business")
                .param("cefrLevel", "B1")
                .param("isPublished", "true"))
                .andExpect(status().isOk());

        verify(courseService, times(1)).search(any());
    }

    // ========== POST /api/v1/courses - Create Course Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createCourse_WithValidData_ReturnsCreated() throws Exception {
        // Given
        when(courseService.create(any(CreateCourseDTO.class))).thenReturn(mockCourseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCourseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("English Basics (A1)"))
                .andExpect(jsonPath("$.cefrLevel").value("A1"));

        verify(courseService, times(1)).create(any(CreateCourseDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createCourse_WithInvalidCefrLevel_ReturnsBadRequest() throws Exception {
        // Given
        CreateCourseDTO invalidDTO = CreateCourseDTO.builder()
                .title("Test Course")
                .cefrLevel("D1") // Invalid CEFR level
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(courseService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createCourse_WithMissingTitle_ReturnsBadRequest() throws Exception {
        // Given
        CreateCourseDTO invalidDTO = CreateCourseDTO.builder()
                .cefrLevel("A1")
                .build(); // Missing title

        // When & Then
        mockMvc.perform(post("/api/v1/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(courseService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createCourse_WithDuplicateTitle_ReturnsConflict() throws Exception {
        // Given
        when(courseService.create(any(CreateCourseDTO.class)))
                .thenThrow(new DuplicateCourseException("Course with this title already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCourseDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Duplicate Course"))
                .andExpect(jsonPath("$.message").value("Course with this title already exists"));

        verify(courseService, times(1)).create(any(CreateCourseDTO.class));
    }

    // Note: Authorization tests (401/403) are not included in @WebMvcTest
    // as it doesn't fully configure Spring Security. These should be tested
    // in integration tests with @SpringBootTest.

    // ========== PUT /api/v1/courses/{id} - Update Course Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateCourse_WithValidData_ReturnsOk() throws Exception {
        // Given
        CourseDTO updatedCourse = CourseDTO.builder()
                .id(1L)
                .title("Advanced Business English")
                .cefrLevel("C1")
                .isPublished(false)
                .build();
        when(courseService.update(eq(1L), any(UpdateCourseDTO.class))).thenReturn(updatedCourse);

        // When & Then
        mockMvc.perform(put("/api/v1/courses/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCourseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Advanced Business English"))
                .andExpect(jsonPath("$.cefrLevel").value("C1"));

        verify(courseService, times(1)).update(eq(1L), any(UpdateCourseDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateCourse_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(courseService.update(eq(999L), any(UpdateCourseDTO.class)))
                .thenThrow(new CourseNotFoundException("Course not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/courses/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCourseDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(courseService, times(1)).update(eq(999L), any(UpdateCourseDTO.class));
    }

    // ========== DELETE /api/v1/courses/{id} - Delete Course Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteCourse_WithValidId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(courseService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/courses/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteCourse_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new CourseNotFoundException("Course not found with id: 999"))
                .when(courseService).delete(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/courses/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(courseService, times(1)).delete(999L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteCourse_WithPublishedCourse_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalStateException("Cannot delete published course. Unpublish it first."))
                .when(courseService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/courses/1")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot delete published course. Unpublish it first."));

        verify(courseService, times(1)).delete(1L);
    }

    // ========== POST /api/v1/courses/{id}/publish - Publish Course Tests
    // ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void publishCourse_WithValidId_ReturnsOk() throws Exception {
        // Given
        CourseDTO publishedCourse = CourseDTO.builder()
                .id(1L)
                .title("English Basics (A1)")
                .isPublished(true)
                .build();
        when(courseService.publish(1L)).thenReturn(publishedCourse);

        // When & Then
        mockMvc.perform(post("/api/v1/courses/1/publish")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isPublished").value(true));

        verify(courseService, times(1)).publish(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void publishCourse_WithoutContent_ReturnsBadRequest() throws Exception {
        // Given
        when(courseService.publish(1L))
                .thenThrow(new IllegalStateException("Cannot publish course without sections and lessons"));

        // When & Then
        mockMvc.perform(post("/api/v1/courses/1/publish")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot publish course without sections and lessons"));

        verify(courseService, times(1)).publish(1L);
    }

    // ========== POST /api/v1/courses/{id}/unpublish - Unpublish Course Tests
    // ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void unpublishCourse_WithValidId_ReturnsOk() throws Exception {
        // Given
        CourseDTO unpublishedCourse = CourseDTO.builder()
                .id(1L)
                .title("English Basics (A1)")
                .isPublished(false)
                .build();
        when(courseService.unpublish(1L)).thenReturn(unpublishedCourse);

        // When & Then
        mockMvc.perform(post("/api/v1/courses/1/unpublish")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isPublished").value(false));

        verify(courseService, times(1)).unpublish(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void unpublishCourse_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(courseService.unpublish(999L))
                .thenThrow(new CourseNotFoundException("Course not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/api/v1/courses/999/unpublish")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(courseService, times(1)).unpublish(999L);
    }

}
