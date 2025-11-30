package com.lexia.backend.file.enums;

/**
 * Categories for uploaded files.
 * Each category has specific allowed MIME types and size limits.
 */
public enum FileCategory {
    /**
     * User avatar images
     * Allowed: JPG, PNG, GIF, WebP
     * Max size: 5MB
     */
    AVATAR("avatars"),

    /**
     * Course thumbnail images
     * Allowed: JPG, PNG, WebP
     * Max size: 5MB
     */
    COURSE_THUMBNAIL("courses/thumbnails"),

    /**
     * Lesson audio files for LISTENING lessons
     * Allowed: MP3, WAV, OGG, M4A
     * Max size: 50MB
     */
    LESSON_AUDIO("lessons/audio"),

    /**
     * Lesson images and attachments
     * Allowed: JPG, PNG, GIF, WebP
     * Max size: 10MB
     */
    LESSON_IMAGE("lessons/images"),

    /**
     * PDF documents (resources, guides)
     * Allowed: PDF
     * Max size: 10MB
     */
    DOCUMENT("documents"),

    /**
     * Certificate PDFs
     * Allowed: PDF
     * Max size: 10MB
     */
    CERTIFICATE("documents/certificates");

    private final String storagePath;

    FileCategory(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Get the base storage path for this category.
     * Files will be stored in: uploads/{storagePath}/{yyyy-MM}/{filename}
     *
     * @return the storage path segment for this category
     */
    public String getStoragePath() {
        return storagePath;
    }
}
