package com.lexia.backend.repository;

import com.lexia.backend.entity.FlashcardDeck;
import com.lexia.backend.entity.FlashcardDeck.SourceType;
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
 * Repository for FlashcardDeck entity.
 * Provides CRUD operations and custom queries for flashcard deck management.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see FlashcardDeck
 */
@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, UUID> {

    // ========== Basic Queries ==========

    /**
     * Finds all decks belonging to a user.
     * @param userId the user ID
     * @return list of user's decks
     */
    List<FlashcardDeck> findByUserId(UUID userId);

    /**
     * Finds all decks belonging to a user with pagination.
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of user's decks
     */
    Page<FlashcardDeck> findByUserId(UUID userId, Pageable pageable);

    /**
     * Finds decks by user ID ordered by creation date (newest first).
     * @param userId the user ID
     * @return list of user's decks sorted by creation date
     */
    List<FlashcardDeck> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Finds a specific deck by ID and user ID.
     * Used to verify ownership.
     * @param id deck ID
     * @param userId user ID
     * @return the deck if found and owned by user
     */
    Optional<FlashcardDeck> findByIdAndUserId(UUID id, UUID userId);

    // ========== Source Type Queries ==========

    /**
     * Finds decks by user and source type.
     * @param userId the user ID
     * @param sourceType the source type filter
     * @return list of matching decks
     */
    List<FlashcardDeck> findByUserIdAndSourceType(UUID userId, SourceType sourceType);

    /**
     * Finds a deck by source type and source ID.
     * Useful for finding lesson-based decks.
     * @param sourceType the source type (usually LESSON)
     * @param sourceId the source ID (lesson ID)
     * @return the deck if found
     */
    Optional<FlashcardDeck> findBySourceTypeAndSourceId(SourceType sourceType, Long sourceId);

    /**
     * Finds a deck by user, source type, and source ID.
     * Ensures user ownership when querying by lesson.
     * @param userId the user ID
     * @param sourceType the source type
     * @param sourceId the source ID
     * @return the deck if found
     */
    Optional<FlashcardDeck> findByUserIdAndSourceTypeAndSourceId(
            UUID userId, SourceType sourceType, Long sourceId);

    /**
     * Checks if a user already has a deck for a specific lesson.
     * Uses derived query for type-safe enum handling.
     * @param userId the user ID
     * @param sourceType should be SourceType.LESSON
     * @param sourceId the lesson ID
     * @return true if deck exists
     */
    boolean existsByUserIdAndSourceTypeAndSourceId(UUID userId, SourceType sourceType, Long sourceId);

    // ========== CEFR Level Queries ==========

    /**
     * Finds decks by user and CEFR level.
     * @param userId the user ID
     * @param cefrLevel the CEFR level (A1-C2)
     * @return list of matching decks
     */
    List<FlashcardDeck> findByUserIdAndCefrLevel(UUID userId, String cefrLevel);

    /**
     * Finds decks by CEFR level (for admin/content management).
     * @param cefrLevel the CEFR level
     * @param pageable pagination parameters
     * @return page of matching decks
     */
    Page<FlashcardDeck> findByCefrLevel(String cefrLevel, Pageable pageable);

    // ========== Count Queries ==========

    /**
     * Counts decks owned by a user.
     * @param userId the user ID
     * @return deck count
     */
    long countByUserId(UUID userId);

    /**
     * Counts decks by user and source type.
     * @param userId the user ID
     * @param sourceType the source type
     * @return deck count
     */
    long countByUserIdAndSourceType(UUID userId, SourceType sourceType);

    /**
     * Gets total card count for a user across all decks.
     * @param userId the user ID
     * @return total card count
     */
    @Query("SELECT COALESCE(SUM(d.cardCount), 0) FROM FlashcardDeck d WHERE d.userId = :userId")
    long getTotalCardCountByUserId(@Param("userId") UUID userId);

    // ========== Search Queries ==========

    /**
     * Searches decks by title (case-insensitive contains).
     * @param userId the user ID
     * @param titleSearch search term
     * @return list of matching decks
     */
    @Query("SELECT d FROM FlashcardDeck d WHERE d.userId = :userId " +
           "AND LOWER(d.title) LIKE LOWER(CONCAT('%', :titleSearch, '%'))")
    List<FlashcardDeck> searchByTitle(
            @Param("userId") UUID userId, 
            @Param("titleSearch") String titleSearch);

    /**
     * Finds recently updated decks for a user.
     * @param userId the user ID
     * @param since only decks updated after this time
     * @return list of recently updated decks
     */
    List<FlashcardDeck> findByUserIdAndUpdatedAtAfterOrderByUpdatedAtDesc(
            UUID userId, Instant since);

    // ========== Update Queries ==========

    /**
     * Updates the card count for a deck.
     * @param deckId the deck ID
     * @param cardCount the new card count
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE FlashcardDeck d SET d.cardCount = :cardCount, d.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE d.id = :deckId")
    int updateCardCount(@Param("deckId") UUID deckId, @Param("cardCount") int cardCount);

    // ========== Delete Queries ==========

    /**
     * Deletes all decks for a user.
     * @param userId the user ID
     * @return number of decks deleted
     */
    @Modifying
    @Query("DELETE FROM FlashcardDeck d WHERE d.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Deletes a deck by ID and user ID (ensures ownership).
     * @param id deck ID
     * @param userId user ID
     * @return number of decks deleted (0 or 1)
     */
    int deleteByIdAndUserId(UUID id, UUID userId);

    // ========== Statistics Queries ==========

    /**
     * Gets deck statistics for a user.
     * @param userId the user ID
     * @return array with [deckCount, totalCards, lessonDecks, aiDecks, userDecks]
     */
    @Query(value = """
        SELECT 
            COUNT(*) as deck_count,
            COALESCE(SUM(card_count), 0) as total_cards,
            COUNT(*) FILTER (WHERE source_type = 'lesson') as lesson_decks,
            COUNT(*) FILTER (WHERE source_type = 'ai_generated') as ai_decks,
            COUNT(*) FILTER (WHERE source_type = 'user_created') as user_decks
        FROM flashcard_decks
        WHERE user_id = :userId
        """, nativeQuery = true)
    Object[] getDeckStatistics(@Param("userId") UUID userId);
}
