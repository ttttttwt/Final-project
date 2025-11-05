package com.lexia.backend.mapper;

import com.lexia.backend.dto.EnrollmentDTO;
import com.lexia.backend.entity.Enrollment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between {@link Enrollment} entities and
 * {@link EnrollmentDTO}.
 * Provides static methods for entity-to-DTO conversions.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class EnrollmentMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private EnrollmentMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Convert Enrollment entity to EnrollmentDTO.
     * 
     * @param enrollment the enrollment entity
     * @return EnrollmentDTO with mapped fields, or null if input is null
     */
    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .thumbnailUrl(enrollment.getCourse().getThumbnailUrl())
                .cefrLevel(enrollment.getCourse().getCefrLevel())
                .enrolledAt(enrollment.getEnrolledAt())
                .progressPercentage(enrollment.getProgressPercentage())
                .completedAt(enrollment.getCompletedAt())
                .isCompleted(enrollment.isCompleted())
                .build();
    }

    /**
     * Convert list of Enrollment entities to list of EnrollmentDTOs.
     * 
     * @param enrollments list of enrollment entities
     * @return list of EnrollmentDTOs, empty list if input is null or empty
     */
    public static List<EnrollmentDTO> toDTOList(List<Enrollment> enrollments) {
        if (enrollments == null) {
            return List.of();
        }

        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
