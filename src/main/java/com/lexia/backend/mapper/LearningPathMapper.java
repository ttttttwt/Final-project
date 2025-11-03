package com.lexia.backend.mapper;

import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.dto.UserPathProgressDTO;
import com.lexia.backend.entity.LearningPath;
import com.lexia.backend.entity.LearningPathCourse;
import com.lexia.backend.entity.UserLearningPath;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between LearningPath entities and DTOs.
 * Provides centralized mapping logic for learning path data transformations.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class LearningPathMapper {

    private LearningPathMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a LearningPath entity to LearningPathDTO.
     * Includes associated courses ordered by orderIndex.
     *
     * @param learningPath the learning path entity
     * @return LearningPathDTO representation, or null if learningPath is null
     */
    public static LearningPathDTO toDTO(LearningPath learningPath) {
        if (learningPath == null) {
            return null;
        }

        List<LearningPathDTO.LearningPathCourseDTO> courseDTOs = learningPath.getLearningPathCourses() != null
                ? learningPath.getLearningPathCourses().stream()
                        .map(LearningPathMapper::toCourseDTO)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        int totalCourses = courseDTOs.size();
        // Estimate 15 hours per course as default
        int estimatedHours = totalCourses * 15;

        return LearningPathDTO.builder()
                .id(learningPath.getId())
                .name(learningPath.getName())
                .description(learningPath.getDescription())
                .cefrLevel(learningPath.getCefrLevel())
                .isDefault(learningPath.getIsDefault())
                .courses(courseDTOs)
                .totalCourses(totalCourses)
                .estimatedHours(estimatedHours)
                .createdAt(learningPath.getCreatedAt())
                .updatedAt(learningPath.getUpdatedAt())
                .build();
    }

    /**
     * Converts a LearningPathCourse entity to LearningPathCourseDTO.
     *
     * @param learningPathCourse the learning path course association
     * @return LearningPathCourseDTO representation, or null if learningPathCourse
     *         is null
     */
    public static LearningPathDTO.LearningPathCourseDTO toCourseDTO(LearningPathCourse learningPathCourse) {
        if (learningPathCourse == null || learningPathCourse.getCourse() == null) {
            return null;
        }

        var course = learningPathCourse.getCourse();

        return LearningPathDTO.LearningPathCourseDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseThumbnailUrl(course.getThumbnailUrl())
                .courseCefrLevel(course.getCefrLevel())
                .orderIndex(learningPathCourse.getOrderIndex())
                .sectionCount(course.getSections() != null ? course.getSections().size() : 0)
                .build();
    }

    /**
     * Converts a UserLearningPath entity to UserPathProgressDTO.
     * Calculates progress percentage based on completed courses.
     *
     * @param userLearningPath the user learning path enrollment
     * @param coursesCompleted number of courses user has completed in this path
     * @return UserPathProgressDTO representation, or null if userLearningPath is
     *         null
     */
    public static UserPathProgressDTO toProgressDTO(UserLearningPath userLearningPath, int coursesCompleted) {
        if (userLearningPath == null) {
            return null;
        }

        var learningPath = userLearningPath.getLearningPath();
        var currentCourse = userLearningPath.getCurrentCourse();

        int totalCourses = learningPath.getLearningPathCourses() != null
                ? learningPath.getLearningPathCourses().size()
                : 0;

        int progressPercentage = totalCourses > 0
                ? (int) Math.round((coursesCompleted * 100.0) / totalCourses)
                : 0;

        return UserPathProgressDTO.builder()
                .enrollmentId(userLearningPath.getId())
                .pathId(learningPath.getId())
                .pathName(learningPath.getName())
                .pathCefrLevel(learningPath.getCefrLevel())
                .currentCourseId(currentCourse != null ? currentCourse.getId() : null)
                .currentCourseTitle(currentCourse != null ? currentCourse.getTitle() : null)
                .coursesCompleted(coursesCompleted)
                .totalCourses(totalCourses)
                .progressPercentage(progressPercentage)
                .startedAt(userLearningPath.getStartedAt())
                .completedAt(userLearningPath.getCompletedAt())
                .isCompleted(userLearningPath.isCompleted())
                .build();
    }

    /**
     * Converts a list of LearningPath entities to a list of LearningPathDTOs.
     *
     * @param learningPaths list of learning path entities
     * @return list of LearningPathDTOs, or empty list if input is null or empty
     */
    public static List<LearningPathDTO> toDTOList(List<LearningPath> learningPaths) {
        if (learningPaths == null || learningPaths.isEmpty()) {
            return Collections.emptyList();
        }

        return learningPaths.stream()
                .map(LearningPathMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of UserLearningPath entities to a list of
     * UserPathProgressDTOs.
     *
     * @param userLearningPaths   list of user learning path enrollments
     * @param coursesCompletedMap map of pathId to courses completed count
     * @return list of UserPathProgressDTOs, or empty list if input is null or empty
     */
    public static List<UserPathProgressDTO> toProgressDTOList(
            List<UserLearningPath> userLearningPaths,
            java.util.Map<Long, Integer> coursesCompletedMap) {
        if (userLearningPaths == null || userLearningPaths.isEmpty()) {
            return Collections.emptyList();
        }

        return userLearningPaths.stream()
                .map(ulp -> {
                    Long pathId = ulp.getLearningPath().getId();
                    int coursesCompleted = coursesCompletedMap != null
                            ? coursesCompletedMap.getOrDefault(pathId, 0)
                            : 0;
                    return toProgressDTO(ulp, coursesCompleted);
                })
                .collect(Collectors.toList());
    }
}
