package com.lexia.backend.service;

import com.lexia.backend.dto.ActivityDTO;
import com.lexia.backend.dto.AdminDashboardDTO;
import com.lexia.backend.entity.AdminActivityLog;
import com.lexia.backend.repository.AdminActivityLogRepository;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminDashboardService.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService Tests")
class AdminDashboardServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private CourseRepository courseRepository;

        @Mock
        private LessonRepository lessonRepository;

        @Mock
        private AdminActivityLogRepository adminActivityLogRepository;

        @InjectMocks
        private AdminDashboardService adminDashboardService;

        @Nested
        @DisplayName("getDashboardStats Tests")
        class GetDashboardStatsTests {

                @Test
                @DisplayName("Should return correct total counts")
                void shouldReturnCorrectTotalCounts() {
                        // Arrange
                        when(userRepository.count()).thenReturn(100L);
                        when(courseRepository.count()).thenReturn(10L);
                        when(courseRepository.countByIsPublishedTrue()).thenReturn(7L);
                        when(lessonRepository.count()).thenReturn(50L);

                        // Mock CEFR level counts
                        when(courseRepository.countByCefrLevel("A1")).thenReturn(2L);
                        when(courseRepository.countByCefrLevel("A2")).thenReturn(1L);
                        when(courseRepository.countByCefrLevel("B1")).thenReturn(3L);
                        when(courseRepository.countByCefrLevel("B2")).thenReturn(2L);
                        when(courseRepository.countByCefrLevel("C1")).thenReturn(1L);
                        when(courseRepository.countByCefrLevel("C2")).thenReturn(1L);

                        // Mock recent activities
                        when(adminActivityLogRepository.findRecentActivities(any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        // Act
                        AdminDashboardDTO result = adminDashboardService.getDashboardStats();

                        // Assert
                        assertThat(result.getTotalUsers()).isEqualTo(100);
                        assertThat(result.getTotalCourses()).isEqualTo(10);
                        assertThat(result.getPublishedCourses()).isEqualTo(7);
                        assertThat(result.getTotalLessons()).isEqualTo(50);
                }

                @Test
                @DisplayName("Should return courses by CEFR level")
                void shouldReturnCoursesByLevel() {
                        // Arrange
                        when(userRepository.count()).thenReturn(0L);
                        when(courseRepository.count()).thenReturn(10L);
                        when(courseRepository.countByIsPublishedTrue()).thenReturn(0L);
                        when(lessonRepository.count()).thenReturn(0L);

                        when(courseRepository.countByCefrLevel("A1")).thenReturn(2L);
                        when(courseRepository.countByCefrLevel("A2")).thenReturn(1L);
                        when(courseRepository.countByCefrLevel("B1")).thenReturn(3L);
                        when(courseRepository.countByCefrLevel("B2")).thenReturn(2L);
                        when(courseRepository.countByCefrLevel("C1")).thenReturn(1L);
                        when(courseRepository.countByCefrLevel("C2")).thenReturn(1L);

                        when(adminActivityLogRepository.findRecentActivities(any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        // Act
                        AdminDashboardDTO result = adminDashboardService.getDashboardStats();

                        // Assert
                        Map<String, Long> coursesByLevel = result.getCoursesByLevel();
                        assertThat(coursesByLevel).hasSize(6);
                        assertThat(coursesByLevel.get("A1")).isEqualTo(2);
                        assertThat(coursesByLevel.get("A2")).isEqualTo(1);
                        assertThat(coursesByLevel.get("B1")).isEqualTo(3);
                        assertThat(coursesByLevel.get("B2")).isEqualTo(2);
                        assertThat(coursesByLevel.get("C1")).isEqualTo(1);
                        assertThat(coursesByLevel.get("C2")).isEqualTo(1);
                }

                @Test
                @DisplayName("Should return recent activities from activity log")
                void shouldReturnRecentActivitiesFromActivityLog() {
                        // Arrange
                        when(userRepository.count()).thenReturn(0L);
                        when(courseRepository.count()).thenReturn(0L);
                        when(courseRepository.countByIsPublishedTrue()).thenReturn(0L);
                        when(lessonRepository.count()).thenReturn(0L);
                        when(courseRepository.countByCefrLevel(any())).thenReturn(0L);

                        // Create test activity logs
                        AdminActivityLog courseCreated = AdminActivityLog.builder()
                                        .id(UUID.randomUUID())
                                        .userId(UUID.randomUUID())
                                        .userName("Admin User")
                                        .action(AdminActivityLog.ActionType.COURSE_CREATED)
                                        .entityType(AdminActivityLog.EntityType.COURSE)
                                        .entityId("1")
                                        .entityName("New Course")
                                        .description("Admin User created course \"New Course\"")
                                        .createdAt(LocalDateTime.now().minusHours(1))
                                        .build();

                        AdminActivityLog lessonUpdated = AdminActivityLog.builder()
                                        .id(UUID.randomUUID())
                                        .userId(UUID.randomUUID())
                                        .userName("Content Manager")
                                        .action(AdminActivityLog.ActionType.LESSON_UPDATED)
                                        .entityType(AdminActivityLog.EntityType.LESSON)
                                        .entityId("5")
                                        .entityName("Lesson 1")
                                        .description("Content Manager updated lesson \"Lesson 1\"")
                                        .createdAt(LocalDateTime.now().minusHours(2))
                                        .build();

                        when(adminActivityLogRepository.findRecentActivities(any(PageRequest.class)))
                                        .thenReturn(List.of(courseCreated, lessonUpdated));

                        // Act
                        AdminDashboardDTO result = adminDashboardService.getDashboardStats();

                        // Assert
                        List<ActivityDTO> activities = result.getRecentActivities();
                        assertThat(activities).hasSize(2);
                        assertThat(activities.get(0).getType()).isEqualTo("COURSE_CREATED");
                        assertThat(activities.get(0).getUserName()).isEqualTo("Admin User");
                        assertThat(activities.get(1).getType()).isEqualTo("LESSON_UPDATED");
                }

                @Test
                @DisplayName("Should handle empty data gracefully")
                void shouldHandleEmptyDataGracefully() {
                        // Arrange
                        when(userRepository.count()).thenReturn(0L);
                        when(courseRepository.count()).thenReturn(0L);
                        when(courseRepository.countByIsPublishedTrue()).thenReturn(0L);
                        when(lessonRepository.count()).thenReturn(0L);
                        when(courseRepository.countByCefrLevel(any())).thenReturn(0L);

                        when(adminActivityLogRepository.findRecentActivities(any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        // Act
                        AdminDashboardDTO result = adminDashboardService.getDashboardStats();

                        // Assert
                        assertThat(result.getTotalUsers()).isZero();
                        assertThat(result.getTotalCourses()).isZero();
                        assertThat(result.getRecentActivities()).isEmpty();
                        assertThat(result.getCoursesByLevel()).hasSize(6);
                }
        }
}
