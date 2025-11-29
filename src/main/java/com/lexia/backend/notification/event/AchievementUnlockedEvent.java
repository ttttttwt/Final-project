package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a user unlocks an achievement.
 */
@Getter
public class AchievementUnlockedEvent extends NotificationEvent {

    private final UUID achievementId;
    private final String achievementName;
    private final String achievementIcon;
    private final String description;

    public AchievementUnlockedEvent(Object source, UUID userId, UUID achievementId,
            String achievementName, String achievementIcon, String description) {
        super(
                source,
                userId,
                "Achievement Unlocked! 🏆",
                "You earned: " + achievementName,
                Map.of(
                        "achievementId", achievementId.toString(),
                        "achievementName", achievementName,
                        "achievementIcon", achievementIcon,
                        "description", description));
        this.achievementId = achievementId;
        this.achievementName = achievementName;
        this.achievementIcon = achievementIcon;
        this.description = description;
    }

    @Override
    public String getNotificationType() {
        return "ACHIEVEMENT_UNLOCKED";
    }

    @Override
    public boolean isRealTime() {
        return true;
    }
}
