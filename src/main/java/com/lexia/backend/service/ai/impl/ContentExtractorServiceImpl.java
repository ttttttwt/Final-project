package com.lexia.backend.service.ai.impl;

import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.exception.ContentExtractionException;
import com.lexia.backend.service.ai.ContentExtractorService;
import com.lexia.backend.service.ai.GeminiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.thoroldvix.api.TranscriptApiFactory;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.api.TranscriptContent;

import org.jsoup.Jsoup;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Implementation of ContentExtractorService handling all source types.
 * 
 * <p>
 * Extraction strategies:
 * </p>
 * <ul>
 * <li>PDF: Apache PDFBox with page range support</li>
 * <li>DOCX: Apache POI (to be added)</li>
 * <li>IMAGE: Gemini Vision API for OCR</li>
 * <li>YOUTUBE: Transcript API with fallback to audio</li>
 * <li>WEBSITE: HTML scraping with content extraction</li>
 * <li>TEXT: Direct passthrough</li>
 * </ul>
 * 
 * @since Sprint 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentExtractorServiceImpl implements ContentExtractorService {

    private static final int MAX_CONTENT_LENGTH = 50000;
    private static final Pattern YOUTUBE_VIDEO_ID = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    private final GeminiClientService geminiClient;
    private final HttpClient httpClient;

    @Override
    public String extractContent(UserCustomMaterial material) {
        if (material == null) {
            throw new ContentExtractionException("UNKNOWN", "Material is null");
        }

        CustomMaterialSourceType sourceType = material.getSourceType();
        log.info("Extracting content from {} for material {}", sourceType, material.getId());

        try {
            String content = switch (sourceType) {
                case TEXT -> extractFromText(material);
                case PDF -> extractFromPdf(material);
                case DOCX -> extractFromDocx(material);
                case IMAGE -> extractFromImage(material);
                case YOUTUBE -> extractFromYoutube(material);
                case WEBSITE -> extractFromWebsite(material);
            };

            // Truncate if too long
            if (content.length() > MAX_CONTENT_LENGTH) {
                log.warn("Content truncated from {} to {} chars for material {}",
                        content.length(), MAX_CONTENT_LENGTH, material.getId());
                content = content.substring(0, MAX_CONTENT_LENGTH);
            }

            return content;

        } catch (ContentExtractionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Content extraction failed for material {}: {}", material.getId(), e.getMessage(), e);
            throw new ContentExtractionException(sourceType.getValue(), e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(CustomMaterialSourceType sourceType) {
        return sourceType != null;
    }

    // ===== Source-specific extractors =====

    private String extractFromText(UserCustomMaterial material) {
        String text = material.getContentText();
        if (text == null || text.isBlank()) {
            throw new ContentExtractionException("TEXT", "No text content provided");
        }
        return text.trim();
    }

    private String extractFromPdf(UserCustomMaterial material) {
        String fileUrl = material.getOriginalFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ContentExtractionException("PDF", "No file URL provided");
        }

        // Get page range from metadata
        Map<String, Object> metadata = material.getInputMetadata();
        Integer pageStart = null;
        Integer pageEnd = null;
        if (metadata != null) {
            if (metadata.get("pageStart") instanceof Number start) {
                pageStart = start.intValue();
            }
            if (metadata.get("pageEnd") instanceof Number end) {
                pageEnd = end.intValue();
            }
        }

        // Build extraction prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                Extract all text content from this PDF document.

                Requirements:
                1. Maintain paragraph structure and formatting
                2. Preserve headings and lists
                3. Skip headers, footers, and page numbers
                4. Return ONLY the text content, no explanations

                """);

        if (pageStart != null && pageEnd != null) {
            prompt.append(String.format("Extract only pages %d to %d.%n", pageStart, pageEnd));
        } else if (pageStart != null) {
            prompt.append(String.format("Start from page %d.%n", pageStart));
        } else if (pageEnd != null) {
            prompt.append(String.format("Extract up to page %d.%n", pageEnd));
        }

        prompt.append("\nPDF URL: ").append(fileUrl);

        try {
            // Use Gemini multimodal to extract PDF content
            var response = geminiClient.generateContent(prompt.toString());
            String content = response.content();

            if (content == null || content.isBlank()) {
                throw new ContentExtractionException("PDF", "No text extracted from PDF");
            }

            return content.trim();
        } catch (ContentExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ContentExtractionException("PDF", "Failed to extract text: " + e.getMessage(), e);
        }
    }

    private String extractFromDocx(UserCustomMaterial material) {
        String fileUrl = material.getOriginalFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ContentExtractionException("DOCX", "No file URL provided");
        }

        log.info("Extracting DOCX content from: {}", fileUrl);

        try {
            // Download the DOCX file
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new ContentExtractionException("DOCX",
                        "Failed to download file: HTTP " + response.statusCode());
            }

            // Parse DOCX with Apache POI
            try (InputStream is = response.body();
                    XWPFDocument document = new XWPFDocument(is)) {

                List<XWPFParagraph> paragraphs = document.getParagraphs();
                if (paragraphs.isEmpty()) {
                    throw new ContentExtractionException("DOCX", "No content found in document");
                }

                // Get page range from metadata (approximate by paragraph chunks)
                Map<String, Object> metadata = material.getInputMetadata();
                int startPara = 0;
                int endPara = paragraphs.size();

                if (metadata != null) {
                    // Approximate: ~10 paragraphs per page
                    int parasPerPage = 10;
                    if (metadata.get("pageStart") instanceof Number start) {
                        startPara = Math.max(0, (start.intValue() - 1) * parasPerPage);
                    }
                    if (metadata.get("pageEnd") instanceof Number end) {
                        endPara = Math.min(paragraphs.size(), end.intValue() * parasPerPage);
                    }
                }

                // Extract text from paragraphs
                StringBuilder content = new StringBuilder();
                for (int i = startPara; i < endPara && i < paragraphs.size(); i++) {
                    String text = paragraphs.get(i).getText();
                    if (text != null && !text.isBlank()) {
                        content.append(text.trim()).append("\n\n");
                    }
                }

                String result = content.toString().trim();
                if (result.isEmpty()) {
                    throw new ContentExtractionException("DOCX", "No text extracted from document");
                }

                log.info("Extracted {} characters from DOCX", result.length());
                return result;
            }

        } catch (ContentExtractionException e) {
            throw e;
        } catch (IOException e) {
            throw new ContentExtractionException("DOCX", "Failed to read document: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ContentExtractionException("DOCX", "Failed to extract text: " + e.getMessage(), e);
        }
    }

    private String extractFromImage(UserCustomMaterial material) {
        String fileUrl = material.getOriginalFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ContentExtractionException("IMAGE", "No file URL provided");
        }

        // Use Gemini Vision for OCR
        String ocrPrompt = """
                Extract all text from this image. If the image contains a document,
                extract the text maintaining paragraph structure. If it contains
                handwritten notes, do your best to interpret them.

                Return ONLY the extracted text, no explanations.
                """;

        try {
            var response = geminiClient.generateContent(ocrPrompt + "\n\nImage URL: " + fileUrl);
            return response.content();
        } catch (Exception e) {
            throw new ContentExtractionException("IMAGE", "OCR failed: " + e.getMessage(), e);
        }
    }

    private String extractFromYoutube(UserCustomMaterial material) {
        Map<String, Object> metadata = material.getInputMetadata();
        String sourceUrl = metadata != null ? (String) metadata.get("sourceUrl") : null;

        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new ContentExtractionException("YOUTUBE", "No YouTube URL provided");
        }

        // Extract video ID
        Matcher matcher = YOUTUBE_VIDEO_ID.matcher(sourceUrl);
        if (!matcher.find()) {
            throw new ContentExtractionException("YOUTUBE", "Invalid YouTube URL format");
        }
        String videoId = matcher.group(1);

        // Update material title from YouTube metadata
        updateMaterialTitleFromYoutube(material, videoId);

        try {
            // Tier 1: Primary transcript API (YouTube Transcript API)
            String transcript = fetchTranscriptTier1(videoId);
            if (transcript != null && !transcript.isBlank()) {
                log.info("Got transcript from Tier 1 (YouTube API) for video {}", videoId);
                return applyTimeRange(transcript, metadata);
            }

            // Tier 2: Gemini AI Transcript Generation
            log.warn("Tier 1 failed for video {}, trying Tier 2 (Gemini Transcript Generation)", videoId);
            transcript = generateTranscriptWithGemini(videoId);
            if (transcript != null && !transcript.isBlank()) {
                log.info("Got transcript from Tier 2 (Gemini) for video {}", videoId);
                return applyTimeRange(transcript, metadata);
            }

            // Tier 3: Metadata Fallback (Gemini AI)
            log.warn("Tier 2 failed for video {}, using metadata fallback", videoId);
            return generateContentFromMetadata(videoId);

        } catch (ContentExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ContentExtractionException("YOUTUBE",
                    "Failed to extract content: " + e.getMessage(), e);
        }
    }

    /**
     * Tier 1: Primary transcript API (using youtube-transcript-api library)
     */
    private String fetchTranscriptTier1(String videoId) {
        try {
            log.info("Tier 1: Fetching transcript for video {} using youtube-transcript-api", videoId);

            YoutubeTranscriptApi youtubeTranscriptApi = TranscriptApiFactory.createDefault();
            TranscriptContent transcriptContent = youtubeTranscriptApi.getTranscript(videoId);

            if (transcriptContent != null && transcriptContent.getContent() != null) {
                StringBuilder sb = new StringBuilder();
                for (var fragment : transcriptContent.getContent()) {
                    sb.append(fragment.getText()).append(" ");
                }
                String content = sb.toString().trim();

                log.info("Tier 1: Successfully fetched transcript. Length: {} chars", content.length());
                
                // Log preview of content as requested
                if (content.length() > 0) {
                    log.info("Tier 1 Content Preview: {}", content.substring(0, Math.min(content.length(), 500)));
                }
                
                return content;
            }
        } catch (Exception e) {
            log.warn("Tier 1: youtube-transcript-api failed for video {}: {}", videoId, e.getMessage());
        }

        // Fallback to YouTube's internal timedtext API
        return fetchTranscriptAlternative(videoId);
    }

    /**
     * Tier 2: Use Gemini to generate a transcript for the video
     */
    private String generateTranscriptWithGemini(String videoId) {
        try {
            // Get video metadata first to help Gemini
            String oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="
                    + videoId + "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oEmbedUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            String title = "Unknown Video";
            String author = "Unknown Channel";
            if (response.statusCode() == 200) {
                String body = response.body();
                title = Optional.ofNullable(extractJsonField(body, "title")).orElse(title);
                author = Optional.ofNullable(extractJsonField(body, "author_name")).orElse(author);
            }

            String prompt = String.format("""
                    You are an expert at transcribing and summarizing YouTube videos for English learners.

                    Video Information:
                    - Title: %s
                    - Channel: %s
                    - URL: https://www.youtube.com/watch?v=%s

                    Task:
                    Generate a detailed transcript for this video. If you cannot access the video content directly,
                    provide a highly detailed reconstruction of the content based on your knowledge of this video
                    or its likely content.

                    Requirements:
                    1. Provide the full spoken content (or a very detailed summary if full transcript is impossible).
                    2. Include timestamps in [MM:SS] format at the beginning of each major section or paragraph.
                    3. Maintain the flow of the original video.
                    4. Return ONLY the transcript text with timestamps.

                    Format:
                    [00:00] Introduction...
                    [01:30] Main point...
                    """, title, author, videoId);

            var geminiResponse = geminiClient.generateContent(prompt);
            String content = geminiResponse.content();

            if (content != null && !content.isBlank()) {
                log.info("Tier 2: Successfully generated transcript with Gemini. Length: {} chars", content.length());
                if (content.length() > 0) {
                    log.info("Tier 2 Content Preview: {}", content.substring(0, Math.min(content.length(), 500)));
                }
                return "[Transcript generated by Gemini AI]\n\n" + content;
            }
            return null;
        } catch (Exception e) {
            log.error("Tier 2 Gemini transcript generation failed for video {}: {}", videoId, e.getMessage());
            return null;
        }
    }

    /**
     * Parse JSON transcript format: [{"text": "...", "start": 0.0, "duration": 1.0}, ...]
     */
    private String parseJsonTranscript(String json) {
        try {
            StringBuilder transcript = new StringBuilder();
            // Simple regex-based parsing to avoid adding Jackson dependency if not needed
            // Pattern matches "text": "content"
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                String text = matcher.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\n", " ")
                        .trim();
                if (!text.isBlank()) {
                    if (!transcript.isEmpty()) {
                        transcript.append(" ");
                    }
                    transcript.append(text);
                }
            }
            return transcript.isEmpty() ? null : transcript.toString();
        } catch (Exception e) {
            log.debug("Failed to parse JSON transcript: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tier 2: Alternative transcript source - try YouTube's internal API
     */
    private String fetchTranscriptAlternative(String videoId) {
        try {
            // Try YouTube's oEmbed to get basic info first
            String oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="
                    + videoId + "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oEmbedUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // If oEmbed works, the video exists - try alternative transcript format
            if (response.statusCode() == 200) {
                // Try a different transcript endpoint format
                String altUrl = "https://video.google.com/timedtext?lang=en&v=" + videoId;

                HttpRequest altRequest = HttpRequest.newBuilder()
                        .uri(URI.create(altUrl))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> altResponse = httpClient.send(altRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (altResponse.statusCode() == 200 && !altResponse.body().isEmpty()
                        && altResponse.body().contains("<text")) {
                    String content = parseTimedText(altResponse.body());
                    if (content != null) {
                        log.info("Tier 1 Fallback: Successfully fetched transcript from timedtext API. Length: {} chars", content.length());
                        if (content.length() > 0) {
                            log.info("Tier 1 Fallback Content Preview: {}", content.substring(0, Math.min(content.length(), 500)));
                        }
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Alternative transcript API failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Parse timedtext XML format from YouTube
     */
    private String parseTimedText(String xml) {
        try {
            // Simple parsing of <text start="..." dur="...">content</text>
            StringBuilder transcript = new StringBuilder();
            Pattern textPattern = Pattern.compile("<text[^>]*>([^<]*)</text>");
            Matcher m = textPattern.matcher(xml);

            while (m.find()) {
                String text = m.group(1)
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .trim();
                if (!text.isBlank()) {
                    if (!transcript.isEmpty()) {
                        transcript.append(" ");
                    }
                    transcript.append(text);
                }
            }

            return transcript.isEmpty() ? null : transcript.toString();
        } catch (Exception e) {
            log.debug("Failed to parse timedtext XML: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tier 3: Generate learning content from video metadata using Gemini
     */
    private String generateContentFromMetadata(String videoId) {
        try {
            // Get video metadata from oEmbed
            String oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="
                    + videoId + "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oEmbedUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ContentExtractionException("YOUTUBE",
                        "Could not fetch video metadata");
            }

            // Parse oEmbed JSON for title and channel
            String body = response.body();
            String title = extractJsonField(body, "title");
            String author = extractJsonField(body, "author_name");

            if (title == null || title.isBlank()) {
                throw new ContentExtractionException("YOUTUBE",
                        "Could not extract video title");
            }

            // Use Gemini to generate learning content based on metadata
            String prompt = String.format("""
                    You are helping create English learning content based on a YouTube video.

                    Video Information:
                    - Title: %s
                    - Channel: %s
                    - Video URL: https://www.youtube.com/watch?v=%s

                    Since no transcript is available, create educational content based on what the video
                    is likely about, given its title and channel. Generate:

                    1. A brief summary of the likely topic (100-150 words)
                    2. 10 vocabulary words related to this topic with definitions
                    3. 5 discussion questions for English practice

                    Format as plain text with clear sections.

                    Note: Mark this as "Content generated from video metadata (transcript unavailable)"
                    """, title, author != null ? author : "Unknown", videoId);

            var geminiResponse = geminiClient.generateContent(prompt);
            String generated = geminiResponse.content();

            if (generated != null && !generated.isBlank()) {
                log.info("Tier 3: Successfully generated content from metadata for video {}", videoId);
                if (generated.length() > 0) {
                    log.info("Tier 3 Content Preview: {}", generated.substring(0, Math.min(generated.length(), 500)));
                }
                return "[Content generated from video metadata - transcript unavailable]\n\n"
                        + "Video: " + title + "\n"
                        + "Channel: " + (author != null ? author : "Unknown") + "\n\n"
                        + generated;
            }

            throw new ContentExtractionException("YOUTUBE",
                    "Failed to generate content from video metadata");

        } catch (ContentExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ContentExtractionException("YOUTUBE",
                    "Fallback content generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Simple JSON field extraction (avoids dependency on Jackson in this context)
     */
    private String extractJsonField(String json, String field) {
        try {
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
            Matcher m = Pattern.compile(pattern).matcher(json);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractFromWebsite(UserCustomMaterial material) {
        Map<String, Object> metadata = material.getInputMetadata();
        String sourceUrl = metadata != null ? (String) metadata.get("sourceUrl") : null;

        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new ContentExtractionException("WEBSITE", "No URL provided");
        }

        // Domain allowlist check (basic SSRF protection)
        if (!isAllowedDomain(sourceUrl)) {
            throw new ContentExtractionException("WEBSITE",
                    "Domain not allowed. Please use a public article URL.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sourceUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla/5.0 (compatible; LexiaBot/1.0)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ContentExtractionException("WEBSITE",
                        "Failed to fetch page: HTTP " + response.statusCode());
            }

            String html = response.body();
            
            // Update material title from website <title> tag
            updateMaterialTitleFromWebsite(material, html);

            // Extract main content (basic extraction)
            return extractMainContent(html);

        } catch (ContentExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ContentExtractionException("WEBSITE",
                    "Failed to fetch content: " + e.getMessage(), e);
        }
    }

    // ===== Helper methods =====

    private String parseTranscript(String response) {
        // Basic transcript parsing - would need to be adjusted based on actual API
        // response
        // For now, return the response as-is, cleaned up
        return response
                .replaceAll("<[^>]+>", " ") // Remove HTML tags
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    /**
     * Applies time range filter to transcript if timeStart/timeEnd are specified.
     * Attempts to parse timestamp patterns like [00:00], (0:00), or 0:00 - text
     * format.
     */
    private String applyTimeRange(String transcript, Map<String, Object> metadata) {
        if (metadata == null) {
            return transcript;
        }

        Integer timeStart = null;
        Integer timeEnd = null;

        if (metadata.get("timeStart") instanceof Number start) {
            timeStart = start.intValue();
        }
        if (metadata.get("timeEnd") instanceof Number end) {
            timeEnd = end.intValue();
        }

        // If no range specified, return full transcript
        if (timeStart == null && timeEnd == null) {
            return transcript;
        }

        // Default values if only one is specified
        if (timeStart == null)
            timeStart = 0;
        if (timeEnd == null)
            timeEnd = Integer.MAX_VALUE;

        // Try to parse and filter timestamped content
        // Common patterns: [00:00], (0:00), 00:00 - text, or newline-separated with
        // timestamps
        StringBuilder filtered = new StringBuilder();

        // Pattern for timestamps: [HH:MM:SS] or [MM:SS] or (HH:MM:SS) or MM:SS at line
        // start
        Pattern timestampPattern = Pattern.compile(
                "(?:\\[|\\()?(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})(?:\\]|\\))?\\s*[-–:]?\\s*([^\\[\\(\\n]+)");

        Matcher matcher = timestampPattern.matcher(transcript);
        boolean hasTimestamps = false;

        while (matcher.find()) {
            hasTimestamps = true;
            int hours = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
            int minutes = Integer.parseInt(matcher.group(2));
            int secs = Integer.parseInt(matcher.group(3));
            int totalSeconds = hours * 3600 + minutes * 60 + secs;
            String text = matcher.group(4).trim();

            // Check if within range
            if (totalSeconds >= timeStart && totalSeconds <= timeEnd) {
                if (!filtered.isEmpty()) {
                    filtered.append(" ");
                }
                filtered.append(text);
            }
        }

        // If we successfully parsed timestamps, return filtered content
        if (hasTimestamps && !filtered.isEmpty()) {
            log.info("Filtered transcript from {}s to {}s, result length: {} chars",
                    timeStart, timeEnd, filtered.length());
            return filtered.toString();
        }

        // Fallback: if no timestamps found, estimate by position
        // Assuming ~150 words per minute speaking rate
        log.warn("No timestamps found in transcript, using position-based estimate");
        String[] words = transcript.split("\\s+");
        int wordsPerSecond = 3; // ~180 wpm
        int startWord = timeStart * wordsPerSecond;
        int endWord = Math.min(timeEnd * wordsPerSecond, words.length);

        if (startWord >= words.length) {
            return transcript; // Range beyond content, return full
        }

        StringBuilder result = new StringBuilder();
        for (int i = Math.max(0, startWord); i < endWord && i < words.length; i++) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(words[i]);
        }

        return result.isEmpty() ? transcript : result.toString();
    }

    private boolean isAllowedDomain(String url) {
        // Basic allowlist - production would have comprehensive list
        String[] allowedPatterns = {
                "medium.com", "dev.to", "hashnode.dev",
                "wikipedia.org", "bbc.com", "bbc.co.uk",
                "theguardian.com", "nytimes.com", "cnn.com",
                "techcrunch.com", "wired.com", "arstechnica.com",
                "github.com", "stackoverflow.com",
                "docs.google.com", "notion.so"
        };

        String lowerUrl = url.toLowerCase();
        for (String pattern : allowedPatterns) {
            if (lowerUrl.contains(pattern)) {
                return true;
            }
        }

        // Block internal IPs and localhost
        if (lowerUrl.contains("localhost") || lowerUrl.contains("127.0.0.1")
                || lowerUrl.contains("0.0.0.0") || lowerUrl.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            return false;
        }

        // Allow HTTPS URLs by default (with caution notice in logs)
        if (url.startsWith("https://")) {
            log.warn("Allowing unlisted domain: {}", url);
            return true;
        }

        return false;
    }

    private String extractMainContent(String html) {
        // Basic content extraction - removes scripts, styles, and extracts text
        // Production would use a library like Jsoup with readability algorithm

        String content = html
                .replaceAll("(?s)<script[^>]*>.*?</script>", "") // Remove scripts
                .replaceAll("(?s)<style[^>]*>.*?</style>", "") // Remove styles
                .replaceAll("(?s)<nav[^>]*>.*?</nav>", "") // Remove navigation
                .replaceAll("(?s)<footer[^>]*>.*?</footer>", "") // Remove footer
                .replaceAll("(?s)<header[^>]*>.*?</header>", "") // Remove header
                .replaceAll("<[^>]+>", " ") // Remove remaining HTML
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();

        return content;
    }

    /**
     * Updates the material title using YouTube oEmbed API.
     */
    private void updateMaterialTitleFromYoutube(UserCustomMaterial material, String videoId) {
        try {
            String oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="
                    + videoId + "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oEmbedUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String title = extractJsonField(response.body(), "title");
                if (title != null && !title.isBlank()) {
                    log.info("Updating material {} title to: {}", material.getId(), title);
                    material.setTitle(title);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch YouTube title for material {}: {}", material.getId(), e.getMessage());
        }
    }

    /**
     * Updates the material title using the <title> tag from HTML.
     */
    private void updateMaterialTitleFromWebsite(UserCustomMaterial material, String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            String title = doc.title();
            if (title != null && !title.isBlank()) {
                log.info("Updating material {} title to: {}", material.getId(), title);
                material.setTitle(title);
            }
        } catch (Exception e) {
            log.warn("Failed to extract website title for material {}: {}", material.getId(), e.getMessage());
        }
    }
}
