package com.lexia.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.LessonProgressDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.EnrollmentNotFoundException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.repository.EnrollmentRepository;
import com.lexia.backend.repository.LessonProgressRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.service.EnrollmentService;
import com.lexia.backend.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ProgressService}.
 * Manages lesson completion tracking and streak calculation.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Uses @Transactional to ensure atomic updates across lesson_progress and
     * enrollments tables.
     * </p>
     * <p>
     * Parses JSON using Jackson ObjectMapper to validate format before storing in
     * JSONB column.
     * </p>
     */
    @Override
    @Transactional
    public LessonProgressDTO completeLesson(User user, Long lessonId, String resultDetailsJson) {
        log.info("Marking lesson {} as complete for user {}", lessonId, user.getId());

        // Verify lesson exists and get course ID
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with id: " + lessonId));

        Long courseId = lesson.getSection().getCourse().getId();

        // Verify user is enrolled in the course
        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new EnrollmentNotFoundException(
                    "You must be enrolled in the course before completing lessons");
        }

        // Parse result details JSON
        Map<String, Object> resultDetails = parseResultDetails(resultDetailsJson);

        // Find or create lesson progress
        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    log.debug("Creating new progress record for user {} and lesson {}", user.getId(), lessonId);
                    return LessonProgress.builder()
                            .userId(user.getId())
                            .lesson(lesson)
                            .status(LessonProgress.Status.NOT_STARTED)
                            .attempts(0)
                            .build();
                });

        // Extract score from result details if available
        Integer score = null;
        if (resultDetails != null && resultDetails.containsKey("score")) {
            Object scoreObj = resultDetails.get("score");
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).intValue();
            }
        }

        // Update progress (markCompleted handles score, resultDetails, and attempts)
        progress.markCompleted(score, resultDetailsJson);

        progress = lessonProgressRepository.save(progress);
        log.info("Lesson {} completed for user {} (attempt #{}, score: {})",
                lessonId, user.getId(), progress.getAttempts(), progress.getScore());

        // Trigger enrollment progress recalculation
        enrollmentService.updateEnrollmentProgress(user.getId(), courseId);

        // Build and return DTO
        return buildLessonProgressDTO(progress);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Streak calculation algorithm:
     * <ol>
     * <li>Get all distinct completion dates (date only, no time)</li>
     * <li>Sort dates in descending order (newest first)</li>
     * <li>Check if today or yesterday has activity (current streak)</li>
     * <li>Count consecutive days backwards from today</li>
     * <li>Find longest consecutive sequence in all history</li>
     * </ol>
     * 
     * <p>
     * Uses system default timezone for "today" calculation.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public StreakDTO getStreak(User user) {
        log.debug("Calculating streak for user {}", user.getId());

        // Get all completed lessons
        List<LessonProgress> completedLessons = lessonProgressRepository
                .findByUserIdAndStatus(user.getId(), LessonProgress.Status.COMPLETED);

        if (completedLessons.isEmpty()) {
            log.info("User {} has no completed lessons", user.getId());
            return StreakDTO.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .lastActivityDate(null)
                    .isActiveToday(false)
                    .totalActiveDays(0)
                    .build();
        }

        // Extract unique activity dates (date only, no time)
        Set<LocalDate> activityDates = completedLessons.stream()
                .map(progress -> progress.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());

        // Sort dates descending (newest first)
        List<LocalDate> sortedDates = activityDates.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate lastActivityDate = sortedDates.get(0);

        // Check if active today
        boolean isActiveToday = lastActivityDate.equals(today);

        // Calculate current streak
        int currentStreak = calculateCurrentStreak(sortedDates, today);

        // Calculate longest streak
        int longestStreak = calculateLongestStreak(sortedDates);

        log.info("User {} streak: current={}, longest={}, activeDays={}",
                user.getId(), currentStreak, longestStreak, activityDates.size());

        return StreakDTO.builder()
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .lastActivityDate(lastActivityDate)
                .isActiveToday(isActiveToday)
                .totalActiveDays(activityDates.size())
                .build();
    }

    /**
     * Parses JSON string into Map for JSONB storage.
     * 
     * @param json JSON string to parse
     * @return parsed map, or null if json is null/empty
     * @throws IllegalArgumentException if JSON is invalid
     */
    private Map<String, Object> parseResultDetails(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json, Map.class);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON format for result details: {}", json, e);
            throw new IllegalArgumentException("Invalid JSON format for result details: " + e.getMessage());
        }
    }

    /**
     * Builds LessonProgressDTO from entity.
     */
    private LessonProgressDTO buildLessonProgressDTO(LessonProgress progress) {
        // Parse resultDetails JSON back to Map
        Map<String, Object> resultDetailsMap = null;
        if (progress.getResultDetails() != null) {
            resultDetailsMap = parseResultDetails(progress.getResultDetails());
        }

        return LessonProgressDTO.builder()
                .id(progress.getId().toString())
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .lessonType("GENERAL") // Lesson entity doesn't have type field yet
                .status(progress.getStatus().name())
                .score(progress.getScore())
                .attempts(progress.getAttempts())
                .startedAt(progress.getCreatedAt()) // Use createdAt as startedAt
                .completedAt(progress.getCompletedAt())
                .resultDetails(resultDetailsMap)
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    /**
     * Calculates current streak from today backwards.
     * Streak is valid if activity was today OR yesterday (allows missing 1 day).
     */
    private int calculateCurrentStreak(List<LocalDate> sortedDates, LocalDate today) {
        if (sortedDates.isEmpty()) {
            return 0;
        }

        LocalDate lastActivity = sortedDates.get(0);

        // If last activity was more than 1 day ago, streak is broken
        if (lastActivity.isBefore(today.minusDays(1))) {
            return 0;
        }

        // Count consecutive days backwards from today/yesterday
        int streak = 0;
        LocalDate expectedDate = lastActivity;

        for (LocalDate date : sortedDates) {
            if (date.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (date.isBefore(expectedDate)) {
                // Gap found, break streak
                break;
            }
        }

        return streak;
    }

    /**
     * Finds the longest consecutive streak in all history.
     */
    private int calculateLongestStreak(List<LocalDate> sortedDates) {
        if (sortedDates.isEmpty()) {
            return 0;
        }

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate current = sortedDates.get(i);
            LocalDate previous = sortedDates.get(i - 1);

            // Check if dates are consecutive (previous - 1 day = current, since sorted
            // descending)
            if (previous.minusDays(1).equals(current)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }
}
