package com.lexia.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for handling flashcard image uploads.
 * Allows users to upload custom images for their flashcards.
 * 
 * @author LEXIA Team
 * @since Sprint 6
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai/flashcards")
@Tag(name = "Flashcard Images", description = "Flashcard image upload endpoints")
public class FlashcardImageUploadController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${flashcard.image.upload-dir:./uploads/flashcards}")
    private String uploadDir;

    /**
     * Upload a custom image for a flashcard.
     * 
     * @param file      The image file to upload
     * @param deckId    The deck ID
     * @param cardIndex The card index within the deck
     * @return The URL of the uploaded image
     */
    @Operation(summary = "Upload flashcard image", description = "Upload a custom image for a specific flashcard")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCardImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deckId") UUID deckId,
            @RequestParam("cardIndex") int cardIndex) {

        log.info("Received image upload request for deck {} card {}", deckId, cardIndex);

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file type. Allowed: PNG, JPEG, WebP, GIF"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size exceeds 5MB limit"));
        }

        try {
            // Create directory if not exists
            Path deckDir = Paths.get(uploadDir, deckId.toString());
            Files.createDirectories(deckDir);

            // Determine file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "png";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            }

            // Save file with prefix "user_" to distinguish from AI-generated
            String filename = "user_" + cardIndex + "." + extension;
            Path filePath = deckDir.resolve(filename);
            Files.write(filePath, file.getBytes());

            log.info("Saved uploaded image to: {}", filePath);

            // Return the URL
            String imageUrl = "/uploads/flashcards/" + deckId + "/" + filename;
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("imageUrl", imageUrl));

        } catch (IOException e) {
            log.error("Failed to save uploaded image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save image"));
        }
    }
}
