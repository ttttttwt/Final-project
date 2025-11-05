package com.lexia.backend.mapper;

import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.LessonProgress;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting progress data to DTOs.
 * Provides static methods for progress-related conversions.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public class ProgressMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ProgressMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Convert course and lesson progress data to CourseProgressDTO.
     * 
     * @param course             the course entity
     * @param lessonProgressList list of lesson progress records for the course
     * @param totalLessons       total number of lessons in the course
     * @return CourseProgressDTO with progress details
     */
    public static CourseProgressDTO toCourseProgressDTO(
            Course course,
            List<LessonProgress> lessonProgressList,
            int totalLessons) {
        if (course == null) {
            return null;
        }

        // Create a map of lessonId -> LessonProgress for quick lookup
        Map<Long, LessonProgress> progressMap = lessonProgressList.stream()
                .collect(Collectors.toMap(
                        lp -> lp.getLesson().getId(),
                        lp -> lp,
                        (existing, replacement) -> existing));

        // Count completed lessons
        long completedCount = lessonProgressList.stream()
                .filter(LessonProgress::isCompleted)
                .count();

        // Calculate progress percentage
        int progressPercentage = totalLessons > 0
                ? (int) Math.round((completedCount * 100.0) / totalLessons)
                : 0;

        // Build lesson progress summaries
        List<CourseProgressDTO.LessonProgressSummary> lessonProgressSummaries = buildLessonProgressSummaries(course,
                progressMap);

        return CourseProgressDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .cefrLevel(course.getCefrLevel())
                .totalLessons(totalLessons)
                .completedLessons((int) completedCount)
                .progressPercentage(progressPercentage)
                .lessonProgress(lessonProgressSummaries)
                .build();
    }

    /**
     * Build lesson progress summaries for all lessons in a course.
     * 
     * @param course      the course entity
     * @param progressMap map of lessonId to LessonProgress
     * @return list of lesson progress summaries
     */
    private static List<CourseProgressDTO.LessonProgressSummary> buildLessonProgressSummaries(
            Course course,
            Map<Long, LessonProgress> progressMap) {
        return course.getSections().stream()
                .flatMap(section -> section.getLessons().stream()
                        .map(lesson -> {
                            LessonProgress progress = progressMap.get(lesson.getId());
                            return buildLessonProgressSummary(lesson, progress);
                        }))
                .collect(Collectors.toList());
    }

    /**
     * Build a single lesson progress summary.
     * 
     * @param lesson   the lesson entity
     * @param progress the progress record (can be null)
     * @return lesson progress summary
     */
    private static CourseProgressDTO.LessonProgressSummary buildLessonProgressSummary(
            Lesson lesson,
            LessonProgress progress) {
        return CourseProgressDTO.LessonProgressSummary.builder()
                .lessonId(lesson.getId())
                .lessonTitle(lesson.getTitle())
                .lessonType(lesson.getLessonType().name())
                .sectionTitle(lesson.getSection().getTitle())
                .status(progress != null ? progress.getStatus().name() : "NOT_STARTED")
                .score(progress != null ? progress.getScore() : null)
                .attempts(progress != null ? progress.getAttempts() : 0)
                .build();
    }
}
