package com.lexia.backend.repository;

import com.lexia.backend.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link LessonProgress} entity.
 * Provides CRUD operations and custom queries for lesson progress tracking.
 * 
 * <p>
 * Uses indexes:
 * <ul>
 * <li>idx_lesson_progress_user_lesson - composite unique constraint
 * queries</li>
 * <li>idx_lesson_progress_user - user progress queries</li>
 * <li>idx_lesson_progress_status - status filtering</li>
 * <li>idx_lesson_progress_completed_at - streak calculation queries</li>
 * <li>idx_lesson_progress_date_range - date range queries for analytics</li>
 * </ul>
 * </p>
 * 
 * @see LessonProgress
 * @author LEXIA Team
 * @since Sprint 2
 */
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    /**
     * Find progress record for a specific user and lesson.
     * Uses idx_lesson_progress_user_lesson.
     * 
     * @param userId   the user's UUID
     * @param lessonId the lesson ID
     * @return Optional containing the progress if found
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId AND lp.lesson.id = :lessonId")
    Optional<LessonProgress> findByUserIdAndLessonId(@Param("userId") UUID userId, @Param("lessonId") Long lessonId);

    /**
     * Find all progress records for a user, ordered by most recent update.
     * Uses idx_lesson_progress_user.
     * 
     * @param userId the user's UUID
     * @return list of progress records
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId ORDER BY lp.updatedAt DESC")
    List<LessonProgress> findByUserId(@Param("userId") UUID userId);

    /**
     * Find all progress records for a user with a specific status.
     * Uses idx_lesson_progress_status.
     * 
     * @param userId the user's UUID
     * @param status the lesson status
     * @return list of progress records matching the status
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId AND lp.status = :status ORDER BY lp.updatedAt DESC")
    List<LessonProgress> findByUserIdAndStatus(@Param("userId") UUID userId,
            @Param("status") LessonProgress.Status status);

    /**
     * Find all completed lessons for a user within a date range.
     * Used for streak calculation and analytics.
     * Uses idx_lesson_progress_date_range.
     * 
     * @param userId    the user's UUID
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return list of completed progress records
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.completedAt IS NOT NULL " +
            "AND lp.completedAt >= :startDate AND lp.completedAt <= :endDate " +
            "ORDER BY lp.completedAt ASC")
    List<LessonProgress> findCompletedByUserIdBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count completed lessons for a user within a date range.
     * Useful for daily/weekly/monthly statistics.
     * Uses idx_lesson_progress_date_range.
     * 
     * @param userId    the user's UUID
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return number of completed lessons
     */
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.completedAt IS NOT NULL " +
            "AND lp.completedAt >= :startDate AND lp.completedAt <= :endDate")
    long countByUserIdAndCompletedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all progress records for lessons in a specific section.
     * Useful for section-level progress tracking.
     * 
     * @param userId    the user's UUID
     * @param sectionId the section ID
     * @return list of progress records for lessons in the section
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.lesson.section.id = :sectionId ORDER BY lp.lesson.orderIndex ASC")
    List<LessonProgress> findByUserIdAndSectionId(@Param("userId") UUID userId, @Param("sectionId") Long sectionId);

    /**
     * Find all progress records for lessons in a specific course.
     * Useful for course-level progress tracking.
     * 
     * @param userId   the user's UUID
     * @param courseId the course ID
     * @return list of progress records for lessons in the course
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.lesson.section.course.id = :courseId " +
            "ORDER BY lp.lesson.section.orderIndex ASC, lp.lesson.orderIndex ASC")
    List<LessonProgress> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") Long courseId);

    /**
     * Count completed lessons for a user in a specific course.
     * Used for enrollment progress calculation.
     * 
     * @param userId   the user's UUID
     * @param courseId the course ID
     * @return number of completed lessons
     */
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.lesson.section.course.id = :courseId AND lp.status = 'COMPLETED'")
    long countCompletedByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") Long courseId);

    /**
     * Check if a user has completed a specific lesson.
     * 
     * @param userId   the user's UUID
     * @param lessonId the lesson ID
     * @return true if lesson is completed, false otherwise
     */
    @Query("SELECT COUNT(lp) > 0 FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.lesson.id = :lessonId AND lp.status = 'COMPLETED'")
    boolean isLessonCompleted(@Param("userId") UUID userId, @Param("lessonId") Long lessonId);

    /**
     * Find all completed lessons for a user, ordered by completion date.
     * Uses idx_lesson_progress_completed_at.
     * 
     * @param userId the user's UUID
     * @return list of completed progress records
     */
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.userId = :userId " +
            "AND lp.completedAt IS NOT NULL ORDER BY lp.completedAt DESC")
    List<LessonProgress> findCompletedByUserId(@Param("userId") UUID userId);
}
