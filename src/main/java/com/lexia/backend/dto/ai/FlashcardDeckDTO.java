package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a flashcard deck response.
 * Contains deck metadata, cards, and optional progress information.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard deck with cards and metadata")
public class FlashcardDeckDTO {

    /**
     * Deck unique identifier.
     */
    @Schema(description = "Deck UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * Owner user ID.
     */
    @Schema(description = "Owner user UUID")
    private UUID userId;

    /**
     * Deck title.
     */
    @Schema(description = "Deck title", example = "Business Meeting Vocabulary")
    private String title;

    /**
     * Deck description.
     */
    @Schema(description = "Deck description", 
            example = "Essential vocabulary for business meetings and presentations")
    private String description;

    /**
     * Source type: LESSON, AI_GENERATED, or USER_CREATED.
     */
    @Schema(description = "How the deck was created",
            allowableValues = {"LESSON", "AI_GENERATED", "USER_CREATED"})
    private String sourceType;

    /**
     * Source lesson ID (if sourceType is LESSON).
     */
    @Schema(description = "Source lesson ID (if from lesson)")
    private Long sourceId;

    /**
     * Course title (if sourceType is LESSON).
     */
    @Schema(description = "Course title (if from lesson)")
    private String courseTitle;

    /**
     * Lesson title (if sourceType is LESSON).
     */
    @Schema(description = "Lesson title (if from lesson)")
    private String lessonTitle;

    /**
     * CEFR level (A1-C2).
     */
    @Schema(description = "CEFR level", 
            example = "B2",
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String cefrLevel;

    /**
     * List of flashcards.
     */
    @Schema(description = "Flashcards in this deck")
    private List<FlashcardCardDTO> cards;

    /**
     * Number of cards in the deck.
     */
    @Schema(description = "Total number of cards", example = "25")
    private Integer cardCount;

    /**
     * Deck creation timestamp.
     */
    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    // ========== Optional Progress Fields ==========

    /**
     * Number of cards due for review (if progress is loaded).
     */
    @Schema(description = "Cards due for review")
    private Integer dueCount;

    /**
     * Number of new cards not yet studied.
     */
    @Schema(description = "New cards not yet studied")
    private Integer newCount;

    /**
     * Number of mastered cards (mastery level >= 4).
     */
    @Schema(description = "Mastered cards")
    private Integer masteredCount;

    /**
     * Overall accuracy percentage.
     */
    @Schema(description = "Overall accuracy percentage", example = "85.5")
    private Double accuracyRate;
}
