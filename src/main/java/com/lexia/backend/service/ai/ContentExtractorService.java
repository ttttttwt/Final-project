package com.lexia.backend.service.ai;

import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialSourceType;

/**
 * Service interface for extracting content from various source types.
 * 
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>PDF - Text extraction via Apache PDFBox or Gemini Multimodal</li>
 * <li>DOCX - Text extraction via Apache POI</li>
 * <li>IMAGE - OCR via Gemini Vision</li>
 * <li>YOUTUBE - Transcript via API or audio fallback</li>
 * <li>WEBSITE - Content scraping with readability extraction</li>
 * <li>TEXT - Direct passthrough (already text)</li>
 * </ul>
 * 
 * @since Sprint 5
 */
public interface ContentExtractorService {

    /**
     * Extracts text content from a material based on its source type.
     * 
     * @param material The material with source info
     * @return Extracted text content
     * @throws ContentExtractionException if extraction fails
     */
    String extractContent(UserCustomMaterial material);

    /**
     * Checks if this extractor can handle the given source type.
     * 
     * @param sourceType The source type
     * @return true if supported
     */
    boolean supports(CustomMaterialSourceType sourceType);

    /**
     * Gets the maximum content length supported.
     * 
     * @return max chars (default 50000)
     */
    default int getMaxContentLength() {
        return 50000; // ~12,500 tokens at 4 chars/token
    }
}
