package com.lexia.backend.file.enums;

/**
 * Categories for uploaded files.
 * Each category has specific allowed MIME types, size limits, and access
 * control.
 */
public enum FileCategory {
    /**
     * User avatar images
     * Allowed: JPG, PNG, GIF, WebP
     * Max size: 5MB
     * Access: Public (can be viewed without authentication)
     */
    AVATAR("avatars", true),

    /**
     * Course thumbnail images
     * Allowed: JPG, PNG, WebP
     * Max size: 5MB
     * Access: Public (visible to all users browsing courses)
     */
    COURSE_THUMBNAIL("courses/thumbnails", true),

    /**
     * Lesson audio files for LISTENING lessons
     * Allowed: MP3, WAV, OGG, M4A
     * Max size: 50MB
     * Access: Private (requires authentication and enrollment)
     */
    LESSON_AUDIO("lessons/audio", false),

    /**
     * Lesson images and attachments
     * Allowed: JPG, PNG, GIF, WebP
     * Max size: 10MB
     * Access: Private (requires authentication and enrollment)
     */
    LESSON_IMAGE("lessons/images", false),

    /**
     * PDF documents (resources, guides)
     * Allowed: PDF
     * Max size: 10MB
     * Access: Private (requires authentication)
     */
    DOCUMENT("documents", false),

    /**
     * Certificate PDFs
     * Allowed: PDF
     * Max size: 10MB
     * Access: Private (only owner and admin can access)
     */
    CERTIFICATE("documents/certificates", false);

    private final String storagePath;
    private final boolean publicByDefault;

    FileCategory(String storagePath, boolean publicByDefault) {
        this.storagePath = storagePath;
        this.publicByDefault = publicByDefault;
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

    /**
     * Check if files in this category are public by default.
     * Public files can be accessed without authentication.
     *
     * @return true if files are public by default
     */
    public boolean isPublicByDefault() {
        return publicByDefault;
    }
}
