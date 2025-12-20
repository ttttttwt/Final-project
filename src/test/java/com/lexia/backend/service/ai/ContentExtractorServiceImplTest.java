package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.GeminiResponseDTO;
import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.exception.ContentExtractionException;
import com.lexia.backend.service.ai.impl.ContentExtractorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentExtractorServiceImplTest {

    @Mock
    private GeminiClientService geminiClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private ContentExtractorServiceImpl extractorService;

    private UserCustomMaterial material;

    @BeforeEach
    void setUp() {
        material = UserCustomMaterial.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .title("Test Material")
                .build();
    }

    @Nested
    class TextSourceTests {
        @Test
        void extractContent_WithValidText_ReturnsText() {
            material.setSourceType(CustomMaterialSourceType.TEXT);
            material.setContentText("Direct text content");

            String result = extractorService.extractContent(material);

            assertEquals("Direct text content", result);
        }

        @Test
        void extractContent_WithEmptyText_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.TEXT);
            material.setContentText("");

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }

        @Test
        void extractContent_WithNullText_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.TEXT);
            material.setContentText(null);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }
    }

    @Nested
    class PdfSourceTests {
        @Test
        void extractContent_WithValidPdf_CallsGemini() {
            material.setSourceType(CustomMaterialSourceType.PDF);
            material.setOriginalFileUrl("https://example.com/file.pdf");

            GeminiResponseDTO mockResponse = new GeminiResponseDTO(
                    "Extracted PDF content", "model", null, Instant.now(), 100, false, "STOP");
            when(geminiClient.generateContent(anyString())).thenReturn(mockResponse);

            String result = extractorService.extractContent(material);

            assertEquals("Extracted PDF content", result);
            verify(geminiClient).generateContent(contains("PDF URL: https://example.com/file.pdf"));
        }

        @Test
        void extractContent_WithPageRange_IncludesInstructions() {
            material.setSourceType(CustomMaterialSourceType.PDF);
            material.setOriginalFileUrl("https://example.com/file.pdf");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("pageStart", 5);
            metadata.put("pageEnd", 10);
            material.setInputMetadata(metadata);

            GeminiResponseDTO mockResponse = new GeminiResponseDTO(
                    "Content", "model", null, Instant.now(), 100, false, "STOP");
            when(geminiClient.generateContent(anyString())).thenReturn(mockResponse);

            extractorService.extractContent(material);

            verify(geminiClient).generateContent(contains("Extract only pages 5 to 10"));
        }

        @Test
        void extractContent_NoFileUrl_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.PDF);
            material.setOriginalFileUrl(null);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }
    }

    @Nested
    class ImageSourceTests {
        @Test
        void extractContent_WithValidImage_CallsGeminiVision() {
            material.setSourceType(CustomMaterialSourceType.IMAGE);
            material.setOriginalFileUrl("https://example.com/image.png");

            GeminiResponseDTO mockResponse = new GeminiResponseDTO(
                    "Extracted Image Text", "model", null, Instant.now(), 100, false, "STOP");
            when(geminiClient.generateContent(anyString())).thenReturn(mockResponse);

            String result = extractorService.extractContent(material);

            assertEquals("Extracted Image Text", result);
            verify(geminiClient).generateContent(contains("Image URL: https://example.com/image.png"));
        }

        @Test
        void extractContent_NoFileUrl_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.IMAGE);
            material.setOriginalFileUrl(null);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }
    }

    @Nested
    class YoutubeSourceTests {
        @Test
        void extractContent_WithValidYoutubeUrl_ExtractsTranscript() throws Exception {
            material.setSourceType(CustomMaterialSourceType.YOUTUBE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "https://youtube.com/watch?v=ABCDEFGHIJK");
            material.setInputMetadata(metadata);

            when(httpClient.send(any(java.net.http.HttpRequest.class),
                    any(java.net.http.HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("<text>Transcript content</text>");

            String result = extractorService.extractContent(material);

            assertEquals("Transcript content", result);
        }

        @Test
        void extractContent_WithInvalidUrl_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.YOUTUBE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "invalid-url");
            material.setInputMetadata(metadata);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }

        @Test
        void extractContent_ApiFailure_ReturnsFallback() throws Exception {
            material.setSourceType(CustomMaterialSourceType.YOUTUBE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "https://youtube.com/watch?v=ABCDEFGHIJK");
            material.setInputMetadata(metadata);

            when(httpClient.send(any(java.net.http.HttpRequest.class),
                    any(java.net.http.HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(404);

            String result = extractorService.extractContent(material);

            assertTrue(result.contains("Transcript extraction failed"));
            assertTrue(result.contains("ABCDEFGHIJK"));
        }
    }

    @Nested
    class WebsiteSourceTests {
        @Test
        void extractContent_WithAllowedDomain_ExtractsContent() throws Exception {
            material.setSourceType(CustomMaterialSourceType.WEBSITE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "https://medium.com/article");
            material.setInputMetadata(metadata);

            when(httpClient.send(any(java.net.http.HttpRequest.class),
                    any(java.net.http.HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("<html><body><p>Article content</p></body></html>");

            String result = extractorService.extractContent(material);

            assertEquals("Article content", result);
        }

        @Test
        void extractContent_WithDisallowedDomain_ThrowsException() {
            material.setSourceType(CustomMaterialSourceType.WEBSITE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "http://malicious-site.com");
            material.setInputMetadata(metadata);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }

        @Test
        void extractContent_HttpError_ThrowsException() throws Exception {
            material.setSourceType(CustomMaterialSourceType.WEBSITE);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", "https://medium.com/article");
            material.setInputMetadata(metadata);

            when(httpClient.send(any(java.net.http.HttpRequest.class),
                    any(java.net.http.HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(500);

            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(material));
        }
    }

    @Nested
    class GeneralTests {
        @Test
        void extractContent_NullMaterial_ThrowsException() {
            assertThrows(ContentExtractionException.class, () -> extractorService.extractContent(null));
        }

        @Test
        void supports_ReturnsTrue_ForAnyType() {
            assertTrue(extractorService.supports(CustomMaterialSourceType.PDF));
            assertTrue(extractorService.supports(CustomMaterialSourceType.TEXT));
            assertTrue(extractorService.supports(CustomMaterialSourceType.YOUTUBE));
        }

        @Test
        void extractContent_TruncatesLongContent() {
            // For TEXT type which is simplest
            material.setSourceType(CustomMaterialSourceType.TEXT);
            // Create string > 50000 chars
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 50001; i++) {
                sb.append("a");
            }
            material.setContentText(sb.toString());

            String result = extractorService.extractContent(material);

            assertEquals(50000, result.length());
        }
    }
}
