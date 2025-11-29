package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user completes a course.
 */
@Getter
public class CourseCompletedEvent extends NotificationEvent {

    private final UUID courseId;
    private final String courseTitle;
    private final int completionTimeMinutes;

    public CourseCompletedEvent(Object source, UUID userId, UUID courseId, String courseTitle,
            int completionTimeMinutes) {
        super(
                source,
                userId,
                "Congratulations! 🎉",
                "You completed " + courseTitle,
                Map.of(
                        "courseId", courseId.toString(),
                        "courseTitle", courseTitle,
                        "completionTime", completionTimeMinutes));
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.completionTimeMinutes = completionTimeMinutes;
    }

    @Override
    public String getNotificationType() {
        return "COURSE_COMPLETED";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
