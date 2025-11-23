package com.lexia.backend.service;

import com.lexia.backend.dto.LessonProgressDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.entity.User;

/**
 * Service interface for managing lesson progress and learning streaks.
 * 
 * <p>
 * Handles:
 * <ul>
 * <li>Marking lessons as complete with result details</li>
 * <li>Calculating consecutive day streaks</li>
 * <li>Tracking daily learning activity</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface ProgressService {

    /**
     * Marks a lesson as complete for the authenticated user.
     * 
     * <p>
     * This method:
     * <ul>
     * <li>Creates or updates LessonProgress record with COMPLETED status</li>
     * <li>Stores result details as JSONB (e.g., quiz answers, AI feedback)</li>
     * <li>Increments attempt counter</li>
     * <li>Triggers enrollment progress recalculation via
     * {@link EnrollmentService#updateEnrollmentProgress}</li>
     * </ul>
     * 
     * <p>
     * The resultDetailsJson parameter should be valid JSON string. Example:
     * 
     * <pre>
     * {
     *   "score": 85,
     *   "correctAnswers": 17,
     *   "totalQuestions": 20,
     *   "timeSpent": 420,
     *   "aiNarrative": "Great job on pronunciation!"
     * }
     * </pre>
     * 
     * @param user              the authenticated user completing the lesson
     * @param lessonId          the ID of the lesson to mark complete
     * @param resultDetailsJson JSON string containing completion results (can be
     *                          null)
     * @return DTO with updated progress details
     * @throws com.lexia.backend.exception.LessonNotFoundException     if lesson
     *                                                                 doesn't exist
     * @throws com.lexia.backend.exception.EnrollmentNotFoundException if user not
     *                                                                 enrolled in
     *                                                                 course
     * @throws IllegalArgumentException                                if
     *                                                                 resultDetailsJson
     *                                                                 is invalid
     *                                                                 JSON
     */
    LessonProgressDTO completeLesson(User user, Long lessonId, String resultDetailsJson);

    /**
     * Retrieves progress for all lessons in a specific course for the authenticated user.
     * 
     * @param user     the authenticated user
     * @param courseId the course ID
     * @return DTO containing course progress summary and lesson-by-lesson status
     */
    com.lexia.backend.dto.CourseProgressDTO getCourseProgress(User user, Long courseId);

    /**
     * Retrieves aggregated dashboard overview data for the authenticated user.
     * Includes stats, weekly goals, recent activity, and recommendations.
     * 
     * @param user the authenticated user
     * @return DTO containing all dashboard data
     */
    com.lexia.backend.dto.DashboardOverviewDTO getDashboardOverview(User user);

    /**
     * Calculates the user's current learning streak.
     * 
     * <p>
     * Streak is defined as consecutive days with at least one lesson completion.
     * Uses timezone-aware date calculations to determine "today" correctly.
     * 
     * <p>
     * The streak breaks if there's a gap of more than 24 hours between completions.
     * 
     * @param user the authenticated user
     * @return DTO containing current streak, longest streak, and activity stats
     */
    StreakDTO getStreak(User user);

    /**
     * Retrieves progress summary for the authenticated user over a specified period.
     * 
     * @param user the authenticated user
     * @param days number of days to look back
     * @return DTO containing progress summary and daily activities
     */
    com.lexia.backend.dto.ProgressSummaryDTO getProgressSummary(User user, int days);
}
