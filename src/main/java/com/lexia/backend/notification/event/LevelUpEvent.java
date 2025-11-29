package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user levels up their CEFR level.
 */
@Getter
public class LevelUpEvent extends NotificationEvent {

    private final String previousLevel;
    private final String newLevel;

    public LevelUpEvent(Object source, UUID userId, String previousLevel, String newLevel) {
        super(
                source,
                userId,
                "Level Up! 🚀",
                "Congratulations! You've advanced from " + previousLevel + " to " + newLevel + "!",
                Map.of(
                        "previousLevel", previousLevel,
                        "newLevel", newLevel,
                        "celebration", "🎉"));
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    @Override
    public String getNotificationType() {
        return "LEVEL_UP";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
