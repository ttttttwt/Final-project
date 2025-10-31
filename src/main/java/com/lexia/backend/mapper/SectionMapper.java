package com.lexia.backend.mapper;

import com.lexia.backend.dto.SectionDTO;
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
}
