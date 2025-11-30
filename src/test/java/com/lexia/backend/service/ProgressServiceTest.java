package com.lexia.backend.service;

import com.lexia.backend.dto.ProgressSummaryDTO;
import com.lexia.backend.entity.LessonProgress;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.LessonProgressRepository;
import com.lexia.backend.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
    }

    @Test
    void getProgressSummary_ShouldReturnZeroStats_WhenNoActivity() {
        // Arrange
        int days = 7;
        when(lessonProgressRepository.findCompletedByUserIdBetween(eq(user.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProgressSummaryDTO summary = progressService.getProgressSummary(user, days);

        // Assert
        assertNotNull(summary);
        assertEquals(0, summary.getTotalLessonsCompleted());
        assertEquals(0, summary.getTotalTimeSpentMinutes());
        assertEquals(0, summary.getAverageTimePerLesson());
        assertEquals(0, summary.getActiveDays());
        assertEquals(days, summary.getDailyActivities().size());

        // Verify all daily activities have 0 stats
        assertTrue(summary.getDailyActivities().stream()
                .allMatch(d -> d.getLessonsCompleted() == 0 && d.getTimeSpentMinutes() == 0));
    }

    @Test
    void getProgressSummary_ShouldAggregateCorrectly_WhenActivityExists() {
        // Arrange
        int days = 7;
        LocalDateTime now = LocalDateTime.now();

        LessonProgress progress1 = new LessonProgress();
        progress1.setCompletedAt(now); // Today

        LessonProgress progress2 = new LessonProgress();
        progress2.setCompletedAt(now.minusDays(1)); // Yesterday

        when(lessonProgressRepository.findCompletedByUserIdBetween(eq(user.getId()), any(), any()))
                .thenReturn(java.util.Arrays.asList(progress1, progress2));

        // Act
        ProgressSummaryDTO summary = progressService.getProgressSummary(user, days);

        // Assert
        assertEquals(2, summary.getTotalLessonsCompleted());
        assertEquals(30, summary.getTotalTimeSpentMinutes()); // 2 lessons * 15 mins
        assertEquals(15, summary.getAverageTimePerLesson());
        assertEquals(2, summary.getActiveDays());
        assertEquals(days, summary.getDailyActivities().size());
    }
}
