package com.lexia.backend.file.service;

import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for file storage operations.
 * Provides abstraction for different storage backends (local, S3, Azure Blob,
 * etc.)
 */
public interface FileStorageService {

    /**
     * Store a file and return the file entity with metadata.
     *
     * @param file     the uploaded file
     * @param category the file category
     * @param userId   the ID of the user uploading the file
     * @return the saved file entity
     */
    FileEntity store(MultipartFile file, FileCategory category, UUID userId);

    /**
     * Load a file as a resource for download/streaming.
     *
     * @param fileId the file ID
     * @return the file as a Spring Resource
     */
    Resource loadAsResource(UUID fileId);

    /**
     * Get file entity by ID.
     *
     * @param fileId the file ID
     * @return the file entity
     */
    FileEntity getFileById(UUID fileId);

    /**
     * Delete a file from storage and database.
     *
     * @param fileId the file ID
     */
    void delete(UUID fileId);

    /**
     * Check if a file exists.
     *
     * @param fileId the file ID
     * @return true if file exists
     */
    boolean exists(UUID fileId);

    /**
     * Get the public URL for a file.
     * For local storage, returns the download endpoint URL.
     * For cloud storage, returns the CDN/S3 URL.
     *
     * @param fileId the file ID
     * @return the public URL
     */
    String getPublicUrl(UUID fileId);

    /**
     * Record file access (increment counter, update timestamp).
     *
     * @param fileId the file ID
     */
    void recordAccess(UUID fileId);

    /**
     * Check if the given user is the owner of the file.
     *
     * @param fileId the file ID
     * @param userId the user ID
     * @return true if user owns the file
     */
    boolean isOwner(UUID fileId, UUID userId);
}
