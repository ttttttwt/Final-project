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
    private final com.lexia.backend.repository.CourseRepository courseRepository;
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
     */
    @Override
    @Transactional(readOnly = true)
    public com.lexia.backend.dto.CourseProgressDTO getCourseProgress(User user, Long courseId) {
        log.debug("Fetching course progress for user {} and course {}", user.getId(), courseId);

        // Verify course exists
        com.lexia.backend.entity.Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new com.lexia.backend.exception.ResourceNotFoundException("Course not found with id: " + courseId));

        // Get all lesson progress for this course
        List<LessonProgress> lessonProgressList = lessonProgressRepository.findByUserIdAndCourseId(user.getId(), courseId);

        // Calculate total lessons
        int totalLessons = course.getSections().stream()
                .mapToInt(section -> section.getLessons().size())
                .sum();

        // Map to DTO
        return com.lexia.backend.mapper.ProgressMapper.toCourseProgressDTO(course, lessonProgressList, totalLessons);
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public com.lexia.backend.dto.DashboardOverviewDTO getDashboardOverview(User user) {
        log.debug("Fetching dashboard overview for user {}", user.getId());

        // 1. Get Streak Data
        StreakDTO streak = getStreak(user);

        // 2. Get Enrollments & Stats
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        int enrolledCourses = enrollments.size();
        
        // Calculate completed lessons across all courses
        // We can use a count query for better performance
        long completedLessonsCount = lessonProgressRepository.countByUserIdAndCompletedAtBetween(
                user.getId(), 
                java.time.LocalDateTime.of(1970, 1, 1, 0, 0), // From beginning
                java.time.LocalDateTime.now()
        );

        // Estimate total lessons (can be refined by summing up actual course lessons)
        int totalLessons = enrolledCourses * 20; // Approximation if not easily available

        // Calculate average score
        List<LessonProgress> allProgress = lessonProgressRepository.findByUserId(user.getId());
        double averageScore = allProgress.stream()
                .filter(p -> p.getScore() != null)
                .mapToInt(LessonProgress::getScore)
                .average()
                .orElse(0.0);

        // Calculate total study time (mocked for now as we don't track time per session yet)
        int totalStudyMinutes = (int) completedLessonsCount * 15; 

        com.lexia.backend.dto.DashboardStatsDTO stats = com.lexia.backend.dto.DashboardStatsDTO.builder()
                .enrolledCourses(enrolledCourses)
                .completedLessons((int) completedLessonsCount)
                .totalLessons(totalLessons)
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .totalStudyMinutes(totalStudyMinutes)
                .averageScore(Math.round(averageScore * 10.0) / 10.0)
                .build();

        // 3. Get Recent Activity (Top 5)
        List<LessonProgress> recentProgress = lessonProgressRepository.findCompletedByUserId(user.getId());
        List<com.lexia.backend.dto.ActivityDTO> recentActivities = recentProgress.stream()
                .limit(5)
                .map(p -> com.lexia.backend.dto.ActivityDTO.builder()
                        .id(p.getId().toString())
                        .type("LESSON_COMPLETED")
                        .description("Completed lesson: " + p.getLesson().getTitle())
                        .timestamp(p.getCompletedAt())
                        .link("/courses/" + p.getLesson().getSection().getCourse().getId() + "/lessons/" + p.getLesson().getId())
                        .score(p.getScore() != null ? p.getScore() : 0)
                        .build())
                .collect(Collectors.toList());

        // 4. Generate Weekly Goals (Mock logic for now)
        // In a real app, these would be stored in a GoalRepository
        List<com.lexia.backend.dto.GoalDTO> weeklyGoals = new ArrayList<>();
        
        // Goal 1: Complete 5 lessons this week
        LocalDate startOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        long lessonsThisWeek = lessonProgressRepository.countByUserIdAndCompletedAtBetween(
                user.getId(),
                startOfWeek.atStartOfDay(),
                java.time.LocalDateTime.now()
        );
        
        weeklyGoals.add(com.lexia.backend.dto.GoalDTO.builder()
                .id("goal-weekly-lessons")
                .title("Weekly Warrior")
                .description("Complete 5 lessons this week")
                .currentProgress((int) lessonsThisWeek)
                .targetProgress(5)
                .unit("lessons")
                .isCompleted(lessonsThisWeek >= 5)
                .build());

        // Goal 2: Maintain streak
        weeklyGoals.add(com.lexia.backend.dto.GoalDTO.builder()
                .id("goal-streak")
                .title("Consistency is Key")
                .description("Reach a 3-day streak")
                .currentProgress(streak.getCurrentStreak())
                .targetProgress(3)
                .unit("days")
                .isCompleted(streak.getCurrentStreak() >= 3)
                .build());

        // 5. Generate Recommendations
        List<com.lexia.backend.dto.RecommendationDTO> recommendations = new ArrayList<>();
        
        if (streak.getCurrentStreak() < 3) {
             recommendations.add(com.lexia.backend.dto.RecommendationDTO.builder()
                    .id("rec-streak")
                    .title("Build your streak")
                    .description("Study for 10 minutes today to keep your streak alive!")
                    .type("TIP")
                    .reason("Streak is at risk")
                    .build());
        }

        if (averageScore < 70 && averageScore > 0) {
            recommendations.add(com.lexia.backend.dto.RecommendationDTO.builder()
                    .id("rec-review")
                    .title("Review recent lessons")
                    .description("Try retaking lessons with lower scores to improve mastery.")
                    .type("LESSON")
                    .reason("Average score below 70%")
                    .build());
        } else {
             recommendations.add(com.lexia.backend.dto.RecommendationDTO.builder()
                    .id("rec-challenge")
                    .title("Challenge yourself")
                    .description("You're doing great! Try a Speaking lesson next.")
                    .type("CHALLENGE")
                    .reason("High performance")
                    .build());
        }

        return com.lexia.backend.dto.DashboardOverviewDTO.builder()
                .stats(stats)
                .weeklyGoals(weeklyGoals)
                .recentActivities(recentActivities)
                .recommendations(recommendations)
                .build();
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public com.lexia.backend.dto.ProgressSummaryDTO getProgressSummary(User user, int days) {
        log.debug("Fetching progress summary for user {} over last {} days", user.getId(), days);

        // Calculate date range
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
        java.time.LocalDateTime startDate = endDate.minusDays(days);

        // Fetch completed lessons in range
        List<LessonProgress> completedLessons = lessonProgressRepository.findCompletedByUserIdBetween(
                user.getId(),
                startDate,
                endDate);

        // Group by date
        Map<String, List<LessonProgress>> lessonsByDate = completedLessons.stream()
                .collect(Collectors.groupingBy(
                        lp -> lp.getCompletedAt().toLocalDate().toString()));

        // Generate daily activities filling in missing days
        List<com.lexia.backend.dto.DailyActivityDTO> dailyActivities = new ArrayList<>();
        long totalLessonsCompleted = 0;
        long totalTimeSpentMinutes = 0;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.toString();
            List<LessonProgress> dailyProgress = lessonsByDate.getOrDefault(dateStr, Collections.emptyList());

            int dailyLessons = dailyProgress.size();
            // Estimate time spent: 15 mins per lesson (since we don't track actual time yet)
            int dailyTime = dailyLessons * 15;

            dailyActivities.add(com.lexia.backend.dto.DailyActivityDTO.builder()
                    .date(dateStr)
                    .lessonsCompleted(dailyLessons)
                    .timeSpentMinutes(dailyTime)
                    .build());

            totalLessonsCompleted += dailyLessons;
            totalTimeSpentMinutes += dailyTime;
        }

        // Calculate active days (days with at least one lesson)
        int activeDays = (int) dailyActivities.stream()
                .filter(d -> d.getLessonsCompleted() > 0)
                .count();

        long averageTimePerLesson = totalLessonsCompleted > 0
                ? totalTimeSpentMinutes / totalLessonsCompleted
                : 0;

        return com.lexia.backend.dto.ProgressSummaryDTO.builder()
                .totalLessonsCompleted(totalLessonsCompleted)
                .totalTimeSpentMinutes(totalTimeSpentMinutes)
                .averageTimePerLesson(averageTimePerLesson)
                .activeDays(activeDays)
                .dailyActivities(dailyActivities)
                .build();
    }
}
