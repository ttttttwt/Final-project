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
 * Response DTO for file upload operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful file upload")
public class FileUploadResponse {

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

    @Schema(description = "URL to download the file", example = "/api/v1/files/550e8400-e29b-41d4-a716-446655440000/download")
    private String url;

    @Schema(description = "Upload timestamp", example = "2025-11-28T10:30:00Z")
    private Instant uploadedAt;
}
