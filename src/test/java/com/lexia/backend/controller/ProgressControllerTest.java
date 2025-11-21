package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseProgressDTO testProgress;

    @BeforeEach
    void setUp() {
        testProgress = CourseProgressDTO.builder()
                .courseId(1L)
                .courseTitle("Test Course")
                .totalLessons(10)
                .completedLessons(5)
                .progressPercentage(50)
                .lessonProgress(Collections.emptyList())
                .build();
    }

    @Test
    @WithMockUser
    void getCourseProgress_ShouldReturnProgress() throws Exception {
        when(progressService.getCourseProgress(any(), eq(1L))).thenReturn(testProgress);

        mockMvc.perform(get("/api/v1/progress/courses/1/lessons")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1L))
                .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                .andExpect(jsonPath("$.progressPercentage").value(50));
    }
}
