package com.lexia.backend.enums;

/**
 * AI correction mode for Role-Play conversations.
 * 
 * <p>
 * Modes:
 * </p>
 * <ul>
 * <li>STRICT - "Strict Teacher": Corrects errors immediately during
 * conversation</li>
 * <li>POLITE - "Polite Colleague": Collects errors, provides summary at
 * end</li>
 * </ul>
 * 
 * @since Sprint 5
 */
public enum AiCorrectionMode {
    /** Corrects grammar/vocabulary errors immediately */
    STRICT("STRICT"),
    /** Summarizes errors at end of conversation (default) */
    POLITE("POLITE");

    private final String value;

    AiCorrectionMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AiCorrectionMode fromValue(String value) {
        if (value == null) {
            return POLITE; // Default
        }
        for (AiCorrectionMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return POLITE; // Default for unknown values
    }
}
