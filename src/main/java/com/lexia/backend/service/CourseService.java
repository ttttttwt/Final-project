package com.lexia.backend.service;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CourseSearchDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.DuplicateCourseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for course management operations.
 * 
 * <p>
 * Handles CRUD operations for courses including creation, updating, deletion,
 * publishing, and searching. Enforces business rules such as:
 * </p>
 * <ul>
 * <li>Only users with CONTENT_MANAGER role can create/edit courses</li>
 * <li>Course titles must be unique</li>
 * <li>Published courses cannot be deleted (must unpublish first)</li>
 * <li>Courses must have content before publishing</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface CourseService {

    /**
     * Creates a new course.
     * 
     * <p>
     * Business rules:
     * </p>
     * <ul>
     * <li>Title must be unique</li>
     * <li>User must have CONTENT_MANAGER role (enforced by controller)</li>
     * <li>Course is created as unpublished by default</li>
     * </ul>
     * 
     * @param dto the course data to create
     * @return CourseDTO containing the created course
     * @throws DuplicateCourseException if a course with the same title already
     *                                  exists
     */
    CourseDTO create(CreateCourseDTO dto);

    /**
     * Updates an existing course.
     * 
     * <p>
     * Only provided fields are updated. Null fields are ignored.
     * </p>
     * 
     * @param id  the course ID
     * @param dto the fields to update
     * @return CourseDTO containing the updated course
     * @throws CourseNotFoundException  if course not found
     * @throws DuplicateCourseException if updating title to an existing title
     */
    CourseDTO update(Long id, UpdateCourseDTO dto);

    /**
     * Retrieves a course by ID.
     * 
     * @param id the course ID
     * @return CourseDTO containing the course data
     * @throws CourseNotFoundException if course not found
     */
    CourseDTO getById(Long id);

    /**
     * Retrieves a course by ID with all sections included.
     * Uses @EntityGraph to prevent N+1 queries.
     * 
     * @param id the course ID
     * @return CourseDTO containing the course data with sections
     * @throws CourseNotFoundException if course not found
     */
    CourseDTO getByIdWithSections(Long id);

    /**
     * Deletes a course.
     * 
     * <p>
     * Business rule: Published courses cannot be deleted.
     * Must unpublish first using {@link #unpublish(Long)}.
     * </p>
     * 
     * @param id the course ID
     * @throws CourseNotFoundException if course not found
     * @throws IllegalStateException   if course is published
     */
    void delete(Long id);

    /**
     * Publishes a course, making it visible to learners.
     * 
     * <p>
     * Business rule: Course must have at least one section with lessons
     * before it can be published.
     * </p>
     * 
     * @param id the course ID
     * @return CourseDTO containing the published course
     * @throws CourseNotFoundException if course not found
     * @throws IllegalStateException   if course has no content (sections/lessons)
     */
    CourseDTO publish(Long id);

    /**
     * Unpublishes a course, hiding it from learners.
     * 
     * @param id the course ID
     * @return CourseDTO containing the unpublished course
     * @throws CourseNotFoundException if course not found
     */
    CourseDTO unpublish(Long id);

    /**
     * Searches for courses using filters and pagination.
     * 
     * <p>
     * Supports filtering by:
     * </p>
     * <ul>
     * <li>Title (case-insensitive partial match)</li>
     * <li>CEFR level (exact match)</li>
     * <li>Publication status</li>
     * <li>Creation date range</li>
     * </ul>
     * 
     * @param searchDTO the search criteria and pagination parameters
     * @return Page of CourseDTO matching the search criteria
     */
    Page<CourseDTO> search(CourseSearchDTO searchDTO);

    /**
     * Retrieves all published courses with pagination.
     * 
     * @param pageable pagination parameters
     * @return Page of published CourseDTO
     */
    Page<CourseDTO> getAllPublished(Pageable pageable);

    /**
     * Retrieves all courses (published and unpublished) with pagination.
     * Typically used by content managers.
     * 
     * @param pageable pagination parameters
     * @return Page of all CourseDTO
     */
    Page<CourseDTO> getAll(Pageable pageable);
}
