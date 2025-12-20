package com.lexia.backend.controller;

import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

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

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.lexia.backend.auth.JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private com.lexia.backend.filter.UserActivityFilter userActivityFilter;

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
    void getCourseProgress_ShouldReturnProgress() throws Exception {
        // Mock User
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        // Set Authentication in Context
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(progressService.getCourseProgress(any(User.class), eq(1L))).thenReturn(testProgress);

        mockMvc.perform(get("/api/v1/progress/courses/1/lessons")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1L))
                .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                .andExpect(jsonPath("$.progressPercentage").value(50));
    }
}
