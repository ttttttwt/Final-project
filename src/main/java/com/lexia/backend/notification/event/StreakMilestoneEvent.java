package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user reaches a streak milestone.
 */
@Getter
public class StreakMilestoneEvent extends NotificationEvent {

    private final int streakDays;
    private final String milestone;

    public StreakMilestoneEvent(Object source, UUID userId, int streakDays) {
        super(
                source,
                userId,
                "Streak Milestone! 🔥",
                getMilestoneMessage(streakDays),
                Map.of(
                        "streakDays", streakDays,
                        "milestone", streakDays + "-day streak",
                        "reward", getReward(streakDays)));
        this.streakDays = streakDays;
        this.milestone = streakDays + "-day streak";
    }

    private static String getMilestoneMessage(int days) {
        return switch (days) {
            case 7 -> "Amazing! You've maintained a 7-day learning streak! 🎯";
            case 14 -> "Incredible! 2 weeks of consistent learning! 🌟";
            case 30 -> "Outstanding! You've reached a 30-day streak! 🏆";
            case 60 -> "Phenomenal! 60 days of dedication! 💪";
            case 100 -> "Legendary! 100-day learning streak achieved! 🏅";
            default -> "Great job! You've reached a " + days + "-day streak! 🔥";
        };
    }

    private static String getReward(int days) {
        return switch (days) {
            case 7 -> "Badge: Week Warrior";
            case 14 -> "Badge: Two-Week Champion";
            case 30 -> "Badge: Dedicated Learner";
            case 60 -> "Badge: Consistency Master";
            case 100 -> "Badge: Century Achiever";
            default -> "Streak Badge";
        };
    }

    @Override
    public String getNotificationType() {
        return "STREAK_MILESTONE";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
