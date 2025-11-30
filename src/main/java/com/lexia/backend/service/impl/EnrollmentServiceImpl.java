package com.lexia.backend.service.impl;

import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.dto.EnrollmentDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.EnrollmentNotFoundException;
import com.lexia.backend.mapper.EnrollmentMapper;
import com.lexia.backend.mapper.ProgressMapper;
import com.lexia.backend.notification.event.CourseCompletedEvent;
import com.lexia.backend.notification.event.EnrollmentConfirmedEvent;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.EnrollmentRepository;
import com.lexia.backend.repository.LessonProgressRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link EnrollmentService}.
 * Manages course enrollments and progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Uses @Transactional to handle race conditions with UNIQUE constraint.
     * If duplicate enrollment is attempted, catches exception and throws
     * IllegalStateException.
     * </p>
     */
    @Override
    @Transactional
    public EnrollmentDTO enroll(User user, Long courseId) {
        log.info("Enrolling user {} in course {}", user.getId(), courseId);

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            log.warn("User {} is already enrolled in course {}", user.getId(), courseId);
            throw new IllegalStateException("You are already enrolled in this course");
        }

        // Verify course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .userId(user.getId())
                .course(course)
                .progressPercentage(0)
                .build();

        try {
            enrollment = enrollmentRepository.save(enrollment);
            log.info("User {} successfully enrolled in course {}", user.getId(), courseId);

            // Publish enrollment confirmed event for notification
            publishEnrollmentConfirmedEvent(user.getId(), course);

            return EnrollmentMapper.toDTO(enrollment);
        } catch (Exception e) {
            // Handle race condition where another thread enrolled user simultaneously
            log.error("Failed to enroll user {} in course {}: {}", user.getId(), courseId, e.getMessage());
            throw new IllegalStateException("You are already enrolled in this course");
        }
    }

    /**
     * Publishes an EnrollmentConfirmedEvent for notification system.
     *
     * @param userId the user who enrolled
     * @param course the course enrolled in
     */
    private void publishEnrollmentConfirmedEvent(UUID userId, Course course) {
        try {
            EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                    this,
                    userId,
                    course.getId(),
                    course.getTitle(),
                    course.getCefrLevel(),
                    course.getThumbnailUrl());
            eventPublisher.publishEvent(event);
            log.debug("Published EnrollmentConfirmedEvent for user {} in course {}", userId, course.getId());
        } catch (Exception e) {
            // Don't fail enrollment if notification fails
            log.warn("Failed to publish EnrollmentConfirmedEvent for user {} in course {}: {}",
                    userId, course.getId(), e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getMyEnrollments(User user) {
        log.debug("Fetching enrollments for user {}", user.getId());

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        log.info("Found {} enrollments for user {}", enrollments.size(), user.getId());

        return EnrollmentMapper.toDTOList(enrollments);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Calculates progress as: (completed_lessons / total_lessons) * 100
     * </p>
     * <p>
     * Retrieves lesson progress for detailed per-lesson status.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public CourseProgressDTO getCourseProgress(User user, Long courseId) {
        log.debug("Fetching course progress for user {} in course {}", user.getId(), courseId);

        // Verify enrollment exists
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException(
                        "You are not enrolled in this course. Please enroll first."));

        // Get course with sections and lessons
        Course course = courseRepository.findByIdWithSections(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Get total lesson count
        long totalLessons = lessonRepository.countByCourseId(courseId);

        // Get lesson progress for this course
        List<LessonProgress> lessonProgressList = lessonProgressRepository
                .findByUserIdAndCourseId(user.getId(), courseId);

        log.info("User {} has completed {}/{} lessons in course {}",
                user.getId(), lessonProgressList.size(), totalLessons, courseId);

        // Build detailed progress DTO
        return ProgressMapper.toCourseProgressDTO(course, lessonProgressList, (int) totalLessons);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Recalculates progress percentage based on completed lessons.
     * Updates enrollment record and sets completedAt timestamp if 100% complete.
     * </p>
     */
    @Override
    @Transactional
    public void updateEnrollmentProgress(UUID userId, Long courseId) {
        log.debug("Updating enrollment progress for user {} in course {}", userId, courseId);

        // Get enrollment
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        if (enrollment == null) {
            log.warn("No enrollment found for user {} in course {}", userId, courseId);
            return;
        }

        // Get total lessons in course
        long totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons == 0) {
            log.warn("Course {} has no lessons", courseId);
            return;
        }

        // Count completed lessons
        long completedLessons = lessonProgressRepository
                .countCompletedByUserIdAndCourseId(userId, courseId);

        // Calculate progress percentage
        int progressPercentage = (int) Math.round((completedLessons * 100.0) / totalLessons);

        // Check if course was just completed (not previously completed)
        boolean wasNotCompleted = !enrollment.isCompleted();

        // Update enrollment
        enrollment.updateProgress(progressPercentage);
        enrollmentRepository.save(enrollment);

        log.info("Updated progress for user {} in course {}: {}% ({}/{} lessons)",
                userId, courseId, progressPercentage, completedLessons, totalLessons);

        // Publish course completed event if just completed
        if (wasNotCompleted && enrollment.isCompleted()) {
            publishCourseCompletedEvent(userId, enrollment);
        }
    }

    /**
     * Publishes a CourseCompletedEvent for notification system.
     *
     * @param userId     the user who completed the course
     * @param enrollment the completed enrollment
     */
    private void publishCourseCompletedEvent(UUID userId, Enrollment enrollment) {
        try {
            Course course = enrollment.getCourse();
            // Calculate total time from enrollment to completion
            int completionTimeMinutes = (int) Duration.between(
                    enrollment.getEnrolledAt(),
                    enrollment.getCompletedAt()).toMinutes();

            CourseCompletedEvent event = new CourseCompletedEvent(
                    this,
                    userId,
                    course.getId(),
                    course.getTitle(),
                    completionTimeMinutes);
            eventPublisher.publishEvent(event);
            log.info("Published CourseCompletedEvent for user {} completing course {}",
                    userId, course.getId());
        } catch (Exception e) {
            // Don't fail progress update if notification fails
            log.warn("Failed to publish CourseCompletedEvent for user {} in course {}: {}",
                    userId, enrollment.getCourse().getId(), e.getMessage());
        }
    }
}
