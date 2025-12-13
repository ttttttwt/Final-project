package com.lexia.backend.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.*;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.repository.FlashcardDeckRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.UserFlashcardProgressRepository;
import com.lexia.backend.service.ai.impl.FlashcardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FlashcardServiceImpl.
 * Tests deck CRUD, AI generation, study sessions, and progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@ExtendWith(MockitoExtension.class)
class FlashcardServiceImplTest {

    @Mock
    private FlashcardDeckRepository deckRepository;

    @Mock
    private UserFlashcardProgressRepository progressRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private GeminiClientService geminiClientService;

    @Mock
    private AiUsageTracker aiUsageTracker;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    @Captor
    private ArgumentCaptor<FlashcardDeck> deckCaptor;

    @Captor
    private ArgumentCaptor<List<UserFlashcardProgress>> progressListCaptor;

    private UUID userId;
    private UUID deckId;
    private FlashcardDeck testDeck;
    private List<FlashcardCard> testCards;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deckId = UUID.randomUUID();
        
        testCards = createTestCards();
        
        testDeck = FlashcardDeck.builder()
                .id(deckId)
                .userId(userId)
                .title("Test Deck")
                .description("Test Description")
                .sourceType(FlashcardDeck.SourceType.USER_CREATED)
                .cefrLevel("B1")
                .cards(new ArrayList<>(testCards))
                .cardCount(testCards.size())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private List<FlashcardCard> createTestCards() {
        List<FlashcardCard> cards = new ArrayList<>();
        
        cards.add(FlashcardCard.builder()
                .front("collaborate")
                .back(FlashcardBack.builder()
                        .definition("to work together with others")
                        .partOfSpeech("verb")
                        .pronunciation("/kəˈlæbəˌreɪt/")
                        .exampleSentence("We need to collaborate on this project.")
                        .synonyms(List.of("cooperate", "work together"))
                        .build())
                .tags(List.of("business", "b1"))
                .difficulty(3)
                .build());
        
        cards.add(FlashcardCard.builder()
                .front("deadline")
                .back(FlashcardBack.builder()
                        .definition("a time by which something must be done")
                        .partOfSpeech("noun")
                        .pronunciation("/ˈdedlaɪn/")
                        .exampleSentence("The deadline for submission is Friday.")
                        .synonyms(List.of("due date", "cutoff"))
                        .build())
                .tags(List.of("business", "b1"))
                .difficulty(2)
                .build());
        
        return cards;
    }

    // ========== Deck CRUD Tests ==========

    @Nested
    @DisplayName("createDeck Tests")
    class CreateDeckTests {

        @Test
        @DisplayName("Should create deck successfully with cards")
        void createDeck_Success_WithCards() {
            CreateFlashcardDeckDTO request = CreateFlashcardDeckDTO.builder()
                    .title("New Deck")
                    .description("New Description")
                    .cefrLevel("B1")
                    .sourceType("USER_CREATED")
                    .cards(List.of(
                            FlashcardCardDTO.builder()
                                    .front("test")
                                    .back(FlashcardBackDTO.builder()
                                            .definition("test definition")
                                            .build())
                                    .build()
                    ))
                    .build();

            when(deckRepository.save(any(FlashcardDeck.class))).thenAnswer(invocation -> {
                FlashcardDeck deck = invocation.getArgument(0);
                deck.setId(UUID.randomUUID());
                deck.setCreatedAt(Instant.now());
                deck.setUpdatedAt(Instant.now());
                return deck;
            });
            when(progressRepository.saveAll(anyList())).thenReturn(List.of());

            FlashcardDeckDTO result = flashcardService.createDeck(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New Deck");
            assertThat(result.getDescription()).isEqualTo("New Description");
            assertThat(result.getCefrLevel()).isEqualTo("B1");
            assertThat(result.getCardCount()).isEqualTo(1);

            verify(deckRepository).save(deckCaptor.capture());
            FlashcardDeck saved = deckCaptor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getSourceType()).isEqualTo(FlashcardDeck.SourceType.USER_CREATED);
        }

        @Test
        @DisplayName("Should create empty deck without cards")
        void createDeck_Success_Empty() {
            CreateFlashcardDeckDTO request = CreateFlashcardDeckDTO.builder()
                    .title("Empty Deck")
                    .build();

            when(deckRepository.save(any(FlashcardDeck.class))).thenAnswer(invocation -> {
                FlashcardDeck deck = invocation.getArgument(0);
                deck.setId(UUID.randomUUID());
                deck.setCreatedAt(Instant.now());
                deck.setUpdatedAt(Instant.now());
                return deck;
            });

            FlashcardDeckDTO result = flashcardService.createDeck(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Empty Deck");
            assertThat(result.getCardCount()).isEqualTo(0);
            
            // No progress initialization for empty deck
            verify(progressRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception for duplicate lesson deck")
        void createDeck_DuplicateLessonDeck_ThrowsException() {
            CreateFlashcardDeckDTO request = CreateFlashcardDeckDTO.builder()
                    .title("Lesson Deck")
                    .sourceType("LESSON")
                    .sourceId(100L)
                    .build();

            when(deckRepository.existsByUserIdAndSourceTypeAndSourceId(
                    eq(userId), eq(FlashcardDeck.SourceType.LESSON), eq(100L)))
                    .thenReturn(true);

            assertThatThrownBy(() -> flashcardService.createDeck(request, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("getDeck Tests")
    class GetDeckTests {

        @Test
        @DisplayName("Should get deck successfully")
        void getDeck_Success() {
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

            FlashcardDeckDTO result = flashcardService.getDeck(deckId, userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(deckId);
            assertThat(result.getTitle()).isEqualTo("Test Deck");
            assertThat(result.getCards()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw exception when deck not found")
        void getDeck_NotFound_ThrowsException() {
            when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flashcardService.getDeck(deckId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own deck")
        void getDeck_NotOwner_ThrowsException() {
            UUID otherUserId = UUID.randomUUID();
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

            assertThatThrownBy(() -> flashcardService.getDeck(deckId, otherUserId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("does not own");
        }
    }

    @Nested
    @DisplayName("getUserDecks Tests")
    class GetUserDecksTests {

        @Test
        @DisplayName("Should get user decks with pagination")
        void getUserDecks_WithPagination_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<FlashcardDeck> deckPage = new PageImpl<>(List.of(testDeck), pageable, 1);
            
            when(deckRepository.findByUserId(userId, pageable)).thenReturn(deckPage);

            Page<FlashcardDeckDTO> result = flashcardService.getUserDecks(userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Deck");
        }

        @Test
        @DisplayName("Should get all user decks without pagination")
        void getUserDecks_WithoutPagination_Success() {
            when(deckRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(testDeck));

            List<FlashcardDeckDTO> result = flashcardService.getUserDecks(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Deck");
        }

        @Test
        @DisplayName("Should get decks by source type")
        void getUserDecksBySourceType_Success() {
            when(deckRepository.findByUserIdAndSourceType(userId, FlashcardDeck.SourceType.LESSON))
                    .thenReturn(List.of(testDeck));

            List<FlashcardDeckDTO> result = flashcardService.getUserDecksBySourceType(userId, "LESSON");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateDeck Tests")
    class UpdateDeckTests {

        @Test
        @DisplayName("Should update deck title and description")
        void updateDeck_TitleAndDescription_Success() {
            UpdateFlashcardDeckDTO request = UpdateFlashcardDeckDTO.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(deckRepository.save(any(FlashcardDeck.class))).thenReturn(testDeck);

            FlashcardDeckDTO result = flashcardService.updateDeck(deckId, request, userId);

            assertThat(result).isNotNull();
            verify(deckRepository).save(deckCaptor.capture());
            assertThat(deckCaptor.getValue().getTitle()).isEqualTo("Updated Title");
            assertThat(deckCaptor.getValue().getDescription()).isEqualTo("Updated Description");
        }

        @Test
        @DisplayName("Should update deck cards and reset progress")
        void updateDeck_Cards_ResetsProgress() {
            UpdateFlashcardDeckDTO request = UpdateFlashcardDeckDTO.builder()
                    .cards(List.of(
                            FlashcardCardDTO.builder()
                                    .front("new card")
                                    .back(FlashcardBackDTO.builder()
                                            .definition("new definition")
                                            .build())
                                    .build()
                    ))
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(deckRepository.save(any(FlashcardDeck.class))).thenReturn(testDeck);
            when(progressRepository.deleteByUserIdAndDeckId(userId, deckId)).thenReturn(2);
            when(progressRepository.saveAll(anyList())).thenReturn(List.of());

            flashcardService.updateDeck(deckId, request, userId);

            verify(progressRepository).deleteByUserIdAndDeckId(userId, deckId);
            verify(progressRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should not update when no changes")
        void updateDeck_NoChanges_SkipsUpdate() {
            UpdateFlashcardDeckDTO request = UpdateFlashcardDeckDTO.builder().build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

            flashcardService.updateDeck(deckId, request, userId);

            verify(deckRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDeck Tests")
    class DeleteDeckTests {

        @Test
        @DisplayName("Should delete deck and progress")
        void deleteDeck_Success() {
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(progressRepository.deleteByUserIdAndDeckId(userId, deckId)).thenReturn(2);

            flashcardService.deleteDeck(deckId, userId);

            verify(progressRepository).deleteByUserIdAndDeckId(userId, deckId);
            verify(deckRepository).delete(testDeck);
        }

        @Test
        @DisplayName("Should throw exception when not owner")
        void deleteDeck_NotOwner_ThrowsException() {
            UUID otherUserId = UUID.randomUUID();
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

            assertThatThrownBy(() -> flashcardService.deleteDeck(deckId, otherUserId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(deckRepository, never()).delete(any());
        }
    }

    // ========== Card Management Tests ==========

    @Nested
    @DisplayName("Card Management Tests")
    class CardManagementTests {

        @Test
        @DisplayName("Should add card to deck")
        void addCard_Success() {
            FlashcardCardDTO newCard = FlashcardCardDTO.builder()
                    .front("new word")
                    .back(FlashcardBackDTO.builder()
                            .definition("new definition")
                            .build())
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(deckRepository.save(any(FlashcardDeck.class))).thenReturn(testDeck);
            when(progressRepository.save(any(UserFlashcardProgress.class)))
                    .thenReturn(UserFlashcardProgress.builder().build());

            FlashcardDeckDTO result = flashcardService.addCard(deckId, newCard, userId);

            assertThat(result).isNotNull();
            verify(deckRepository).save(deckCaptor.capture());
            assertThat(deckCaptor.getValue().getCards()).hasSize(3);
            verify(progressRepository).save(any(UserFlashcardProgress.class));
        }

        @Test
        @DisplayName("Should update card in deck")
        void updateCard_Success() {
            FlashcardCardDTO updatedCard = FlashcardCardDTO.builder()
                    .front("updated word")
                    .back(FlashcardBackDTO.builder()
                            .definition("updated definition")
                            .build())
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(deckRepository.save(any(FlashcardDeck.class))).thenReturn(testDeck);

            flashcardService.updateCard(deckId, 0, updatedCard, userId);

            verify(deckRepository).save(deckCaptor.capture());
            assertThat(deckCaptor.getValue().getCards().get(0).getFront()).isEqualTo("updated word");
        }

        @Test
        @DisplayName("Should throw exception for invalid card index")
        void updateCard_InvalidIndex_ThrowsException() {
            FlashcardCardDTO updatedCard = FlashcardCardDTO.builder()
                    .front("updated")
                    .back(FlashcardBackDTO.builder().definition("def").build())
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));

            assertThatThrownBy(() -> flashcardService.updateCard(deckId, 99, updatedCard, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should remove card from deck")
        void removeCard_Success() {
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(deckRepository.save(any(FlashcardDeck.class))).thenReturn(testDeck);
            when(progressRepository.deleteByUserIdAndDeckIdAndCardIndex(userId, deckId, 0))
                    .thenReturn(1);
            when(progressRepository.findByUserIdAndDeckId(userId, deckId)).thenReturn(List.of());

            flashcardService.removeCard(deckId, 0, userId);

            verify(deckRepository).save(deckCaptor.capture());
            assertThat(deckCaptor.getValue().getCards()).hasSize(1);
            verify(progressRepository).deleteByUserIdAndDeckIdAndCardIndex(userId, deckId, 0);
        }
    }

    // ========== Study Session Tests ==========

    @Nested
    @DisplayName("Study Session Tests")
    class StudySessionTests {

        @Test
        @DisplayName("Should get study session with due cards")
        void getStudySession_Success() {
            List<UserFlashcardProgress> dueCards = List.of(
                    createProgress(0, 1),
                    createProgress(1, 0)
            );

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(progressRepository.findDueCardsForDeck(eq(userId), eq(deckId), any(Instant.class)))
                    .thenReturn(dueCards);
            when(progressRepository.findNewCardsForDeck(userId, deckId))
                    .thenReturn(List.of(createProgress(1, 0)));
            when(progressRepository.countDueCardsForDeck(eq(userId), eq(deckId), any(Instant.class)))
                    .thenReturn(2L);
            when(progressRepository.countByUserIdAndDeckIdAndMasteryLevel(eq(userId), eq(deckId), anyInt()))
                    .thenReturn(0L);

            FlashcardStudySessionDTO result = flashcardService.getStudySession(deckId, userId, 20);

            assertThat(result).isNotNull();
            assertThat(result.getDeckId()).isEqualTo(deckId);
            assertThat(result.getDeckTitle()).isEqualTo("Test Deck");
            assertThat(result.getTotalCards()).isEqualTo(2);
            assertThat(result.getStats()).isNotNull();
        }

        @Test
        @DisplayName("Should submit review results")
        void submitReview_Success() {
            UserFlashcardProgress progress = createProgress(0, 1);
            
            FlashcardReviewResultDTO results = FlashcardReviewResultDTO.builder()
                    .reviews(List.of(
                            FlashcardReviewResultDTO.CardReviewDTO.builder()
                                    .cardIndex(0)
                                    .quality(4)
                                    .build()
                    ))
                    .sessionTimeMs(60000L)
                    .build();

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(progressRepository.findByUserIdAndDeckIdAndCardIndex(userId, deckId, 0))
                    .thenReturn(Optional.of(progress));
            when(progressRepository.save(any(UserFlashcardProgress.class))).thenReturn(progress);
            when(progressRepository.findDueCardsForDeck(eq(userId), eq(deckId), any(Instant.class)))
                    .thenReturn(List.of());
            when(progressRepository.findNewCardsForDeck(userId, deckId)).thenReturn(List.of());
            when(progressRepository.countDueCardsForDeck(eq(userId), eq(deckId), any(Instant.class)))
                    .thenReturn(0L);
            when(progressRepository.countByUserIdAndDeckIdAndMasteryLevel(eq(userId), eq(deckId), anyInt()))
                    .thenReturn(0L);

            FlashcardStudySessionDTO result = flashcardService.submitReview(deckId, userId, results);

            assertThat(result).isNotNull();
            verify(progressRepository).save(any(UserFlashcardProgress.class));
        }

        private UserFlashcardProgress createProgress(int cardIndex, int reviewCount) {
            return UserFlashcardProgress.builder()
                    .id((long) cardIndex)
                    .userId(userId)
                    .deckId(deckId)
                    .cardIndex(cardIndex)
                    .masteryLevel(0)
                    .reviewCount(reviewCount)
                    .correctCount(0)
                    .intervalDays(1)
                    .build();
        }
    }

    // ========== Progress & Statistics Tests ==========

    @Nested
    @DisplayName("Progress Tests")
    class ProgressTests {

        @Test
        @DisplayName("Should get deck progress")
        void getDeckProgress_Success() {
            List<UserFlashcardProgress> progressList = List.of(
                    UserFlashcardProgress.builder()
                            .id(1L)
                            .userId(userId)
                            .deckId(deckId)
                            .cardIndex(0)
                            .masteryLevel(2)
                            .reviewCount(5)
                            .correctCount(4)
                            .intervalDays(6)
                            .build()
            );

            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(progressRepository.findByUserIdAndDeckId(userId, deckId)).thenReturn(progressList);

            List<FlashcardProgressDTO> result = flashcardService.getDeckProgress(deckId, userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMasteryLevel()).isEqualTo(2);
            assertThat(result.get(0).getReviewCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should get due card count")
        void getDueCardCount_Success() {
            when(deckRepository.findById(deckId)).thenReturn(Optional.of(testDeck));
            when(progressRepository.countDueCardsForDeck(eq(userId), eq(deckId), any(Instant.class)))
                    .thenReturn(5L);

            long result = flashcardService.getDueCardCount(deckId, userId);

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should get total due card count")
        void getTotalDueCardCount_Success() {
            when(progressRepository.countDueCards(eq(userId), any(Instant.class))).thenReturn(10L);

            long result = flashcardService.getTotalDueCardCount(userId);

            assertThat(result).isEqualTo(10L);
        }
    }

    // ========== Lesson Deck Tests ==========

    @Nested
    @DisplayName("Lesson Deck Tests")
    class LessonDeckTests {

        @Test
        @DisplayName("Should check if lesson deck exists")
        void hasLessonDeck_True() {
            when(deckRepository.existsByUserIdAndSourceTypeAndSourceId(
                    userId, FlashcardDeck.SourceType.LESSON, 100L))
                    .thenReturn(true);

            boolean result = flashcardService.hasLessonDeck(100L, userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should get lesson deck")
        void getLessonDeck_Success() {
            testDeck.setSourceType(FlashcardDeck.SourceType.LESSON);
            testDeck.setSourceId(100L);

            when(deckRepository.findByUserIdAndSourceTypeAndSourceId(
                    userId, FlashcardDeck.SourceType.LESSON, 100L))
                    .thenReturn(Optional.of(testDeck));

            FlashcardDeckDTO result = flashcardService.getLessonDeck(100L, userId);

            assertThat(result).isNotNull();
            assertThat(result.getSourceType()).isEqualTo("LESSON");
            assertThat(result.getSourceId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should return null when lesson deck not found")
        void getLessonDeck_NotFound() {
            when(deckRepository.findByUserIdAndSourceTypeAndSourceId(
                    userId, FlashcardDeck.SourceType.LESSON, 100L))
                    .thenReturn(Optional.empty());

            FlashcardDeckDTO result = flashcardService.getLessonDeck(100L, userId);

            assertThat(result).isNull();
        }
    }

    // ========== AI Generation Tests ==========

    @Nested
    @DisplayName("AI Generation Tests")
    class AIGenerationTests {

        @Test
        @DisplayName("Should throw exception when lesson deck already exists")
        void generateFromLesson_DuplicateDeck_ThrowsException() {
            Long lessonId = 100L;
            GenerateFlashcardsDTO request = GenerateFlashcardsDTO.builder()
                    .lessonId(lessonId)
                    .build();

            when(deckRepository.existsByUserIdAndSourceTypeAndSourceId(
                    userId, FlashcardDeck.SourceType.LESSON, lessonId))
                    .thenReturn(true);

            assertThatThrownBy(() -> flashcardService.generateFromLesson(request, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(geminiClientService, never()).generateContent(anyString());
        }

        @Test
        @DisplayName("Should throw exception when lesson not found")
        void generateFromLesson_LessonNotFound_ThrowsException() {
            Long lessonId = 999L;
            GenerateFlashcardsDTO request = GenerateFlashcardsDTO.builder()
                    .lessonId(lessonId)
                    .build();

            when(deckRepository.existsByUserIdAndSourceTypeAndSourceId(
                    userId, FlashcardDeck.SourceType.LESSON, lessonId))
                    .thenReturn(false);
            when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flashcardService.generateFromLesson(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== Statistics Tests ==========

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get user deck stats")
        void getUserDeckStats_Success() {
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 0)).thenReturn(10L);
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 1)).thenReturn(15L);
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 2)).thenReturn(20L);
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 3)).thenReturn(25L);
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 4)).thenReturn(5L);
            when(progressRepository.countByUserIdAndMasteryLevel(userId, 5)).thenReturn(5L);

            FlashcardStudySessionDTO.DeckStatsDTO result = flashcardService.getUserDeckStats(userId);

            assertThat(result).isNotNull();
            assertThat(result.getNewCount()).isEqualTo(10);
            assertThat(result.getLearningCount()).isEqualTo(15);
            assertThat(result.getReviewingCount()).isEqualTo(45); // 20 + 25
            assertThat(result.getMasteredCount()).isEqualTo(10); // 5 + 5
        }
    }
}
