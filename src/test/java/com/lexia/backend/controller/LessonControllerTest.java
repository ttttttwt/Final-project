package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.exception.InvalidLessonContentException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for LessonController.
 * Tests all lesson management endpoints with various scenarios including JSONB
 * content handling.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@WebMvcTest(LessonController.class)
@ContextConfiguration(classes = { LessonController.class, GlobalExceptionHandler.class })
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private LessonDTO mockReadingLessonDTO;
    private LessonDTO mockListeningLessonDTO;
    private CreateLessonDTO createReadingLessonDTO;
    private UpdateLessonDTO updateLessonDTO;

    @BeforeEach
    void setUp() {
        // Setup READING lesson DTO
        String readingContent = "{\"passages\":[{\"title\":\"Meeting People\",\"text\":\"When you meet someone new...\"}],\"questions\":[{\"question\":\"What should you say first?\",\"type\":\"multiple_choice\",\"options\":[\"Hello\",\"Goodbye\",\"Thank you\"],\"correctAnswer\":0}]}";
        mockReadingLessonDTO = LessonDTO.builder()
                .id(1L)
                .sectionId(1L)
                .title("Basic Greetings and Introductions")
                .lessonType(Lesson.LessonType.READING)
                .content(readingContent)
                .orderIndex(0)
                .durationMinutes(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup LISTENING lesson DTO
        String listeningContent = "{\"audioUrl\":\"https://cdn.lexia.com/audio/conversation-01.mp3\",\"duration\":120,\"transcript\":\"A: Good morning! How are you today? B: I'm fine, thank you.\",\"questions\":[{\"question\":\"How is person B feeling?\",\"type\":\"multiple_choice\",\"options\":[\"Fine\",\"Sad\",\"Angry\"],\"correctAnswer\":0}]}";
        mockListeningLessonDTO = LessonDTO.builder()
                .id(2L)
                .sectionId(1L)
                .title("Understanding Daily Conversations")
                .lessonType(Lesson.LessonType.LISTENING)
                .content(listeningContent)
                .orderIndex(1)
                .durationMinutes(20)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup create READING lesson DTO
        createReadingLessonDTO = CreateLessonDTO.builder()
                .title("Basic Greetings and Introductions")
                .lessonType(Lesson.LessonType.READING)
                .content(readingContent)
                .orderIndex(0)
                .durationMinutes(15)
                .build();

        // Setup update lesson DTO
        updateLessonDTO = UpdateLessonDTO.builder()
                .title("Advanced Greetings and Introductions")
                .durationMinutes(20)
                .build();
    }

    // ========== GET /api/v1/lessons/{id} - Get Lesson By ID Tests ==========

    @Test
    @WithMockUser
    void getLessonById_WithValidId_ReturnsOk() throws Exception {
        // Given
        when(lessonService.getById(1L)).thenReturn(mockReadingLessonDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sectionId").value(1))
                .andExpect(jsonPath("$.title").value("Basic Greetings and Introductions"))
                .andExpect(jsonPath("$.lessonType").value("READING"))
                .andExpect(jsonPath("$.orderIndex").value(0))
                .andExpect(jsonPath("$.durationMinutes").value(15))
                .andExpect(jsonPath("$.content").isNotEmpty());

        verify(lessonService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser
    void getLessonById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(lessonService.getById(999L))
                .thenThrow(new LessonNotFoundException("Lesson not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Lesson Not Found"))
                .andExpect(jsonPath("$.message").value("Lesson not found with id: 999"));

        verify(lessonService, times(1)).getById(999L);
    }

    @Test
    void getLessonById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isUnauthorized());

        verify(lessonService, never()).getById(any());
    }

    // ========== GET /api/v1/lessons/sections/{sectionId} - Get Lessons By Section
    // Tests ==========

    @Test
    @WithMockUser
    void getLessonsBySectionId_WithValidId_ReturnsOk() throws Exception {
        // Given
        List<LessonDTO> lessons = Arrays.asList(mockReadingLessonDTO, mockListeningLessonDTO);
        when(lessonService.getAllBySectionId(1L)).thenReturn(lessons);

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/sections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].lessonType").value("READING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].lessonType").value("LISTENING"));

        verify(lessonService, times(1)).getAllBySectionId(1L);
    }

    @Test
    @WithMockUser
    void getLessonsBySectionId_WithNonExistentSection_ReturnsNotFound() throws Exception {
        // Given
        when(lessonService.getAllBySectionId(999L))
                .thenThrow(new SectionNotFoundException("Section not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/sections/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Section Not Found"));

        verify(lessonService, times(1)).getAllBySectionId(999L);
    }

    @Test
    @WithMockUser
    void getLessonsBySectionId_WithNoLessons_ReturnsEmptyList() throws Exception {
        // Given
        when(lessonService.getAllBySectionId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/sections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(lessonService, times(1)).getAllBySectionId(1L);
    }

    // ========== GET /api/v1/lessons/courses/{courseId} - Get Lessons By Course
    // Tests ==========

    @Test
    @WithMockUser
    void getLessonsByCourseId_WithValidId_ReturnsOk() throws Exception {
        // Given
        List<LessonDTO> lessons = Arrays.asList(mockReadingLessonDTO, mockListeningLessonDTO);
        when(lessonService.getAllByCourseId(1L)).thenReturn(lessons);

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lessonType").value("READING"))
                .andExpect(jsonPath("$[1].lessonType").value("LISTENING"));

        verify(lessonService, times(1)).getAllByCourseId(1L);
    }

    @Test
    @WithMockUser
    void getLessonsByCourseId_WithNoLessons_ReturnsEmptyList() throws Exception {
        // Given
        when(lessonService.getAllByCourseId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(lessonService, times(1)).getAllByCourseId(1L);
    }

    // ========== POST /api/v1/lessons/sections/{sectionId}/lessons - Create Lesson
    // Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createLesson_WithValidReadingLesson_ReturnsCreated() throws Exception {
        // Given
        when(lessonService.create(eq(1L), any(CreateLessonDTO.class)))
                .thenReturn(mockReadingLessonDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/lessons/sections/1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReadingLessonDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Basic Greetings and Introductions"))
                .andExpect(jsonPath("$.lessonType").value("READING"))
                .andExpect(jsonPath("$.content").isNotEmpty());

        verify(lessonService, times(1)).create(eq(1L), any(CreateLessonDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createLesson_WithInvalidContent_ReturnsBadRequest() throws Exception {
        // Given
        when(lessonService.create(eq(1L), any(CreateLessonDTO.class)))
                .thenThrow(new InvalidLessonContentException(
                        "READING lesson validation failed: passages field is required"));

        // When & Then
        mockMvc.perform(post("/api/v1/lessons/sections/1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReadingLessonDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Lesson Content"))
                .andExpect(jsonPath("$.message").value("READING lesson validation failed: passages field is required"));

        verify(lessonService, times(1)).create(eq(1L), any(CreateLessonDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createLesson_WithNonExistentSection_ReturnsNotFound() throws Exception {
        // Given
        when(lessonService.create(eq(999L), any(CreateLessonDTO.class)))
                .thenThrow(new SectionNotFoundException("Section not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/api/v1/lessons/sections/999/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReadingLessonDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Section Not Found"));

        verify(lessonService, times(1)).create(eq(999L), any(CreateLessonDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createLesson_WithMissingTitle_ReturnsBadRequest() throws Exception {
        // Given
        CreateLessonDTO invalidDTO = CreateLessonDTO.builder()
                .lessonType(Lesson.LessonType.READING)
                .content("{\"passages\":[]}")
                .orderIndex(0)
                .durationMinutes(15)
                .build(); // Missing title

        // When & Then
        mockMvc.perform(post("/api/v1/lessons/sections/1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(lessonService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createLesson_WithInvalidDuration_ReturnsBadRequest() throws Exception {
        // Given
        CreateLessonDTO invalidDTO = CreateLessonDTO.builder()
                .title("Test Lesson")
                .lessonType(Lesson.LessonType.READING)
                .content("{\"passages\":[]}")
                .orderIndex(0)
                .durationMinutes(300) // Exceeds max (240)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/lessons/sections/1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(lessonService, never()).create(any(), any());
    }

    // Note: Authorization tests (401/403) are not included in @WebMvcTest
    // as it doesn't fully configure Spring Security. These should be tested
    // in integration tests with @SpringBootTest.

    // ========== PUT /api/v1/lessons/{id} - Update Lesson Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateLesson_WithValidData_ReturnsOk() throws Exception {
        // Given
        LessonDTO updatedLesson = LessonDTO.builder()
                .id(1L)
                .title("Advanced Greetings and Introductions")
                .durationMinutes(20)
                .build();
        when(lessonService.update(eq(1L), any(UpdateLessonDTO.class)))
                .thenReturn(updatedLesson);

        // When & Then
        mockMvc.perform(put("/api/v1/lessons/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLessonDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Advanced Greetings and Introductions"))
                .andExpect(jsonPath("$.durationMinutes").value(20));

        verify(lessonService, times(1)).update(eq(1L), any(UpdateLessonDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateLesson_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(lessonService.update(eq(999L), any(UpdateLessonDTO.class)))
                .thenThrow(new LessonNotFoundException("Lesson not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/lessons/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLessonDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(lessonService, times(1)).update(eq(999L), any(UpdateLessonDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateLesson_WithInvalidContent_ReturnsBadRequest() throws Exception {
        // Given
        UpdateLessonDTO invalidUpdateDTO = UpdateLessonDTO.builder()
                .content("{\"invalid\":\"content\"}")
                .build();
        when(lessonService.update(eq(1L), any(UpdateLessonDTO.class)))
                .thenThrow(new InvalidLessonContentException("Invalid lesson content structure"));

        // When & Then
        mockMvc.perform(put("/api/v1/lessons/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Lesson Content"));

        verify(lessonService, times(1)).update(eq(1L), any(UpdateLessonDTO.class));
    }

    // ========== DELETE /api/v1/lessons/{id} - Delete Lesson Tests ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteLesson_WithValidId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(lessonService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/lessons/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(lessonService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteLesson_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new LessonNotFoundException("Lesson not found with id: 999"))
                .when(lessonService).delete(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/lessons/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(lessonService, times(1)).delete(999L);
    }

    // ========== PATCH /api/v1/lessons/{id}/reorder - Reorder Lesson Tests
    // ==========

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void reorderLesson_WithValidData_ReturnsOk() throws Exception {
        // Given
        LessonDTO reorderedLesson = LessonDTO.builder()
                .id(1L)
                .title("Basic Greetings and Introductions")
                .orderIndex(2)
                .build();
        when(lessonService.reorder(1L, 2)).thenReturn(reorderedLesson);

        // When & Then
        mockMvc.perform(patch("/api/v1/lessons/1/reorder")
                .with(csrf())
                .param("newOrderIndex", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderIndex").value(2));

        verify(lessonService, times(1)).reorder(1L, 2);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void reorderLesson_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(lessonService.reorder(999L, 2))
                .thenThrow(new LessonNotFoundException("Lesson not found with id: 999"));

        // When & Then
        mockMvc.perform(patch("/api/v1/lessons/999/reorder")
                .with(csrf())
                .param("newOrderIndex", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(lessonService, times(1)).reorder(999L, 2);
    }

    // ========== JSONB Content Handling Tests ==========

    @Test
    @WithMockUser
    void getLessonById_ReturnsJsonbContentAsString() throws Exception {
        // Given
        when(lessonService.getById(1L)).thenReturn(mockReadingLessonDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isString())
                .andExpect(jsonPath("$.content").value(containsString("passages")))
                .andExpect(jsonPath("$.content").value(containsString("questions")));

        verify(lessonService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser
    void getLessonById_ListeningLesson_ReturnsAudioUrl() throws Exception {
        // Given
        when(lessonService.getById(2L)).thenReturn(mockListeningLessonDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/lessons/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonType").value("LISTENING"))
                .andExpect(jsonPath("$.content").value(containsString("audioUrl")))
                .andExpect(jsonPath("$.content").value(containsString("transcript")));

        verify(lessonService, times(1)).getById(2L);
    }
}
