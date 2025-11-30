package com.lexia.backend.notification.event;

import com.lexia.backend.notification.dto.CreateNotificationRequest;
import com.lexia.backend.notification.dto.NotificationDTO;
import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import com.lexia.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for notification events.
 * Handles all notification events and creates appropriate notifications.
 *
 * <p>
 * Events handled:
 * </p>
 * <ul>
 * <li>CourseCompletedEvent - When user completes a course</li>
 * <li>LessonCompletedEvent - When user completes a lesson</li>
 * <li>EnrollmentConfirmedEvent - When user enrolls in a course</li>
 * <li>StreakMilestoneEvent - When user reaches streak milestones</li>
 * <li>StreakLostEvent - When user loses their streak</li>
 * <li>AchievementUnlockedEvent - When user unlocks an achievement</li>
 * <li>LevelUpEvent - When user advances CEFR level</li>
 * </ul>
 *
 * <p>
 * Note: Notifications are always persisted to DB regardless of user
 * preferences.
 * Real-time delivery respects user preferences (quiet hours, category toggles).
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handle course completed event.
     */
    @EventListener
    @Async
    public void handleCourseCompleted(CourseCompletedEvent event) {
        log.info("Handling CourseCompletedEvent for user: {}, course: {}",
                event.getUserId(), event.getCourseTitle());
        createAndSendNotification(event, NotificationType.COURSE_COMPLETED, NotificationPriority.HIGH);
    }

    /**
     * Handle lesson completed event.
     */
    @EventListener
    @Async
    public void handleLessonCompleted(LessonCompletedEvent event) {
        log.debug("Handling LessonCompletedEvent for user: {}, lesson: {}",
                event.getUserId(), event.getLessonTitle());
        createAndSendNotification(event, NotificationType.LESSON_COMPLETED, NotificationPriority.NORMAL);
    }

    /**
     * Handle enrollment confirmed event.
     */
    @EventListener
    @Async
    public void handleEnrollmentConfirmed(EnrollmentConfirmedEvent event) {
        log.info("Handling EnrollmentConfirmedEvent for user: {}, course: {}",
                event.getUserId(), event.getCourseTitle());
        createAndSendNotification(event, NotificationType.ENROLLMENT_CONFIRMED, NotificationPriority.NORMAL);
    }

    /**
     * Handle streak milestone event.
     */
    @EventListener
    @Async
    public void handleStreakMilestone(StreakMilestoneEvent event) {
        log.info("Handling StreakMilestoneEvent for user: {}, days: {}",
                event.getUserId(), event.getStreakDays());
        createAndSendNotification(event, NotificationType.STREAK_MILESTONE, NotificationPriority.HIGH);
    }

    /**
     * Handle streak lost event.
     */
    @EventListener
    @Async
    public void handleStreakLost(StreakLostEvent event) {
        log.info("Handling StreakLostEvent for user: {}, previous: {} days",
                event.getUserId(), event.getPreviousStreak());
        createAndSendNotification(event, NotificationType.STREAK_LOST, NotificationPriority.HIGH);
    }

    /**
     * Handle achievement unlocked event.
     */
    @EventListener
    @Async
    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        log.info("Handling AchievementUnlockedEvent for user: {}, achievement: {}",
                event.getUserId(), event.getAchievementName());
        createAndSendNotification(event, NotificationType.ACHIEVEMENT_UNLOCKED, NotificationPriority.HIGH);
    }

    /**
     * Handle level up event.
     */
    @EventListener
    @Async
    public void handleLevelUp(LevelUpEvent event) {
        log.info("Handling LevelUpEvent for user: {}, {} -> {}",
                event.getUserId(), event.getPreviousLevel(), event.getNewLevel());
        createAndSendNotification(event, NotificationType.LEVEL_UP, NotificationPriority.HIGH);
    }

    /**
     * Create notification and optionally send in real-time based on user
     * preferences.
     * <p>
     * Notifications are always persisted to DB for later retrieval.
     * Real-time WebSocket delivery respects user preferences:
     * - Quiet hours
     * - Category toggles (learning, achievements, reminders, system)
     * - In-app notification toggle
     * </p>
     *
     * @param event    the notification event
     * @param type     the notification type
     * @param priority the notification priority
     */
    private void createAndSendNotification(NotificationEvent event, NotificationType type,
            NotificationPriority priority) {
        try {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .type(type)
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .data(event.getData())
                    .priority(priority)
                    .build();

            // Always persist notification to DB
            NotificationDTO notification = notificationService.createNotification(event.getUserId(), request);

            // Only send real-time if:
            // 1. Notification was created successfully
            // 2. Event is marked for real-time delivery
            // 3. User preferences allow it (quiet hours, category enabled, in-app enabled)
            if (notification != null && event.isRealTime()) {
                boolean shouldSendRealTime = notificationService.shouldNotify(event.getUserId(), request);
                if (shouldSendRealTime) {
                    notificationService.sendRealTimeNotification(event.getUserId(), notification);
                    log.debug("Real-time notification sent for event: {}", event.getNotificationType());
                } else {
                    log.debug("Real-time notification skipped (user preferences) for event: {}",
                            event.getNotificationType());
                }
            }

            log.debug("Notification created for event: {}", event.getNotificationType());
        } catch (Exception e) {
            log.error("Failed to create notification for event: {}", event.getNotificationType(), e);
        }
    }
}
