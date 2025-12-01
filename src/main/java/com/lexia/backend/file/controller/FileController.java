package com.lexia.backend.file.controller;

import com.lexia.backend.entity.User;
import com.lexia.backend.file.dto.FileMetadataDTO;
import com.lexia.backend.file.dto.FileUploadResponse;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.mapper.FileMapper;
import com.lexia.backend.file.service.FileSecurityService;
import com.lexia.backend.file.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for file upload and management operations.
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Upload", description = "File upload and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;
    private final FileSecurityService fileSecurityService;

    public FileController(FileStorageService fileStorageService, FileSecurityService fileSecurityService) {
        this.fileStorageService = fileStorageService;
        this.fileSecurityService = fileSecurityService;
    }

    /**
     * Upload a single file.
     */
    @Operation(summary = "Upload a file", description = "Upload a single file with category specification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully", content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or category"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "File category", required = true) @RequestParam("category") FileCategory category,
            @AuthenticationPrincipal User user) {

        LOG.info("File upload request: {} ({}) by user: {}",
                file.getOriginalFilename(), category, user.getEmail());

        UUID userId = user.getId();
        FileEntity savedFile = fileStorageService.store(file, category, userId);

        LOG.info("File uploaded successfully: {}", savedFile.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileMapper.toUploadResponse(savedFile));
    }

    /**
     * Get file metadata.
     */
    @Operation(summary = "Get file metadata", description = "Retrieve metadata for a specific file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File metadata retrieved", content = @Content(schema = @Schema(implementation = FileMetadataDTO.class))),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/metadata")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileMetadataDTO> getMetadata(
            @Parameter(description = "File ID", required = true) @PathVariable("id") UUID id) {

        LOG.debug("Get metadata request for file: {}", id);
        FileEntity fileEntity = fileStorageService.getFileById(id);
        return ResponseEntity.ok(FileMapper.toMetadataDTO(fileEntity));
    }

    /**
     * Download a file.
     * Public files can be accessed without authentication.
     * Private files require authentication and ownership/admin role.
     */
    @Operation(summary = "Download file", description = "Download a file by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File content"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File ID", required = true) @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {

        LOG.debug("Download request for file: {}", id);

        FileEntity fileEntity = fileStorageService.getFileById(id);

        // Use centralized security service for access control
        if (!fileSecurityService.canAccess(fileEntity, user)) {
            throw new AccessDeniedException("You don't have permission to access this file");
        }

        Resource resource = fileStorageService.loadAsResource(id);

        // Record access asynchronously (non-blocking)
        fileStorageService.recordAccess(id);

        // Determine content disposition (inline for images/audio, attachment for
        // others)
        String disposition = getContentDisposition(fileEntity.getMimeType());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + fileEntity.getOriginalFilename() + "\"")
                .body(resource);
    }

    /**
     * Delete a file.
     * Only file owner or admin can delete.
     */
    @Operation(summary = "Delete file", description = "Delete a file by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "File ID", required = true) @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {

        LOG.info("Delete request for file: {} by user: {}", id, user.getEmail());

        FileEntity fileEntity = fileStorageService.getFileById(id);

        // Use centralized security service for access control
        if (!fileSecurityService.canDelete(fileEntity, user)) {
            throw new AccessDeniedException("You don't have permission to delete this file");
        }

        fileStorageService.delete(id);
        LOG.info("File deleted: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a file exists.
     */
    @Operation(summary = "Check file exists", description = "Check if a file exists by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File exists"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}/exists")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkExists(
            @Parameter(description = "File ID", required = true) @PathVariable("id") UUID id) {

        boolean exists = fileStorageService.exists(id);
        if (!exists) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(true);
    }

    // === Helper Methods ===

    /**
     * Determine content disposition based on MIME type.
     * Images and audio are displayed inline, others as attachment.
     */
    private String getContentDisposition(String mimeType) {
        if (mimeType == null) {
            return "attachment";
        }
        if (mimeType.startsWith("image/") || mimeType.startsWith("audio/")) {
            return "inline";
        }
        return "attachment";
    }
}
