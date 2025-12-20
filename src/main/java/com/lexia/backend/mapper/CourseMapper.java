package com.lexia.backend.mapper;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.entity.Course;

/**
 * Mapper utility class for converting between Course entities and DTOs.
 * Provides centralized mapping logic for course data transformations.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class CourseMapper {

    private CourseMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a Course entity to CourseDTO.
     * Includes derived fields like sectionCount.
     * Uses effective thumbnail URL (file URL takes precedence over external URL).
     *
     * @param course the course entity
     * @return CourseDTO representation, or null if course is null
     */
    public static CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }

        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getEffectiveThumbnailUrl())
                .cefrLevel(course.getCefrLevel())
                .isPublished(course.getIsPublished())
                .sectionCount(course.getSections() != null ? course.getSections().size() : 0)
                .enrollmentCount(course.getEnrollmentCount() != null ? course.getEnrollmentCount() : 0)
                .completionCount(course.getCompletionCount() != null ? course.getCompletionCount() : 0)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    /**
     * Creates a Course entity from CreateCourseDTO.
     * Sets default values for isPublished (false).
     *
     * @param dto the create course data
     * @return a new Course entity with data from DTO
     * @throws IllegalArgumentException if dto is null
     */
    public static Course toEntity(CreateCourseDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateCourseDTO cannot be null");
        }

        return Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .cefrLevel(dto.getCefrLevel())
                .isPublished(false) // Default: unpublished
                .build();
    }

    /**
     * Updates a Course entity from UpdateCourseDTO.
     * Only updates fields that are present in the DTO (non-null values).
     * This allows partial updates without overwriting unchanged fields.
     *
     * @param course the course entity to update
     * @param dto    the update course data
     * @throws IllegalArgumentException if course or dto is null
     */
    public static void updateEntityFromDTO(Course course, UpdateCourseDTO dto) {
        if (course == null) {
            throw new IllegalArgumentException("Course entity cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateCourseDTO cannot be null");
        }

        // Only update non-null fields (partial update support)
        if (dto.getTitle() != null) {
            course.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
        if (dto.getThumbnailUrl() != null) {
            course.setThumbnailUrl(dto.getThumbnailUrl());
        }
        if (dto.getCefrLevel() != null) {
            course.setCefrLevel(dto.getCefrLevel());
        }
        if (dto.getIsPublished() != null) {
            course.setIsPublished(dto.getIsPublished());
        }
    }
}
