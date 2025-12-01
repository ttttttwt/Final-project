package com.lexia.backend.service.impl;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.file.dto.FileUploadResponse;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.mapper.FileMapper;
import com.lexia.backend.file.service.FileStorageService;
import com.lexia.backend.mapper.LessonMapper;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.SectionRepository;
import com.lexia.backend.service.AdminActivityLogService;
import com.lexia.backend.service.LessonContentValidator;
import com.lexia.backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of LessonService for managing lessons.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final LessonContentValidator contentValidator;
    private final AdminActivityLogService adminActivityLogService;
    private final FileStorageService fileStorageService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LessonDTO create(Long sectionId, CreateLessonDTO dto) {
        log.debug("Creating new lesson in section ID: {}", sectionId);

        // Verify section exists
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", sectionId);
                    return new SectionNotFoundException("Section not found with ID: " + sectionId);
                });

        // Validate lesson content
        contentValidator.validate(dto.getLessonType(), dto.getContent());

        // Set order index if not provided
        Integer orderIndex = dto.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrder = lessonRepository.findMaxOrderIndexBySectionId(sectionId);
            orderIndex = (maxOrder != null) ? maxOrder + 1 : 0;
        }

        // Create lesson entity
        Lesson lesson = LessonMapper.toEntity(dto);
        lesson.setSection(section);
        lesson.setOrderIndex(orderIndex);

        // Save lesson
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Successfully created lesson with ID: {} in section ID: {}", savedLesson.getId(), sectionId);

        // Log activity
        String courseTitle = section.getCourse().getTitle();
        adminActivityLogService.logLessonCreated(
                getCurrentUserId(),
                getCurrentUserName(),
                savedLesson.getId(),
                savedLesson.getTitle(),
                courseTitle);

        return LessonMapper.toDTO(savedLesson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LessonDTO update(Long id, UpdateLessonDTO dto) {
        log.debug("Updating lesson with ID: {}", id);

        // Fetch existing lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        // Validate content if being updated
        if (dto.getContent() != null) {
            // Use lesson type from DTO if provided, otherwise use existing
            Lesson.LessonType typeToValidate = dto.getLessonType() != null
                    ? dto.getLessonType()
                    : lesson.getLessonType();
            contentValidator.validate(typeToValidate, dto.getContent());
        }

        // Update entity fields (partial update - only non-null fields)
        if (dto.getTitle() != null) {
            lesson.setTitle(dto.getTitle());
        }
        if (dto.getLessonType() != null) {
            lesson.setLessonType(dto.getLessonType());
        }
        if (dto.getContent() != null) {
            lesson.setContent(dto.getContent());
        }
        if (dto.getOrderIndex() != null) {
            lesson.setOrderIndex(dto.getOrderIndex());
        }
        if (dto.getDurationMinutes() != null) {
            lesson.setDurationMinutes(dto.getDurationMinutes());
        }

        // Save updated lesson
        Lesson updatedLesson = lessonRepository.save(lesson);
        log.info("Successfully updated lesson with ID: {}", id);

        // Log activity
        String courseTitle = lesson.getSection().getCourse().getTitle();
        adminActivityLogService.logLessonUpdated(
                getCurrentUserId(),
                getCurrentUserName(),
                updatedLesson.getId(),
                updatedLesson.getTitle(),
                courseTitle);

        return LessonMapper.toDTO(updatedLesson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public LessonDTO getById(Long id) {
        log.debug("Fetching lesson with ID: {}", id);

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        log.debug("Successfully retrieved lesson with ID: {}", id);
        return LessonMapper.toDTO(lesson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getAllBySectionId(Long sectionId) {
        log.debug("Fetching all lessons for section ID: {}", sectionId);

        // Verify section exists
        if (!sectionRepository.existsById(sectionId)) {
            log.warn("Section not found with ID: {}", sectionId);
            throw new SectionNotFoundException("Section not found with ID: " + sectionId);
        }

        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
        log.debug("Found {} lessons for section ID: {}", lessons.size(), sectionId);

        return lessons.stream()
                .map(LessonMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getAllByCourseId(Long courseId) {
        log.debug("Fetching all lessons for course ID: {}", courseId);

        List<Lesson> lessons = lessonRepository.findAllByCourseIdOrderBySectionAndLesson(courseId);
        log.debug("Found {} lessons for course ID: {}", lessons.size(), courseId);

        return lessons.stream()
                .map(LessonMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting lesson with ID: {}", id);

        // Fetch lesson for logging info
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        String lessonTitle = lesson.getTitle();
        String courseTitle = lesson.getSection().getCourse().getTitle();

        // Delete lesson
        lessonRepository.deleteById(id);
        log.info("Successfully deleted lesson with ID: {}", id);

        // Log activity
        adminActivityLogService.logLessonDeleted(
                getCurrentUserId(),
                getCurrentUserName(),
                id,
                lessonTitle,
                courseTitle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LessonDTO reorder(Long id, Integer newOrderIndex) {
        log.debug("Reordering lesson with ID: {} to order index: {}", id, newOrderIndex);

        // Fetch lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        Long sectionId = lesson.getSection().getId();
        Integer oldOrderIndex = lesson.getOrderIndex();

        // If same position, no need to reorder
        if (oldOrderIndex.equals(newOrderIndex)) {
            log.debug("Lesson already at order index: {}, skipping reorder", newOrderIndex);
            return LessonMapper.toDTO(lesson);
        }

        // Get all lessons in the section ordered by orderIndex
        List<Lesson> sectionLessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);

        // Validate newOrderIndex bounds
        if (newOrderIndex < 0) {
            newOrderIndex = 0;
        }
        if (newOrderIndex >= sectionLessons.size()) {
            newOrderIndex = sectionLessons.size() - 1;
        }

        // Temporarily set moving lesson's orderIndex to a value outside normal range
        // to avoid unique constraint violation during shifting
        lesson.setOrderIndex(-1);
        lessonRepository.saveAndFlush(lesson);

        // Shift other lessons - order matters to avoid constraint violations!
        if (oldOrderIndex < newOrderIndex) {
            // Moving down: shift lessons between old+1 and new up by 1
            // Shift from lowest to highest index to avoid conflicts
            for (Lesson l : sectionLessons) {
                if (!l.getId().equals(id) && l.getOrderIndex() > oldOrderIndex && l.getOrderIndex() <= newOrderIndex) {
                    l.setOrderIndex(l.getOrderIndex() - 1);
                    lessonRepository.saveAndFlush(l);
                }
            }
        } else {
            // Moving up: shift lessons between new and old-1 down by 1
            // Shift from highest to lowest index to avoid conflicts
            // (process in reverse order so we don't create duplicates)
            for (int i = sectionLessons.size() - 1; i >= 0; i--) {
                Lesson l = sectionLessons.get(i);
                if (!l.getId().equals(id) && l.getOrderIndex() >= newOrderIndex && l.getOrderIndex() < oldOrderIndex) {
                    l.setOrderIndex(l.getOrderIndex() + 1);
                    lessonRepository.saveAndFlush(l);
                }
            }
        }

        // Set final order index
        lesson.setOrderIndex(newOrderIndex);
        Lesson reorderedLesson = lessonRepository.save(lesson);

        log.info("Successfully reordered lesson with ID: {} from index {} to index: {}", id, oldOrderIndex,
                newOrderIndex);

        return LessonMapper.toDTO(reorderedLesson);
    }

    /**
     * Get current authenticated user's ID.
     *
     * @return UUID of current user or null if not authenticated
     */
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return null;
    }

    /**
     * Get current authenticated user's display name.
     *
     * @return display name or "System" if not authenticated
     */
    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            if (user.getProfile() != null && user.getProfile().getFirstName() != null) {
                return user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
            }
            return user.getEmail().split("@")[0];
        }
        return "System";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FileUploadResponse uploadAudio(Long id, MultipartFile file, UUID userId) {
        log.debug("Uploading audio for lesson ID: {}", id);

        // Fetch lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        // Validate lesson type is LISTENING
        if (lesson.getLessonType() != Lesson.LessonType.LISTENING) {
            log.warn("Attempted to upload audio to non-LISTENING lesson ID: {}", id);
            throw new IllegalStateException("Audio can only be uploaded to LISTENING lessons");
        }

        // Delete existing audio file if present
        if (lesson.getAudioFile() != null) {
            UUID existingFileId = lesson.getAudioFile().getId();
            log.debug("Deleting existing audio file: {}", existingFileId);
            fileStorageService.delete(existingFileId);
        }

        // Store new audio file
        FileEntity savedFile = fileStorageService.store(file, FileCategory.LESSON_AUDIO, userId);

        // Update lesson with new audio file reference
        lesson.setAudioFile(savedFile);
        lessonRepository.save(lesson);

        log.info("Successfully uploaded audio for lesson ID: {} with file ID: {}", id, savedFile.getId());

        // Log activity
        String courseTitle = lesson.getSection().getCourse().getTitle();
        adminActivityLogService.logLessonUpdated(
                getCurrentUserId(),
                getCurrentUserName(),
                lesson.getId(),
                lesson.getTitle(),
                courseTitle);

        return FileMapper.toUploadResponse(savedFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAudio(Long id) {
        log.debug("Deleting audio for lesson ID: {}", id);

        // Fetch lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lesson not found with ID: {}", id);
                    return new LessonNotFoundException("Lesson not found with ID: " + id);
                });

        // Check if lesson has an audio file
        if (lesson.getAudioFile() != null) {
            UUID fileId = lesson.getAudioFile().getId();

            // Clear reference first to avoid FK constraint issues
            lesson.setAudioFile(null);
            lessonRepository.save(lesson);

            // Delete the file
            fileStorageService.delete(fileId);

            log.info("Successfully deleted audio for lesson ID: {}", id);

            // Log activity
            String courseTitle = lesson.getSection().getCourse().getTitle();
            adminActivityLogService.logLessonUpdated(
                    getCurrentUserId(),
                    getCurrentUserName(),
                    lesson.getId(),
                    lesson.getTitle(),
                    courseTitle);
        } else {
            log.debug("Lesson ID: {} has no uploaded audio file to delete", id);
        }
    }
}
