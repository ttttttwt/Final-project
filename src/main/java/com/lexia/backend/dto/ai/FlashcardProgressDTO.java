package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing progress for a single flashcard.
 * Contains SM-2 spaced repetition data and review statistics.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Progress tracking for a single flashcard")
public class FlashcardProgressDTO {

    /**
     * Progress record ID.
     */
    @Schema(description = "Progress record ID")
    private Long id;

    /**
     * Card index within the deck (0-based).
     */
    @Schema(description = "Card index in deck (0-based)", example = "0")
    private Integer cardIndex;

    /**
     * Mastery level (0-5).
     * 0=New, 1=Learning, 2=Young, 3=Mature, 4=Master, 5=Expert
     */
    @Schema(description = "Mastery level", 
            example = "2",
            minimum = "0",
            maximum = "5")
    private Integer masteryLevel;

    /**
     * Human-readable mastery level name.
     */
    @Schema(description = "Mastery level name",
            example = "Young",
            allowableValues = {"New", "Learning", "Young", "Mature", "Master", "Expert"})
    private String masteryLevelName;

    /**
     * Total number of reviews.
     */
    @Schema(description = "Total review count", example = "5")
    private Integer reviewCount;

    /**
     * Number of correct answers.
     */
    @Schema(description = "Correct answer count", example = "4")
    private Integer correctCount;

    /**
     * Accuracy percentage.
     */
    @Schema(description = "Accuracy percentage", example = "80.0")
    private Double accuracyRate;

    /**
     * SM-2 ease factor.
     */
    @Schema(description = "SM-2 ease factor", example = "2.50")
    private BigDecimal easeFactor;

    /**
     * Days until next review.
     */
    @Schema(description = "Interval in days until next review", example = "6")
    private Integer intervalDays;

    /**
     * Last review timestamp.
     */
    @Schema(description = "Last review timestamp")
    private Instant lastReviewedAt;

    /**
     * Next scheduled review timestamp.
     */
    @Schema(description = "Next review scheduled at")
    private Instant nextReviewAt;

    /**
     * Whether the card is due for review.
     */
    @Schema(description = "True if card is due for review")
    private Boolean isDue;

    /**
     * The actual card data (optional, included in study sessions).
     */
    @Schema(description = "Card content (included in study sessions)")
    private FlashcardCardDTO card;
}
