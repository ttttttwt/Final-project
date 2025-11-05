package com.lexia.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.LessonProgressDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.EnrollmentNotFoundException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.repository.EnrollmentRepository;
import com.lexia.backend.repository.LessonProgressRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private EnrollmentService enrollmentService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProgressServiceImpl progressService;

    private User testUser;
    private Course testCourse;
    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testCourse = new Course();
        testCourse.setId(1L);

        Section section = new Section();
        section.setId(1L);
        section.setCourse(testCourse);

        testLesson = new Lesson();
        testLesson.setId(1L);
        testLesson.setSection(section);
    }

    @Test
    void completeLesson_WithValidData_ShouldSucceed() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(testLesson));
        when(enrollmentRepository.existsByUserIdAndCourseId(any(), anyLong())).thenReturn(true);
        when(lessonProgressRepository.findByUserIdAndLessonId(any(), anyLong())).thenReturn(Optional.empty());
        String resultDetails = "{\"score\": 95}";

        LessonProgress savedProgress = new LessonProgress();
        savedProgress.setId(1L);
        savedProgress.setLesson(testLesson);
        savedProgress.setStatus(LessonProgress.Status.COMPLETED);
        savedProgress.setScore(95);
        savedProgress.setAttempts(1);
        savedProgress.setResultDetails(resultDetails);
        savedProgress.setCreatedAt(LocalDateTime.now());
        savedProgress.setUpdatedAt(LocalDateTime.now());

        when(lessonProgressRepository.save(any(LessonProgress.class))).thenReturn(savedProgress);

        LessonProgressDTO result = progressService.completeLesson(testUser, testLesson.getId(), resultDetails);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(95, result.getScore());
        assertEquals(1, result.getAttempts());
        verify(enrollmentService, times(1)).updateEnrollmentProgress(testUser.getId(), testCourse.getId());
    }

    @Test
    void completeLesson_WhenNotEnrolled_ShouldThrowEnrollmentNotFoundException() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(testLesson));
        when(enrollmentRepository.existsByUserIdAndCourseId(any(), anyLong())).thenReturn(false);

        assertThrows(EnrollmentNotFoundException.class, () -> progressService.completeLesson(testUser, 1L, "{}"));
    }

    @Test
    void completeLesson_WithInvalidLesson_ShouldThrowLessonNotFoundException() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(LessonNotFoundException.class, () -> progressService.completeLesson(testUser, 99L, "{}"));
    }

    @Test
    void completeLesson_WithInvalidJson_ShouldThrowIllegalArgumentException() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(testLesson));
        when(enrollmentRepository.existsByUserIdAndCourseId(any(), anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> progressService.completeLesson(testUser, 1L, "{invalid-json}"));
    }

    @Test
    void getStreak_WithNoCompletedLessons_ShouldReturnZeroStreak() {
        when(lessonProgressRepository.findByUserIdAndStatus(any(), any())).thenReturn(Collections.emptyList());

        StreakDTO streak = progressService.getStreak(testUser);

        assertEquals(0, streak.getCurrentStreak());
        assertEquals(0, streak.getLongestStreak());
        assertFalse(streak.getIsActiveToday());
    }

    @Test
    void getStreak_WithOneCompletionToday_ShouldReturnOneDayStreak() {
        LessonProgress progress = LessonProgress.builder().completedAt(LocalDateTime.now()).build();
        when(lessonProgressRepository.findByUserIdAndStatus(any(), eq(LessonProgress.Status.COMPLETED)))
                .thenReturn(Collections.singletonList(progress));

        StreakDTO streak = progressService.getStreak(testUser);

        assertEquals(1, streak.getCurrentStreak());
        assertEquals(1, streak.getLongestStreak());
        assertTrue(streak.getIsActiveToday());
    }

    @Test
    void getStreak_WithConsecutiveCompletions_ShouldCalculateCurrentStreak() {
        LessonProgress p1 = LessonProgress.builder().completedAt(LocalDateTime.now()).build();
        LessonProgress p2 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(1)).build();
        LessonProgress p3 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(2)).build();
        when(lessonProgressRepository.findByUserIdAndStatus(any(), eq(LessonProgress.Status.COMPLETED)))
                .thenReturn(Arrays.asList(p1, p2, p3));

        StreakDTO streak = progressService.getStreak(testUser);

        assertEquals(3, streak.getCurrentStreak());
        assertEquals(3, streak.getLongestStreak());
    }

    @Test
    void getStreak_WithGapInCompletions_ShouldCalculateLongestStreak() {
        LessonProgress p1 = LessonProgress.builder().completedAt(LocalDateTime.now()).build(); // Streak 1
        LessonProgress p2 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(3)).build(); // Streak 2,
                                                                                                            // Day 3
        LessonProgress p3 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(4)).build(); // Streak 2,
                                                                                                            // Day 2
        LessonProgress p4 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(5)).build(); // Streak 2,
                                                                                                            // Day 1
        when(lessonProgressRepository.findByUserIdAndStatus(any(), eq(LessonProgress.Status.COMPLETED)))
                .thenReturn(Arrays.asList(p1, p2, p3, p4));

        StreakDTO streak = progressService.getStreak(testUser);

        assertEquals(1, streak.getCurrentStreak());
        assertEquals(3, streak.getLongestStreak());
    }

    @Test
    void getStreak_WithSameDayCompletions_ShouldCountAsOneDay() {
        LessonProgress p1 = LessonProgress.builder().completedAt(LocalDateTime.now()).build();
        LessonProgress p2 = LessonProgress.builder().completedAt(LocalDateTime.now().minusHours(1)).build();
        LessonProgress p3 = LessonProgress.builder().completedAt(LocalDateTime.now().minusDays(1)).build();
        when(lessonProgressRepository.findByUserIdAndStatus(any(), eq(LessonProgress.Status.COMPLETED)))
                .thenReturn(Arrays.asList(p1, p2, p3));

        StreakDTO streak = progressService.getStreak(testUser);

        assertEquals(2, streak.getCurrentStreak());
        assertEquals(2, streak.getLongestStreak());
        assertEquals(2, streak.getTotalActiveDays());
    }
}
