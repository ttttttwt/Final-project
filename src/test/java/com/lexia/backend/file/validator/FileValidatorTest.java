package com.lexia.backend.file.validator;

import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.exception.FileValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileValidator.
 * Tests MIME type validation, file size validation, magic byte validation, and
 * filename sanitization.
 */
@DisplayName("FileValidator Tests")
class FileValidatorTest {

    private FileValidator fileValidator;

    // Magic bytes for test files
    private static final byte[] JPEG_MAGIC_BYTES = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
    private static final byte[] PNG_MAGIC_BYTES = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] GIF_MAGIC_BYTES = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
    private static final byte[] PDF_MAGIC_BYTES = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D };
    private static final byte[] MP3_ID3_MAGIC_BYTES = new byte[] { 0x49, 0x44, 0x33 };

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
    }

    @Nested
    @DisplayName("Empty File Validation")
    class EmptyFileValidation {

        @Test
        @DisplayName("Should reject null file")
        void shouldRejectNullFile() {
            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(null, FileCategory.AVATAR));
            assertEquals("File is empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject empty file")
        void shouldRejectEmptyFile() {
            MultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);

            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(emptyFile, FileCategory.AVATAR));
            assertEquals("File is empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("MIME Type Validation")
    class MimeTypeValidation {

        @Test
        @DisplayName("Should accept valid JPEG for avatar")
        void shouldAcceptValidJpegForAvatar() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should accept valid PNG for avatar")
        void shouldAcceptValidPngForAvatar() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.png", "image/png", PNG_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should accept valid GIF for avatar")
        void shouldAcceptValidGifForAvatar() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.gif", "image/gif", GIF_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should reject PDF for avatar")
        void shouldRejectPdfForAvatar() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", PDF_MAGIC_BYTES);

            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(file, FileCategory.AVATAR));
            assertTrue(exception.getMessage().contains("not allowed"));
        }

        @Test
        @DisplayName("Should accept PDF for document category")
        void shouldAcceptPdfForDocument() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", PDF_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.DOCUMENT));
        }

        @Test
        @DisplayName("Should reject executable file")
        void shouldRejectExecutableFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "virus.exe", "application/octet-stream", new byte[] { 0x4D, 0x5A });

            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(file, FileCategory.AVATAR));
            assertTrue(exception.getMessage().contains("not allowed"));
        }

        @Test
        @DisplayName("Should reject null MIME type")
        void shouldRejectNullMimeType() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "unknown", null, new byte[10]);

            assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should accept MP3 for lesson audio")
        void shouldAcceptMp3ForLessonAudio() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "lesson.mp3", "audio/mpeg", MP3_ID3_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.LESSON_AUDIO));
        }
    }

    @Nested
    @DisplayName("File Size Validation")
    class FileSizeValidation {

        @Test
        @DisplayName("Should reject oversized avatar file")
        void shouldRejectOversizedAvatarFile() {
            // Create a file larger than 5MB limit for avatars
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            System.arraycopy(JPEG_MAGIC_BYTES, 0, largeContent, 0, JPEG_MAGIC_BYTES.length);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "large_avatar.jpg", "image/jpeg", largeContent);

            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(file, FileCategory.AVATAR));
            assertTrue(exception.getMessage().contains("exceeds maximum"));
        }

        @Test
        @DisplayName("Should accept avatar file within size limit")
        void shouldAcceptAvatarWithinSizeLimit() {
            // Create a 1MB file (within 5MB limit)
            byte[] content = new byte[1024 * 1024]; // 1MB
            System.arraycopy(JPEG_MAGIC_BYTES, 0, content, 0, JPEG_MAGIC_BYTES.length);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", content);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should allow larger files for audio category")
        void shouldAllowLargerFilesForAudio() {
            // Create a 10MB file (within 50MB limit for audio)
            byte[] content = new byte[10 * 1024 * 1024]; // 10MB
            System.arraycopy(MP3_ID3_MAGIC_BYTES, 0, content, 0, MP3_ID3_MAGIC_BYTES.length);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "lesson.mp3", "audio/mpeg", content);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.LESSON_AUDIO));
        }
    }

    @Nested
    @DisplayName("Magic Byte Validation")
    class MagicByteValidation {

        @Test
        @DisplayName("Should reject MIME type spoofing (exe disguised as jpeg)")
        void shouldRejectMimeTypeSpoofing() {
            // File claims to be JPEG but has EXE magic bytes
            byte[] exeMagicBytes = new byte[] { 0x4D, 0x5A }; // MZ header for EXE
            MockMultipartFile file = new MockMultipartFile(
                    "file", "fake.jpg", "image/jpeg", exeMagicBytes);

            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.validate(file, FileCategory.AVATAR));
            assertTrue(exception.getMessage().contains("does not match") ||
                    exception.getMessage().contains("spoofing"));
        }

        @Test
        @DisplayName("Should accept file with valid magic bytes")
        void shouldAcceptFileWithValidMagicBytes() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "real.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }

        @Test
        @DisplayName("Should accept PNG with valid magic bytes")
        void shouldAcceptPngWithValidMagicBytes() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "image.png", "image/png", PNG_MAGIC_BYTES);

            assertDoesNotThrow(() -> fileValidator.validate(file, FileCategory.AVATAR));
        }
    }

    @Nested
    @DisplayName("Filename Sanitization")
    class FilenameSanitization {

        @Test
        @DisplayName("Should reject filename with path traversal")
        void shouldRejectFilenameWithPathTraversal() {
            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.sanitizeFilename("../../../etc/passwd"));
            assertTrue(exception.getMessage().contains("path traversal"));
        }

        @Test
        @DisplayName("Should remove path separators from filename")
        void shouldRemovePathSeparators() {
            String sanitized = fileValidator.sanitizeFilename("path/to/file.jpg");
            assertFalse(sanitized.contains("/"));
            assertFalse(sanitized.contains("\\"));
        }

        @Test
        @DisplayName("Should reject null filename")
        void shouldRejectNullFilename() {
            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.sanitizeFilename(null));
            assertEquals("Filename cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject blank filename")
        void shouldRejectBlankFilename() {
            FileValidationException exception = assertThrows(
                    FileValidationException.class,
                    () -> fileValidator.sanitizeFilename("   "));
            assertEquals("Filename cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should truncate long filename")
        void shouldTruncateLongFilename() {
            String longFilename = "a".repeat(300) + ".jpg";
            String sanitized = fileValidator.sanitizeFilename(longFilename);
            assertTrue(sanitized.length() <= 255);
        }

        @Test
        @DisplayName("Should keep valid filename unchanged")
        void shouldKeepValidFilenameUnchanged() {
            String filename = "my_avatar_2025.jpg";
            String sanitized = fileValidator.sanitizeFilename(filename);
            assertEquals(filename, sanitized);
        }
    }

    @Nested
    @DisplayName("Category Configuration")
    class CategoryConfiguration {

        @Test
        @DisplayName("Should return correct allowed MIME types for avatar")
        void shouldReturnCorrectMimeTypesForAvatar() {
            Set<String> allowedTypes = fileValidator.getAllowedMimeTypes(FileCategory.AVATAR);

            assertTrue(allowedTypes.contains("image/jpeg"));
            assertTrue(allowedTypes.contains("image/png"));
            assertTrue(allowedTypes.contains("image/gif"));
            assertTrue(allowedTypes.contains("image/webp"));
            assertFalse(allowedTypes.contains("application/pdf"));
        }

        @Test
        @DisplayName("Should return correct max size for avatar")
        void shouldReturnCorrectMaxSizeForAvatar() {
            long maxSize = fileValidator.getMaxFileSize(FileCategory.AVATAR);
            assertEquals(5L * 1024 * 1024, maxSize); // 5MB
        }

        @Test
        @DisplayName("Should return correct max size for lesson audio")
        void shouldReturnCorrectMaxSizeForLessonAudio() {
            long maxSize = fileValidator.getMaxFileSize(FileCategory.LESSON_AUDIO);
            assertEquals(50L * 1024 * 1024, maxSize); // 50MB
        }

        @Test
        @DisplayName("Should return correct allowed MIME types for lesson audio")
        void shouldReturnCorrectMimeTypesForLessonAudio() {
            Set<String> allowedTypes = fileValidator.getAllowedMimeTypes(FileCategory.LESSON_AUDIO);

            assertTrue(allowedTypes.contains("audio/mpeg"));
            assertTrue(allowedTypes.contains("audio/wav"));
            assertTrue(allowedTypes.contains("audio/ogg"));
            assertTrue(allowedTypes.contains("audio/mp4"));
        }
    }

    @Nested
    @DisplayName("Format Size Utility")
    class FormatSizeUtility {

        @Test
        @DisplayName("Should format bytes correctly")
        void shouldFormatBytesCorrectly() {
            assertEquals("500 B", FileValidator.formatSize(500));
        }

        @Test
        @DisplayName("Should format KB correctly")
        void shouldFormatKBCorrectly() {
            String formatted = FileValidator.formatSize(2048);
            assertTrue(formatted.contains("KB"));
        }

        @Test
        @DisplayName("Should format MB correctly")
        void shouldFormatMBCorrectly() {
            String formatted = FileValidator.formatSize(5 * 1024 * 1024);
            assertTrue(formatted.contains("MB"));
        }

        @Test
        @DisplayName("Should format GB correctly")
        void shouldFormatGBCorrectly() {
            String formatted = FileValidator.formatSize(2L * 1024 * 1024 * 1024);
            assertTrue(formatted.contains("GB"));
        }
    }
}
