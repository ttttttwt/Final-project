package com.lexia.backend.enums;

/**
 * Source types for custom material uploads.
 * 
 * <p>
 * Each type has different processing requirements:
 * </p>
 * <ul>
 * <li>PDF - Native multimodal (text + images), max 10MB</li>
 * <li>DOCX - Word document, max 10MB</li>
 * <li>IMAGE - Image with text (OCR via Gemini Vision), max 10MB</li>
 * <li>YOUTUBE - Video with transcript or audio fallback, max 15 min</li>
 * <li>WEBSITE - Article/blog URL, scraped content</li>
 * <li>TEXT - Raw text input, max 5000 chars</li>
 * </ul>
 * 
 * @since Sprint 5
 */
public enum CustomMaterialSourceType {
    PDF("PDF"),
    DOCX("DOCX"),
    IMAGE("IMAGE"),
    YOUTUBE("YOUTUBE"),
    WEBSITE("WEBSITE"),
    TEXT("TEXT");

    private final String value;

    CustomMaterialSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts database value to enum.
     * 
     * @param value database string value
     * @return corresponding enum
     * @throws IllegalArgumentException if value doesn't match
     */
    public static CustomMaterialSourceType fromValue(String value) {
        for (CustomMaterialSourceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown source type: " + value);
    }

    /**
     * Checks if this source type requires a file upload.
     * 
     * @return true if file upload is required
     */
    public boolean requiresFileUpload() {
        return this == PDF || this == DOCX || this == IMAGE;
    }

    /**
     * Checks if this source type requires a URL.
     * 
     * @return true if URL is required
     */
    public boolean requiresUrl() {
        return this == YOUTUBE || this == WEBSITE;
    }
}
