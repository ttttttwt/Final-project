package com.lexia.backend.email.event;

import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.notification.entity.Notification.NotificationCategory;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.event.*;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for notification events that triggers email delivery.
 * Handles all notification events and queues appropriate emails
 * based on user preferences.
 *
 * <p>
 * Events handled:
 * </p>
 * <ul>
 * <li>CourseCompletedEvent - Course completion email with stats</li>
 * <li>EnrollmentConfirmedEvent - Enrollment confirmation email</li>
 * <li>StreakMilestoneEvent - Streak milestone celebration email</li>
 * <li>StreakLostEvent - Streak lost notification email</li>
 * <li>LevelUpEvent - CEFR level advancement email</li>
 * </ul>
 *
 * <p>
 * Note: Emails respect user notification preferences. Users can opt-out
 * of specific email categories through their notification settings.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotificationPreferencesRepository preferencesRepository;

    /**
     * Handle course completed event.
     * Sends a congratulatory email with course completion statistics.
     *
     * @param event the course completed event
     */
    @EventListener
    @Async
    public void handleCourseCompleted(CourseCompletedEvent event) {
        log.info("Email: Handling CourseCompletedEvent for user: {}, course: {}",
                event.getUserId(), event.getCourseTitle());

        if (!shouldSendEmail(event.getUserId(), NotificationCategory.ACHIEVEMENT, false)) {
            log.debug("Email skipped for CourseCompletedEvent - user preferences");
            return;
        }

        Optional<User> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for CourseCompletedEvent: {}", event.getUserId());
            return;
        }

        User user = userOpt.get();
        Map<String, Object> templateData = new HashMap<>(event.getData());
        templateData.put("userName", getUserDisplayName(user));
        templateData.put("courseTitle", event.getCourseTitle());
        templateData.put("completionTime", event.getCompletionTimeMinutes());

        queueEmail(user, EmailType.COURSE_COMPLETED, templateData);
    }

    /**
     * Handle enrollment confirmed event.
     * Sends a welcome email for the new course enrollment.
     *
     * @param event the enrollment confirmed event
     */
    @EventListener
    @Async
    public void handleEnrollmentConfirmed(EnrollmentConfirmedEvent event) {
        log.info("Email: Handling EnrollmentConfirmedEvent for user: {}, course: {}",
                event.getUserId(), event.getCourseTitle());

        if (!shouldSendEmail(event.getUserId(), NotificationCategory.LEARNING, false)) {
            log.debug("Email skipped for EnrollmentConfirmedEvent - user preferences");
            return;
        }

        Optional<User> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for EnrollmentConfirmedEvent: {}", event.getUserId());
            return;
        }

        User user = userOpt.get();
        Map<String, Object> templateData = new HashMap<>(event.getData());
        templateData.put("userName", getUserDisplayName(user));
        templateData.put("courseTitle", event.getCourseTitle());
        templateData.put("cefrLevel", event.getCefrLevel());
        templateData.put("thumbnailUrl", event.getThumbnailUrl());

        queueEmail(user, EmailType.ENROLLMENT_CONFIRMATION, templateData);
    }

    /**
     * Handle streak milestone event.
     * Sends a celebration email for reaching streak milestones.
     *
     * @param event the streak milestone event
     */
    @EventListener
    @Async
    public void handleStreakMilestone(StreakMilestoneEvent event) {
        log.info("Email: Handling StreakMilestoneEvent for user: {}, days: {}",
                event.getUserId(), event.getStreakDays());

        if (!shouldSendEmail(event.getUserId(), NotificationCategory.ACHIEVEMENT, false)) {
            log.debug("Email skipped for StreakMilestoneEvent - user preferences");
            return;
        }

        Optional<User> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for StreakMilestoneEvent: {}", event.getUserId());
            return;
        }

        User user = userOpt.get();
        Map<String, Object> templateData = new HashMap<>(event.getData());
        templateData.put("userName", getUserDisplayName(user));
        templateData.put("streakDays", event.getStreakDays());
        templateData.put("milestone", event.getMilestone());
        templateData.put("nextMilestone", calculateNextMilestone(event.getStreakDays()));

        queueEmail(user, EmailType.STREAK_MILESTONE, templateData);
    }

    /**
     * Handle streak lost event.
     * Sends a supportive email encouraging the user to restart their streak.
     *
     * @param event the streak lost event
     */
    @EventListener
    @Async
    public void handleStreakLost(StreakLostEvent event) {
        log.info("Email: Handling StreakLostEvent for user: {}, previous: {} days",
                event.getUserId(), event.getPreviousStreak());

        if (!shouldSendEmail(event.getUserId(), NotificationCategory.ENGAGEMENT, false)) {
            log.debug("Email skipped for StreakLostEvent - user preferences");
            return;
        }

        Optional<User> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for StreakLostEvent: {}", event.getUserId());
            return;
        }

        User user = userOpt.get();
        Map<String, Object> templateData = new HashMap<>(event.getData());
        templateData.put("userName", getUserDisplayName(user));
        templateData.put("previousStreak", event.getPreviousStreak());

        queueEmail(user, EmailType.STREAK_LOST, templateData);
    }

    /**
     * Handle level up event.
     * Sends a congratulatory email for advancing CEFR level.
     *
     * @param event the level up event
     */
    @EventListener
    @Async
    public void handleLevelUp(LevelUpEvent event) {
        log.info("Email: Handling LevelUpEvent for user: {}, {} -> {}",
                event.getUserId(), event.getPreviousLevel(), event.getNewLevel());

        if (!shouldSendEmail(event.getUserId(), NotificationCategory.ACHIEVEMENT, false)) {
            log.debug("Email skipped for LevelUpEvent - user preferences");
            return;
        }

        Optional<User> userOpt = userRepository.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for LevelUpEvent: {}", event.getUserId());
            return;
        }

        User user = userOpt.get();
        Map<String, Object> templateData = new HashMap<>(event.getData());
        templateData.put("userName", getUserDisplayName(user));
        templateData.put("previousLevel", event.getPreviousLevel());
        templateData.put("newLevel", event.getNewLevel());
        templateData.put("levelDescription", getLevelDescription(event.getNewLevel()));

        queueEmail(user, EmailType.LEVEL_UP, templateData);
    }

    /**
     * Checks if email should be sent based on user preferences.
     * Considers both global email toggle and category-specific preferences.
     *
     * @param userId    the user ID
     * @param category  the notification category
     * @param mandatory whether this is a mandatory email
     * @return true if email should be sent
     */
    private boolean shouldSendEmail(UUID userId, NotificationCategory category, boolean mandatory) {
        // Mandatory emails always get sent (e.g., password reset, email verification)
        if (mandatory) {
            return true;
        }

        Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
        if (prefsOpt.isEmpty()) {
            // No preferences set - default to sending emails
            return true;
        }

        NotificationPreferences prefs = prefsOpt.get();

        // Check global email toggle
        if (Boolean.FALSE.equals(prefs.getEmailEnabled())) {
            return false;
        }

        // Check category-specific preference
        return prefs.isCategoryEnabled(category);
    }

    /**
     * Queue an email for delivery.
     *
     * @param user         the recipient user
     * @param emailType    the type of email
     * @param templateData the template variables
     */
    private void queueEmail(User user, EmailType emailType, Map<String, Object> templateData) {
        try {
            // Determine user locale (default to English)
            String locale = getUserLocale(user);

            EmailRequest request = EmailRequest.builder()
                    .recipientId(user.getId())
                    .recipientEmail(user.getEmail())
                    .recipientName(getUserDisplayName(user))
                    .emailType(emailType)
                    .templateData(templateData)
                    .locale(locale)
                    .build();

            emailService.queueEmail(request);
            log.debug("Email queued: {} for user: {}", emailType, user.getId());
        } catch (Exception e) {
            log.error("Failed to queue email: {} for user: {}", emailType, user.getId(), e);
        }
    }

    /**
     * Gets user display name from profile, fallback to email prefix.
     *
     * @param user the user
     * @return the display name
     */
    private String getUserDisplayName(User user) {
        UserProfile profile = user.getProfile();
        if (profile != null) {
            // Try full name first
            if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
                return profile.getFullName();
            }
            // Try first name + last name
            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();
            if (firstName != null && !firstName.isBlank()) {
                if (lastName != null && !lastName.isBlank()) {
                    return firstName + " " + lastName;
                }
                return firstName;
            }
        }
        // Fallback to email prefix
        String email = user.getEmail();
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    /**
     * Gets user locale from profile or defaults to English.
     *
     * @param user the user
     * @return the user's preferred locale code
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

    /**
     * Calculate the next streak milestone.
     *
     * @param currentStreak current streak days
     * @return next milestone days
     */
    private int calculateNextMilestone(int currentStreak) {
        int[] milestones = { 7, 14, 30, 60, 90, 180, 365 };
        for (int milestone : milestones) {
            if (milestone > currentStreak) {
                return milestone;
            }
        }
        return currentStreak + 365; // Next yearly milestone
    }

    /**
     * Get CEFR level description.
     *
     * @param level the CEFR level
     * @return description of the level
     */
    private String getLevelDescription(String level) {
        return switch (level.toUpperCase()) {
            case "A1" -> "Basic - Can understand and use familiar everyday expressions";
            case "A2" -> "Elementary - Can communicate in simple and routine tasks";
            case "B1" -> "Intermediate - Can deal with most situations while travelling";
            case "B2" -> "Upper Intermediate - Can interact with fluency and spontaneity";
            case "C1" -> "Advanced - Can use language flexibly and effectively";
            case "C2" -> "Proficiency - Can understand virtually everything heard or read";
            default -> "English Proficiency Level";
        };
    }
}
