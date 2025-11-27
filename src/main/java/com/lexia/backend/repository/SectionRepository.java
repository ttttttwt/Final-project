package com.lexia.backend.repository;

import com.lexia.backend.entity.Section;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Section entity.
 * 
 * <p>
 * Provides CRUD operations and custom queries for section management.
 * Sections are automatically ordered by their orderIndex field.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Section
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    /**
     * Find all sections for a course, ordered by their order index.
     * Uses index idx_sections_course_order for optimal performance.
     * 
     * @param courseId the course ID
     * @return list of sections ordered by orderIndex ascending
     */
    List<Section> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    /**
     * Find all sections for a course with lessons eagerly fetched.
     * Used when lesson count is needed for the response.
     * 
     * @param courseId the course ID
     * @return list of sections with lessons loaded, ordered by orderIndex ascending
     */
    @EntityGraph(attributePaths = { "lessons" })
    @Query("SELECT s FROM Section s WHERE s.course.id = :courseId ORDER BY s.orderIndex ASC")
    List<Section> findByCourseIdWithLessons(@Param("courseId") Long courseId);

    /**
     * Find a section by ID with lessons eagerly fetched.
     * Used when lesson count is needed for the response.
     * 
     * @param sectionId the section ID
     * @return Optional containing the section with lessons loaded
     */
    @EntityGraph(attributePaths = { "lessons" })
    @Query("SELECT s FROM Section s WHERE s.id = :sectionId")
    Optional<Section> findByIdWithLessons(@Param("sectionId") Long sectionId);

    /**
     * Count sections in a course.
     * Useful for validating course completeness before publishing.
     * 
     * @param courseId the course ID
     * @return count of sections in the course
     */
    long countByCourseId(Long courseId);

    /**
     * Check if a section exists for a course at a specific order index.
     * Used to prevent duplicate order indexes within a course.
     * 
     * @param courseId   the course ID
     * @param orderIndex the order index
     * @return true if section exists at that position, false otherwise
     */
    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    /**
     * Find section by course ID and order index.
     * 
     * @param courseId   the course ID
     * @param orderIndex the order index
     * @return Optional containing the section if found
     */
    Optional<Section> findByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    /**
     * Get the maximum order index for sections in a course.
     * Used when adding new sections to determine the next order index.
     * 
     * @param courseId the course ID
     * @return the maximum order index, or null if no sections exist
     */
    @Query("SELECT MAX(s.orderIndex) FROM Section s WHERE s.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);

    /**
     * Delete all sections for a course.
     * Note: This is handled automatically by CASCADE DELETE in the database,
     * but provided for explicit service-level operations if needed.
     * 
     * @param courseId the course ID
     */
    void deleteByCourseId(Long courseId);
}
