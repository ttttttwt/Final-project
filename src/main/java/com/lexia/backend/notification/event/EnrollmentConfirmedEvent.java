package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user enrolls in a course.
 */
@Getter
public class EnrollmentConfirmedEvent extends NotificationEvent {

    private final Long courseId;
    private final String courseTitle;
    private final String cefrLevel;
    private final String thumbnailUrl;

    public EnrollmentConfirmedEvent(Object source, UUID userId, Long courseId, String courseTitle,
            String cefrLevel, String thumbnailUrl) {
        super(
                source,
                userId,
                "Enrollment Confirmed! 📚",
                "You are now enrolled in " + courseTitle,
                Map.of(
                        "courseId", courseId,
                        "courseTitle", courseTitle,
                        "cefrLevel", cefrLevel,
                        "courseThumbnail", thumbnailUrl != null ? thumbnailUrl : ""));
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.cefrLevel = cefrLevel;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String getNotificationType() {
        return "ENROLLMENT_CONFIRMED";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
