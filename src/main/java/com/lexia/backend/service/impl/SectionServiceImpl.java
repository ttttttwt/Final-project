package com.lexia.backend.service.impl;

import com.lexia.backend.dto.CreateSectionDTO;
import com.lexia.backend.dto.ReorderSectionsDTO;
import com.lexia.backend.dto.SectionDTO;
import com.lexia.backend.dto.UpdateSectionDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Section;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.mapper.SectionMapper;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.SectionRepository;
import com.lexia.backend.service.AdminActivityLogService;
import com.lexia.backend.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SectionService for managing course sections.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final AdminActivityLogService adminActivityLogService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SectionDTO create(Long courseId, CreateSectionDTO dto) {
        log.debug("Creating new section for course ID: {} with title: {}", courseId, dto.getTitle());

        // Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", courseId);
                    return new CourseNotFoundException("Course not found with ID: " + courseId);
                });

        // Determine order index
        Integer orderIndex = dto.getOrderIndex();
        if (orderIndex == null) {
            // Add at the end
            Integer maxIndex = sectionRepository.findMaxOrderIndexByCourseId(courseId);
            orderIndex = (maxIndex == null) ? 0 : maxIndex + 1;
            log.debug("No order index provided, using: {}", orderIndex);
        } else {
            // Shift existing sections if inserting
            shiftSectionsDown(courseId, orderIndex);
        }

        // Create section
        Section section = SectionMapper.toEntity(dto, course, orderIndex);
        Section savedSection = sectionRepository.save(section);

        log.info("Successfully created section with ID: {} for course ID: {}", savedSection.getId(), courseId);

        // Log activity
        adminActivityLogService.logSectionCreated(
                getCurrentUserId(),
                getCurrentUserName(),
                savedSection.getId(),
                savedSection.getTitle(),
                course.getTitle());

        // Re-fetch with lessons to get accurate lessonCount (will be 0 for new section)
        return sectionRepository.findByIdWithLessons(savedSection.getId())
                .map(SectionMapper::toDTO)
                .orElse(SectionMapper.toDTO(savedSection));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SectionDTO update(Long sectionId, UpdateSectionDTO dto) {
        log.debug("Updating section with ID: {}", sectionId);

        // Fetch section
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", sectionId);
                    return new SectionNotFoundException("Section not found with ID: " + sectionId);
                });

        // Update fields
        SectionMapper.updateEntityFromDTO(section, dto);

        // Handle order index update if provided
        if (dto.getOrderIndex() != null && !dto.getOrderIndex().equals(section.getOrderIndex())) {
            updateSectionOrder(section, dto.getOrderIndex());
        }

        Section updatedSection = sectionRepository.save(section);
        log.info("Successfully updated section with ID: {}", sectionId);

        // Log activity
        adminActivityLogService.logSectionUpdated(
                getCurrentUserId(),
                getCurrentUserName(),
                updatedSection.getId(),
                updatedSection.getTitle(),
                section.getCourse().getTitle());

        // Re-fetch with lessons to get accurate lessonCount
        return sectionRepository.findByIdWithLessons(updatedSection.getId())
                .map(SectionMapper::toDTO)
                .orElse(SectionMapper.toDTO(updatedSection));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SectionDTO getById(Long sectionId) {
        log.debug("Fetching section with ID: {}", sectionId);

        // Use findByIdWithLessons to eagerly fetch lessons for lessonCount
        Section section = sectionRepository.findByIdWithLessons(sectionId)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", sectionId);
                    return new SectionNotFoundException("Section not found with ID: " + sectionId);
                });

        return SectionMapper.toDTO(section);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getByCourseId(Long courseId) {
        log.debug("Fetching sections for course ID: {}", courseId);

        // Verify course exists
        if (!courseRepository.existsById(courseId)) {
            log.warn("Course not found with ID: {}", courseId);
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        // Use findByCourseIdWithLessons to eagerly fetch lessons for lessonCount
        List<Section> sections = sectionRepository.findByCourseIdWithLessons(courseId);
        log.debug("Found {} sections for course ID: {}", sections.size(), courseId);

        return sections.stream()
                .map(SectionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long sectionId) {
        log.debug("Deleting section with ID: {}", sectionId);

        // Fetch section
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", sectionId);
                    return new SectionNotFoundException("Section not found with ID: " + sectionId);
                });

        Long courseId = section.getCourse().getId();
        String courseTitle = section.getCourse().getTitle();
        String sectionTitle = section.getTitle();
        Integer deletedOrderIndex = section.getOrderIndex();

        // Delete section (cascades to lessons)
        sectionRepository.delete(section);

        // Re-index remaining sections
        reindexSectionsAfterDeletion(courseId, deletedOrderIndex);

        // Log activity
        adminActivityLogService.logSectionDeleted(
                getCurrentUserId(),
                getCurrentUserName(),
                sectionId,
                sectionTitle,
                courseTitle);

        log.info("Successfully deleted section with ID: {} and re-indexed remaining sections", sectionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<SectionDTO> reorder(Long courseId, ReorderSectionsDTO dto) {
        log.debug("Reordering sections for course ID: {}", courseId);

        // Verify course exists
        if (!courseRepository.existsById(courseId)) {
            log.warn("Course not found with ID: {}", courseId);
            throw new CourseNotFoundException("Course not found with ID: " + courseId);
        }

        // Get existing sections
        List<Section> existingSections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        Set<Long> existingIds = existingSections.stream()
                .map(Section::getId)
                .collect(Collectors.toSet());

        // Validate provided section IDs
        Set<Long> providedIds = new HashSet<>(dto.getSectionIds());
        if (!existingIds.equals(providedIds)) {
            log.warn("Section IDs mismatch for course ID: {}. Expected: {}, Got: {}",
                    courseId, existingIds, providedIds);
            throw new IllegalArgumentException(
                    "Section IDs must match exactly with course sections. " +
                            "Expected IDs: " + existingIds + ", Provided: " + providedIds);
        }

        // Update order indexes
        List<Long> newOrder = dto.getSectionIds();
        for (int i = 0; i < newOrder.size(); i++) {
            Long sectionId = newOrder.get(i);
            Section section = existingSections.stream()
                    .filter(s -> s.getId().equals(sectionId))
                    .findFirst()
                    .orElseThrow();
            section.setOrderIndex(i);
            sectionRepository.save(section);
        }

        log.info("Successfully reordered {} sections for course ID: {}", newOrder.size(), courseId);

        // Return updated sections in new order with lessons eagerly fetched
        return sectionRepository.findByCourseIdWithLessons(courseId).stream()
                .map(SectionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Shifts sections down (increases orderIndex) starting from the given index.
     * Used when inserting a new section at a specific position.
     */
    private void shiftSectionsDown(Long courseId, Integer fromIndex) {
        List<Section> sectionsToShift = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .filter(s -> s.getOrderIndex() >= fromIndex)
                .collect(Collectors.toList());

        for (Section section : sectionsToShift) {
            section.setOrderIndex(section.getOrderIndex() + 1);
            sectionRepository.save(section);
        }

        log.debug("Shifted {} sections down from index {}", sectionsToShift.size(), fromIndex);
    }

    /**
     * Updates a section's order index, shifting other sections as needed.
     */
    private void updateSectionOrder(Section section, Integer newOrderIndex) {
        Long courseId = section.getCourse().getId();
        Integer oldOrderIndex = section.getOrderIndex();

        if (newOrderIndex.equals(oldOrderIndex)) {
            return;
        }

        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        if (newOrderIndex < oldOrderIndex) {
            // Moving up: shift sections in between down
            sections.stream()
                    .filter(s -> !s.getId().equals(section.getId()))
                    .filter(s -> s.getOrderIndex() >= newOrderIndex && s.getOrderIndex() < oldOrderIndex)
                    .forEach(s -> {
                        s.setOrderIndex(s.getOrderIndex() + 1);
                        sectionRepository.save(s);
                    });
        } else {
            // Moving down: shift sections in between up
            sections.stream()
                    .filter(s -> !s.getId().equals(section.getId()))
                    .filter(s -> s.getOrderIndex() > oldOrderIndex && s.getOrderIndex() <= newOrderIndex)
                    .forEach(s -> {
                        s.setOrderIndex(s.getOrderIndex() - 1);
                        sectionRepository.save(s);
                    });
        }

        section.setOrderIndex(newOrderIndex);
        log.debug("Updated section {} order from {} to {}", section.getId(), oldOrderIndex, newOrderIndex);
    }

    /**
     * Re-indexes sections after a deletion to maintain consecutive order indexes.
     */
    private void reindexSectionsAfterDeletion(Long courseId, Integer deletedIndex) {
        List<Section> sectionsToReindex = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .filter(s -> s.getOrderIndex() > deletedIndex)
                .collect(Collectors.toList());

        for (Section section : sectionsToReindex) {
            section.setOrderIndex(section.getOrderIndex() - 1);
            sectionRepository.save(section);
        }

        log.debug("Re-indexed {} sections after deletion", sectionsToReindex.size());
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
}
