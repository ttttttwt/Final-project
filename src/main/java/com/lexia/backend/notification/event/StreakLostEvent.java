package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user loses their streak.
 */
@Getter
public class StreakLostEvent extends NotificationEvent {

    private final int previousStreak;

    public StreakLostEvent(Object source, UUID userId, int previousStreak) {
        super(
                source,
                userId,
                "Streak Lost 😢",
                "Your " + previousStreak + "-day streak has ended. Start a new streak today!",
                Map.of(
                        "previousStreak", previousStreak,
                        "action", "Start learning to begin a new streak"));
        this.previousStreak = previousStreak;
    }

    @Override
    public String getNotificationType() {
        return "STREAK_LOST";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
