package com.lexia.backend.notification.event;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a custom material processing completes.
 *
 * @since Sprint 5
 */
@Getter
public class MaterialProcessingCompletedEvent extends NotificationEvent {

    private final UUID materialId;
    private final String materialTitle;
    private final boolean success;
    private final String errorMessage;

    /**
     * Creates a new MaterialProcessingCompletedEvent.
     *
     * @param source        the object that fired the event
     * @param userId        the user who owns the material
     * @param materialId    the material ID
     * @param materialTitle the material title
     * @param success       whether processing succeeded
     * @param errorMessage  error message if failed (null if success)
     */
    public MaterialProcessingCompletedEvent(Object source, UUID userId, UUID materialId,
            String materialTitle, boolean success, String errorMessage) {
        super(source,
                userId,
                success ? "Material Ready! 🎉" : "Processing Failed ❌",
                success
                        ? "Your material \"" + materialTitle + "\" is now ready for learning"
                        : "Processing failed for \"" + materialTitle + "\": " + errorMessage,
                Map.of(
                        "materialId", materialId.toString(),
                        "materialTitle", materialTitle,
                        "success", success,
                        "errorMessage", errorMessage != null ? errorMessage : ""));
        this.materialId = materialId;
        this.materialTitle = materialTitle;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getNotificationType() {
        return success ? "CUSTOM_MATERIAL_READY" : "CUSTOM_MATERIAL_FAILED";
    }

    @Override
    public boolean isRealTime() {
        return true; // Notify user immediately
    }
}
