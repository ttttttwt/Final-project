package com.lexia.backend.service;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.exception.InvalidLessonContentException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for lesson management operations.
 * 
 * <p>
 * Handles CRUD operations for lessons including creation, updating, deletion,
 * and retrieval. Enforces business rules such as:
 * </p>
 * <ul>
 * <li>Lesson content must be valid JSONB matching lesson type schema</li>
 * <li>Lessons must belong to an existing section</li>
 * <li>Order index must be unique within a section</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface LessonService {

    /**
     * Creates a new lesson within a section.
     * 
     * <p>
     * Business rules:
     * </p>
     * <ul>
     * <li>Section must exist</li>
     * <li>Content must be valid JSONB matching the lesson type schema</li>
     * <li>Order index is set automatically if not provided</li>
     * <li>Duration must be between 1-240 minutes</li>
     * </ul>
     * 
     * @param sectionId the section ID where the lesson will be created
     * @param dto       the lesson data to create
     * @return LessonDTO containing the created lesson
     * @throws SectionNotFoundException      if section not found
     * @throws InvalidLessonContentException if content validation fails
     */
    LessonDTO create(Long sectionId, CreateLessonDTO dto);

    /**
     * Updates an existing lesson.
     * 
     * <p>
     * Only provided fields are updated. Null fields are ignored.
     * If content is updated, it will be validated against the lesson type schema.
     * </p>
     * 
     * @param id  the lesson ID
     * @param dto the fields to update
     * @return LessonDTO containing the updated lesson
     * @throws LessonNotFoundException       if lesson not found
     * @throws InvalidLessonContentException if content validation fails
     */
    LessonDTO update(Long id, UpdateLessonDTO dto);

    /**
     * Retrieves a lesson by ID.
     * 
     * @param id the lesson ID
     * @return LessonDTO containing the lesson data
     * @throws LessonNotFoundException if lesson not found
     */
    LessonDTO getById(Long id);

    /**
     * Retrieves all lessons for a specific section, ordered by order_index.
     * 
     * @param sectionId the section ID
     * @return List of LessonDTO ordered by order_index
     * @throws SectionNotFoundException if section not found
     */
    List<LessonDTO> getAllBySectionId(Long sectionId);

    /**
     * Retrieves all lessons for a specific course (across all sections),
     * ordered by section and lesson order.
     * 
     * @param courseId the course ID
     * @return List of LessonDTO ordered by section and lesson order_index
     */
    List<LessonDTO> getAllByCourseId(Long courseId);

    /**
     * Deletes a lesson.
     * 
     * <p>
     * Cascade deletion of related data (e.g., lesson progress) is handled
     * at the database level.
     * </p>
     * 
     * @param id the lesson ID
     * @throws LessonNotFoundException if lesson not found
     */
    void delete(Long id);

    /**
     * Reorders a lesson within its section.
     * 
     * @param id            the lesson ID
     * @param newOrderIndex the new order index
     * @return LessonDTO containing the updated lesson
     * @throws LessonNotFoundException if lesson not found
     */
    LessonDTO reorder(Long id, Integer newOrderIndex);

    /**
     * Uploads an audio file for a LISTENING lesson.
     * If the lesson already has an audio file, it will be replaced.
     * 
     * @param id     the lesson ID
     * @param file   the audio file to upload
     * @param userId the ID of the user performing the upload
     * @return FileUploadResponse containing the uploaded file metadata
     * @throws LessonNotFoundException if lesson not found
     * @throws IllegalStateException   if lesson is not a LISTENING type
     */
    FileUploadResponse uploadAudio(Long id, MultipartFile file, UUID userId);

    /**
     * Deletes the audio file for a lesson.
     * Only deletes if the lesson has an uploaded audio file (not URL in content).
     * 
     * @param id the lesson ID
     * @throws LessonNotFoundException if lesson not found
     */
    void deleteAudio(Long id);
}
