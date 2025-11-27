package com.lexia.backend.service;

import com.lexia.backend.dto.CreateSectionDTO;
import com.lexia.backend.dto.ReorderSectionsDTO;
import com.lexia.backend.dto.SectionDTO;
import com.lexia.backend.dto.UpdateSectionDTO;
import com.lexia.backend.exception.CourseNotFoundException;

import java.util.List;

/**
 * Service interface for section management operations.
 * 
 * <p>
 * Handles CRUD operations for sections within courses. Sections organize
 * lessons into logical groups and are ordered within a course.
 * </p>
 * 
 * <p>
 * Business rules:
 * </p>
 * <ul>
 * <li>Only users with CONTENT_MANAGER role can create/edit sections</li>
 * <li>Each section has a unique order_index within its parent course</li>
 * <li>Sections can be reordered within a course</li>
 * <li>Deleting a section cascades to delete all its lessons</li>
 * </ul>
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface SectionService {

    /**
     * Creates a new section within a course.
     * 
     * <p>
     * If orderIndex is not provided in the DTO, the section is added at the end.
     * If orderIndex is provided, existing sections are shifted to make room.
     * </p>
     *
     * @param courseId the course ID to add the section to
     * @param dto      the section data to create
     * @return SectionDTO containing the created section
     * @throws CourseNotFoundException if course not found
     */
    SectionDTO create(Long courseId, CreateSectionDTO dto);

    /**
     * Updates an existing section.
     * 
     * <p>
     * Only provided fields are updated. Null fields are ignored.
     * </p>
     *
     * @param sectionId the section ID
     * @param dto       the fields to update
     * @return SectionDTO containing the updated section
     * @throws SectionNotFoundException if section not found
     */
    SectionDTO update(Long sectionId, UpdateSectionDTO dto);

    /**
     * Retrieves a section by ID.
     *
     * @param sectionId the section ID
     * @return SectionDTO containing the section data
     * @throws SectionNotFoundException if section not found
     */
    SectionDTO getById(Long sectionId);

    /**
     * Retrieves all sections for a course, ordered by orderIndex.
     *
     * @param courseId the course ID
     * @return List of SectionDTO ordered by orderIndex ascending
     * @throws CourseNotFoundException if course not found
     */
    List<SectionDTO> getByCourseId(Long courseId);

    /**
     * Deletes a section and all its lessons.
     * 
     * <p>
     * After deletion, remaining sections are re-indexed to maintain
     * consecutive order indexes.
     * </p>
     *
     * @param sectionId the section ID
     * @throws SectionNotFoundException if section not found
     */
    void delete(Long sectionId);

    /**
     * Reorders sections within a course.
     * 
     * <p>
     * The sectionIds list must contain all section IDs for the course
     * in the desired new order.
     * </p>
     *
     * @param courseId the course ID
     * @param dto      containing the new order of section IDs
     * @return List of SectionDTO in the new order
     * @throws CourseNotFoundException  if course not found
     * @throws IllegalArgumentException if section IDs don't match course sections
     */
    List<SectionDTO> reorder(Long courseId, ReorderSectionsDTO dto);
}
