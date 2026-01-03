package com.lexia.backend.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.mapper.FlashcardMapper;
import com.lexia.backend.repository.FlashcardDeckRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.UserFlashcardProgressRepository;
import com.lexia.backend.service.ai.AiUsageTracker;
import com.lexia.backend.service.ai.FlashcardService;
import com.lexia.backend.service.ai.FlashcardImageService;
import com.lexia.backend.service.ai.GeminiClientService;
import com.lexia.backend.service.ai.AIConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of FlashcardService with AI-powered flashcard generation.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>AI generation from lesson content via Google Gemini</li>
 * <li>Fallback generation when AI fails</li>
 * <li>SM-2 spaced repetition algorithm</li>
 * <li>Progress tracking per card</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardDeckRepository deckRepository;
    private final UserFlashcardProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final GeminiClientService geminiClientService;
    private final AiUsageTracker aiUsageTracker;
    private final ObjectMapper objectMapper;
    private final AIConfigService aiConfigService;
    private final FlashcardImageService flashcardImageService;

    /** Default number of cards to include in a study session */
    private static final int DEFAULT_STUDY_SESSION_SIZE = 20;

    /** Maximum cards per deck */
    private static final int MAX_CARDS_PER_DECK = 500;

    /** Pattern to extract JSON from AI response */
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}|\\[[\\s\\S]*\\]");

    /** Prompt template for flashcard generation from lesson */
    private static final String FLASHCARD_GENERATION_PROMPT = """
            You are an expert English vocabulary teacher creating flashcards for language learners.

            TASK: Generate %d vocabulary flashcards from the following lesson content.
            CEFR Level: %s
            Focus Areas: %s

            LESSON CONTENT:
            %s

            OUTPUT REQUIREMENTS:
            - Generate flashcards for the most important vocabulary words/phrases
            - Each card should have: word/phrase, definition, part of speech, pronunciation (IPA), example sentence
            - Include synonyms and collocations where relevant
            - Assign difficulty level (1-5) based on word frequency and complexity
            - Output ONLY valid JSON array matching this schema:

            [
              {
                "front": "collaborate",
                "back": {
                  "definition": "to work together with others towards a shared goal",
                  "partOfSpeech": "verb",
                  "pronunciation": "/kəˈlæbəˌreɪt/",
                  "exampleSentence": "The two departments will collaborate on the new project.",
                  "synonyms": ["cooperate", "work together", "team up"],
                  "collocations": ["collaborate with", "collaborate on", "closely collaborate"]
                },
                "tags": ["business", "teamwork"],
                "difficulty": 3
              }
            ]

            Generate exactly %d cards. Output ONLY the JSON array, no additional text.
            """;

    /** Prompt template for flashcard generation from topic */
    private static final String TOPIC_FLASHCARD_GENERATION_PROMPT = """
            You are an expert English vocabulary teacher creating flashcards for language learners.

            TASK: Generate %d vocabulary flashcards about the topic: %s
            CEFR Level: %s
            Focus Areas: %s
            Additional Context: %s

            OUTPUT REQUIREMENTS:
            - Generate flashcards for vocabulary words/phrases commonly used in this topic
            - Use the additional context to focus on specific aspects of the topic if provided
            - Each card should have: word/phrase, definition, part of speech, pronunciation (IPA), example sentence
            - Include synonyms and collocations where relevant
            - Assign difficulty level (1-5) based on word frequency and complexity
            - Make sure vocabulary is appropriate for the CEFR level
            - Output ONLY valid JSON array matching this schema:

            [
              {
                "front": "negotiate",
                "back": {
                  "definition": "to discuss something with someone to reach an agreement",
                  "partOfSpeech": "verb",
                  "pronunciation": "/nɪˈɡoʊʃieɪt/",
                  "exampleSentence": "We need to negotiate the terms of the contract.",
                  "synonyms": ["bargain", "discuss", "work out"],
                  "collocations": ["negotiate with", "negotiate a deal", "negotiate terms"]
                },
                "tags": ["business", "communication"],
                "difficulty": 3
              }
            ]

            Generate exactly %d cards. Output ONLY the JSON array, no additional text.
            """;

    // ========== Generation Methods ==========

    @Override
    public FlashcardDeckDTO generateFromLesson(GenerateFlashcardsDTO request, UUID userId) {
        // Check if feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("flashcards");
        if (!featureConfig.isEnabled()) {
            throw new AiServiceException("Flashcards feature is currently disabled by administrator");
        }

        log.info("Generating flashcards from lesson {} for user {}", request.getLessonId(), userId);

        // Check if deck already exists for this lesson
        if (hasLessonDeck(request.getLessonId(), userId)) {
            throw new IllegalStateException("A flashcard deck already exists for this lesson");
        }

        // Fetch the lesson
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", request.getLessonId()));

        // Extract content from lesson
        String lessonContent = extractLessonContent(lesson);
        String cefrLevel = request.getCefrLevel() != null ? request.getCefrLevel() : determineCefrLevel(lesson);
        int maxCards = request.getMaxCards() != null ? request.getMaxCards() : 20;

        // Generate flashcards using AI
        List<FlashcardCard> cards;
        boolean usedFallback = false;

        try {
            cards = generateCardsWithAI(lessonContent, cefrLevel, maxCards, request.getFocusAreas(), userId,
                    featureConfig);
        } catch (Exception e) {
            log.warn("AI generation failed, using fallback: {}", e.getMessage());
            cards = generateFallbackCards(lessonContent, cefrLevel, maxCards);
            usedFallback = true;
        }

        // Create the deck
        String title = request.getCustomTitle() != null ? request.getCustomTitle() : "Vocabulary: " + lesson.getTitle();

        FlashcardDeck deck = FlashcardDeck.builder()
                .userId(userId)
                .title(title)
                .description("Generated from lesson: " + lesson.getTitle())
                .sourceType(FlashcardDeck.SourceType.LESSON)
                .sourceId(request.getLessonId())
                .cefrLevel(cefrLevel)
                .cards(cards)
                .cardCount(cards.size())
                .build();

        // Set PENDING status on all cards so frontend polling triggers
        setImageStatusPendingForAllCards(cards);

        deck = deckRepository.save(deck);

        // Initialize progress records for all cards
        initializeProgressRecords(deck, userId);

        log.info("Created flashcard deck {} with {} cards (fallback: {})",
                deck.getId(), cards.size(), usedFallback);

        // Trigger async image generation
        triggerImageGeneration(deck.getId(), userId);

        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    @Override
    public FlashcardDeckDTO generateFromTopic(GenerateFlashcardsByTopicDTO request, UUID userId) {
        // Check if feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("flashcards");
        if (!featureConfig.isEnabled()) {
            throw new AiServiceException("Flashcards feature is currently disabled by administrator");
        }

        log.info("Generating flashcards from topic '{}' for user {}", request.getTopic(), userId);

        String cefrLevel = request.getCefrLevelOrDefault();
        int maxCards = request.getCardCountOrDefault();
        String focusAreasStr = request.getFocusAreas() != null && !request.getFocusAreas().isEmpty()
                ? String.join(", ", request.getFocusAreas())
                : "general vocabulary";

        // Get description for additional context
        String description = request.getDescription() != null ? request.getDescription() : "";

        // Generate flashcards using AI
        List<FlashcardCard> cards;
        boolean usedFallback = false;

        try {
            cards = generateCardsFromTopicWithAI(request.getTopic(), cefrLevel, maxCards, focusAreasStr, description,
                    userId,
                    featureConfig);
        } catch (Exception e) {
            log.warn("AI generation from topic failed, using fallback: {}", e.getMessage());
            cards = generateFallbackCardsForTopic(request.getTopic(), cefrLevel, maxCards);
            usedFallback = true;
        }

        // Validate we got cards
        if (cards == null || cards.isEmpty()) {
            throw new AiServiceException("Failed to generate flashcards for topic: " + request.getTopic());
        }

        // Create the deck
        String title = request.getEffectiveTitle();
        String deckDescription = request.getDescription() != null ? request.getDescription()
                : "AI-generated vocabulary for: " + request.getTopic();

        FlashcardDeck deck = FlashcardDeck.builder()
                .userId(userId)
                .title(title)
                .description(deckDescription)
                .sourceType(FlashcardDeck.SourceType.AI_GENERATED)
                .cefrLevel(cefrLevel)
                .cards(cards)
                .cardCount(cards.size())
                .build();

        // Set PENDING status on all cards so frontend polling triggers
        setImageStatusPendingForAllCards(cards);

        deck = deckRepository.save(deck);

        // Initialize progress records for all cards
        initializeProgressRecords(deck, userId);

        log.info("Created AI-generated deck {} with {} cards from topic '{}' (fallback: {})",
                deck.getId(), cards.size(), request.getTopic(), usedFallback);

        // Trigger async image generation
        triggerImageGeneration(deck.getId(), userId);

        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    // ========== Deck CRUD Methods ==========

    @Override
    public FlashcardDeckDTO createDeck(CreateFlashcardDeckDTO request, UUID userId) {
        log.debug("Creating deck '{}' for user {}", request.getTitle(), userId);

        // Validate request
        request.validate();

        // Check source constraints
        if ("LESSON".equals(request.getSourceType()) && request.getSourceId() != null) {
            if (hasLessonDeck(request.getSourceId(), userId)) {
                throw new IllegalStateException("A flashcard deck already exists for this lesson");
            }
        }

        FlashcardDeck.SourceType sourceType = request.getSourceType() != null
                ? FlashcardDeck.SourceType.valueOf(request.getSourceType())
                : FlashcardDeck.SourceType.USER_CREATED;

        List<FlashcardCard> cards = request.getCards() != null ? FlashcardMapper.toCardEntityList(request.getCards())
                : new ArrayList<>();

        FlashcardDeck deck = FlashcardDeck.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceType(sourceType)
                .sourceId(request.getSourceId())
                .cefrLevel(request.getCefrLevel())
                .cards(cards)
                .cardCount(cards.size())
                .build();

        deck = deckRepository.save(deck);

        // Initialize progress records
        if (!cards.isEmpty()) {
            initializeProgressRecords(deck, userId);
        }

        log.info("Created deck {} with {} cards", deck.getId(), cards.size());

        // Trigger async image generation if deck has cards
        if (!cards.isEmpty()) {
            triggerImageGeneration(deck.getId(), userId);
        }

        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardDeckDTO getDeck(UUID deckId, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);
        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardDeckDTO getDeckSummary(UUID deckId, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);
        return enrichDeckDTO(FlashcardMapper.toDeckDTOWithoutCards(deck));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashcardDeckDTO> getUserDecks(UUID userId, Pageable pageable) {
        return deckRepository.findByUserId(userId, pageable)
                .map(FlashcardMapper::toDeckDTOWithoutCards)
                .map(this::enrichDeckDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardDeckDTO> getUserDecks(UUID userId) {
        return deckRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FlashcardMapper::toDeckDTOWithoutCards)
                .map(this::enrichDeckDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardDeckDTO> getUserDecksBySourceType(UUID userId, String sourceType) {
        FlashcardDeck.SourceType type = FlashcardDeck.SourceType.valueOf(sourceType.toUpperCase());
        return deckRepository.findByUserIdAndSourceType(userId, type).stream()
                .map(FlashcardMapper::toDeckDTOWithoutCards)
                .map(this::enrichDeckDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FlashcardDeckDTO updateDeck(UUID deckId, UpdateFlashcardDeckDTO request, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        if (!request.hasUpdates()) {
            return FlashcardMapper.toDeckDTO(deck);
        }

        if (request.getTitle() != null) {
            deck.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            deck.setDescription(request.getDescription());
        }
        if (request.getCefrLevel() != null) {
            deck.setCefrLevel(request.getCefrLevel());
        }
        if (request.getCards() != null) {
            // Smart diffing: preserve progress for unchanged cards
            updateCardsWithProgressPreservation(deck, request.getCards(), userId, deckId);
        }

        deck = deckRepository.save(deck);
        log.info("Updated deck {}", deckId);
        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    /**
     * Updates deck cards while preserving progress for unchanged cards.
     * Cards are matched by front text and back content.
     */
    private void updateCardsWithProgressPreservation(
            FlashcardDeck deck,
            List<FlashcardCardDTO> newCardDTOs,
            UUID userId,
            UUID deckId) {

        List<FlashcardCard> oldCards = deck.getCards();
        List<FlashcardCard> newCards = FlashcardMapper.toCardEntityList(newCardDTOs);

        // Build map of old cards by signature (front + backs) to their index
        Map<String, Integer> oldCardIndexMap = new HashMap<>();
        for (int i = 0; i < oldCards.size(); i++) {
            oldCardIndexMap.put(getCardSignature(oldCards.get(i)), i);
        }

        // Find which old card indices to preserve progress for
        Map<Integer, Integer> oldToNewIndexMap = new HashMap<>();
        Set<Integer> newCardIndicesNeedingProgress = new HashSet<>();

        for (int newIndex = 0; newIndex < newCards.size(); newIndex++) {
            String signature = getCardSignature(newCards.get(newIndex));
            Integer oldIndex = oldCardIndexMap.get(signature);

            if (oldIndex != null) {
                // Card exists in old deck, map progress from old index to new index
                oldToNewIndexMap.put(oldIndex, newIndex);
            } else {
                // New card, will need progress initialized
                newCardIndicesNeedingProgress.add(newIndex);
            }
        }

        // Update card indices in existing progress records
        List<UserFlashcardProgress> existingProgress = progressRepository.findByUserIdAndDeckId(userId, deckId);

        List<UserFlashcardProgress> progressToKeep = new ArrayList<>();
        for (UserFlashcardProgress progress : existingProgress) {
            Integer newIndex = oldToNewIndexMap.get(progress.getCardIndex());
            if (newIndex != null) {
                // Create NEW progress record with updated card index
                // (we can't reuse the old entity after delete-all as it has stale ID)
                UserFlashcardProgress newProgress = UserFlashcardProgress.builder()
                        .userId(userId)
                        .deck(deck)
                        .cardIndex(newIndex)
                        .easeFactor(progress.getEaseFactor())
                        .intervalDays(progress.getIntervalDays())
                        .masteryLevel(progress.getMasteryLevel())
                        .nextReviewAt(progress.getNextReviewAt())
                        .lastReviewedAt(progress.getLastReviewedAt())
                        .reviewCount(progress.getReviewCount())
                        .correctCount(progress.getCorrectCount())
                        .consecutiveCorrect(progress.getConsecutiveCorrect())
                        .build();
                progressToKeep.add(newProgress);
            }
            // Else: progress is for a removed card, don't keep it
        }

        // Delete all old progress and save fresh copies
        progressRepository.deleteByUserIdAndDeckId(userId, deckId);
        if (!progressToKeep.isEmpty()) {
            progressRepository.saveAll(progressToKeep);
        }

        // Set new cards on deck
        deck.setCards(newCards);
        deck.updateCardCount();

        // Initialize progress for truly new cards
        if (!newCardIndicesNeedingProgress.isEmpty()) {
            List<UserFlashcardProgress> newProgressRecords = newCardIndicesNeedingProgress.stream()
                    .map(cardIndex -> UserFlashcardProgress.builder()
                            .userId(userId)
                            .deck(deck)
                            .cardIndex(cardIndex)
                            .build())
                    .collect(Collectors.toList());
            progressRepository.saveAll(newProgressRecords);
        }

        // Trigger AI image generation for new cards with imageSource = "AI"
        List<Integer> cardsToGenerate = new ArrayList<>();
        for (Integer cardIndex : newCardIndicesNeedingProgress) {
            FlashcardCard card = newCards.get(cardIndex);
            FlashcardBack back = card.getBack();
            if (back != null && "AI".equals(back.getImageSource())) {
                // Set status to PENDING (will be saved with deck)
                back.setImageStatus("PENDING");
                cardsToGenerate.add(cardIndex);
            }
        }

        if (!cardsToGenerate.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (Integer cardIndex : cardsToGenerate) {
                        log.info("Triggering AI image generation for deck {} card {}", deckId, cardIndex);
                        try {
                            flashcardImageService.generateImageForCard(deckId, cardIndex, userId);
                        } catch (Exception e) {
                            log.error("Failed to trigger image generation for card {}", cardIndex, e);
                        }
                    }
                }
            });
        }

        log.debug("Updated cards for deck {}: preserved {} progress records, created {} new ones",
                deckId, progressToKeep.size(), newCardIndicesNeedingProgress.size());
    }

    /**
     * Generates a unique signature for a card based on front and back content.
     */
    private String getCardSignature(FlashcardCard card) {
        StringBuilder sig = new StringBuilder();
        sig.append(normalizeString(card.getFront()));
        if (card.getBack() != null) {
            FlashcardBack back = card.getBack();
            sig.append("|def:").append(normalizeString(back.getDefinition()));
            // Treat null and empty string as same for optional fields
            String pos = normalizeString(back.getPartOfSpeech());
            if (!pos.isEmpty()) {
                sig.append("|pos:").append(pos);
            }
        }
        return sig.toString();
    }

    private String normalizeString(String input) {
        return input == null ? "" : input.trim();
    }

    /**
     * Sets PENDING image status for all cards so frontend polling triggers.
     */
    private void setImageStatusPendingForAllCards(List<FlashcardCard> cards) {
        for (FlashcardCard card : cards) {
            if (card.getBack() != null) {
                card.getBack().setImageStatus("PENDING");
                card.getBack().setImageSource("AI");
            }
        }
    }

    @Override
    public void deleteDeck(UUID deckId, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        // Delete progress records first
        progressRepository.deleteByUserIdAndDeckId(userId, deckId);

        // Delete the deck
        deckRepository.delete(deck);
        log.info("Deleted deck {} for user {}", deckId, userId);
    }

    // ========== Card Management Methods ==========

    @Override
    public FlashcardDeckDTO addCard(UUID deckId, FlashcardCardDTO cardDTO, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        if (deck.getCardCount() >= MAX_CARDS_PER_DECK) {
            throw new IllegalStateException("Deck has reached maximum capacity of " + MAX_CARDS_PER_DECK + " cards");
        }

        FlashcardCard card = FlashcardMapper.toCardEntity(cardDTO);
        deck.addCard(card);
        deck = deckRepository.save(deck);

        // Initialize progress for the new card
        int newCardIndex = deck.getCards().size() - 1;
        UserFlashcardProgress progress = UserFlashcardProgress.builder()
                .userId(userId)
                .deck(deck)
                .cardIndex(newCardIndex)
                .build();
        progressRepository.save(progress);

        log.debug("Added card to deck {}, new count: {}", deckId, deck.getCardCount());
        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    @Override
    public FlashcardDeckDTO updateCard(UUID deckId, int cardIndex, FlashcardCardDTO cardDTO, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        if (cardIndex < 0 || cardIndex >= deck.getCards().size()) {
            throw new ResourceNotFoundException("Card", "index " + cardIndex);
        }

        FlashcardCard card = FlashcardMapper.toCardEntity(cardDTO);
        deck.getCards().set(cardIndex, card);
        deck = deckRepository.save(deck);

        log.debug("Updated card {} in deck {}", cardIndex, deckId);
        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    @Override
    public FlashcardDeckDTO removeCard(UUID deckId, int cardIndex, UUID userId) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        if (cardIndex < 0 || cardIndex >= deck.getCards().size()) {
            throw new ResourceNotFoundException("Card", "index " + cardIndex);
        }

        deck.removeCard(cardIndex);
        deck = deckRepository.save(deck);

        // Remove and re-index progress records
        progressRepository.deleteByUserIdAndDeckIdAndCardIndex(userId, deckId, cardIndex);
        reindexProgressRecords(userId, deckId, cardIndex);

        log.debug("Removed card {} from deck {}, new count: {}", cardIndex, deckId, deck.getCardCount());
        return enrichDeckDTO(FlashcardMapper.toDeckDTO(deck));
    }

    // ========== Study Session Methods ==========

    @Override
    @Transactional(readOnly = true)
    public FlashcardStudySessionDTO getStudySession(UUID deckId, UUID userId, Integer maxCards) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);
        int sessionSize = maxCards != null ? Math.min(maxCards, DEFAULT_STUDY_SESSION_SIZE * 2)
                : DEFAULT_STUDY_SESSION_SIZE;
        Instant now = Instant.now();

        // Get due cards
        List<UserFlashcardProgress> dueCards = progressRepository.findDueCardsForDeck(userId, deckId, now);

        // Get new cards (never reviewed)
        List<UserFlashcardProgress> newCards = progressRepository.findNewCardsForDeck(userId, deckId);

        // Combine and limit
        List<UserFlashcardProgress> sessionCards = new ArrayList<>();

        // Add overdue cards first
        for (UserFlashcardProgress card : dueCards) {
            if (sessionCards.size() >= sessionSize)
                break;
            if (card.getReviewCount() > 0) {
                sessionCards.add(card);
            }
        }

        // Then add new cards
        for (UserFlashcardProgress card : newCards) {
            if (sessionCards.size() >= sessionSize)
                break;
            sessionCards.add(card);
        }

        // Map to DTOs with card data
        List<FlashcardProgressDTO> cardsToStudy = sessionCards.stream()
                .map(progress -> {
                    FlashcardProgressDTO dto = FlashcardMapper.toProgressDTO(progress);
                    // Include the card data
                    if (progress.getCardIndex() < deck.getCards().size()) {
                        FlashcardCard card = deck.getCards().get(progress.getCardIndex());
                        dto.setCard(FlashcardMapper.toCardDTO(card));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // Calculate statistics
        FlashcardStudySessionDTO.DeckStatsDTO stats = calculateDeckStats(userId, deckId);

        return FlashcardStudySessionDTO.builder()
                .deckId(deckId)
                .deckTitle(deck.getTitle())
                .sessionStartedAt(now)
                .totalCards(deck.getCardCount())
                .dueCards((int) progressRepository.countDueCardsForDeck(userId, deckId, now))
                .newCards(newCards.size())
                .sessionSize(cardsToStudy.size())
                .cardsToStudy(cardsToStudy)
                .stats(stats)
                .build();
    }

    @Override
    public FlashcardStudySessionDTO submitReview(UUID deckId, UUID userId, FlashcardReviewResultDTO results) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);

        for (FlashcardReviewResultDTO.CardReviewDTO review : results.getReviews()) {
            Optional<UserFlashcardProgress> progressOpt = progressRepository
                    .findByUserIdAndDeckIdAndCardIndex(userId, deckId, review.getCardIndex());

            if (progressOpt.isPresent()) {
                UserFlashcardProgress progress = progressOpt.get();
                progress.recordReview(review.getQuality());
                progressRepository.save(progress);
            } else {
                log.warn("Progress record not found for card index {} in deck {}",
                        review.getCardIndex(), deckId);
            }
        }

        log.info("Submitted {} reviews for deck {}", results.getReviews().size(), deckId);

        // Return updated session
        return getStudySession(deckId, userId, DEFAULT_STUDY_SESSION_SIZE);
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardStudySessionDTO getPracticeSession(UUID deckId, UUID userId, Integer maxCards) {
        FlashcardDeck deck = findDeckWithOwnershipCheck(deckId, userId);
        int sessionSize = maxCards != null ? Math.min(maxCards, DEFAULT_STUDY_SESSION_SIZE * 2)
                : DEFAULT_STUDY_SESSION_SIZE;
        Instant now = Instant.now();

        // Get ALL progress records for this deck (not just due ones)
        List<UserFlashcardProgress> allCards = progressRepository.findByUserIdAndDeckId(userId, deckId);

        // Shuffle for random order
        Collections.shuffle(allCards);

        // Limit to session size
        List<UserFlashcardProgress> sessionCards = allCards.stream()
                .limit(sessionSize)
                .collect(Collectors.toList());

        // Map to DTOs with card data
        List<FlashcardProgressDTO> cardsToStudy = sessionCards.stream()
                .map(progress -> {
                    FlashcardProgressDTO dto = FlashcardMapper.toProgressDTO(progress);
                    if (progress.getCardIndex() < deck.getCards().size()) {
                        FlashcardCard card = deck.getCards().get(progress.getCardIndex());
                        dto.setCard(FlashcardMapper.toCardDTO(card));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // Calculate statistics
        FlashcardStudySessionDTO.DeckStatsDTO stats = calculateDeckStats(userId, deckId);

        return FlashcardStudySessionDTO.builder()
                .deckId(deckId)
                .deckTitle(deck.getTitle())
                .sessionStartedAt(now)
                .totalCards(deck.getCardCount())
                .dueCards(0) // Practice mode ignores due status
                .newCards(0)
                .sessionSize(cardsToStudy.size())
                .cardsToStudy(cardsToStudy)
                .stats(stats)
                .isPracticeMode(true) // Flag for frontend
                .build();
    }

    // ========== Progress & Statistics Methods ==========

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardProgressDTO> getDeckProgress(UUID deckId, UUID userId) {
        findDeckWithOwnershipCheck(deckId, userId);

        List<UserFlashcardProgress> progressList = progressRepository.findByUserIdAndDeckId(userId, deckId);
        return progressList.stream()
                .map(FlashcardMapper::toProgressDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getDueCardCount(UUID deckId, UUID userId) {
        findDeckWithOwnershipCheck(deckId, userId);
        return progressRepository.countDueCardsForDeck(userId, deckId, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDueCardCount(UUID userId) {
        return progressRepository.countDueCards(userId, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasLessonDeck(Long lessonId, UUID userId) {
        return deckRepository.existsByUserIdAndSourceTypeAndSourceId(
                userId, FlashcardDeck.SourceType.LESSON, lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardDeckDTO getLessonDeck(Long lessonId, UUID userId) {
        return deckRepository.findByUserIdAndSourceTypeAndSourceId(
                userId, FlashcardDeck.SourceType.LESSON, lessonId)
                .map(FlashcardMapper::toDeckDTO)
                .map(this::enrichDeckDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardStudySessionDTO.DeckStatsDTO getUserDeckStats(UUID userId) {
        long newCount = progressRepository.countByUserIdAndMasteryLevel(userId, 0);
        long learningCount = progressRepository.countByUserIdAndMasteryLevel(userId, 1);
        long reviewingCount = progressRepository.countByUserIdAndMasteryLevel(userId, 2) +
                progressRepository.countByUserIdAndMasteryLevel(userId, 3);
        long masteredCount = progressRepository.countByUserIdAndMasteryLevel(userId, 4) +
                progressRepository.countByUserIdAndMasteryLevel(userId, 5);

        return FlashcardStudySessionDTO.DeckStatsDTO.builder()
                .newCount((int) newCount)
                .learningCount((int) learningCount)
                .reviewingCount((int) reviewingCount)
                .masteredCount((int) masteredCount)
                .build();
    }

    // ========== Private Helper Methods ==========

    /**
     * Enriches the deck DTO with course and lesson titles if applicable.
     * Also populates progress statistics.
     */
    private FlashcardDeckDTO enrichDeckDTO(FlashcardDeckDTO dto) {
        if (dto == null)
            return null;

        // Populate Course/Lesson titles
        if ("LESSON".equals(dto.getSourceType()) && dto.getSourceId() != null) {
            try {
                lessonRepository.findById(dto.getSourceId()).ifPresent(lesson -> {
                    dto.setLessonTitle(lesson.getTitle());
                    if (lesson.getSection() != null && lesson.getSection().getCourse() != null) {
                        dto.setCourseTitle(lesson.getSection().getCourse().getTitle());
                    }
                });
            } catch (Exception e) {
                log.warn("Failed to fetch lesson details for deck {}: {}", dto.getId(), e.getMessage());
            }
        }

        // Populate Progress Stats
        try {
            // Due count
            long dueCount = progressRepository.countDueCardsForDeck(dto.getUserId(), dto.getId(), Instant.now());
            dto.setDueCount((int) dueCount);

            // Other stats
            Object[] stats = progressRepository.getDeckProgressStatistics(dto.getUserId(), dto.getId());
            if (stats != null && stats.length >= 8) {
                // Columns: 0:total, 1:new, 2:learning, 3:reviewing, 4:mastered, 5:reviews,
                // 6:correct, 7:accuracy
                dto.setNewCount(stats[1] != null ? ((Number) stats[1]).intValue() : 0);
                dto.setMasteredCount(stats[4] != null ? ((Number) stats[4]).intValue() : 0);
                dto.setAccuracyRate(stats[7] != null ? ((Number) stats[7]).doubleValue() : 0.0);
            }

            // Calculate average mastery level
            progressRepository.getAverageMasteryLevel(dto.getUserId(), dto.getId())
                    .ifPresent(dto::setMasteryLevel);

            // Get next review time (earliest among all cards)
            progressRepository.findEarliestNextReview(dto.getUserId(), dto.getId())
                    .ifPresent(dto::setNextReview);

            // Load per-card progress for mastery display
            List<UserFlashcardProgress> allProgress = progressRepository.findByUserIdAndDeckId(dto.getUserId(),
                    dto.getId());
            List<FlashcardProgressDTO> cardProgressList = allProgress.stream()
                    .map(FlashcardMapper::toProgressDTO)
                    .collect(Collectors.toList());
            dto.setCardProgress(cardProgressList);
        } catch (Exception e) {
            log.warn("Failed to fetch progress stats for deck {}: {}", dto.getId(), e.getMessage());
        }

        return dto;
    }

    /**
     * Finds a deck and verifies ownership.
     */
    private FlashcardDeck findDeckWithOwnershipCheck(UUID deckId, UUID userId) {
        FlashcardDeck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashcardDeck", deckId));

        if (!deck.getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this flashcard deck");
        }

        return deck;
    }

    /**
     * Initializes progress records for all cards in a deck.
     */
    private void initializeProgressRecords(FlashcardDeck deck, UUID userId) {
        List<UserFlashcardProgress> progressRecords = new ArrayList<>();

        for (int i = 0; i < deck.getCards().size(); i++) {
            UserFlashcardProgress progress = UserFlashcardProgress.builder()
                    .userId(userId)
                    .deck(deck)
                    .cardIndex(i)
                    .build();
            progressRecords.add(progress);
        }

        progressRepository.saveAll(progressRecords);
    }

    /**
     * Re-indexes progress records after a card is removed.
     */
    private void reindexProgressRecords(UUID userId, UUID deckId, int removedIndex) {
        List<UserFlashcardProgress> progressList = progressRepository.findByUserIdAndDeckId(userId, deckId);

        for (UserFlashcardProgress progress : progressList) {
            if (progress.getCardIndex() > removedIndex) {
                progress.setCardIndex(progress.getCardIndex() - 1);
                progressRepository.save(progress);
            }
        }
    }

    /**
     * Calculates statistics for a deck.
     */
    private FlashcardStudySessionDTO.DeckStatsDTO calculateDeckStats(UUID userId, UUID deckId) {
        long newCount = progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 0);
        long learningCount = progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 1);
        long reviewingCount = progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 2) +
                progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 3);
        long masteredCount = progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 4) +
                progressRepository.countByUserIdAndDeckIdAndMasteryLevel(userId, deckId, 5);

        return FlashcardStudySessionDTO.DeckStatsDTO.builder()
                .newCount((int) newCount)
                .learningCount((int) learningCount)
                .reviewingCount((int) reviewingCount)
                .masteredCount((int) masteredCount)
                .build();
    }

    /**
     * Triggers asynchronous image generation for a deck.
     * Uses TransactionSynchronization to ensure the deck is committed
     * before the async job tries to read it.
     */
    private void triggerImageGeneration(UUID deckId, UUID userId) {
        if (flashcardImageService != null) {
            // Register to run AFTER the current transaction commits
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                flashcardImageService.generateImagesForDeck(deckId, userId);
                                log.debug("Triggered image generation for deck {}", deckId);
                            } catch (Exception e) {
                                log.warn("Failed to trigger image generation for deck {}: {}", deckId, e.getMessage());
                            }
                        }
                    });
        }
    }

    private List<FlashcardCard> generateCardsWithAI(
            String lessonContent,
            String cefrLevel,
            int maxCards,
            List<String> focusAreas,
            UUID userId,
            com.lexia.backend.dto.ai.AIFeatureConfig featureConfig) {

        String focusAreasStr = focusAreas != null && !focusAreas.isEmpty() ? String.join(", ", focusAreas)
                : "general vocabulary";

        String prompt = String.format(FLASHCARD_GENERATION_PROMPT,
                maxCards, cefrLevel, focusAreasStr, lessonContent, maxCards);

        // Track AI usage
        long startTime = System.currentTimeMillis();

        try {
            // Use configured model settings
            GeminiResponseDTO response = geminiClientService.generateStructuredContent(
                    prompt,
                    featureConfig.getModelId(),
                    (float) featureConfig.getTemperature(),
                    featureConfig.getMaxTokens());

            // Track successful usage
            trackAiUsage(userId, response, System.currentTimeMillis() - startTime, true, null);

            return parseAIGeneratedCards(response.content());
        } catch (Exception e) {
            // Track failed usage
            trackAiUsage(userId, null, System.currentTimeMillis() - startTime, false, e.getMessage());
            throw new AiServiceException("Failed to generate flashcards with AI", e);
        }
    }

    /**
     * Generates flashcards from a topic using Gemini AI.
     */
    private List<FlashcardCard> generateCardsFromTopicWithAI(
            String topic,
            String cefrLevel,
            int maxCards,
            String focusAreas,
            String description,
            UUID userId,
            com.lexia.backend.dto.ai.AIFeatureConfig featureConfig) {

        // Use description for additional context, default to empty if not provided
        String contextInfo = (description != null && !description.isBlank())
                ? description
                : "No additional context provided";

        String prompt = String.format(TOPIC_FLASHCARD_GENERATION_PROMPT,
                maxCards, topic, cefrLevel, focusAreas, contextInfo, maxCards);

        // Track AI usage
        long startTime = System.currentTimeMillis();

        try {
            // Use configured model settings
            GeminiResponseDTO response = geminiClientService.generateStructuredContent(
                    prompt,
                    featureConfig.getModelId(),
                    (float) featureConfig.getTemperature(),
                    featureConfig.getMaxTokens());

            // Track successful usage
            trackAiUsage(userId, response, System.currentTimeMillis() - startTime, true, null);

            return parseAIGeneratedCards(response.content());
        } catch (Exception e) {
            // Track failed usage
            trackAiUsage(userId, null, System.currentTimeMillis() - startTime, false, e.getMessage());
            throw new AiServiceException("Failed to generate flashcards from topic with AI", e);
        }
    }

    /**
     * Generates fallback flashcards for a topic when AI fails.
     */
    private List<FlashcardCard> generateFallbackCardsForTopic(String topic, String cefrLevel, int maxCards) {
        log.info("Using fallback flashcard generation for topic: {}", topic);

        // Simple fallback: Create a basic card explaining that AI generation failed
        // In production, you might want to have pre-generated vocabulary lists by topic
        List<FlashcardCard> cards = new ArrayList<>();

        FlashcardCard card = new FlashcardCard();
        card.setFront(topic);
        FlashcardBack back = FlashcardBack.builder()
                .definition("Vocabulary topic for learning. Please add your own cards.")
                .partOfSpeech("noun")
                .exampleSentence("Study the vocabulary related to " + topic + ".")
                .build();
        card.setBack(back);
        card.setTags(List.of(topic.toLowerCase()));
        card.setDifficulty(2);
        cards.add(card);

        return cards;
    }

    /**
     * Parses AI-generated cards from JSON response.
     */
    private List<FlashcardCard> parseAIGeneratedCards(String content) {
        try {
            String jsonContent = extractJson(content);

            // Try to parse as array
            List<Map<String, Object>> cardMaps = objectMapper.readValue(
                    jsonContent, new TypeReference<List<Map<String, Object>>>() {
                    });

            return cardMaps.stream()
                    .map(this::mapToFlashcardCard)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI-generated cards: {}", e.getMessage());
            throw new AiServiceException("Failed to parse AI-generated flashcards", e);
        }
    }

    /**
     * Extracts JSON from AI response.
     */
    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new AiServiceException("Empty AI response");
        }

        Matcher matcher = JSON_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }

        // Try the whole content
        return content.trim();
    }

    /**
     * Maps a JSON map to a FlashcardCard.
     */
    @SuppressWarnings("unchecked")
    private FlashcardCard mapToFlashcardCard(Map<String, Object> map) {
        try {
            String front = (String) map.get("front");
            if (front == null || front.isBlank()) {
                return null;
            }

            Map<String, Object> backMap = (Map<String, Object>) map.get("back");
            FlashcardBack back = null;

            if (backMap != null) {
                back = FlashcardBack.builder()
                        .definition((String) backMap.get("definition"))
                        .partOfSpeech((String) backMap.get("partOfSpeech"))
                        .pronunciation((String) backMap.get("pronunciation"))
                        .exampleSentence((String) backMap.get("exampleSentence"))
                        .synonyms(backMap.get("synonyms") instanceof List ? (List<String>) backMap.get("synonyms")
                                : Collections.emptyList())
                        .collocations(
                                backMap.get("collocations") instanceof List ? (List<String>) backMap.get("collocations")
                                        : Collections.emptyList())
                        .build();
            }

            List<String> tags = map.get("tags") instanceof List ? (List<String>) map.get("tags")
                    : Collections.emptyList();

            Integer difficulty = map.get("difficulty") instanceof Number ? ((Number) map.get("difficulty")).intValue()
                    : 3;

            return FlashcardCard.builder()
                    .front(front)
                    .back(back)
                    .tags(tags)
                    .difficulty(difficulty)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse card: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generates fallback cards when AI fails.
     */
    private List<FlashcardCard> generateFallbackCards(String lessonContent, String cefrLevel, int maxCards) {
        List<FlashcardCard> cards = new ArrayList<>();

        // Extract potential vocabulary words (simple approach)
        Set<String> words = extractVocabularyWords(lessonContent);

        int count = 0;
        for (String word : words) {
            if (count >= maxCards)
                break;

            FlashcardCard card = FlashcardCard.builder()
                    .front(word)
                    .back(FlashcardBack.builder()
                            .definition("Definition for: " + word)
                            .build())
                    .tags(List.of(cefrLevel.toLowerCase()))
                    .difficulty(3)
                    .build();
            cards.add(card);
            count++;
        }

        return cards;
    }

    /**
     * Extracts vocabulary words from lesson content.
     */
    private Set<String> extractVocabularyWords(String content) {
        Set<String> words = new LinkedHashSet<>();

        // Parse JSON content to extract vocabulary
        try {
            JsonNode rootNode = objectMapper.readTree(content);
            extractWordsFromJsonNode(rootNode, words);
        } catch (JsonProcessingException e) {
            // Fall back to simple text extraction
            String[] tokens = content.split("\\s+");
            for (String token : tokens) {
                String cleaned = token.replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (cleaned.length() >= 4 && cleaned.length() <= 20) {
                    words.add(cleaned);
                }
            }
        }

        return words;
    }

    /**
     * Recursively extracts words from JSON nodes.
     */
    private void extractWordsFromJsonNode(JsonNode node, Set<String> words) {
        if (node.isTextual()) {
            String text = node.asText();
            // Look for vocabulary-related fields
            String[] tokens = text.split("\\s+");
            for (String token : tokens) {
                String cleaned = token.replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (cleaned.length() >= 4 && cleaned.length() <= 20) {
                    words.add(cleaned);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                extractWordsFromJsonNode(element, words);
            }
        } else if (node.isObject()) {
            // Prioritize vocabulary fields
            if (node.has("vocabulary")) {
                extractWordsFromJsonNode(node.get("vocabulary"), words);
            }
            if (node.has("keyTerms")) {
                extractWordsFromJsonNode(node.get("keyTerms"), words);
            }
            if (node.has("term")) {
                words.add(node.get("term").asText().toLowerCase());
            }
            if (node.has("word")) {
                words.add(node.get("word").asText().toLowerCase());
            }

            // Process other fields
            node.fields().forEachRemaining(entry -> extractWordsFromJsonNode(entry.getValue(), words));
        }
    }

    /**
     * Extracts content from a lesson for AI processing.
     */
    private String extractLessonContent(Lesson lesson) {
        StringBuilder content = new StringBuilder();
        content.append("Title: ").append(lesson.getTitle()).append("\n");
        content.append("Type: ").append(lesson.getLessonType()).append("\n\n");

        int initialLength = content.length();

        log.info("Extracting content for lesson {} (Type: {}). Raw content: {}",
                lesson.getId(), lesson.getLessonType(), lesson.getContent());

        // Parse and include relevant content
        try {
            JsonNode rootNode = objectMapper.readTree(lesson.getContent());
            log.info("Parsed JSON keys: {}", rootNode.fieldNames());

            // Extract based on lesson type
            switch (lesson.getLessonType()) {
                case READING:
                    // Handle 'passages' array (new schema)
                    if (rootNode.has("passages") && rootNode.get("passages").isArray()) {
                        content.append("Passages:\n");
                        for (JsonNode passage : rootNode.get("passages")) {
                            if (passage.has("title")) {
                                content.append("Title: ").append(passage.get("title").asText()).append("\n");
                            }
                            if (passage.has("text")) {
                                content.append(passage.get("text").asText()).append("\n\n");
                            }
                        }
                    }
                    // Handle 'passage' string (legacy/fallback)
                    else if (rootNode.has("passage")) {
                        content.append("Passage:\n").append(rootNode.get("passage").asText()).append("\n");
                    }

                    if (rootNode.has("vocabulary")) {
                        content.append("Vocabulary: ").append(rootNode.get("vocabulary").toString()).append("\n");
                    }
                    break;

                case LISTENING:
                    if (rootNode.has("transcript")) {
                        content.append("Transcript:\n").append(rootNode.get("transcript").asText()).append("\n");
                    }
                    if (rootNode.has("vocabulary")) {
                        content.append("Vocabulary: ").append(rootNode.get("vocabulary").toString()).append("\n");
                    }
                    break;

                case QUIZ:
                    if (rootNode.has("questions")) {
                        content.append("Questions: ").append(rootNode.get("questions").toString()).append("\n");
                    }
                    break;

                case SPEAKING:
                    if (rootNode.has("scenario")) {
                        content.append("Scenario: ").append(rootNode.get("scenario").asText()).append("\n");
                    }
                    if (rootNode.has("prompts")) {
                        content.append("Prompts: ").append(rootNode.get("prompts").toString()).append("\n");
                    }
                    if (rootNode.has("vocabulary")) {
                        content.append("Vocabulary: ").append(rootNode.get("vocabulary").toString()).append("\n");
                    }
                    break;
            }

            // If no structured content was extracted, fall back to the full JSON
            if (content.length() == initialLength) {
                log.warn("No structured content extracted for lesson {}. Falling back to raw JSON.", lesson.getId());
                content.append(rootNode.toPrettyString());
            }

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse lesson content as JSON: {}", e.getMessage());
            // Fall back to raw content
            content.append(lesson.getContent());
        }

        return content.toString();
    }

    /**
     * Determines CEFR level from lesson metadata.
     */
    private String determineCefrLevel(Lesson lesson) {
        // Try to extract from lesson content or section/course metadata
        try {
            JsonNode rootNode = objectMapper.readTree(lesson.getContent());
            if (rootNode.has("cefrLevel")) {
                return rootNode.get("cefrLevel").asText();
            }
        } catch (JsonProcessingException e) {
            // Ignore
        }

        // Default to B1 if not found
        return "B1";
    }

    /**
     * Tracks AI usage for monitoring and quotas.
     */
    private void trackAiUsage(UUID userId, GeminiResponseDTO response, long responseTimeMs,
            boolean success, String errorMessage) {
        try {
            // Fetch feature config to get the model ID if the response is null (for failure
            // tracking)
            String modelId = response != null ? response.model()
                    : aiConfigService.getFeatureConfig("flashcards").getModelId();

            AiUsageTrackingRequest trackingRequest = AiUsageTrackingRequest.builder()
                    .userId(userId)
                    .contentType(AiUsageTracker.CONTENT_TYPE_FLASHCARD)
                    .modelId(modelId)
                    .inputTokens(
                            response != null && response.tokenUsage() != null ? response.tokenUsage().inputTokens() : 0)
                    .outputTokens(
                            response != null && response.tokenUsage() != null ? response.tokenUsage().outputTokens()
                                    : 0)
                    .responseTimeMs((int) responseTimeMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();

            aiUsageTracker.trackUsageAsync(trackingRequest);
        } catch (Exception e) {
            log.warn("Failed to track AI usage: {}", e.getMessage());
        }
    }
}
