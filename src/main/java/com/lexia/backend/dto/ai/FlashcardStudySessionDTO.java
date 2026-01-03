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
 * DTO representing a flashcard study session.
 * Contains cards due for review with their progress data.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard study session with due cards")
public class FlashcardStudySessionDTO {

    /**
     * Deck ID being studied.
     */
    @Schema(description = "Deck UUID")
    private UUID deckId;

    /**
     * Deck title.
     */
    @Schema(description = "Deck title", example = "Business Meeting Vocabulary")
    private String deckTitle;

    /**
     * Session start timestamp.
     */
    @Schema(description = "Session start time")
    private Instant sessionStartedAt;

    /**
     * Total cards in the deck.
     */
    @Schema(description = "Total cards in deck", example = "50")
    private Integer totalCards;

    /**
     * Number of cards due for review.
     */
    @Schema(description = "Cards due for review", example = "15")
    private Integer dueCards;

    /**
     * Number of new cards (never reviewed).
     */
    @Schema(description = "New cards to learn", example = "5")
    private Integer newCards;

    /**
     * Number of cards to review in this session.
     * May be limited to avoid overwhelming the user.
     */
    @Schema(description = "Cards in this session", example = "20")
    private Integer sessionSize;

    /**
     * Cards for this study session with progress data.
     */
    @Schema(description = "Cards to study in this session")
    private List<FlashcardProgressDTO> cardsToStudy;

    /**
     * Deck statistics summary.
     */
    @Schema(description = "Deck progress statistics")
    private DeckStatsDTO stats;

    /**
     * Whether this is a practice session (no progress updates).
     */
    @Schema(description = "True if this is a practice session", example = "false")
    @Builder.Default
    private Boolean isPracticeMode = false;

    /**
     * Nested DTO for deck statistics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Deck progress statistics summary")
    public static class DeckStatsDTO {

        /**
         * Cards at mastery level 0 (New).
         */
        @Schema(description = "New cards (never studied)", example = "10")
        private Integer newCount;

        /**
         * Cards at mastery level 1 (Learning).
         */
        @Schema(description = "Learning cards", example = "15")
        private Integer learningCount;

        /**
         * Cards at mastery level 2-3 (Young/Mature).
         */
        @Schema(description = "Reviewing cards", example = "20")
        private Integer reviewingCount;

        /**
         * Cards at mastery level 4-5 (Master/Expert).
         */
        @Schema(description = "Mastered cards", example = "5")
        private Integer masteredCount;

        /**
         * Total reviews across all cards.
         */
        @Schema(description = "Total reviews completed", example = "150")
        private Long totalReviews;

        /**
         * Overall accuracy percentage.
         */
        @Schema(description = "Overall accuracy percentage", example = "82.5")
        private Double overallAccuracy;
    }
}
