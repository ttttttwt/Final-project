package com.lexia.backend.mapper;

import com.lexia.backend.dto.CreateLessonDTO;
import com.lexia.backend.dto.LessonDTO;
import com.lexia.backend.entity.Lesson;

/**
 * Mapper utility class for converting between Lesson entities and DTOs.
 * Provides centralized mapping logic for lesson data transformations.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class LessonMapper {

    private LessonMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a Lesson entity to LessonDTO.
     * Content is returned as-is (JSON string) without parsing.
     * Audio URL is included if an audio file is uploaded.
     *
     * @param lesson the lesson entity
     * @return LessonDTO representation, or null if lesson is null
     */
    public static LessonDTO toDTO(Lesson lesson) {
        if (lesson == null) {
            return null;
        }

        return LessonDTO.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection() != null ? lesson.getSection().getId() : null)
                .title(lesson.getTitle())
                .lessonType(lesson.getLessonType())
                .content(lesson.getContent())
                .orderIndex(lesson.getOrderIndex())
                .durationMinutes(lesson.getDurationMinutes())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .audioUrl(lesson.getEffectiveAudioUrl())
                .build();
    }

    /**
     * Creates a Lesson entity from CreateLessonDTO.
     * Content is stored as-is (JSON string).
     * Content validation should be done by LessonContentValidator before calling
     * this method.
     *
     * @param dto the create lesson data
     * @return a new Lesson entity with data from DTO
     * @throws IllegalArgumentException if dto is null
     */
    public static Lesson toEntity(CreateLessonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateLessonDTO cannot be null");
        }

        return Lesson.builder()
                .title(dto.getTitle())
                .lessonType(dto.getLessonType())
                .content(dto.getContent())
                .orderIndex(dto.getOrderIndex())
                .durationMinutes(dto.getDurationMinutes())
                .build();
    }

    /**
     * Updates a Lesson entity from CreateLessonDTO.
     * All fields from DTO are applied to the entity.
     * Content validation should be done by LessonContentValidator before calling
     * this method.
     *
     * @param lesson the lesson entity to update
     * @param dto    the lesson data to apply
     * @throws IllegalArgumentException if lesson or dto is null
     */
    public static void updateEntityFromDTO(Lesson lesson, CreateLessonDTO dto) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson entity cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("CreateLessonDTO cannot be null");
        }

        lesson.setTitle(dto.getTitle());
        lesson.setLessonType(dto.getLessonType());
        lesson.setContent(dto.getContent());
        lesson.setOrderIndex(dto.getOrderIndex());
        lesson.setDurationMinutes(dto.getDurationMinutes());
    }
}
