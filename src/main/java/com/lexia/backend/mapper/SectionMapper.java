package com.lexia.backend.mapper;

import com.lexia.backend.dto.CreateSectionDTO;
import com.lexia.backend.dto.SectionDTO;
import com.lexia.backend.dto.UpdateSectionDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Section;

/**
 * Mapper utility class for converting between Section entities and DTOs.
 * Provides centralized mapping logic for section data transformations.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class SectionMapper {

    private SectionMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a Section entity to SectionDTO.
     * Includes derived fields like lessonCount and courseId.
     *
     * @param section the section entity
     * @return SectionDTO representation, or null if section is null
     */
    public static SectionDTO toDTO(Section section) {
        if (section == null) {
            return null;
        }

        return SectionDTO.builder()
                .id(section.getId())
                .courseId(section.getCourse() != null ? section.getCourse().getId() : null)
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .lessonCount(section.getLessons() != null ? section.getLessons().size() : 0)
                .createdAt(section.getCreatedAt())
                .build();
    }

    /**
     * Creates a Section entity from CreateSectionDTO.
     * Note: Course must be set separately after calling this method.
     *
     * @param dto        the create section data
     * @param course     the parent course
     * @param orderIndex the order index to use (may differ from DTO if
     *                   auto-calculated)
     * @return a new Section entity with data from DTO
     * @throws IllegalArgumentException if dto or course is null
     */
    public static Section toEntity(CreateSectionDTO dto, Course course, Integer orderIndex) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateSectionDTO cannot be null");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        return Section.builder()
                .course(course)
                .title(dto.getTitle())
                .orderIndex(orderIndex)
                .build();
    }

    /**
     * Updates a Section entity from UpdateSectionDTO.
     * Only updates fields that are present in the DTO (non-null values).
     * This allows partial updates without overwriting unchanged fields.
     *
     * @param section the section entity to update
     * @param dto     the update section data
     * @throws IllegalArgumentException if section or dto is null
     */
    public static void updateEntityFromDTO(Section section, UpdateSectionDTO dto) {
        if (section == null) {
            throw new IllegalArgumentException("Section entity cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateSectionDTO cannot be null");
        }

        // Only update non-null fields (partial update support)
        if (dto.getTitle() != null) {
            section.setTitle(dto.getTitle());
        }
        // Note: orderIndex updates are handled separately via reorder logic
    }
}
