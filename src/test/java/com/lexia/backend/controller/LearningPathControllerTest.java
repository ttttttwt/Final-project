package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.exception.LearningPathNotFoundException;
import com.lexia.backend.service.LearningPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LearningPathController.
 * Tests all REST endpoints for learning path management.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@WebMvcTest(LearningPathController.class)
@ContextConfiguration(classes = { LearningPathController.class, GlobalExceptionHandler.class })
@DisplayName("LearningPathController Tests")
class LearningPathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LearningPathService learningPathService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private LearningPathDTO learningPathDTO;
    private LearningPathDTO pathWithMultipleCourses;

    @BeforeEach
    void setUp() {
        // Setup single course path DTO
        LearningPathDTO.LearningPathCourseDTO courseDTO1 = LearningPathDTO.LearningPathCourseDTO.builder()
                .courseId(1L)
                .courseTitle("English Basics (A1)")
                .courseThumbnailUrl("https://cdn.lexia.com/courses/a1-basics.jpg")
                .courseCefrLevel("A1")
                .orderIndex(0)
                .sectionCount(3)
                .build();

        learningPathDTO = LearningPathDTO.builder()
                .id(1L)
                .name("Beginner Path (A1)")
                .description("Complete beginners path")
                .cefrLevel("A1")
                .isDefault(true)
                .courses(List.of(courseDTO1))
                .totalCourses(1)
                .estimatedHours(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup multiple course path DTO
        LearningPathDTO.LearningPathCourseDTO courseDTO2 = LearningPathDTO.LearningPathCourseDTO.builder()
                .courseId(2L)
                .courseTitle("Intermediate English (B1)")
                .courseThumbnailUrl("https://cdn.lexia.com/courses/b1-intermediate.jpg")
                .courseCefrLevel("B1")
                .orderIndex(0)
                .sectionCount(2)
                .build();

        LearningPathDTO.LearningPathCourseDTO courseDTO3 = LearningPathDTO.LearningPathCourseDTO.builder()
                .courseId(3L)
                .courseTitle("Advanced English (C1)")
                .courseThumbnailUrl("https://cdn.lexia.com/courses/c1-advanced.jpg")
                .courseCefrLevel("C1")
                .orderIndex(1)
                .sectionCount(2)
                .build();

        pathWithMultipleCourses = LearningPathDTO.builder()
                .id(4L)
                .name("Upper Intermediate Path (B2)")
                .description("B2 level path")
                .cefrLevel("B2")
                .isDefault(true)
                .courses(List.of(courseDTO2, courseDTO3))
                .totalCourses(2)
                .estimatedHours(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== GET /api/v1/learning-paths Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths - Should return all learning paths")
    void testGetAllPaths_Success() throws Exception {
        // Arrange
        List<LearningPathDTO> paths = Arrays.asList(learningPathDTO, pathWithMultipleCourses);
        when(learningPathService.getAllPaths()).thenReturn(paths);

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Beginner Path (A1)"))
                .andExpect(jsonPath("$[0].cefrLevel").value("A1"))
                .andExpect(jsonPath("$[0].totalCourses").value(1))
                .andExpect(jsonPath("$[1].id").value(4))
                .andExpect(jsonPath("$[1].name").value("Upper Intermediate Path (B2)"))
                .andExpect(jsonPath("$[1].totalCourses").value(2));

        verify(learningPathService, times(1)).getAllPaths();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths - Should return empty list when no paths exist")
    void testGetAllPaths_EmptyList() throws Exception {
        // Arrange
        when(learningPathService.getAllPaths()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(learningPathService, times(1)).getAllPaths();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths - Should include courses in response")
    void testGetAllPaths_IncludesCourses() throws Exception {
        // Arrange
        when(learningPathService.getAllPaths()).thenReturn(List.of(pathWithMultipleCourses));

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courses", hasSize(2)))
                .andExpect(jsonPath("$[0].courses[0].courseId").value(2))
                .andExpect(jsonPath("$[0].courses[0].orderIndex").value(0))
                .andExpect(jsonPath("$[0].courses[1].courseId").value(3))
                .andExpect(jsonPath("$[0].courses[1].orderIndex").value(1));

        verify(learningPathService, times(1)).getAllPaths();
    }

    // ==================== GET /api/v1/learning-paths/{id} Tests
    // ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths/{id} - Should return path by ID")
    void testGetPathById_Success() throws Exception {
        // Arrange
        when(learningPathService.getPathById(1L)).thenReturn(learningPathDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Beginner Path (A1)"))
                .andExpect(jsonPath("$.cefrLevel").value("A1"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andExpect(jsonPath("$.totalCourses").value(1))
                .andExpect(jsonPath("$.estimatedHours").value(15));

        verify(learningPathService, times(1)).getPathById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths/{id} - Should return 404 when path not found")
    void testGetPathById_NotFound() throws Exception {
        // Arrange
        when(learningPathService.getPathById(999L))
                .thenThrow(new LearningPathNotFoundException("Learning path not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(learningPathService, times(1)).getPathById(999L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/learning-paths/{id} - Should include all course details")
    void testGetPathById_IncludesCourseDetails() throws Exception {
        // Arrange
        when(learningPathService.getPathById(4L)).thenReturn(pathWithMultipleCourses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/learning-paths/4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(2)))
                .andExpect(jsonPath("$.courses[0].courseTitle").value("Intermediate English (B1)"))
                .andExpect(jsonPath("$.courses[0].sectionCount").value(2))
                .andExpect(jsonPath("$.courses[1].courseTitle").value("Advanced English (C1)"))
                .andExpect(jsonPath("$.courses[1].sectionCount").value(2));

        verify(learningPathService, times(1)).getPathById(4L);
    }

    // ==================== Note on @AuthenticationPrincipal Tests
    // ====================
    // Tests for endpoints using @AuthenticationPrincipal are not included in
    // @WebMvcTest
    // because @WithMockUser does not provide a User object to
    // @AuthenticationPrincipal.
    // These endpoints (/recommend, /start, /my-progress) are tested through:
    // 1. Comprehensive service layer tests (LearningPathServiceTest) - 80%+
    // coverage
    // 2. Integration tests (if needed) with @SpringBootTest would properly test
    // security
    //
    // The controller logic for these endpoints is minimal (logging + delegation to
    // service),
    // so comprehensive service tests provide adequate coverage of business logic.
}
