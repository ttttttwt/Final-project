package com.lexia.backend.email.scheduler;

import com.lexia.backend.dto.ProgressSummaryDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scheduled task for sending weekly progress digest emails.
 * Runs every Sunday at 9:00 AM UTC and sends personalized
 * weekly learning summaries to users who have opted in.
 *
 * <p>
 * Can be disabled via configuration: lexia.email.weekly-digest.enabled=false
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@Component
@ConditionalOnProperty(name = "lexia.email.weekly-digest.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class WeeklyDigestScheduler {

    private static final int WEEKLY_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final ProgressService progressService;

    /**
     * Sends weekly progress digest emails to all eligible users.
     * Runs every Sunday at 9:00 AM UTC.
     */
    @Scheduled(cron = "0 0 9 * * SUN") // Every Sunday at 9:00 AM
    public void sendWeeklyDigests() {
        log.info("Starting weekly digest email job");

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate weekStart = today.minusDays(6); // Last 7 days including today

        String periodStart = weekStart.format(DATE_FORMATTER);
        String periodEnd = today.format(DATE_FORMATTER);

        int sentCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        try {
            // Get all active users
            List<User> activeUsers = userRepository.findAll().stream()
                    .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                    .toList();

            log.info("Processing {} active users for weekly digest", activeUsers.size());

            for (User user : activeUsers) {
                try {
                    if (shouldSendDigest(user)) {
                        sendDigestToUser(user, periodStart, periodEnd);
                        sentCount++;
                    } else {
                        skippedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error sending digest to user {}: {}", user.getId(), e.getMessage());
                    errorCount++;
                }
            }

            log.info("Weekly digest job completed: sent={}, skipped={}, errors={}",
                    sentCount, skippedCount, errorCount);

        } catch (Exception e) {
            log.error("Fatal error in weekly digest job: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if user should receive weekly digest.
     * Based on notification preferences and activity.
     */
    private boolean shouldSendDigest(User user) {
        // Check notification preferences
        Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(user.getId());
        if (prefsOpt.isEmpty()) {
            // Default to sending if no preferences set
            return true;
        }

        NotificationPreferences prefs = prefsOpt.get();

        // Check global email toggle
        if (Boolean.FALSE.equals(prefs.getEmailEnabled())) {
            return false;
        }

        // Check reminders/engagement category (weekly digest falls under this)
        return Boolean.TRUE.equals(prefs.getRemindersEnabled());
    }

    /**
     * Send weekly digest to a specific user.
     */
    private void sendDigestToUser(User user, String periodStart, String periodEnd) {
        // Get user's progress summary for the past week
        ProgressSummaryDTO progressSummary = progressService.getProgressSummary(user, WEEKLY_DAYS);
        StreakDTO streak = progressService.getStreak(user);

        // Build template data
        Map<String, Object> templateData = buildTemplateData(user, progressSummary, streak, periodStart, periodEnd);

        // Determine user locale
        String locale = getUserLocale(user);

        // Queue the email
        EmailRequest request = EmailRequest.builder()
                .recipientId(user.getId())
                .recipientEmail(user.getEmail())
                .recipientName(getUserDisplayName(user))
                .emailType(EmailType.WEEKLY_PROGRESS)
                .templateData(templateData)
                .locale(locale)
                .build();

        emailService.queueEmail(request);
        log.debug("Weekly digest queued for user: {}", user.getId());
    }

    /**
     * Build template data for the weekly digest email.
     */
    private Map<String, Object> buildTemplateData(User user, ProgressSummaryDTO progress,
            StreakDTO streak, String periodStart, String periodEnd) {
        Map<String, Object> data = new HashMap<>();

        // User info
        data.put("userName", getUserDisplayName(user));
        data.put("periodStart", periodStart);
        data.put("periodEnd", periodEnd);

        // Progress stats
        data.put("lessonsCompleted", progress.getTotalLessonsCompleted());
        data.put("totalMinutes", progress.getTotalTimeSpentMinutes());
        data.put("totalHours", formatHours(progress.getTotalTimeSpentMinutes()));
        data.put("activeDays", progress.getActiveDays());

        // Streak info
        if (streak != null) {
            data.put("currentStreak", streak.getCurrentStreak());
            data.put("longestStreak", streak.getLongestStreak());
            data.put("streakActive", streak.getCurrentStreak() > 0);
        } else {
            data.put("currentStreak", 0);
            data.put("longestStreak", 0);
            data.put("streakActive", false);
        }

        // Calculate improvement metrics
        data.put("hasActivity", progress.getTotalLessonsCompleted() > 0);
        data.put("averageMinutesPerDay", progress.getActiveDays() > 0
                ? progress.getTotalTimeSpentMinutes() / progress.getActiveDays()
                : 0);

        // Motivational message based on activity
        data.put("motivationalMessage", getMotivationalMessage(progress, streak));

        // Goals for next week
        data.put("suggestedGoal", getSuggestedGoal(progress));

        return data;
    }

    /**
     * Get motivational message based on user's activity.
     */
    private String getMotivationalMessage(ProgressSummaryDTO progress, StreakDTO streak) {
        if (progress.getTotalLessonsCompleted() == 0) {
            return "Every journey begins with a single step. Start your learning today!";
        } else if (streak != null && streak.getCurrentStreak() >= 7) {
            return "Amazing! You've maintained your streak for a whole week. Keep up the incredible work!";
        } else if (progress.getActiveDays() >= 5) {
            return "Outstanding consistency! You're building great learning habits.";
        } else if (progress.getTotalLessonsCompleted() >= 10) {
            return "Great progress! You've completed " + progress.getTotalLessonsCompleted() + " lessons this week.";
        } else {
            return "Keep going! Every lesson brings you closer to your goals.";
        }
    }

    /**
     * Get suggested goal for next week based on current activity.
     */
    private String getSuggestedGoal(ProgressSummaryDTO progress) {
        if (progress.getTotalLessonsCompleted() == 0) {
            return "Try to complete at least 3 lessons this week";
        } else if (progress.getActiveDays() < 3) {
            return "Aim for learning at least 4 days this week";
        } else if (progress.getTotalLessonsCompleted() < 5) {
            return "Challenge yourself to complete " + (progress.getTotalLessonsCompleted() + 2) + " lessons";
        } else {
            return "Maintain your momentum and try a new course section";
        }
    }

    /**
     * Format minutes as hours and minutes string.
     */
    private String formatHours(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + " hr";
        }
        return hours + " hr " + remainingMinutes + " min";
    }

    /**
     * Get user display name from profile or email.
     */
    private String getUserDisplayName(User user) {
        UserProfile profile = user.getProfile();
        if (profile != null) {
            if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
                return profile.getFullName();
            }
            if (profile.getFirstName() != null && !profile.getFirstName().isBlank()) {
                return profile.getFirstName();
            }
        }
        // Fallback to email prefix
        String email = user.getEmail();
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    /**
     * Get user locale from profile.
     */
    private String getUserLocale(User user) {
        if (user.getProfile() != null && user.getProfile().getLanguage() != null) {
            String lang = user.getProfile().getLanguage();
            if ("vi".equalsIgnoreCase(lang)) {
                return "vi";
            }
        }
        return "en";
    }
}
