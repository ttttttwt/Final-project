package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user completes a course.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Getter
public class CourseCompletedEvent extends NotificationEvent {

    private final Long courseId;
    private final String courseTitle;
    private final int completionTimeMinutes;

    /**
     * Creates a new CourseCompletedEvent.
     *
     * @param source                the object on which the event initially occurred
     * @param userId                the user who completed the course
     * @param courseId              the ID of the completed course
     * @param courseTitle           the title of the completed course
     * @param completionTimeMinutes total time spent completing the course in
     *                              minutes
     */
    public CourseCompletedEvent(Object source, UUID userId, Long courseId, String courseTitle,
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
