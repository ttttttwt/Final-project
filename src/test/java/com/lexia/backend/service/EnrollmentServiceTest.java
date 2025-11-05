package com.lexia.backend.service;

import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.dto.EnrollmentDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.EnrollmentNotFoundException;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.EnrollmentRepository;
import com.lexia.backend.repository.LessonProgressRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private User testUser;
    private Course testCourse;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");

        testEnrollment = Enrollment.builder()
                .id(1L)
                .userId(testUser.getId())
                .course(testCourse)
                .progressPercentage(0)
                .build();
    }

    @Test
    void enroll_WithNewCourse_ShouldSucceed() {
        when(enrollmentRepository.existsByUserIdAndCourseId(testUser.getId(), testCourse.getId())).thenReturn(false);
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentDTO result = enrollmentService.enroll(testUser, testCourse.getId());

        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getCourseId());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enroll_WhenAlreadyEnrolled_ShouldThrowIllegalStateException() {
        when(enrollmentRepository.existsByUserIdAndCourseId(testUser.getId(), testCourse.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> enrollmentService.enroll(testUser, testCourse.getId()));
    }

    @Test
    void enroll_WithNonExistentCourse_ShouldThrowCourseNotFoundException() {
        when(enrollmentRepository.existsByUserIdAndCourseId(any(UUID.class), anyLong())).thenReturn(false);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> enrollmentService.enroll(testUser, 99L));
    }

    @Test
    void enroll_WhenSaveFailsDueToRaceCondition_ShouldThrowIllegalStateException() {
        when(enrollmentRepository.existsByUserIdAndCourseId(testUser.getId(), testCourse.getId())).thenReturn(false);
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenThrow(new DataIntegrityViolationException("Simulating race condition"));

        assertThrows(IllegalStateException.class, () -> enrollmentService.enroll(testUser, testCourse.getId()));
    }

    @Test
    void getMyEnrollments_WhenUserHasEnrollments_ShouldReturnEnrollmentDTOList() {
        when(enrollmentRepository.findByUserId(testUser.getId())).thenReturn(Collections.singletonList(testEnrollment));

        List<EnrollmentDTO> results = enrollmentService.getMyEnrollments(testUser);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testEnrollment.getId(), results.get(0).getId());
    }

    @Test
    void getMyEnrollments_WhenUserHasNoEnrollments_ShouldReturnEmptyList() {
        when(enrollmentRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        List<EnrollmentDTO> results = enrollmentService.getMyEnrollments(testUser);

        assertTrue(results.isEmpty());
    }

    @Test
    void getCourseProgress_WhenNotEnrolled_ShouldThrowEnrollmentNotFoundException() {
        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EnrollmentNotFoundException.class,
                () -> enrollmentService.getCourseProgress(testUser, testCourse.getId()));
    }

    @Test
    void getCourseProgress_WithValidEnrollment_ShouldReturnProgress() {
        Section section = new Section();
        section.setId(1L);
        section.setCourse(testCourse);
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSection(section);
        testCourse.setSections(Collections.singletonList(section));

        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.of(testEnrollment));
        when(courseRepository.findByIdWithSections(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(lessonRepository.countByCourseId(testCourse.getId())).thenReturn(10L);
        when(lessonProgressRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Collections.emptyList());

        CourseProgressDTO progress = enrollmentService.getCourseProgress(testUser, testCourse.getId());

        assertNotNull(progress);
        assertEquals(testCourse.getId(), progress.getCourseId());
        assertEquals(0, progress.getCompletedLessons());
        assertEquals(10, progress.getTotalLessons());
        assertEquals(0, progress.getProgressPercentage());
    }

    @Test
    void updateEnrollmentProgress_WithNoEnrollment_ShouldDoNothing() {
        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.empty());

        enrollmentService.updateEnrollmentProgress(testUser.getId(), testCourse.getId());

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void updateEnrollmentProgress_WithNoLessonsInCourse_ShouldDoNothing() {
        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.of(testEnrollment));
        when(lessonRepository.countByCourseId(testCourse.getId())).thenReturn(0L);

        enrollmentService.updateEnrollmentProgress(testUser.getId(), testCourse.getId());

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void updateEnrollmentProgress_ShouldCalculateAndUpdateProgress() {
        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.of(testEnrollment));
        when(lessonRepository.countByCourseId(testCourse.getId())).thenReturn(10L);
        when(lessonProgressRepository.countCompletedByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(5L);

        enrollmentService.updateEnrollmentProgress(testUser.getId(), testCourse.getId());

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        assertEquals(50, testEnrollment.getProgressPercentage());
    }

    @Test
    void updateEnrollmentProgress_OnCourseCompletion_ShouldSetCompletedAt() {
        when(enrollmentRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(Optional.of(testEnrollment));
        when(lessonRepository.countByCourseId(testCourse.getId())).thenReturn(10L);
        when(lessonProgressRepository.countCompletedByUserIdAndCourseId(testUser.getId(), testCourse.getId()))
                .thenReturn(10L);

        assertNull(testEnrollment.getCompletedAt());

        enrollmentService.updateEnrollmentProgress(testUser.getId(), testCourse.getId());

        verify(enrollmentRepository, times(1)).save(testEnrollment);
        assertEquals(100, testEnrollment.getProgressPercentage());
        assertNotNull(testEnrollment.getCompletedAt());
    }
}
