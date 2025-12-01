package com.lexia.backend.file.service.impl;

import com.lexia.backend.entity.User;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.exception.FileNotFoundException;
import com.lexia.backend.file.exception.FileStorageException;
import com.lexia.backend.file.mapper.FileMapper;
import com.lexia.backend.file.repository.FileRepository;
import com.lexia.backend.file.service.FileMetadataExtractor;
import com.lexia.backend.file.service.FileStorageService;
import com.lexia.backend.file.validator.FileValidator;
import com.lexia.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Local filesystem implementation of FileStorageService.
 * Stores files in the local filesystem under the configured upload directory.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final FileMetadataExtractor metadataExtractor;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    public LocalFileStorageService(FileRepository fileRepository,
            UserRepository userRepository,
            FileValidator fileValidator,
            FileMetadataExtractor metadataExtractor) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileValidator = fileValidator;
        this.metadataExtractor = metadataExtractor;
    }

    /**
     * Initialize the storage directory on startup.
     */
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            LOG.info("File storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    @Override
    @Transactional
    public FileEntity store(MultipartFile file, FileCategory category, UUID userId) {
        LOG.debug("Storing file: {} for category: {} by user: {}",
                file.getOriginalFilename(), category, userId);

        // 1. Validate file
        fileValidator.validate(file, category);

        // 2. Get user reference
        User uploadedBy = null;
        if (userId != null) {
            uploadedBy = userRepository.findById(userId).orElse(null);
        }

        // 3. Generate unique filename and storage path
        String fileId = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String filename = fileId + extension;
        String storagePath = buildStoragePath(category, filename);

        // 4. Save file to filesystem
        Path targetPath = rootLocation.resolve(storagePath).normalize();
        try {
            // Ensure parent directories exist
            Files.createDirectories(targetPath.getParent());

            // Copy file to target location
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.debug("File saved to: {}", targetPath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + file.getOriginalFilename(), e);
        }

        // 5. Extract metadata (width/height for images, duration for audio)
        FileMetadataExtractor.ExtractedMetadata extractedMetadata = metadataExtractor.extract(file, category);

        // 6. Create and save file entity
        FileEntity fileEntity = FileEntity.builder()
                .originalFilename(fileValidator.sanitizeFilename(file.getOriginalFilename()))
                .storagePath(storagePath)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .category(category)
                .uploadedBy(uploadedBy)
                .isPublic(category.isPublicByDefault())
                .width(extractedMetadata.getWidth())
                .height(extractedMetadata.getHeight())
                .durationSeconds(extractedMetadata.getDurationSeconds())
                .build();

        FileEntity savedEntity = fileRepository.save(fileEntity);
        LOG.info("File stored successfully with ID: {}", savedEntity.getId());

        return savedEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAsResource(UUID fileId) {
        FileEntity fileEntity = getFileById(fileId);

        try {
            Path filePath = rootLocation.resolve(fileEntity.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found or not readable: " + fileId);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Error reading file: " + fileId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFileById(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + fileId));
    }

    @Override
    @Transactional
    public void delete(UUID fileId) {
        FileEntity fileEntity = getFileById(fileId);

        // Delete from filesystem
        try {
            Path filePath = rootLocation.resolve(fileEntity.getStoragePath()).normalize();
            Files.deleteIfExists(filePath);
            LOG.debug("Deleted file from filesystem: {}", filePath);
        } catch (IOException e) {
            LOG.warn("Failed to delete file from filesystem: {}", e.getMessage());
            // Continue with database deletion even if filesystem delete fails
        }

        // Delete from database
        fileRepository.delete(fileEntity);
        LOG.info("File deleted: {}", fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID fileId) {
        if (!fileRepository.existsById(fileId)) {
            return false;
        }

        FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
        if (fileEntity == null) {
            return false;
        }

        Path filePath = rootLocation.resolve(fileEntity.getStoragePath()).normalize();
        return Files.exists(filePath);
    }

    @Override
    public String getPublicUrl(UUID fileId) {
        return FileMapper.buildDownloadUrl(fileId.toString());
    }

    /**
     * Record file access asynchronously to avoid blocking the download request.
     * Uses a new transaction to prevent issues with the read transaction.
     */
    @Override
    @Async("fileAccessExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAccess(UUID fileId) {
        try {
            fileRepository.findById(fileId).ifPresent(fileEntity -> {
                fileEntity.recordAccess();
                fileRepository.save(fileEntity);
                LOG.debug("Recorded access for file: {}", fileId);
            });
        } catch (Exception e) {
            // Log but don't fail - access recording is not critical
            LOG.warn("Failed to record access for file {}: {}", fileId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
        if (fileEntity == null || fileEntity.getUploadedBy() == null) {
            return false;
        }
        return fileEntity.getUploadedBy().getId().equals(userId);
    }

    // === Helper Methods ===

    /**
     * Build storage path: {category}/{yyyy-MM}/{filename}
     */
    private String buildStoragePath(FileCategory category, String filename) {
        String monthDir = LocalDate.now().format(DATE_FORMATTER);
        return String.format("%s/%s/%s", category.getStoragePath(), monthDir, filename);
    }

    /**
     * Extract file extension from filename.
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
