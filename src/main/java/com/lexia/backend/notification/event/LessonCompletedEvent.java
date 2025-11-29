package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user completes a lesson.
 */
@Getter
public class LessonCompletedEvent extends NotificationEvent {

    private final UUID courseId;
    private final Long lessonId;
    private final String lessonTitle;
    private final String sectionTitle;

    public LessonCompletedEvent(Object source, UUID userId, UUID courseId, Long lessonId,
            String lessonTitle, String sectionTitle) {
        super(
                source,
                userId,
                "Lesson Completed! ✅",
                "You completed " + lessonTitle,
                Map.of(
                        "courseId", courseId.toString(),
                        "lessonId", lessonId,
                        "lessonTitle", lessonTitle,
                        "sectionTitle", sectionTitle));
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.sectionTitle = sectionTitle;
    }

    @Override
    public String getNotificationType() {
        return "LESSON_COMPLETED";
    }

    @Override
    public boolean isRealTime() {
        return false; // Lesson completion is not urgent
    }
}
