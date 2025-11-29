package com.lexia.backend.service;

import com.lexia.backend.entity.AdminActivityLog;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for admin activity logging.
 * Records admin/content manager actions for dashboard display.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
public interface AdminActivityLogService {

    /**
     * Log a course creation action.
     *
     * @param userId      the user who created the course
     * @param userName    display name of the user
     * @param courseId    the course ID
     * @param courseTitle the course title
     */
    void logCourseCreated(UUID userId, String userName, Long courseId, String courseTitle);

    /**
     * Log a course update action.
     *
     * @param userId      the user who updated the course
     * @param userName    display name of the user
     * @param courseId    the course ID
     * @param courseTitle the course title
     * @param changes     description of changes (optional)
     */
    void logCourseUpdated(UUID userId, String userName, Long courseId, String courseTitle, String changes);

    /**
     * Log a course publish action.
     *
     * @param userId      the user who published the course
     * @param userName    display name of the user
     * @param courseId    the course ID
     * @param courseTitle the course title
     */
    void logCoursePublished(UUID userId, String userName, Long courseId, String courseTitle);

    /**
     * Log a course unpublish action.
     *
     * @param userId      the user who unpublished the course
     * @param userName    display name of the user
     * @param courseId    the course ID
     * @param courseTitle the course title
     */
    void logCourseUnpublished(UUID userId, String userName, Long courseId, String courseTitle);

    /**
     * Log a course deletion action.
     *
     * @param userId      the user who deleted the course
     * @param userName    display name of the user
     * @param courseId    the course ID
     * @param courseTitle the course title
     */
    void logCourseDeleted(UUID userId, String userName, Long courseId, String courseTitle);

    /**
     * Log a section creation action.
     *
     * @param userId       the user who created the section
     * @param userName     display name of the user
     * @param sectionId    the section ID
     * @param sectionTitle the section title
     * @param courseTitle  the parent course title
     */
    void logSectionCreated(UUID userId, String userName, Long sectionId, String sectionTitle, String courseTitle);

    /**
     * Log a section update action.
     *
     * @param userId       the user who updated the section
     * @param userName     display name of the user
     * @param sectionId    the section ID
     * @param sectionTitle the section title
     * @param courseTitle  the parent course title
     */
    void logSectionUpdated(UUID userId, String userName, Long sectionId, String sectionTitle, String courseTitle);

    /**
     * Log a section deletion action.
     *
     * @param userId       the user who deleted the section
     * @param userName     display name of the user
     * @param sectionId    the section ID
     * @param sectionTitle the section title
     * @param courseTitle  the parent course title
     */
    void logSectionDeleted(UUID userId, String userName, Long sectionId, String sectionTitle, String courseTitle);

    /**
     * Log a lesson creation action.
     *
     * @param userId      the user who created the lesson
     * @param userName    display name of the user
     * @param lessonId    the lesson ID
     * @param lessonTitle the lesson title
     * @param courseTitle the parent course title
     */
    void logLessonCreated(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle);

    /**
     * Log a lesson update action.
     *
     * @param userId      the user who updated the lesson
     * @param userName    display name of the user
     * @param lessonId    the lesson ID
     * @param lessonTitle the lesson title
     * @param courseTitle the parent course title
     */
    void logLessonUpdated(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle);

    /**
     * Log a lesson deletion action.
     *
     * @param userId      the user who deleted the lesson
     * @param userName    display name of the user
     * @param lessonId    the lesson ID
     * @param lessonTitle the lesson title
     * @param courseTitle the parent course title
     */
    void logLessonDeleted(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle);

    /**
     * Get recent activity logs.
     *
     * @param limit maximum number of activities to return
     * @return list of recent activity logs
     */
    List<AdminActivityLog> getRecentActivities(int limit);
}
