package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between Flashcard entities and DTOs.
 * Provides centralized mapping logic for flashcard data transformations.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
public class FlashcardMapper {

    private FlashcardMapper() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== FlashcardBack Mapping ==========

    /**
     * Converts FlashcardBack entity to FlashcardBackDTO.
     * 
     * @param back the entity
     * @return the DTO, or null if input is null
     */
    public static FlashcardBackDTO toBackDTO(FlashcardBack back) {
        if (back == null) {
            return null;
        }
        return FlashcardBackDTO.builder()
                .definition(back.getDefinition())
                .partOfSpeech(back.getPartOfSpeech())
                .pronunciation(back.getPronunciation())
                .exampleSentence(back.getExampleSentence())
                .synonyms(back.getSynonyms() != null ? back.getSynonyms() : Collections.emptyList())
                .collocations(back.getCollocations() != null ? back.getCollocations() : Collections.emptyList())
                .notes(back.getNotes())
                .imageUrl(back.getImageUrl())
                .imageStatus(back.getImageStatus())
                .imageSource(back.getImageSource())
                .audioUrl(back.getAudioUrl())
                .build();
    }

    /**
     * Converts FlashcardBackDTO to FlashcardBack entity.
     * 
     * @param dto the DTO
     * @return the entity, or null if input is null
     */
    public static FlashcardBack toBackEntity(FlashcardBackDTO dto) {
        if (dto == null) {
            return null;
        }
        return FlashcardBack.builder()
                .definition(dto.getDefinition())
                .partOfSpeech(dto.getPartOfSpeech())
                .pronunciation(dto.getPronunciation())
                .exampleSentence(dto.getExampleSentence())
                .synonyms(dto.getSynonyms() != null ? dto.getSynonyms() : Collections.emptyList())
                .collocations(dto.getCollocations() != null ? dto.getCollocations() : Collections.emptyList())
                .notes(dto.getNotes())
                .imageUrl(dto.getImageUrl())
                .imageStatus(dto.getImageStatus())
                .imageSource(dto.getImageSource())
                .audioUrl(dto.getAudioUrl())
                .build();
    }

    // ========== FlashcardCard Mapping ==========

    /**
     * Converts FlashcardCard entity to FlashcardCardDTO.
     * 
     * @param card the entity
     * @return the DTO, or null if input is null
     */
    public static FlashcardCardDTO toCardDTO(FlashcardCard card) {
        if (card == null) {
            return null;
        }
        return FlashcardCardDTO.builder()
                .front(card.getFront())
                .back(toBackDTO(card.getBack()))
                .tags(card.getTags() != null ? card.getTags() : Collections.emptyList())
                .difficulty(card.getDifficulty() != null ? card.getDifficulty() : 3)
                .build();
    }

    /**
     * Converts FlashcardCardDTO to FlashcardCard entity.
     * 
     * @param dto the DTO
     * @return the entity, or null if input is null
     */
    public static FlashcardCard toCardEntity(FlashcardCardDTO dto) {
        if (dto == null) {
            return null;
        }
        return FlashcardCard.builder()
                .front(dto.getFront())
                .back(toBackEntity(dto.getBack()))
                .tags(dto.getTags() != null ? dto.getTags() : Collections.emptyList())
                .difficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 3)
                .build();
    }

    /**
     * Converts list of FlashcardCard entities to DTOs.
     * 
     * @param cards the entities
     * @return the DTOs, or empty list if input is null
     */
    public static List<FlashcardCardDTO> toCardDTOList(List<FlashcardCard> cards) {
        if (cards == null) {
            return Collections.emptyList();
        }
        return cards.stream()
                .map(FlashcardMapper::toCardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of FlashcardCardDTOs to entities.
     * 
     * @param dtos the DTOs
     * @return the entities, or empty list if input is null
     */
    public static List<FlashcardCard> toCardEntityList(List<FlashcardCardDTO> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(FlashcardMapper::toCardEntity)
                .collect(Collectors.toList());
    }

    // ========== FlashcardDeck Mapping ==========

    /**
     * Converts FlashcardDeck entity to FlashcardDeckDTO.
     * Does not include progress information.
     * 
     * @param deck the entity
     * @return the DTO, or null if input is null
     */
    public static FlashcardDeckDTO toDeckDTO(FlashcardDeck deck) {
        if (deck == null) {
            return null;
        }
        return FlashcardDeckDTO.builder()
                .id(deck.getId())
                .userId(deck.getUserId())
                .title(deck.getTitle())
                .description(deck.getDescription())
                .sourceType(deck.getSourceType() != null ? deck.getSourceType().name() : null)
                .sourceId(deck.getSourceId())
                .cefrLevel(deck.getCefrLevel())
                .cards(toCardDTOList(deck.getCards()))
                .cardCount(deck.getCardCount())
                .createdAt(deck.getCreatedAt())
                .updatedAt(deck.getUpdatedAt())
                .build();
    }

    /**
     * Converts FlashcardDeck entity to FlashcardDeckDTO without cards.
     * Useful for listing decks.
     * 
     * @param deck the entity
     * @return the DTO without cards, or null if input is null
     */
    public static FlashcardDeckDTO toDeckDTOWithoutCards(FlashcardDeck deck) {
        if (deck == null) {
            return null;
        }
        return FlashcardDeckDTO.builder()
                .id(deck.getId())
                .userId(deck.getUserId())
                .title(deck.getTitle())
                .description(deck.getDescription())
                .sourceType(deck.getSourceType() != null ? deck.getSourceType().name() : null)
                .sourceId(deck.getSourceId())
                .cefrLevel(deck.getCefrLevel())
                .cardCount(deck.getCardCount())
                .createdAt(deck.getCreatedAt())
                .updatedAt(deck.getUpdatedAt())
                .build();
    }

    /**
     * Converts list of FlashcardDeck entities to DTOs without cards.
     * 
     * @param decks the entities
     * @return the DTOs, or empty list if input is null
     */
    public static List<FlashcardDeckDTO> toDeckDTOListWithoutCards(List<FlashcardDeck> decks) {
        if (decks == null) {
            return Collections.emptyList();
        }
        return decks.stream()
                .map(FlashcardMapper::toDeckDTOWithoutCards)
                .collect(Collectors.toList());
    }

    /**
     * Creates a FlashcardDeck entity from CreateFlashcardDeckDTO.
     * 
     * @param dto the create DTO
     * @return new entity
     * @throws IllegalArgumentException if dto is null
     */
    public static FlashcardDeck toEntity(CreateFlashcardDeckDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateFlashcardDeckDTO cannot be null");
        }

        List<FlashcardCard> cards = toCardEntityList(dto.getCards());

        return FlashcardDeck.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .sourceType(dto.getSourceType() != null
                        ? FlashcardDeck.SourceType.valueOf(dto.getSourceType())
                        : FlashcardDeck.SourceType.USER_CREATED)
                .sourceId(dto.getSourceId())
                .cefrLevel(dto.getCefrLevel())
                .cards(cards)
                .cardCount(cards.size())
                .build();
    }

    /**
     * Updates a FlashcardDeck entity from UpdateFlashcardDeckDTO.
     * Only updates non-null fields.
     * 
     * @param deck the entity to update
     * @param dto  the update data
     * @throws IllegalArgumentException if deck or dto is null
     */
    public static void updateEntityFromDTO(FlashcardDeck deck, UpdateFlashcardDeckDTO dto) {
        if (deck == null) {
            throw new IllegalArgumentException("FlashcardDeck cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateFlashcardDeckDTO cannot be null");
        }

        if (dto.getTitle() != null) {
            deck.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            deck.setDescription(dto.getDescription());
        }
        if (dto.getCefrLevel() != null) {
            deck.setCefrLevel(dto.getCefrLevel());
        }
        if (dto.getCards() != null) {
            deck.setCards(toCardEntityList(dto.getCards()));
            deck.updateCardCount();
        }
    }

    // ========== UserFlashcardProgress Mapping ==========

    /**
     * Converts UserFlashcardProgress entity to FlashcardProgressDTO.
     * 
     * @param progress the entity
     * @return the DTO, or null if input is null
     */
    public static FlashcardProgressDTO toProgressDTO(UserFlashcardProgress progress) {
        if (progress == null) {
            return null;
        }
        return FlashcardProgressDTO.builder()
                .id(progress.getId())
                .cardIndex(progress.getCardIndex())
                .masteryLevel(progress.getMasteryLevel())
                .masteryLevelName(progress.getMasteryLevelName())
                .reviewCount(progress.getReviewCount())
                .correctCount(progress.getCorrectCount())
                .accuracyRate(progress.getAccuracyRate())
                .easeFactor(progress.getEaseFactor())
                .intervalDays(progress.getIntervalDays())
                .lastReviewedAt(progress.getLastReviewedAt())
                .nextReviewAt(progress.getNextReviewAt())
                .isDue(progress.isDue())
                .build();
    }

    /**
     * Converts UserFlashcardProgress entity to FlashcardProgressDTO with card data.
     * 
     * @param progress the entity
     * @param card     the card data from the deck
     * @return the DTO with card, or null if progress is null
     */
    public static FlashcardProgressDTO toProgressDTOWithCard(
            UserFlashcardProgress progress,
            FlashcardCard card) {
        if (progress == null) {
            return null;
        }
        FlashcardProgressDTO dto = toProgressDTO(progress);
        dto.setCard(toCardDTO(card));
        return dto;
    }

    /**
     * Converts list of UserFlashcardProgress entities to DTOs.
     * 
     * @param progressList the entities
     * @return the DTOs, or empty list if input is null
     */
    public static List<FlashcardProgressDTO> toProgressDTOList(List<UserFlashcardProgress> progressList) {
        if (progressList == null) {
            return Collections.emptyList();
        }
        return progressList.stream()
                .map(FlashcardMapper::toProgressDTO)
                .collect(Collectors.toList());
    }

    // ========== Study Session Mapping ==========

    /**
     * Creates a FlashcardStudySessionDTO from deck and progress data.
     * 
     * @param deck     the flashcard deck
     * @param dueCards list of due progress records
     * @param stats    array of statistics from repository query
     * @return the study session DTO
     */
    public static FlashcardStudySessionDTO toStudySessionDTO(
            FlashcardDeck deck,
            List<UserFlashcardProgress> dueCards,
            Object[] stats) {

        List<FlashcardProgressDTO> cardsToStudy = dueCards.stream()
                .map(progress -> {
                    FlashcardCard card = deck.getCards().get(progress.getCardIndex());
                    return toProgressDTOWithCard(progress, card);
                })
                .collect(Collectors.toList());

        // Parse statistics array
        FlashcardStudySessionDTO.DeckStatsDTO deckStats = null;
        if (stats != null && stats.length >= 8) {
            deckStats = FlashcardStudySessionDTO.DeckStatsDTO.builder()
                    .newCount(((Number) stats[1]).intValue())
                    .learningCount(((Number) stats[2]).intValue())
                    .reviewingCount(((Number) stats[3]).intValue())
                    .masteredCount(((Number) stats[4]).intValue())
                    .totalReviews(((Number) stats[5]).longValue())
                    .overallAccuracy(stats[7] != null ? ((Number) stats[7]).doubleValue() : 0.0)
                    .build();
        }

        int newCardsCount = (int) dueCards.stream()
                .filter(p -> p.getReviewCount() == 0)
                .count();

        return FlashcardStudySessionDTO.builder()
                .deckId(deck.getId())
                .deckTitle(deck.getTitle())
                .sessionStartedAt(Instant.now())
                .totalCards(deck.getCardCount())
                .dueCards(dueCards.size())
                .newCards(newCardsCount)
                .sessionSize(cardsToStudy.size())
                .cardsToStudy(cardsToStudy)
                .stats(deckStats)
                .build();
    }
}
