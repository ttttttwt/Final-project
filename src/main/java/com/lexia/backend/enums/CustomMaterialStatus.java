package com.lexia.backend.enums;

/**
 * Processing status for custom materials.
 * 
 * <p>
 * Status flow:
 * </p>
 * 
 * <pre>
 * PENDING → PROCESSING → COMPLETED
 *                ↓
 *              FAILED
 * </pre>
 * 
 * @since Sprint 5
 */
public enum CustomMaterialStatus {
    /** Material uploaded, waiting in queue */
    PENDING("PENDING"),
    /** AI is processing the material */
    PROCESSING("PROCESSING"),
    /** Processing completed successfully */
    COMPLETED("COMPLETED"),
    /** Processing failed (see error message) */
    FAILED("FAILED");

    private final String value;

    CustomMaterialStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CustomMaterialStatus fromValue(String value) {
        for (CustomMaterialStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    /**
     * Checks if this status indicates the material is ready for use.
     * 
     * @return true if COMPLETED
     */
    public boolean isReady() {
        return this == COMPLETED;
    }

    /**
     * Checks if this status indicates processing is still ongoing.
     * 
     * @return true if PENDING or PROCESSING
     */
    public boolean isProcessing() {
        return this == PENDING || this == PROCESSING;
    }
}
