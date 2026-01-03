package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.entity.FlashcardBack;
import com.lexia.backend.entity.FlashcardCard;
import com.lexia.backend.entity.FlashcardDeck;
import com.lexia.backend.enums.ImageStatus;
import com.lexia.backend.repository.FlashcardDeckRepository;
import com.lexia.backend.service.ai.FlashcardImageService;
import com.lexia.backend.service.ai.AiUsageTracker;
import com.lexia.backend.dto.ai.AiUsageTrackingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of FlashcardImageService using Gemini Imagen 3.0.
 * Generates contextual illustrations for flashcards asynchronously.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@Service
public class FlashcardImageServiceImpl implements FlashcardImageService {

    private static final Logger log = LoggerFactory.getLogger(FlashcardImageServiceImpl.class);
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final String CONTENT_TYPE_IMAGE = "flashcard_image";

    private final FlashcardDeckRepository deckRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiUsageTracker aiUsageTracker;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.imagen.api.url:https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-fast-generate-001:predict}")
    private String imagenApiUrl;

    @Value("${gemini.nanobanana.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent}")
    private String nanoBananaApiUrl;

    @Value("${flashcard.image.upload-dir:./uploads/flashcards}")
    private String uploadDir;

    @Value("${flashcard.image.width:512}")
    private int imageWidth;

    @Value("${flashcard.image.height:512}")
    private int imageHeight;

    // Track if Imagen 4 quota is exceeded to skip directly to fallback
    private volatile boolean imagenQuotaExceeded = false;

    public FlashcardImageServiceImpl(
            FlashcardDeckRepository deckRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            AiUsageTracker aiUsageTracker) {
        this.deckRepository = deckRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.aiUsageTracker = aiUsageTracker;
    }

    @Override
    @Async("imageGenerationExecutor")
    @Transactional
    public void generateImagesForDeck(UUID deckId, UUID userId) {
        log.info("Starting image generation for deck: {}", deckId);

        FlashcardDeck deck = deckRepository.findByIdAndUserId(deckId, userId).orElse(null);
        if (deck == null) {
            log.error("Deck not found: {}", deckId);
            return;
        }

        List<FlashcardCard> cards = deck.getCards();
        if (cards == null || cards.isEmpty()) {
            log.warn("No cards in deck: {}", deckId);
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            try {
                generateImageForCardInternal(deck, i, userId);
            } catch (Exception e) {
                log.error("Failed to generate image for card {} in deck {}: {}", i, deckId, e.getMessage());
                updateCardImageStatus(deck, i, ImageStatus.FAILED, null);
            }
        }

        deckRepository.save(deck);
        log.info("Completed image generation for deck: {}", deckId);
    }

    @Override
    @Async("imageGenerationExecutor")
    @Transactional
    public void generateImageForCard(UUID deckId, int cardIndex, UUID userId) {
        FlashcardDeck deck = deckRepository.findByIdAndUserId(deckId, userId).orElse(null);
        if (deck == null) {
            log.error("Deck not found: {}", deckId);
            return;
        }

        generateImageForCardInternal(deck, cardIndex, userId);
        deckRepository.save(deck);
    }

    @Override
    @Async("imageGenerationExecutor")
    @Transactional
    public void retryFailedImages(UUID deckId, UUID userId) {
        log.info("Retrying failed images for deck: {}", deckId);

        FlashcardDeck deck = deckRepository.findByIdAndUserId(deckId, userId).orElse(null);
        if (deck == null) {
            log.error("Deck not found: {}", deckId);
            return;
        }

        List<FlashcardCard> cards = deck.getCards();
        for (int i = 0; i < cards.size(); i++) {
            FlashcardBack back = cards.get(i).getBack();
            if (back != null && ImageStatus.FAILED.name().equals(back.getImageStatus())) {
                try {
                    generateImageForCardInternal(deck, i, userId);
                } catch (Exception e) {
                    log.error("Retry failed for card {} in deck {}: {}", i, deckId, e.getMessage());
                }
            }
        }

        deckRepository.save(deck);
    }

    private void generateImageForCardInternal(FlashcardDeck deck, int cardIndex, UUID userId) {
        FlashcardCard card = deck.getCards().get(cardIndex);
        FlashcardBack back = card.getBack();

        if (back == null) {
            log.warn("Card {} has no back content", cardIndex);
            return;
        }

        // Mark as generating
        updateCardImageStatus(deck, cardIndex, ImageStatus.GENERATING, null);
        deckRepository.save(deck);

        // Build prompt
        String prompt = buildImagePrompt(card.getFront(), back.getDefinition(), back.getExampleSentence());
        log.debug("Generated prompt for card {}: {}", cardIndex, prompt);

        // Generate image with retry and fallback
        byte[] imageData = null;
        int attempts = 0;
        boolean usedFallback = false;

        while (imageData == null && attempts < MAX_RETRY_ATTEMPTS) {
            attempts++;
            try {
                // If quota is known to be exceeded, skip directly to fallback
                if (imagenQuotaExceeded) {
                    log.info("Using Nano Banana fallback (Imagen quota previously exceeded)");
                    imageData = callNanoBananaApi(prompt);
                    usedFallback = true;
                } else {
                    imageData = callImagenApi(prompt);
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                log.warn("Attempt {} failed for card {}: {}", attempts, cardIndex, errorMsg);

                // Check if this is a quota exceeded error (429)
                if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED")
                        || errorMsg.contains("quota"))) {
                    log.warn("Imagen 4 quota exceeded, switching to Nano Banana fallback");
                    imagenQuotaExceeded = true;

                    // Try with Nano Banana immediately
                    try {
                        imageData = callNanoBananaApi(prompt);
                        usedFallback = true;
                        log.info("Successfully generated image using Nano Banana fallback");
                    } catch (Exception fallbackEx) {
                        log.error("Nano Banana fallback also failed: {}", fallbackEx.getMessage());
                    }
                }

                if (imageData == null && attempts >= MAX_RETRY_ATTEMPTS) {
                    updateCardImageStatus(deck, cardIndex, ImageStatus.FAILED, null);
                    return;
                }
            }
        }

        if (imageData == null) {
            updateCardImageStatus(deck, cardIndex, ImageStatus.FAILED, null);
            return;
        }

        // Save image
        try {
            String imagePath = saveImage(imageData, deck.getId(), cardIndex);
            updateCardImageStatus(deck, cardIndex, ImageStatus.COMPLETED, imagePath);
            log.info("Successfully generated image for card {} in deck {}{}", cardIndex, deck.getId(),
                    usedFallback ? " (using Nano Banana fallback)" : "");

            // Track image generation in quota
            trackImageUsage(userId, usedFallback ? "gemini-2.5-flash-image" : "imagen-4.0-fast-generate", true, null);
        } catch (Exception e) {
            log.error("Failed to save image for card {}: {}", cardIndex, e.getMessage());
            updateCardImageStatus(deck, cardIndex, ImageStatus.FAILED, null);
        }
    }

    private String buildImagePrompt(String word, String definition, String exampleSentence) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a minimalist vector illustration for vocabulary learning. ");
        prompt.append("Word: \"").append(word).append("\". ");

        if (definition != null && !definition.isBlank()) {
            prompt.append("Meaning: ").append(definition).append(". ");
        }

        if (exampleSentence != null && !exampleSentence.isBlank()) {
            prompt.append("Context: ").append(exampleSentence).append(". ");
        }

        prompt.append("Style: clean, educational, simple shapes, pastel colors, no text or words in image.");

        return prompt.toString();
    }

    private byte[] callImagenApi(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key not configured");
        }

        // Build request body according to Imagen API docs
        // Uses instances + parameters format with x-goog-api-key header
        String requestBody = String.format("""
                {
                    "instances": [
                        {
                            "prompt": "%s"
                        }
                    ],
                    "parameters": {
                        "sampleCount": 1
                    }
                }
                """, escapeJson(prompt));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(imagenApiUrl, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Imagen API returned: " + response.getStatusCode());
        }

        // Parse response - Imagen returns predictions array with bytesBase64Encoded
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode predictions = root.path("predictions");

        if (predictions.isEmpty() || !predictions.isArray() || predictions.size() == 0) {
            throw new RuntimeException("No predictions in response");
        }

        // Get base64 image from bytesBase64Encoded field
        String base64Image = predictions.get(0).path("bytesBase64Encoded").asText();
        if (base64Image == null || base64Image.isBlank()) {
            // Try alternative field names
            base64Image = predictions.get(0).path("image").path("bytesBase64Encoded").asText();
        }

        if (base64Image == null || base64Image.isBlank()) {
            throw new RuntimeException("No image data in response");
        }

        return Base64.getDecoder().decode(base64Image);
    }

    /**
     * Calls Nano Banana (gemini-2.5-flash-image) API as fallback.
     * Uses generateContent endpoint with different request/response structure.
     */
    private byte[] callNanoBananaApi(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key not configured");
        }

        // Build request body for generateContent endpoint
        String requestBody = String.format("""
                {
                    "contents": [
                        {
                            "parts": [
                                {"text": "%s"}
                            ]
                        }
                    ]
                }
                """, escapeJson(prompt));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String apiUrl = nanoBananaApiUrl + "?key=" + apiKey;
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Nano Banana API returned: " + response.getStatusCode());
        }

        // Parse response - generateContent returns candidates with parts containing
        // inlineData
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode candidates = root.path("candidates");

        if (candidates.isEmpty() || !candidates.isArray() || candidates.size() == 0) {
            throw new RuntimeException("No candidates in response");
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (parts.isEmpty() || !parts.isArray()) {
            throw new RuntimeException("No parts in response");
        }

        // Find the part with inlineData (image)
        for (JsonNode part : parts) {
            if (part.has("inlineData")) {
                String base64Image = part.path("inlineData").path("data").asText();
                if (base64Image != null && !base64Image.isBlank()) {
                    return Base64.getDecoder().decode(base64Image);
                }
            }
        }

        throw new RuntimeException("No image data in Nano Banana response");
    }

    private String saveImage(byte[] imageData, UUID deckId, int cardIndex) throws Exception {
        // Create directory
        Path deckDir = Paths.get(uploadDir, deckId.toString());
        Files.createDirectories(deckDir);
        log.debug("Created directory: {}", deckDir.toAbsolutePath());

        String filename;
        Path imagePath;

        // Read image from bytes
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        if (image == null) {
            // If can't read as image, save raw bytes as PNG
            log.warn("Could not read image from API response, saving raw bytes");
            filename = cardIndex + ".png";
            imagePath = deckDir.resolve(filename);
            Files.write(imagePath, imageData);
        } else {
            // Resize if needed
            if (image.getWidth() != imageWidth || image.getHeight() != imageHeight) {
                image = resizeImage(image, imageWidth, imageHeight);
            }

            // Save as PNG directly (WebP requires additional library)
            // Java standard ImageIO doesn't support WebP by default
            filename = cardIndex + ".png";
            imagePath = deckDir.resolve(filename);

            boolean saved = ImageIO.write(image, "png", imagePath.toFile());
            if (!saved) {
                // Fallback: save raw bytes
                log.warn("ImageIO.write failed for PNG, saving raw bytes");
                Files.write(imagePath, imageData);
            }
        }

        log.info("Saved image to: {}", imagePath.toAbsolutePath());

        // Return relative URL path
        return "/uploads/flashcards/" + deckId + "/" + filename;
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private void updateCardImageStatus(FlashcardDeck deck, int cardIndex, ImageStatus status, String imageUrl) {
        FlashcardCard card = deck.getCards().get(cardIndex);
        FlashcardBack back = card.getBack();
        if (back != null) {
            back.setImageStatus(status.name());
            if (imageUrl != null) {
                back.setImageUrl(imageUrl);
            }
        }
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Tracks image generation usage for quota management.
     * Imagen 4 Fast: $0.02/image
     * Gemini 2.5 flash image (Nano Banana): Uses tokens, not tracked here as
     * fallback
     */
    private void trackImageUsage(UUID userId, String modelId, boolean success, String errorMessage) {
        try {
            // Per-image pricing for Imagen models (from Google Cloud pricing)
            Double imageCost = null;
            if (modelId != null && modelId.contains("imagen-4.0-fast")) {
                imageCost = 0.02; // $0.02 per image for Imagen 4 Fast
            } else if (modelId != null && modelId.contains("imagen-4.0-ultra")) {
                imageCost = 0.06; // $0.06 per image for Imagen 4 Ultra
            } else if (modelId != null && modelId.contains("imagen")) {
                imageCost = 0.04; // $0.04 default for other Imagen models
            } else if (modelId != null && (modelId.contains("gemini") && modelId.contains("image"))) {
                // Gemini 2.5 Flash Image generation (Nano Banana fallback)
                // Estimated cost based on typical usage - no official per-image pricing
                imageCost = 0.01; // Estimate $0.01 per image for Gemini flash image
            }

            AiUsageTrackingRequest trackingRequest = AiUsageTrackingRequest.builder()
                    .userId(userId)
                    .contentType(CONTENT_TYPE_IMAGE)
                    .modelId(modelId)
                    .inputTokens(0) // Image generation doesn't use token counting
                    .outputTokens(0)
                    .overrideCostUsd(imageCost)
                    .responseTimeMs(0)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();

            aiUsageTracker.trackUsageAsync(trackingRequest);
            log.debug("Tracked image generation usage for user {} - model: {}, cost: ${}",
                    userId, modelId, imageCost != null ? imageCost : "N/A (token-based)");
        } catch (Exception e) {
            log.warn("Failed to track image usage: {}", e.getMessage());
        }
    }
}
