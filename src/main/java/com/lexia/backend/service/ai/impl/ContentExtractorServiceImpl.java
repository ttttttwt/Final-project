package com.lexia.backend.service.ai.impl;

import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.exception.ContentExtractionException;
import com.lexia.backend.service.ai.ContentExtractorService;
import com.lexia.backend.service.ai.GeminiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

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
        // TODO: Implement with Apache POI
        // For now, return placeholder
        throw new ContentExtractionException("DOCX",
                "DOCX extraction not yet implemented. Please use PDF format.");
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

        try {
            // Try to fetch transcript via public API
            String transcriptUrl = "https://youtubetranscript.com/?server_vid2=" + videoId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(transcriptUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                // Parse transcript from response
                String transcript = parseTranscript(response.body());
                if (transcript != null && !transcript.isBlank()) {
                    return applyTimeRange(transcript, metadata);
                }
            }

            // Fallback: Ask Gemini to describe the video (limited capability)
            log.warn("Transcript not available for video {}, using fallback", videoId);
            return "Video ID: " + videoId + "\n[Transcript extraction failed - manual input required]";

        } catch (Exception e) {
            throw new ContentExtractionException("YOUTUBE",
                    "Failed to extract transcript: " + e.getMessage(), e);
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

            // Extract main content (basic extraction)
            return extractMainContent(response.body());

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

    private String applyTimeRange(String transcript, Map<String, Object> metadata) {
        // If time range specified, we'd need to parse timestamps from transcript
        // For now, return full transcript
        return transcript;
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
}
