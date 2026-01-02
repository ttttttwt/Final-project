package com.lexia.backend.service.ai;

import java.util.UUID;

/**
 * Service for generating AI-powered images for flashcards.
 * Uses Gemini Imagen 3.0 to create contextual illustrations.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
public interface FlashcardImageService {

    /**
     * Generates images for all cards in a deck asynchronously.
     * Each card's imageStatus will be updated as generation progresses.
     *
     * @param deckId the deck UUID
     * @param userId the user UUID (for quota tracking)
     */
    void generateImagesForDeck(UUID deckId, UUID userId);

    /**
     * Generates image for a single card.
     * Called internally by generateImagesForDeck.
     *
     * @param deckId    the deck UUID
     * @param cardIndex the index of the card in the deck
     * @param userId    the user UUID
     */
    void generateImageForCard(UUID deckId, int cardIndex, UUID userId);

    /**
     * Retries failed image generation for a deck.
     *
     * @param deckId the deck UUID
     * @param userId the user UUID
     */
    void retryFailedImages(UUID deckId, UUID userId);
}
