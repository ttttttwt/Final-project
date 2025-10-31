package com.lexia.backend.service.impl;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CourseSearchDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.DuplicateCourseException;
import com.lexia.backend.mapper.CourseMapper;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.service.CourseService;
import com.lexia.backend.specification.CourseSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CourseService for managing courses.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CourseDTO create(CreateCourseDTO dto) {
        log.debug("Creating new course with title: {}", dto.getTitle());

        // Check for duplicate title
        if (courseRepository.existsByTitle(dto.getTitle())) {
            log.warn("Course with title '{}' already exists", dto.getTitle());
            throw new DuplicateCourseException("Course with title '" + dto.getTitle() + "' already exists");
        }

        // Map DTO to entity
        Course course = CourseMapper.toEntity(dto);

        // Save course
        Course savedCourse = courseRepository.save(course);
        log.info("Successfully created course with ID: {} and title: {}", savedCourse.getId(), savedCourse.getTitle());

        return CourseMapper.toDTO(savedCourse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CourseDTO update(Long id, UpdateCourseDTO dto) {
        log.debug("Updating course with ID: {}", id);

        // Fetch existing course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // Check for duplicate title if title is being updated
        if (dto.getTitle() != null && !dto.getTitle().equals(course.getTitle())) {
            if (courseRepository.existsByTitleAndIdNot(dto.getTitle(), id)) {
                log.warn("Course with title '{}' already exists", dto.getTitle());
                throw new DuplicateCourseException("Course with title '" + dto.getTitle() + "' already exists");
            }
        }

        // Update entity fields
        CourseMapper.updateEntityFromDTO(course, dto);

        // Save updated course
        Course updatedCourse = courseRepository.save(course);
        log.info("Successfully updated course with ID: {}", id);

        return CourseMapper.toDTO(updatedCourse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CourseDTO getById(Long id) {
        log.debug("Fetching course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        log.debug("Successfully retrieved course with ID: {}", id);
        return CourseMapper.toDTO(course);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CourseDTO getByIdWithSections(Long id) {
        log.debug("Fetching course with ID: {} including sections", id);

        Course course = courseRepository.findByIdWithSections(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        log.debug("Successfully retrieved course with ID: {} with {} sections", id, course.getSections().size());
        return CourseMapper.toDTO(course);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting course with ID: {}", id);

        // Fetch course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // Check if published
        if (course.getIsPublished()) {
            log.warn("Attempted to delete published course with ID: {}", id);
            throw new IllegalStateException("Cannot delete published course. Unpublish first.");
        }

        // Delete course
        courseRepository.delete(course);
        log.info("Successfully deleted course with ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CourseDTO publish(Long id) {
        log.debug("Publishing course with ID: {}", id);

        // Fetch course with sections
        Course course = courseRepository.findByIdWithSections(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // Validate course has content
        if (course.getSections() == null || course.getSections().isEmpty()) {
            log.warn("Attempted to publish course with ID: {} without sections", id);
            throw new IllegalStateException("Cannot publish course without sections");
        }

        // Check if at least one section has lessons
        boolean hasLessons = course.getSections().stream()
                .anyMatch(section -> section.getLessons() != null && !section.getLessons().isEmpty());

        if (!hasLessons) {
            log.warn("Attempted to publish course with ID: {} without lessons", id);
            throw new IllegalStateException("Cannot publish course without lessons");
        }

        // Publish course
        course.setIsPublished(true);
        Course publishedCourse = courseRepository.save(course);
        log.info("Successfully published course with ID: {}", id);

        return CourseMapper.toDTO(publishedCourse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CourseDTO unpublish(Long id) {
        log.debug("Unpublishing course with ID: {}", id);

        // Fetch course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // Unpublish course
        course.setIsPublished(false);
        Course unpublishedCourse = courseRepository.save(course);
        log.info("Successfully unpublished course with ID: {}", id);

        return CourseMapper.toDTO(unpublishedCourse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> search(CourseSearchDTO searchDTO) {
        log.debug("Searching courses with criteria: {}", searchDTO);

        // Build specification from search criteria
        Specification<Course> spec = CourseSpecifications.advancedSearch(
                searchDTO.getTitle(),
                searchDTO.getCefrLevel(),
                searchDTO.getIsPublished(),
                searchDTO.getCreatedAfter(),
                searchDTO.getCreatedBefore());

        // Build pageable from search DTO
        Pageable pageable = buildPageable(searchDTO);

        // Execute search with pagination
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        log.debug("Found {} courses matching search criteria", coursePage.getTotalElements());
        return coursePage.map(CourseMapper::toDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllPublished(Pageable pageable) {
        log.debug("Fetching all published courses");

        // Use specification to filter published courses
        Specification<Course> spec = CourseSpecifications.isPublished(true);
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        log.debug("Found {} published courses", coursePage.getTotalElements());

        return coursePage.map(CourseMapper::toDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getAll(Pageable pageable) {
        log.debug("Fetching all courses");

        Page<Course> coursePage = courseRepository.findAll(pageable);
        log.debug("Found {} total courses", coursePage.getTotalElements());

        return coursePage.map(CourseMapper::toDTO);
    }

    /**
     * Helper method to build Pageable from CourseSearchDTO.
     * 
     * @param searchDTO the search DTO containing pagination parameters
     * @return Pageable object for repository queries
     */
    private Pageable buildPageable(CourseSearchDTO searchDTO) {
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? Math.min(searchDTO.getSize(), 100) : 10;

        // Parse sort parameter if present
        if (searchDTO.getSort() != null && !searchDTO.getSort().isEmpty()) {
            String[] sortParts = searchDTO.getSort().split(",");
            String field = sortParts[0];
            org.springframework.data.domain.Sort.Direction direction = sortParts.length > 1
                    && "desc".equalsIgnoreCase(sortParts[1])
                            ? org.springframework.data.domain.Sort.Direction.DESC
                            : org.springframework.data.domain.Sort.Direction.ASC;

            return org.springframework.data.domain.PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by(direction, field));
        }

        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
