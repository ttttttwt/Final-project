package com.lexia.backend.file.service;

import com.lexia.backend.file.enums.FileCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileMetadataExtractor.
 */
@DisplayName("FileMetadataExtractor Tests")
class FileMetadataExtractorTest {

    private FileMetadataExtractor metadataExtractor;

    // Valid JPEG magic bytes with some image data
    private static final byte[] JPEG_BYTES = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
    };

    // Valid PNG magic bytes
    private static final byte[] PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    // MP3 with ID3 tag
    private static final byte[] MP3_ID3_BYTES = new byte[] {
            0x49, 0x44, 0x33, 0x04, 0x00, 0x00, 0x00, 0x00
    };

    @BeforeEach
    void setUp() {
        metadataExtractor = new FileMetadataExtractor();
    }

    @Nested
    @DisplayName("Extract Metadata Tests")
    class ExtractMetadataTests {

        @Test
        @DisplayName("Should return empty metadata for null file")
        void shouldReturnEmptyMetadataForNullFile() {
            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(null, FileCategory.AVATAR);

            assertNotNull(result);
            assertNull(result.getWidth());
            assertNull(result.getHeight());
            assertNull(result.getDurationSeconds());
        }

        @Test
        @DisplayName("Should return empty metadata for empty file")
        void shouldReturnEmptyMetadataForEmptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.AVATAR);

            assertNotNull(result);
            assertNull(result.getWidth());
            assertNull(result.getHeight());
            assertNull(result.getDurationSeconds());
        }

        @Test
        @DisplayName("Should return empty metadata for non-media categories")
        void shouldReturnEmptyMetadataForNonMediaCategories() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", new byte[] { 0x25, 0x50, 0x44, 0x46 });

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.DOCUMENT);

            assertNotNull(result);
            assertNull(result.getWidth());
            assertNull(result.getHeight());
            assertNull(result.getDurationSeconds());
        }

        @Test
        @DisplayName("Should handle invalid image data gracefully")
        void shouldHandleInvalidImageDataGracefully() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "corrupted.jpg", "image/jpeg", new byte[] { 0x00, 0x01, 0x02, 0x03 });

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.AVATAR);

            // Should not throw exception, just return empty metadata
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle invalid audio data gracefully")
        void shouldHandleInvalidAudioDataGracefully() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "corrupted.mp3", "audio/mpeg", new byte[] { 0x00, 0x01, 0x02, 0x03 });

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.LESSON_AUDIO);

            // Should not throw exception, just return empty metadata
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Image Metadata Extraction Tests")
    class ImageMetadataTests {

        @Test
        @DisplayName("Should attempt to extract image metadata for AVATAR category")
        void shouldAttemptToExtractImageMetadataForAvatar() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_BYTES);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.AVATAR);

            // Extraction may or may not succeed depending on actual image data
            // But should not throw exception
            assertNotNull(result);
            assertNull(result.getDurationSeconds()); // Image should not have duration
        }

        @Test
        @DisplayName("Should attempt to extract image metadata for COURSE_THUMBNAIL category")
        void shouldAttemptToExtractImageMetadataForCourseThumbnail() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "thumbnail.png", "image/png", PNG_BYTES);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file,
                    FileCategory.COURSE_THUMBNAIL);

            assertNotNull(result);
            assertNull(result.getDurationSeconds());
        }

        @Test
        @DisplayName("Should attempt to extract image metadata for LESSON_IMAGE category")
        void shouldAttemptToExtractImageMetadataForLessonImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "lesson.jpg", "image/jpeg", JPEG_BYTES);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.LESSON_IMAGE);

            assertNotNull(result);
            assertNull(result.getDurationSeconds());
        }
    }

    @Nested
    @DisplayName("Audio Metadata Extraction Tests")
    class AudioMetadataTests {

        @Test
        @DisplayName("Should attempt to extract audio metadata for LESSON_AUDIO category")
        void shouldAttemptToExtractAudioMetadataForLessonAudio() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.mp3", "audio/mpeg", MP3_ID3_BYTES);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.LESSON_AUDIO);

            assertNotNull(result);
            // Width/height should be null for audio
            assertNull(result.getWidth());
            assertNull(result.getHeight());
        }

        @Test
        @DisplayName("Should handle WAV files")
        void shouldHandleWavFiles() {
            // WAV with RIFF header
            byte[] wavBytes = new byte[] {
                    0x52, 0x49, 0x46, 0x46, // RIFF
                    0x00, 0x00, 0x00, 0x00, // size
                    0x57, 0x41, 0x56, 0x45 // WAVE
            };

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.wav", "audio/wav", wavBytes);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.LESSON_AUDIO);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle M4A files")
        void shouldHandleM4aFiles() {
            // M4A with ftyp header
            byte[] m4aBytes = new byte[] {
                    0x00, 0x00, 0x00, 0x00, // size
                    0x66, 0x74, 0x79, 0x70 // ftyp
            };

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.m4a", "audio/mp4", m4aBytes);

            FileMetadataExtractor.ExtractedMetadata result = metadataExtractor.extract(file, FileCategory.LESSON_AUDIO);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("ExtractedMetadata Builder Tests")
    class ExtractedMetadataBuilderTests {

        @Test
        @DisplayName("Should build metadata with all fields")
        void shouldBuildMetadataWithAllFields() {
            FileMetadataExtractor.ExtractedMetadata metadata = FileMetadataExtractor.ExtractedMetadata.builder()
                    .width(1920)
                    .height(1080)
                    .durationSeconds(300)
                    .build();

            assertEquals(1920, metadata.getWidth());
            assertEquals(1080, metadata.getHeight());
            assertEquals(300, metadata.getDurationSeconds());
        }

        @Test
        @DisplayName("Should build empty metadata")
        void shouldBuildEmptyMetadata() {
            FileMetadataExtractor.ExtractedMetadata metadata = FileMetadataExtractor.ExtractedMetadata.builder()
                    .build();

            assertNull(metadata.getWidth());
            assertNull(metadata.getHeight());
            assertNull(metadata.getDurationSeconds());
        }
    }
}
