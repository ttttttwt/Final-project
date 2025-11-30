package com.lexia.backend.file.dto;

import com.lexia.backend.file.enums.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO containing detailed file metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed file metadata")
public class FileMetadataDTO {

    @Schema(description = "Unique file identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Original filename", example = "avatar.jpg")
    private String originalFilename;

    @Schema(description = "MIME type of the file", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "245789")
    private Long size;

    @Schema(description = "File category", example = "AVATAR")
    private FileCategory category;

    @Schema(description = "ID of user who uploaded the file")
    private UUID uploadedBy;

    @Schema(description = "Upload timestamp", example = "2025-11-28T10:30:00Z")
    private Instant uploadedAt;

    // Image-specific metadata
    @Schema(description = "Image width in pixels (null for non-images)", example = "800")
    private Integer width;

    @Schema(description = "Image height in pixels (null for non-images)", example = "600")
    private Integer height;

    // Audio-specific metadata
    @Schema(description = "Audio duration in seconds (null for non-audio)", example = "180")
    private Integer durationSeconds;

    // Access info
    @Schema(description = "Whether file is publicly accessible", example = "true")
    private Boolean isPublic;

    @Schema(description = "Number of times file has been accessed", example = "42")
    private Integer accessCount;

    @Schema(description = "Last access timestamp")
    private Instant lastAccessedAt;

    @Schema(description = "URL to download the file", example = "/api/v1/files/550e8400-e29b-41d4-a716-446655440000/download")
    private String url;
}
