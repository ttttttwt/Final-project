package com.lexia.backend.file.mapper;

import com.lexia.backend.file.dto.FileMetadataDTO;
import com.lexia.backend.file.dto.FileUploadResponse;
import com.lexia.backend.file.entity.FileEntity;

/**
 * Mapper utility class for converting between FileEntity and DTOs.
 */
public final class FileMapper {

    private static final String FILE_DOWNLOAD_URL_TEMPLATE = "/api/v1/files/%s/download";

    private FileMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convert FileEntity to FileUploadResponse.
     *
     * @param entity the file entity
     * @return upload response DTO
     */
    public static FileUploadResponse toUploadResponse(FileEntity entity) {
        if (entity == null) {
            return null;
        }

        return FileUploadResponse.builder()
                .id(entity.getId())
                .originalFilename(entity.getOriginalFilename())
                .mimeType(entity.getMimeType())
                .size(entity.getFileSize())
                .category(entity.getCategory())
                .url(buildDownloadUrl(entity.getId().toString()))
                .uploadedAt(entity.getUploadedAt())
                .build();
    }

    /**
     * Convert FileEntity to FileMetadataDTO.
     *
     * @param entity the file entity
     * @return metadata DTO
     */
    public static FileMetadataDTO toMetadataDTO(FileEntity entity) {
        if (entity == null) {
            return null;
        }

        return FileMetadataDTO.builder()
                .id(entity.getId())
                .originalFilename(entity.getOriginalFilename())
                .mimeType(entity.getMimeType())
                .size(entity.getFileSize())
                .category(entity.getCategory())
                .uploadedBy(entity.getUploadedBy() != null ? entity.getUploadedBy().getId() : null)
                .uploadedAt(entity.getUploadedAt())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .durationSeconds(entity.getDurationSeconds())
                .isPublic(entity.getIsPublic())
                .accessCount(entity.getAccessCount())
                .lastAccessedAt(entity.getLastAccessedAt())
                .url(buildDownloadUrl(entity.getId().toString()))
                .build();
    }

    /**
     * Build download URL for a file.
     *
     * @param fileId the file ID
     * @return download URL
     */
    public static String buildDownloadUrl(String fileId) {
        return String.format(FILE_DOWNLOAD_URL_TEMPLATE, fileId);
    }
}
