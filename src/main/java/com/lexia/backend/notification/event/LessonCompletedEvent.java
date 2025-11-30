package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user completes a lesson.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Getter
public class LessonCompletedEvent extends NotificationEvent {

    private final Long courseId;
    private final Long lessonId;
    private final String lessonTitle;
    private final String sectionTitle;

    /**
     * Creates a new LessonCompletedEvent.
     *
     * @param source       the object on which the event initially occurred
     * @param userId       the user who completed the lesson
     * @param courseId     the ID of the course containing the lesson
     * @param lessonId     the ID of the completed lesson
     * @param lessonTitle  the title of the completed lesson
     * @param sectionTitle the title of the section containing the lesson
     */
    public LessonCompletedEvent(Object source, UUID userId, Long courseId, Long lessonId,
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
