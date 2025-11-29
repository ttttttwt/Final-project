package com.lexia.backend.repository;

import com.lexia.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Course entity.
 * 
 * <p>
 * Provides CRUD operations and custom queries for course management.
 * Extends JpaSpecificationExecutor for dynamic query building with
 * Specifications.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Course
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    /**
     * Find all published courses by CEFR level.
     * Uses composite index idx_courses_cefr_published for optimal performance.
     * 
     * @param cefrLevel   the CEFR level (A1, A2, B1, B2, C1, C2)
     * @param isPublished the publication status
     * @return list of courses matching the criteria
     */
    List<Course> findByCefrLevelAndIsPublished(String cefrLevel, Boolean isPublished);

    /**
     * Search courses by title (case-insensitive, partial match).
     * Uses B-tree index idx_courses_title.
     * 
     * @param title the title keyword to search for
     * @return list of courses with matching titles
     */
    List<Course> findByTitleContainingIgnoreCase(String title);

    /**
     * Check if a course with the given title already exists.
     * Used to prevent duplicate course titles.
     * 
     * @param title the course title to check
     * @return true if course with title exists, false otherwise
     */
    boolean existsByTitle(String title);

    /**
     * Check if a course with the given title exists, excluding a specific course
     * ID.
     * Used for update operations to check for duplicate titles.
     * 
     * @param title the course title to check
     * @param id    the course ID to exclude from the check
     * @return true if another course with the title exists, false otherwise
     */
    boolean existsByTitleAndIdNot(String title, Long id);

    /**
     * Find course by ID with sections eagerly loaded.
     * Uses @EntityGraph to avoid N+1 query problem.
     * Fetches course and its sections in a single query.
     * 
     * @param id the course ID
     * @return Optional containing the course with sections if found
     */
    @EntityGraph(attributePaths = { "sections" })
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithSections(@Param("id") Long id);

    /**
     * Find all published courses ordered by creation date (newest first).
     * Uses index idx_courses_created_at for optimal sorting.
     * 
     * @return list of published courses ordered by creation date descending
     */
    List<Course> findByIsPublishedTrueOrderByCreatedAtDesc();

    /**
     * Count courses by CEFR level.
     * Useful for analytics and dashboard displays.
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @return count of courses at the specified level
     */
    long countByCefrLevel(String cefrLevel);

    /**
     * Count published courses.
     * 
     * @return count of published courses
     */
    long countByIsPublishedTrue();

    /**
     * Find all courses by CEFR level ordered by creation date.
     * 
     * @param cefrLevel the CEFR level (A1, A2, B1, B2, C1, C2)
     * @return list of courses at the specified level ordered by creation date
     *         descending
     */
    List<Course> findByCefrLevelOrderByCreatedAtDesc(String cefrLevel);

    /**
     * Find the most recently created courses.
     * Used for admin dashboard recent activity.
     * 
     * @param pageable pagination info (use PageRequest.of(0, N) to get top N)
     * @return list of most recently created courses
     */
    @Query("SELECT c FROM Course c ORDER BY c.createdAt DESC")
    List<Course> findRecentlyCreated(org.springframework.data.domain.Pageable pageable);

    /**
     * Find the most recently published courses.
     * Used for admin dashboard recent activity.
     * 
     * @param pageable pagination info (use PageRequest.of(0, N) to get top N)
     * @return list of most recently published courses
     */
    @Query("SELECT c FROM Course c WHERE c.isPublished = true ORDER BY c.updatedAt DESC")
    List<Course> findRecentlyPublished(org.springframework.data.domain.Pageable pageable);
}
