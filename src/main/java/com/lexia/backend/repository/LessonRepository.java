package com.lexia.backend.repository;

import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Lesson.LessonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Lesson entity.
 * 
 * <p>
 * Provides CRUD operations and custom queries for lesson management.
 * Lessons are automatically ordered by their orderIndex field within sections.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Lesson
 * @see LessonType
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Find all lessons for a section, ordered by their order index.
     * Uses index idx_lessons_section_order for optimal performance.
     * 
     * @param sectionId the section ID
     * @return list of lessons ordered by orderIndex ascending
     */
    List<Lesson> findBySectionIdOrderByOrderIndexAsc(Long sectionId);

    /**
     * Find lessons by type.
     * Uses index idx_lessons_type for optimal performance.
     * Useful for filtering lessons (e.g., show all QUIZ lessons).
     * 
     * @param lessonType the lesson type (READING, LISTENING, QUIZ, SPEAKING)
     * @return list of lessons of the specified type
     */
    List<Lesson> findByLessonType(LessonType lessonType);

    /**
     * Count lessons in a section.
     * Useful for progress tracking and section statistics.
     * 
     * @param sectionId the section ID
     * @return count of lessons in the section
     */
    long countBySectionId(Long sectionId);

    /**
     * Count lessons by type in a section.
     * Useful for analytics (e.g., "This section has 3 reading lessons").
     * 
     * @param sectionId  the section ID
     * @param lessonType the lesson type
     * @return count of lessons of the specified type in the section
     */
    long countBySectionIdAndLessonType(Long sectionId, LessonType lessonType);

    /**
     * Check if a lesson exists for a section at a specific order index.
     * Used to prevent duplicate order indexes within a section.
     * 
     * @param sectionId  the section ID
     * @param orderIndex the order index
     * @return true if lesson exists at that position, false otherwise
     */
    boolean existsBySectionIdAndOrderIndex(Long sectionId, Integer orderIndex);

    /**
     * Find lesson by section ID and order index.
     * 
     * @param sectionId  the section ID
     * @param orderIndex the order index
     * @return Optional containing the lesson if found
     */
    Optional<Lesson> findBySectionIdAndOrderIndex(Long sectionId, Integer orderIndex);

    /**
     * Get the maximum order index for lessons in a section.
     * Used when adding new lessons to determine the next order index.
     * 
     * @param sectionId the section ID
     * @return the maximum order index, or null if no lessons exist
     */
    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.section.id = :sectionId")
    Integer findMaxOrderIndexBySectionId(@Param("sectionId") Long sectionId);

    /**
     * Find all lessons for a course (across all sections).
     * Ordered by section order index, then lesson order index.
     * Useful for course-level statistics and progress tracking.
     * 
     * @param courseId the course ID
     * @return list of lessons ordered by section and lesson order
     */
    @Query("SELECT l FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId ORDER BY s.orderIndex, l.orderIndex")
    List<Lesson> findAllByCourseIdOrderBySectionAndLesson(@Param("courseId") Long courseId);

    /**
     * Count total lessons in a course.
     * Used for progress calculation and course statistics.
     * 
     * @param courseId the course ID
     * @return total count of lessons across all sections in the course
     */
    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    /**
     * Find lessons by type in a course.
     * Useful for analytics (e.g., "This course has 10 reading lessons").
     * 
     * @param courseId   the course ID
     * @param lessonType the lesson type
     * @return list of lessons of the specified type in the course
     */
    @Query("SELECT l FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId AND l.lessonType = :lessonType ORDER BY s.orderIndex, l.orderIndex")
    List<Lesson> findByCourseIdAndLessonType(@Param("courseId") Long courseId,
            @Param("lessonType") LessonType lessonType);

    /**
     * Delete all lessons for a section.
     * Note: This is handled automatically by CASCADE DELETE in the database,
     * but provided for explicit service-level operations if needed.
     * 
     * @param sectionId the section ID
     */
    void deleteBySectionId(Long sectionId);
}
