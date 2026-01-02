package com.lexia.backend.enums;

/**
 * Enum representing the Common European Framework of Reference for Languages (CEFR) levels.
 * 
 * <p>
 * CEFR defines six proficiency levels from A1 (beginner) to C2 (mastery).
 * This enum provides ordering and progression methods for learning path recommendations.
 * </p>
 * 
 * <p>
 * <strong>Level Descriptions:</strong>
 * <ul>
 * <li>A1 - Beginner: Can understand and use familiar everyday expressions</li>
 * <li>A2 - Elementary: Can communicate in simple routine tasks</li>
 * <li>B1 - Intermediate: Can deal with most situations while traveling</li>
 * <li>B2 - Upper Intermediate: Can interact with fluency and spontaneity</li>
 * <li>C1 - Advanced: Can express ideas fluently and spontaneously</li>
 * <li>C2 - Proficiency: Can understand with ease virtually everything</li>
 * </ul>
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 3
 */
public enum CEFRLevel {
    /**
     * A1 - Beginner level.
     */
    A1("Beginner", 1),

    /**
     * A2 - Elementary level.
     */
    A2("Elementary", 2),

    /**
     * B1 - Intermediate level.
     */
    B1("Intermediate", 3),

    /**
     * B2 - Upper Intermediate level.
     */
    B2("Upper Intermediate", 4),

    /**
     * C1 - Advanced level.
     */
    C1("Advanced", 5),

    /**
     * C2 - Proficiency level (mastery).
     */
    C2("Proficiency", 6);

    private final String displayName;
    private final int order;

    CEFRLevel(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    /**
     * Get the display name of the CEFR level.
     * 
     * @return display name (e.g., "Beginner", "Intermediate")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the numeric order of the level (1-6).
     * 
     * @return order value
     */
    public int getOrder() {
        return order;
    }

    /**
     * Get the next CEFR level in the progression.
     * 
     * <p>
     * Returns the next level in the sequence: A1 → A2 → B1 → B2 → C1 → C2.
     * If already at C2 (highest level), returns C2.
     * </p>
     * 
     * @return the next level, or C2 if already at max
     */
    public CEFRLevel getNextLevel() {
        switch (this) {
            case A1:
                return A2;
            case A2:
                return B1;
            case B1:
                return B2;
            case B2:
                return C1;
            case C1:
            case C2:
            default:
                return C2; // Already at max or unknown
        }
    }

    /**
     * Check if there is a next level available.
     * 
     * @return true if not at C2, false if at max level
     */
    public boolean hasNextLevel() {
        return this != C2;
    }

    /**
     * Get the previous CEFR level in the progression.
     * 
     * <p>
     * Returns the previous level in the sequence: C2 → C1 → B2 → B1 → A2 → A1.
     * If already at A1 (lowest level), returns A1.
     * </p>
     * 
     * @return the previous level, or A1 if already at min
     */
    public CEFRLevel getPreviousLevel() {
        switch (this) {
            case C2:
                return C1;
            case C1:
                return B2;
            case B2:
                return B1;
            case B1:
                return A2;
            case A2:
            case A1:
            default:
                return A1; // Already at min or unknown
        }
    }

    /**
     * Check if there is a previous level available.
     * 
     * @return true if not at A1, false if at min level
     */
    public boolean hasPreviousLevel() {
        return this != A1;
    }

    /**
     * Parse a CEFR level from a string (case-insensitive).
     * 
     * @param level the level string (e.g., "A1", "a1", "B2")
     * @return the corresponding CEFRLevel enum
     * @throws IllegalArgumentException if the level is invalid
     */
    public static CEFRLevel fromString(String level) {
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("CEFR level cannot be null or empty");
        }

        try {
            return CEFRLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid CEFR level: " + level + 
                    ". Valid levels are: A1, A2, B1, B2, C1, C2");
        }
    }

    /**
     * Check if one level is higher than another.
     * 
     * @param other the level to compare with
     * @return true if this level is higher than the other
     */
    public boolean isHigherThan(CEFRLevel other) {
        return this.order > other.order;
    }

    /**
     * Check if one level is lower than another.
     * 
     * @param other the level to compare with
     * @return true if this level is lower than the other
     */
    public boolean isLowerThan(CEFRLevel other) {
        return this.order < other.order;
    }

    /**
     * Get the highest level from a collection.
     * 
     * @param levels collection of CEFR levels
     * @return the highest level, or null if collection is empty
     */
    public static CEFRLevel max(java.util.Collection<CEFRLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }

        return levels.stream()
                .max(java.util.Comparator.comparingInt(CEFRLevel::getOrder))
                .orElse(null);
    }

    /**
     * Get the lowest level from a collection.
     * 
     * @param levels collection of CEFR levels
     * @return the lowest level, or null if collection is empty
     */
    public static CEFRLevel min(java.util.Collection<CEFRLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }

        return levels.stream()
                .min(java.util.Comparator.comparingInt(CEFRLevel::getOrder))
                .orElse(null);
    }
}
