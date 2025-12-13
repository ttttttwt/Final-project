package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity tracking user progress for individual flashcards.
 * Implements the SM-2 spaced repetition algorithm for optimal learning.
 * 
 * <p>SM-2 Algorithm Fields:</p>
 * <ul>
 *   <li><b>easeFactor</b> - Reflects card difficulty (1.30-2.50+), higher = easier</li>
 *   <li><b>intervalDays</b> - Days until next review</li>
 *   <li><b>masteryLevel</b> - Overall mastery (0-5): New, Learning, Young, Mature, Master, Expert</li>
 *   <li><b>reviewCount</b> - Total times reviewed</li>
 *   <li><b>correctCount</b> - Times answered correctly</li>
 * </ul>
 * 
 * <p>Quality Rating Scale (for SM-2):</p>
 * <ul>
 *   <li>0 - Complete blackout, total failure to recall</li>
 *   <li>1 - Incorrect, but remembered upon seeing answer</li>
 *   <li>2 - Incorrect, but answer seemed easy to recall</li>
 *   <li>3 - Correct with significant difficulty</li>
 *   <li>4 - Correct after some hesitation</li>
 *   <li>5 - Perfect, instant recall</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see FlashcardDeck
 */
@Entity
@Table(name = "user_flashcard_progress", 
    indexes = {
        @Index(name = "idx_user_flashcard_progress_user_deck", columnList = "user_id, deck_id"),
        @Index(name = "idx_user_flashcard_progress_next_review", columnList = "next_review_at"),
        @Index(name = "idx_user_flashcard_progress_mastery", columnList = "user_id, mastery_level")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_deck_card", columnNames = {"user_id", "deck_id", "card_index"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "deck")
public class UserFlashcardProgress {

    /**
     * Mastery level constants for clarity.
     */
    public static final int MASTERY_NEW = 0;
    public static final int MASTERY_LEARNING = 1;
    public static final int MASTERY_YOUNG = 2;
    public static final int MASTERY_MATURE = 3;
    public static final int MASTERY_MASTER = 4;
    public static final int MASTERY_EXPERT = 5;

    /**
     * Minimum ease factor (SM-2 minimum).
     */
    public static final BigDecimal MIN_EASE_FACTOR = new BigDecimal("1.30");

    /**
     * Default ease factor for new cards.
     */
    public static final BigDecimal DEFAULT_EASE_FACTOR = new BigDecimal("2.50");

    /**
     * Primary key - auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Reference to the flashcard deck.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private FlashcardDeck deck;

    /**
     * Deck ID for queries without loading the deck entity.
     */
    @Column(name = "deck_id", insertable = false, updatable = false)
    private UUID deckId;

    /**
     * Index of the card within the deck's cards array (0-based).
     */
    @Column(name = "card_index", nullable = false)
    private Integer cardIndex;

    /**
     * Mastery level from 0 (New) to 5 (Expert).
     * <ul>
     *   <li>0 - New: Never reviewed</li>
     *   <li>1 - Learning: Initial reviews, interval < 3 days</li>
     *   <li>2 - Young: Some familiarity, interval 3-7 days</li>
     *   <li>3 - Mature: Good retention, interval 7-30 days</li>
     *   <li>4 - Master: Strong retention, interval 30-90 days</li>
     *   <li>5 - Expert: Excellent retention, interval > 90 days</li>
     * </ul>
     */
    @Column(name = "mastery_level", nullable = false)
    @Builder.Default
    private Integer masteryLevel = MASTERY_NEW;

    /**
     * Total number of reviews for this card.
     */
    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    /**
     * Number of correct responses (quality >= 3).
     */
    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    /**
     * Number of consecutive correct responses.
     * Used for SM-2 interval calculation.
     * Resets to 0 on incorrect answer, increments on correct answer.
     */
    @Column(name = "consecutive_correct", nullable = false)
    @Builder.Default
    private Integer consecutiveCorrect = 0;

    /**
     * SM-2 ease factor (EF).
     * Minimum 1.30, default 2.50.
     * Higher values mean the card is easier for the user.
     */
    @Column(name = "ease_factor", precision = 4, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal easeFactor = DEFAULT_EASE_FACTOR;

    /**
     * Number of days until next review (SM-2 interval).
     */
    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private Integer intervalDays = 1;

    /**
     * Timestamp of last review.
     */
    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    /**
     * Scheduled timestamp for next review.
     * Used for spaced repetition due card queries.
     */
    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    /**
     * Timestamp when this progress record was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ========== SM-2 Algorithm Implementation ==========

    /**
     * Records a review and updates SM-2 parameters.
     * 
     * @param quality User's self-rating (0-5)
     *        0 = total blackout
     *        1 = wrong, remembered after seeing answer
     *        2 = wrong, but answer was easy
     *        3 = correct with difficulty
     *        4 = correct after hesitation
     *        5 = perfect recall
     */
    public void recordReview(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        reviewCount++;
        if (quality >= 3) {
            correctCount++;
            consecutiveCorrect++;
        } else {
            // Reset consecutive correct on failure
            consecutiveCorrect = 0;
        }

        // Update ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        BigDecimal qualityFactor = BigDecimal.valueOf(0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        easeFactor = easeFactor.add(qualityFactor);
        
        // Ensure minimum ease factor
        if (easeFactor.compareTo(MIN_EASE_FACTOR) < 0) {
            easeFactor = MIN_EASE_FACTOR;
        }

        // Calculate new interval based on consecutive correct answers (SM-2 standard)
        if (quality < 3) {
            // Failed: reset to 1 day
            intervalDays = 1;
        } else {
            // Correct: use consecutiveCorrect for interval progression
            if (consecutiveCorrect == 1) {
                intervalDays = 1;
            } else if (consecutiveCorrect == 2) {
                intervalDays = 6;
            } else {
                // consecutiveCorrect >= 3: interval = previousInterval * EF
                intervalDays = (int) Math.round(intervalDays * easeFactor.doubleValue());
            }
        }

        // Update timestamps
        lastReviewedAt = Instant.now();
        nextReviewAt = lastReviewedAt.plusSeconds((long) intervalDays * 24 * 60 * 60);

        // Update mastery level based on interval
        updateMasteryLevel();
    }

    /**
     * Updates mastery level based on current interval.
     */
    private void updateMasteryLevel() {
        if (intervalDays < 3) {
            masteryLevel = MASTERY_LEARNING;
        } else if (intervalDays < 7) {
            masteryLevel = MASTERY_YOUNG;
        } else if (intervalDays < 30) {
            masteryLevel = MASTERY_MATURE;
        } else if (intervalDays < 90) {
            masteryLevel = MASTERY_MASTER;
        } else {
            masteryLevel = MASTERY_EXPERT;
        }
    }

    /**
     * Checks if this card is due for review.
     * @return true if nextReviewAt is null or in the past
     */
    public boolean isDue() {
        return nextReviewAt == null || nextReviewAt.isBefore(Instant.now());
    }

    /**
     * Checks if this card is overdue (more than 1 day past scheduled review).
     * @return true if more than 24 hours overdue
     */
    public boolean isOverdue() {
        if (nextReviewAt == null) return true;
        Instant oneDayAgo = Instant.now().minusSeconds(24 * 60 * 60);
        return nextReviewAt.isBefore(oneDayAgo);
    }

    /**
     * Gets the accuracy rate as a percentage.
     * @return accuracy percentage (0-100), or 0 if no reviews
     */
    public double getAccuracyRate() {
        if (reviewCount == 0) return 0.0;
        return (double) correctCount / reviewCount * 100;
    }

    /**
     * Gets the mastery level as a human-readable string.
     * @return mastery level name
     */
    public String getMasteryLevelName() {
        return switch (masteryLevel) {
            case MASTERY_NEW -> "New";
            case MASTERY_LEARNING -> "Learning";
            case MASTERY_YOUNG -> "Young";
            case MASTERY_MATURE -> "Mature";
            case MASTERY_MASTER -> "Master";
            case MASTERY_EXPERT -> "Expert";
            default -> "Unknown";
        };
    }

    /**
     * Factory method to create a new progress record for a card.
     * @param userId the user ID
     * @param deck the flashcard deck
     * @param cardIndex the index of the card in the deck
     * @return a new UserFlashcardProgress instance
     */
    public static UserFlashcardProgress createNew(UUID userId, FlashcardDeck deck, int cardIndex) {
        return UserFlashcardProgress.builder()
                .userId(userId)
                .deck(deck)
                .cardIndex(cardIndex)
                .masteryLevel(MASTERY_NEW)
                .reviewCount(0)
                .correctCount(0)
                .consecutiveCorrect(0)
                .easeFactor(DEFAULT_EASE_FACTOR)
                .intervalDays(1)
                .build();
    }
}
