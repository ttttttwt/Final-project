package com.lexia.backend.file.validator;

import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Validates uploaded files for security and compliance.
 * Performs MIME type validation, size validation, and magic byte validation.
 */
@Component
public class FileValidator {

    private static final Logger LOG = LoggerFactory.getLogger(FileValidator.class);

    // Allowed MIME types per category
    private static final Map<FileCategory, Set<String>> ALLOWED_MIME_TYPES = Map.of(
            FileCategory.AVATAR, Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
            FileCategory.COURSE_THUMBNAIL, Set.of("image/jpeg", "image/png", "image/webp"),
            FileCategory.LESSON_AUDIO, Set.of("audio/mpeg", "audio/wav", "audio/ogg", "audio/mp4", "audio/x-wav"),
            FileCategory.LESSON_IMAGE, Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
            FileCategory.DOCUMENT, Set.of("application/pdf"),
            FileCategory.CERTIFICATE, Set.of("application/pdf"));

    // Maximum file sizes per category (in bytes)
    private static final Map<FileCategory, Long> MAX_FILE_SIZES = Map.of(
            FileCategory.AVATAR, 5L * 1024 * 1024, // 5 MB
            FileCategory.COURSE_THUMBNAIL, 5L * 1024 * 1024, // 5 MB
            FileCategory.LESSON_AUDIO, 50L * 1024 * 1024, // 50 MB
            FileCategory.LESSON_IMAGE, 10L * 1024 * 1024, // 10 MB
            FileCategory.DOCUMENT, 10L * 1024 * 1024, // 10 MB
            FileCategory.CERTIFICATE, 10L * 1024 * 1024 // 10 MB
    );

    // Magic bytes for file type verification
    // JPEG: FF D8 FF
    private static final byte[] JPEG_MAGIC = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    private static final byte[] PNG_MAGIC = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    // GIF: 47 49 46 38
    private static final byte[] GIF_MAGIC = new byte[] { 0x47, 0x49, 0x46, 0x38 };
    // WebP: 52 49 46 46 (RIFF) ... 57 45 42 50 (WEBP)
    private static final byte[] RIFF_MAGIC = new byte[] { 0x52, 0x49, 0x46, 0x46 };
    private static final byte[] WEBP_MAGIC = new byte[] { 0x57, 0x45, 0x42, 0x50 };
    // PDF: 25 50 44 46 (%PDF)
    private static final byte[] PDF_MAGIC = new byte[] { 0x25, 0x50, 0x44, 0x46 };
    // MP3: ID3 tag (49 44 33) or frame sync (FF FB/FA/F3/F2)
    private static final byte[] ID3_MAGIC = new byte[] { 0x49, 0x44, 0x33 };
    private static final byte MP3_SYNC_BYTE = (byte) 0xFF;
    // WAV: 52 49 46 46 (RIFF) ... 57 41 56 45 (WAVE)
    private static final byte[] WAVE_MAGIC = new byte[] { 0x57, 0x41, 0x56, 0x45 };
    // OGG: 4F 67 67 53 (OggS)
    private static final byte[] OGG_MAGIC = new byte[] { 0x4F, 0x67, 0x67, 0x53 };
    // M4A/MP4: ftyp at offset 4
    private static final byte[] FTYP_MAGIC = new byte[] { 0x66, 0x74, 0x79, 0x70 };

    /**
     * Validate a file against category-specific rules.
     *
     * @param file     the uploaded file
     * @param category the file category
     * @throws FileValidationException if validation fails
     */
    public void validate(MultipartFile file, FileCategory category) {
        // 1. Check if file is empty (do this first before accessing file properties)
        validateNotEmpty(file);

        LOG.debug("Validating file: {} for category: {}", file.getOriginalFilename(), category);

        // 2. Check MIME type
        validateMimeType(file, category);

        // 3. Check file size
        validateFileSize(file, category);

        // 4. Validate file content (magic bytes)
        validateFileContent(file);

        // 5. Sanitize filename
        sanitizeFilename(file.getOriginalFilename());

        LOG.debug("File validation passed: {}", file.getOriginalFilename());
    }

    /**
     * Check if the file is not empty.
     */
    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
    }

    /**
     * Validate MIME type against allowed types for the category.
     */
    private void validateMimeType(MultipartFile file, FileCategory category) {
        String mimeType = file.getContentType();
        Set<String> allowedTypes = ALLOWED_MIME_TYPES.get(category);

        if (mimeType == null || !allowedTypes.contains(mimeType)) {
            throw new FileValidationException(
                    String.format("File type '%s' not allowed for category %s. Allowed types: %s",
                            mimeType, category, allowedTypes));
        }
    }

    /**
     * Validate file size against category-specific limits.
     */
    private void validateFileSize(MultipartFile file, FileCategory category) {
        long maxSize = MAX_FILE_SIZES.get(category);
        if (file.getSize() > maxSize) {
            throw new FileValidationException(
                    String.format("File size %s exceeds maximum allowed %s for category %s",
                            formatSize(file.getSize()), formatSize(maxSize), category));
        }
    }

    /**
     * Validate file content using magic bytes to prevent MIME type spoofing.
     */
    private void validateFileContent(MultipartFile file) {
        String mimeType = file.getContentType();
        if (mimeType == null) {
            throw new FileValidationException("Cannot determine file type");
        }

        try {
            byte[] header = readFileHeader(file, 12);

            boolean isValid = switch (mimeType) {
                case "image/jpeg" -> startsWithMagic(header, JPEG_MAGIC);
                case "image/png" -> startsWithMagic(header, PNG_MAGIC);
                case "image/gif" -> startsWithMagic(header, GIF_MAGIC);
                case "image/webp" -> isWebP(header);
                case "application/pdf" -> startsWithMagic(header, PDF_MAGIC);
                case "audio/mpeg" -> isMp3(header);
                case "audio/wav", "audio/x-wav" -> isWav(header);
                case "audio/ogg" -> startsWithMagic(header, OGG_MAGIC);
                case "audio/mp4" -> isM4a(header);
                default -> {
                    LOG.warn("Unknown MIME type for magic byte validation: {}", mimeType);
                    yield true; // Allow unknown types to pass magic byte check
                }
            };

            if (!isValid) {
                throw new FileValidationException(
                        "File content does not match declared type. Possible file type spoofing detected.");
            }
        } catch (IOException e) {
            throw new FileValidationException("Failed to read file content for validation", e);
        }
    }

    /**
     * Sanitize filename to prevent path traversal attacks.
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new FileValidationException("Filename cannot be empty");
        }

        // Remove path separators and null bytes
        String sanitized = filename
                .replaceAll("[/\\\\]", "")
                .replaceAll("\0", "")
                .trim();

        // Check for path traversal attempts
        if (sanitized.contains("..")) {
            throw new FileValidationException("Invalid filename: path traversal attempt detected");
        }

        // Limit filename length
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        return sanitized;
    }

    /**
     * Get allowed MIME types for a category.
     */
    public Set<String> getAllowedMimeTypes(FileCategory category) {
        return ALLOWED_MIME_TYPES.get(category);
    }

    /**
     * Get maximum file size for a category.
     */
    public long getMaxFileSize(FileCategory category) {
        return MAX_FILE_SIZES.get(category);
    }

    // === Helper Methods ===

    private byte[] readFileHeader(MultipartFile file, int length) throws IOException {
        byte[] header = new byte[length];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < length) {
                // Return only the bytes that were read
                return Arrays.copyOf(header, bytesRead);
            }
        }
        return header;
    }

    private boolean startsWithMagic(byte[] header, byte[] magic) {
        if (header.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebP(byte[] header) {
        // WebP: RIFF....WEBP
        return header.length >= 12 &&
                startsWithMagic(header, RIFF_MAGIC) &&
                header[8] == WEBP_MAGIC[0] &&
                header[9] == WEBP_MAGIC[1] &&
                header[10] == WEBP_MAGIC[2] &&
                header[11] == WEBP_MAGIC[3];
    }

    private boolean isMp3(byte[] header) {
        // Check for ID3 tag or MP3 frame sync
        return startsWithMagic(header, ID3_MAGIC) ||
                (header.length >= 2 && header[0] == MP3_SYNC_BYTE &&
                        (header[1] == (byte) 0xFB || header[1] == (byte) 0xFA ||
                                header[1] == (byte) 0xF3 || header[1] == (byte) 0xF2 ||
                                header[1] == (byte) 0xE3 || header[1] == (byte) 0xE2));
    }

    private boolean isWav(byte[] header) {
        // WAV: RIFF....WAVE
        return header.length >= 12 &&
                startsWithMagic(header, RIFF_MAGIC) &&
                header[8] == WAVE_MAGIC[0] &&
                header[9] == WAVE_MAGIC[1] &&
                header[10] == WAVE_MAGIC[2] &&
                header[11] == WAVE_MAGIC[3];
    }

    private boolean isM4a(byte[] header) {
        // M4A: ....ftyp at bytes 4-7
        return header.length >= 8 &&
                header[4] == FTYP_MAGIC[0] &&
                header[5] == FTYP_MAGIC[1] &&
                header[6] == FTYP_MAGIC[2] &&
                header[7] == FTYP_MAGIC[3];
    }

    /**
     * Format file size for display.
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
