package com.lexia.backend.file.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.drew.metadata.mp3.Mp3Directory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.wav.WavDirectory;
import com.lexia.backend.file.enums.FileCategory;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service for extracting metadata from uploaded files.
 * Extracts image dimensions (width/height) and audio duration.
 */
@Service
public class FileMetadataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(FileMetadataExtractor.class);

    /**
     * Container for extracted file metadata.
     */
    @Data
    @Builder
    public static class ExtractedMetadata {
        private Integer width;
        private Integer height;
        private Integer durationSeconds;
    }

    /**
     * Extract metadata from an uploaded file based on its category.
     *
     * @param file     the uploaded file
     * @param category the file category
     * @return extracted metadata (may have null fields if extraction fails)
     */
    public ExtractedMetadata extract(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            return ExtractedMetadata.builder().build();
        }

        try {
            return switch (category) {
                case AVATAR, COURSE_THUMBNAIL, LESSON_IMAGE, CUSTOM_MATERIAL -> extractImageMetadata(file);
                case LESSON_AUDIO -> extractAudioMetadata(file);
                default -> ExtractedMetadata.builder().build();
            };
        } catch (Exception e) {
            LOG.warn("Failed to extract metadata from file: {}. Error: {}",
                    file.getOriginalFilename(), e.getMessage());
            return ExtractedMetadata.builder().build();
        }
    }

    /**
     * Extract width and height from an image file.
     */
    private ExtractedMetadata extractImageMetadata(MultipartFile file) {
        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            Integer width = null;
            Integer height = null;

            // Try JPEG directory first
            JpegDirectory jpegDir = metadata.getFirstDirectoryOfType(JpegDirectory.class);
            if (jpegDir != null) {
                width = getIntegerSafely(jpegDir, JpegDirectory.TAG_IMAGE_WIDTH);
                height = getIntegerSafely(jpegDir, JpegDirectory.TAG_IMAGE_HEIGHT);
            }

            // Try PNG directory
            if (width == null || height == null) {
                PngDirectory pngDir = metadata.getFirstDirectoryOfType(PngDirectory.class);
                if (pngDir != null) {
                    width = getIntegerSafely(pngDir, PngDirectory.TAG_IMAGE_WIDTH);
                    height = getIntegerSafely(pngDir, PngDirectory.TAG_IMAGE_HEIGHT);
                }
            }

            // Try GIF directory
            if (width == null || height == null) {
                GifHeaderDirectory gifDir = metadata.getFirstDirectoryOfType(GifHeaderDirectory.class);
                if (gifDir != null) {
                    width = getIntegerSafely(gifDir, GifHeaderDirectory.TAG_IMAGE_WIDTH);
                    height = getIntegerSafely(gifDir, GifHeaderDirectory.TAG_IMAGE_HEIGHT);
                }
            }

            // Try WebP directory
            if (width == null || height == null) {
                WebpDirectory webpDir = metadata.getFirstDirectoryOfType(WebpDirectory.class);
                if (webpDir != null) {
                    width = getIntegerSafely(webpDir, WebpDirectory.TAG_IMAGE_WIDTH);
                    height = getIntegerSafely(webpDir, WebpDirectory.TAG_IMAGE_HEIGHT);
                }
            }

            // Fallback to EXIF
            if (width == null || height == null) {
                ExifIFD0Directory exifDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (exifDir != null) {
                    if (width == null) {
                        width = getIntegerSafely(exifDir, ExifIFD0Directory.TAG_IMAGE_WIDTH);
                    }
                    if (height == null) {
                        height = getIntegerSafely(exifDir, ExifIFD0Directory.TAG_IMAGE_HEIGHT);
                    }
                }
            }

            LOG.debug("Extracted image dimensions: {}x{} from {}", width, height, file.getOriginalFilename());

            return ExtractedMetadata.builder()
                    .width(width)
                    .height(height)
                    .build();

        } catch (ImageProcessingException | IOException e) {
            LOG.warn("Failed to extract image metadata from {}: {}", file.getOriginalFilename(), e.getMessage());
            return ExtractedMetadata.builder().build();
        }
    }

    /**
     * Extract duration from an audio file.
     */
    private ExtractedMetadata extractAudioMetadata(MultipartFile file) {
        String mimeType = file.getContentType();
        if (mimeType == null) {
            return ExtractedMetadata.builder().build();
        }

        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            Integer durationSeconds = null;

            // Try MP3 directory
            if (mimeType.contains("mpeg") || mimeType.contains("mp3")) {
                Mp3Directory mp3Dir = metadata.getFirstDirectoryOfType(Mp3Directory.class);
                if (mp3Dir != null) {
                    // MP3 duration calculation from bitrate and file size
                    durationSeconds = calculateMp3Duration(file.getSize(), mp3Dir);
                }
            }

            // Try MP4/M4A directory
            if (durationSeconds == null && (mimeType.contains("mp4") || mimeType.contains("m4a"))) {
                Mp4Directory mp4Dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                if (mp4Dir != null) {
                    Long durationMs = getLongSafely(mp4Dir, Mp4Directory.TAG_DURATION);
                    if (durationMs != null) {
                        durationSeconds = (int) (durationMs / 1000);
                    }
                }
            }

            // Try WAV directory
            if (durationSeconds == null && (mimeType.contains("wav") || mimeType.contains("x-wav"))) {
                WavDirectory wavDir = metadata.getFirstDirectoryOfType(WavDirectory.class);
                if (wavDir != null) {
                    durationSeconds = calculateWavDuration(file.getSize(), wavDir);
                }
            }

            LOG.debug("Extracted audio duration: {} seconds from {}", durationSeconds, file.getOriginalFilename());

            return ExtractedMetadata.builder()
                    .durationSeconds(durationSeconds)
                    .build();

        } catch (ImageProcessingException | IOException e) {
            LOG.warn("Failed to extract audio metadata from {}: {}", file.getOriginalFilename(), e.getMessage());
            return ExtractedMetadata.builder().build();
        }
    }

    /**
     * Calculate MP3 duration from bitrate and file size.
     */
    private Integer calculateMp3Duration(long fileSize, Mp3Directory mp3Dir) {
        try {
            // Try to get bitrate
            Integer bitrate = getIntegerSafely(mp3Dir, Mp3Directory.TAG_BITRATE);
            if (bitrate != null && bitrate > 0) {
                // Duration = file size (bytes) * 8 / bitrate (kbps) / 1000
                return (int) ((fileSize * 8) / (bitrate * 1000));
            }
        } catch (Exception e) {
            LOG.debug("Could not calculate MP3 duration: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Calculate WAV duration from sample rate, channels, and bits per sample.
     */
    private Integer calculateWavDuration(long fileSize, WavDirectory wavDir) {
        try {
            Integer sampleRate = getIntegerSafely(wavDir, WavDirectory.TAG_SAMPLES_PER_SEC);
            Integer channels = getIntegerSafely(wavDir, WavDirectory.TAG_CHANNELS);
            Integer bitsPerSample = getIntegerSafely(wavDir, WavDirectory.TAG_BITS_PER_SAMPLE);

            if (sampleRate != null && channels != null && bitsPerSample != null &&
                    sampleRate > 0 && channels > 0 && bitsPerSample > 0) {
                // WAV header is typically 44 bytes
                long dataSize = fileSize - 44;
                int bytesPerSample = bitsPerSample / 8;
                int bytesPerSecond = sampleRate * channels * bytesPerSample;
                return (int) (dataSize / bytesPerSecond);
            }
        } catch (Exception e) {
            LOG.debug("Could not calculate WAV duration: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Safely get an integer value from a metadata directory.
     */
    private Integer getIntegerSafely(Directory directory, int tagType) {
        if (directory == null || !directory.containsTag(tagType)) {
            return null;
        }
        try {
            return directory.getInt(tagType);
        } catch (MetadataException e) {
            return null;
        }
    }

    /**
     * Safely get a long value from a metadata directory.
     */
    private Long getLongSafely(Directory directory, int tagType) {
        if (directory == null || !directory.containsTag(tagType)) {
            return null;
        }
        try {
            return directory.getLong(tagType);
        } catch (MetadataException e) {
            return null;
        }
    }
}
