package com.lexia.backend.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Base class for all notification events.
 * Extend this class to create specific notification events.
 */
@Getter
public abstract class NotificationEvent extends ApplicationEvent {

    private final UUID userId;
    private final String title;
    private final String message;
    private final Map<String, Object> data;

    protected NotificationEvent(Object source, UUID userId, String title, String message, Map<String, Object> data) {
        super(source);
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.data = data != null ? data : Map.of();
    }

    /**
     * Get the notification type for this event.
     */
    public abstract String getNotificationType();

    /**
     * Get whether this notification should be sent in real-time.
     */
    public abstract boolean isRealTime();
}
