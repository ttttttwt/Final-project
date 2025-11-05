package com.lexia.backend.service;

import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.dto.EnrollmentDTO;
import com.lexia.backend.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for enrollment management.
 * Handles course enrollment, progress tracking, and enrollment queries.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface EnrollmentService {

    /**
     * Enroll a user in a course.
     * 
     * @param user     the user to enroll
     * @param courseId the course ID to enroll in
     * @return EnrollmentDTO with initial progress (0%)
     * @throws com.lexia.backend.exception.CourseNotFoundException if course doesn't
     *                                                             exist
     * @throws IllegalStateException                               if user is
     *                                                             already enrolled
     *                                                             (409 Conflict)
     */
    EnrollmentDTO enroll(User user, Long courseId);

    /**
     * Get all enrollments for a user.
     * 
     * @param user the user
     * @return list of enrollments, empty if none found
     */
    List<EnrollmentDTO> getMyEnrollments(User user);

    /**
     * Get detailed course progress for a user.
     * Includes lesson-by-lesson breakdown with status and scores.
     * 
     * @param user     the user
     * @param courseId the course ID
     * @return CourseProgressDTO with detailed progress information
     * @throws com.lexia.backend.exception.EnrollmentNotFoundException if not
     *                                                                 enrolled
     * @throws com.lexia.backend.exception.CourseNotFoundException     if course
     *                                                                 doesn't exist
     */
    CourseProgressDTO getCourseProgress(User user, Long courseId);

    /**
     * Update enrollment progress percentage.
     * Called internally when lessons are completed.
     * 
     * @param userId   the user's UUID
     * @param courseId the course ID
     */
    void updateEnrollmentProgress(UUID userId, Long courseId);
}
