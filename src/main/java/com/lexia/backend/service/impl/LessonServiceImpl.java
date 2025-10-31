package com.lexia.backend.service.impl;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.dto.UpdateLessonDTO;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.mapper.LessonMapper;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.SectionRepository;
import com.lexia.backend.service.LessonContentValidator;
import com.lexia.backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        // Verify lesson exists
        if (!lessonRepository.existsById(id)) {
            log.warn("Lesson not found with ID: {}", id);
            throw new LessonNotFoundException("Lesson not found with ID: " + id);
        }

        // Delete lesson
        lessonRepository.deleteById(id);
        log.info("Successfully deleted lesson with ID: {}", id);
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

        // Update order index
        lesson.setOrderIndex(newOrderIndex);

        // Save updated lesson
        Lesson reorderedLesson = lessonRepository.save(lesson);
        log.info("Successfully reordered lesson with ID: {} to order index: {}", id, newOrderIndex);

        return LessonMapper.toDTO(reorderedLesson);
    }
}
