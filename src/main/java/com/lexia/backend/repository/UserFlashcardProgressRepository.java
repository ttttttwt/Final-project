package com.lexia.backend.repository;

import com.lexia.backend.entity.UserFlashcardProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserFlashcardProgress entity.
 * Provides queries for spaced repetition scheduling and progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserFlashcardProgress
 */
@Repository
public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long> {

        // ========== Basic Queries ==========

        /**
         * Finds progress record for a specific card.
         * 
         * @param userId    the user ID
         * @param deckId    the deck ID
         * @param cardIndex the card index within the deck
         * @return the progress record if found
         */
        Optional<UserFlashcardProgress> findByUserIdAndDeckIdAndCardIndex(
                        UUID userId, UUID deckId, Integer cardIndex);

        /**
         * Finds all progress records for a user and deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return list of progress records
         */
        List<UserFlashcardProgress> findByUserIdAndDeckId(UUID userId, UUID deckId);

        /**
         * Finds all progress records for a user.
         * 
         * @param userId the user ID
         * @return list of all user's progress records
         */
        List<UserFlashcardProgress> findByUserId(UUID userId);

        /**
         * Checks if a progress record exists.
         * 
         * @param userId    the user ID
         * @param deckId    the deck ID
         * @param cardIndex the card index
         * @return true if record exists
         */
        boolean existsByUserIdAndDeckIdAndCardIndex(UUID userId, UUID deckId, Integer cardIndex);

        // ========== Spaced Repetition Queries ==========

        /**
         * Finds cards that are due for review (nextReviewAt <= now).
         * Ordered by due date (most overdue first).
         * 
         * @param userId the user ID
         * @param now    current timestamp
         * @return list of due cards
         */
        @Query("SELECT p FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now) " +
                        "ORDER BY p.nextReviewAt ASC NULLS FIRST")
        List<UserFlashcardProgress> findDueCards(
                        @Param("userId") UUID userId,
                        @Param("now") Instant now);

        /**
         * Finds due cards for a specific deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @param now    current timestamp
         * @return list of due cards for the deck
         */
        @Query("SELECT p FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND p.deckId = :deckId " +
                        "AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now) " +
                        "ORDER BY p.nextReviewAt ASC NULLS FIRST")
        List<UserFlashcardProgress> findDueCardsForDeck(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId,
                        @Param("now") Instant now);

        /**
         * Finds due cards with limit (for study sessions).
         * 
         * @param userId   the user ID
         * @param now      current timestamp
         * @param pageable pagination (limit)
         * @return page of due cards
         */
        @Query("SELECT p FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now) " +
                        "ORDER BY p.nextReviewAt ASC NULLS FIRST")
        Page<UserFlashcardProgress> findDueCardsWithLimit(
                        @Param("userId") UUID userId,
                        @Param("now") Instant now,
                        Pageable pageable);

        /**
         * Finds new cards (never reviewed) for a deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return list of new cards
         */
        @Query("SELECT p FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND p.deckId = :deckId AND p.reviewCount = 0")
        List<UserFlashcardProgress> findNewCardsForDeck(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId);

        /**
         * Finds cards by mastery level for a deck.
         * 
         * @param userId       the user ID
         * @param deckId       the deck ID
         * @param masteryLevel the mastery level (0-5)
         * @return list of cards at that mastery level
         */
        List<UserFlashcardProgress> findByUserIdAndDeckIdAndMasteryLevel(
                        UUID userId, UUID deckId, Integer masteryLevel);

        // ========== Count Queries ==========

        /**
         * Counts due cards for a user.
         * 
         * @param userId the user ID
         * @param now    current timestamp
         * @return number of due cards
         */
        @Query("SELECT COUNT(p) FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now)")
        long countDueCards(@Param("userId") UUID userId, @Param("now") Instant now);

        /**
         * Counts due cards for a specific deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @param now    current timestamp
         * @return number of due cards
         */
        @Query("SELECT COUNT(p) FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND p.deckId = :deckId AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now)")
        long countDueCardsForDeck(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId,
                        @Param("now") Instant now);

        /**
         * Counts cards by mastery level for a user.
         * 
         * @param userId       the user ID
         * @param masteryLevel the mastery level
         * @return count of cards at that level
         */
        long countByUserIdAndMasteryLevel(UUID userId, Integer masteryLevel);

        /**
         * Counts cards by mastery level for a deck.
         * 
         * @param userId       the user ID
         * @param deckId       the deck ID
         * @param masteryLevel the mastery level
         * @return count of cards at that level
         */
        long countByUserIdAndDeckIdAndMasteryLevel(UUID userId, UUID deckId, Integer masteryLevel);

        /**
         * Counts total reviewed cards for a user.
         * 
         * @param userId the user ID
         * @return count of cards with at least one review
         */
        @Query("SELECT COUNT(p) FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND p.reviewCount > 0")
        long countReviewedCards(@Param("userId") UUID userId);

        // ========== Statistics Queries ==========

        /**
         * Gets progress statistics for a deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return array with statistics
         */
        @Query(value = """
                        SELECT
                            COUNT(*) as total_cards,
                            COUNT(*) FILTER (WHERE review_count = 0) as new_cards,
                            COUNT(*) FILTER (WHERE mastery_level <= 1) as learning_cards,
                            COUNT(*) FILTER (WHERE mastery_level >= 2 AND mastery_level <= 3) as reviewing_cards,
                            COUNT(*) FILTER (WHERE mastery_level >= 4) as mastered_cards,
                            COALESCE(SUM(review_count), 0) as total_reviews,
                            COALESCE(SUM(correct_count), 0) as total_correct,
                            COALESCE(AVG(CASE WHEN review_count > 0
                                THEN correct_count::float / review_count * 100
                                ELSE NULL END), 0) as avg_accuracy
                        FROM user_flashcard_progress
                        WHERE user_id = :userId AND deck_id = :deckId
                        """, nativeQuery = true)
        Object[] getDeckProgressStatistics(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId);

        /**
         * Gets overall progress statistics for a user.
         * 
         * @param userId the user ID
         * @return array with overall statistics
         */
        @Query(value = """
                        SELECT
                            COUNT(*) as total_cards,
                            COUNT(*) FILTER (WHERE review_count = 0) as new_cards,
                            COUNT(*) FILTER (WHERE mastery_level >= 4) as mastered_cards,
                            COALESCE(SUM(review_count), 0) as total_reviews,
                            COALESCE(AVG(ease_factor), 2.5) as avg_ease_factor,
                            COUNT(DISTINCT deck_id) as decks_studied
                        FROM user_flashcard_progress
                        WHERE user_id = :userId
                        """, nativeQuery = true)
        Object[] getUserProgressStatistics(@Param("userId") UUID userId);

        /**
         * Gets mastery distribution for a user.
         * 
         * @param userId the user ID
         * @return list of [masteryLevel, count] arrays
         */
        @Query(value = """
                        SELECT mastery_level, COUNT(*) as count
                        FROM user_flashcard_progress
                        WHERE user_id = :userId
                        GROUP BY mastery_level
                        ORDER BY mastery_level
                        """, nativeQuery = true)
        List<Object[]> getMasteryDistribution(@Param("userId") UUID userId);

        // ========== Batch Operations ==========

        /**
         * Creates progress records for all cards in a deck that don't have records yet.
         * 
         * @param userId    the user ID
         * @param deckId    the deck ID
         * @param cardCount total cards in deck
         * @return number of records created
         */
        @Modifying
        @Query(value = """
                        INSERT INTO user_flashcard_progress
                            (user_id, deck_id, card_index, mastery_level, review_count, correct_count,
                             ease_factor, interval_days, created_at)
                        SELECT
                            :userId, :deckId, generate_series(0, :cardCount - 1),
                            0, 0, 0, 2.50, 1, NOW()
                        ON CONFLICT (user_id, deck_id, card_index) DO NOTHING
                        """, nativeQuery = true)
        int initializeProgressForDeck(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId,
                        @Param("cardCount") int cardCount);

        /**
         * Deletes all progress records for a deck.
         * 
         * @param deckId the deck ID
         * @return number of records deleted
         */
        @Modifying
        @Query("DELETE FROM UserFlashcardProgress p WHERE p.deckId = :deckId")
        int deleteAllByDeckId(@Param("deckId") UUID deckId);

        /**
         * Deletes all progress records for a user.
         * 
         * @param userId the user ID
         * @return number of records deleted
         */
        @Modifying
        @Query("DELETE FROM UserFlashcardProgress p WHERE p.userId = :userId")
        int deleteAllByUserId(@Param("userId") UUID userId);

        /**
         * Deletes progress records for a user and deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return number of records deleted
         */
        @Modifying
        @Query("DELETE FROM UserFlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId")
        int deleteByUserIdAndDeckId(@Param("userId") UUID userId, @Param("deckId") UUID deckId);

        /**
         * Deletes a specific progress record.
         * 
         * @param userId    the user ID
         * @param deckId    the deck ID
         * @param cardIndex the card index
         * @return number of records deleted
         */
        @Modifying
        @Query("DELETE FROM UserFlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId AND p.cardIndex = :cardIndex")
        int deleteByUserIdAndDeckIdAndCardIndex(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId,
                        @Param("cardIndex") Integer cardIndex);

        // ========== Recent Activity Queries ==========

        /**
         * Finds recently reviewed cards for a user.
         * 
         * @param userId the user ID
         * @param since  only cards reviewed after this time
         * @return list of recently reviewed progress records
         */
        List<UserFlashcardProgress> findByUserIdAndLastReviewedAtAfterOrderByLastReviewedAtDesc(
                        UUID userId, Instant since);

        /**
         * Counts reviews completed today for a user.
         * 
         * @param userId     the user ID
         * @param startOfDay start of today
         * @return number of reviews today
         */
        @Query("SELECT COUNT(p) FROM UserFlashcardProgress p WHERE p.userId = :userId " +
                        "AND p.lastReviewedAt >= :startOfDay")
        long countReviewsToday(
                        @Param("userId") UUID userId,
                        @Param("startOfDay") Instant startOfDay);

        // ========== Next Review Queries ==========

        /**
         * Gets the earliest next review time for a deck.
         * Returns the minimum nextReviewAt among all cards that have been reviewed.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return earliest next review time if any
         */
        @Query("SELECT MIN(p.nextReviewAt) FROM UserFlashcardProgress p " +
                        "WHERE p.userId = :userId AND p.deckId = :deckId " +
                        "AND p.nextReviewAt IS NOT NULL")
        Optional<Instant> findEarliestNextReview(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId);

        /**
         * Gets the average mastery level for a deck.
         * 
         * @param userId the user ID
         * @param deckId the deck ID
         * @return average mastery level
         */
        @Query("SELECT AVG(CAST(p.masteryLevel AS double)) FROM UserFlashcardProgress p " +
                        "WHERE p.userId = :userId AND p.deckId = :deckId")
        Optional<Double> getAverageMasteryLevel(
                        @Param("userId") UUID userId,
                        @Param("deckId") UUID deckId);
}
