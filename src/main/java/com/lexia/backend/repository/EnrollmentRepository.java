package com.lexia.backend.repository;

import com.lexia.backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link Enrollment} entity.
 * Provides CRUD operations and custom queries for enrollment management.
 * 
 * <p>
 * Uses indexes:
 * <ul>
 * <li>idx_enrollments_user - user_id queries</li>
 * <li>idx_enrollments_course - course_id queries</li>
 * <li>idx_enrollments_user_course - composite unique constraint queries</li>
 * <li>idx_enrollments_completed - completed enrollment queries</li>
 * </ul>
 * </p>
 * 
 * @see Enrollment
 * @author LEXIA Team
 * @since Sprint 2
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments for a specific user.
     * Orders by most recent enrollment first.
     * Uses idx_enrollments_user.
     * 
     * @param userId the user's UUID
     * @return list of enrollments, empty if none found
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId ORDER BY e.enrolledAt DESC")
    List<Enrollment> findByUserId(@Param("userId") UUID userId);

    /**
     * Find a specific enrollment by user and course.
     * Uses idx_enrollments_user_course.
     * 
     * @param userId   the user's UUID
     * @param courseId the course ID
     * @return Optional containing the enrollment if found
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.course.id = :courseId")
    Optional<Enrollment> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") Long courseId);

    /**
     * Check if user is already enrolled in a course.
     * Uses idx_enrollments_user_course for fast lookup.
     * 
     * @param userId   the user's UUID
     * @param courseId the course ID
     * @return true if enrollment exists, false otherwise
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") Long courseId);

    /**
     * Find all completed enrollments for a user.
     * Uses idx_enrollments_completed partial index.
     * 
     * @param userId the user's UUID
     * @return list of completed enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.completedAt IS NOT NULL ORDER BY e.completedAt DESC")
    List<Enrollment> findCompletedByUserId(@Param("userId") UUID userId);

    /**
     * Find all active (incomplete) enrollments for a user.
     * 
     * @param userId the user's UUID
     * @return list of active enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.completedAt IS NULL ORDER BY e.enrolledAt DESC")
    List<Enrollment> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Count total enrollments for a specific course.
     * Useful for course analytics.
     * Uses idx_enrollments_course.
     * 
     * @param courseId the course ID
     * @return number of enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    /**
     * Count completed enrollments for a specific course.
     * Uses idx_enrollments_completed partial index.
     * 
     * @param courseId the course ID
     * @return number of completed enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.completedAt IS NOT NULL")
    long countCompletedByCourseId(@Param("courseId") Long courseId);

    /**
     * Find recent enrollments across all courses.
     * Used for admin dashboard recent activity.
     * 
     * @param pageable pagination info (use PageRequest.of(0, N) to get top N)
     * @return list of recent enrollments
     */
    @Query("SELECT e FROM Enrollment e ORDER BY e.enrolledAt DESC")
    List<Enrollment> findRecentEnrollments(org.springframework.data.domain.Pageable pageable);

    /**
     * Count enrollments within a date range.
     * Used for counting active enrollments in last N days.
     * 
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return count of enrollments in the date range
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrolledAt >= :startDate AND e.enrolledAt <= :endDate")
    long countByEnrolledAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Count completed enrollments.
     * 
     * @return number of completed enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.completedAt IS NOT NULL")
    long countCompleted();

    /**
     * Count enrollments by course CEFR level.
     * Used for dashboard overview chart.
     * 
     * @param cefrLevel the CEFR level
     * @return count of enrollments for courses at that level
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.cefrLevel = :cefrLevel")
    long countByCefrLevel(@Param("cefrLevel") String cefrLevel);
}
