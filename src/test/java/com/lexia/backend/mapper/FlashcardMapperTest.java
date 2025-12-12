package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FlashcardMapper.
 * Tests all mapping methods for flashcard entities and DTOs.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@DisplayName("FlashcardMapper Tests")
class FlashcardMapperTest {

    // ========== Test Data Builders ==========
    
    private FlashcardBack createTestBack() {
        return FlashcardBack.builder()
                .definition("to work together towards a goal")
                .partOfSpeech("verb")
                .pronunciation("/kəˈlæbəˌreɪt/")
                .exampleSentence("We need to collaborate on this project.")
                .synonyms(Arrays.asList("cooperate", "team up"))
                .collocations(Arrays.asList("collaborate with", "collaborate on"))
                .notes("Common in business contexts")
                .build();
    }

    private FlashcardBackDTO createTestBackDTO() {
        return FlashcardBackDTO.builder()
                .definition("to work together towards a goal")
                .partOfSpeech("verb")
                .pronunciation("/kəˈlæbəˌreɪt/")
                .exampleSentence("We need to collaborate on this project.")
                .synonyms(Arrays.asList("cooperate", "team up"))
                .collocations(Arrays.asList("collaborate with", "collaborate on"))
                .notes("Common in business contexts")
                .build();
    }

    private FlashcardCard createTestCard() {
        return FlashcardCard.builder()
                .front("collaborate")
                .back(createTestBack())
                .tags(Arrays.asList("business", "b2"))
                .difficulty(3)
                .build();
    }

    private FlashcardCardDTO createTestCardDTO() {
        return FlashcardCardDTO.builder()
                .front("collaborate")
                .back(createTestBackDTO())
                .tags(Arrays.asList("business", "b2"))
                .difficulty(3)
                .build();
    }

    private FlashcardDeck createTestDeck() {
        return FlashcardDeck.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .title("Business Vocabulary")
                .description("Essential business terms")
                .sourceType(FlashcardDeck.SourceType.AI_GENERATED)
                .cefrLevel("B2")
                .cards(Arrays.asList(createTestCard()))
                .cardCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UserFlashcardProgress createTestProgress() {
        return UserFlashcardProgress.builder()
                .id(1L)
                .userId(UUID.randomUUID())
                .cardIndex(0)
                .masteryLevel(2)
                .reviewCount(5)
                .correctCount(4)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(6)
                .lastReviewedAt(Instant.now().minusSeconds(86400))
                .nextReviewAt(Instant.now().plusSeconds(86400 * 5))
                .createdAt(Instant.now().minusSeconds(86400 * 7))
                .build();
    }

    // ========== FlashcardBack Tests ==========

    @Nested
    @DisplayName("FlashcardBack Mapping")
    class FlashcardBackMappingTests {

        @Test
        @DisplayName("toBackDTO converts entity to DTO correctly")
        void toBackDTO_convertsCorrectly() {
            FlashcardBack back = createTestBack();

            FlashcardBackDTO dto = FlashcardMapper.toBackDTO(back);

            assertThat(dto).isNotNull();
            assertThat(dto.getDefinition()).isEqualTo(back.getDefinition());
            assertThat(dto.getPartOfSpeech()).isEqualTo(back.getPartOfSpeech());
            assertThat(dto.getPronunciation()).isEqualTo(back.getPronunciation());
            assertThat(dto.getExampleSentence()).isEqualTo(back.getExampleSentence());
            assertThat(dto.getSynonyms()).containsExactlyElementsOf(back.getSynonyms());
            assertThat(dto.getCollocations()).containsExactlyElementsOf(back.getCollocations());
            assertThat(dto.getNotes()).isEqualTo(back.getNotes());
        }

        @Test
        @DisplayName("toBackDTO returns null for null input")
        void toBackDTO_returnsNullForNull() {
            assertThat(FlashcardMapper.toBackDTO(null)).isNull();
        }

        @Test
        @DisplayName("toBackEntity converts DTO to entity correctly")
        void toBackEntity_convertsCorrectly() {
            FlashcardBackDTO dto = createTestBackDTO();

            FlashcardBack entity = FlashcardMapper.toBackEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getDefinition()).isEqualTo(dto.getDefinition());
            assertThat(entity.getPartOfSpeech()).isEqualTo(dto.getPartOfSpeech());
            assertThat(entity.getPronunciation()).isEqualTo(dto.getPronunciation());
        }

        @Test
        @DisplayName("toBackEntity returns null for null input")
        void toBackEntity_returnsNullForNull() {
            assertThat(FlashcardMapper.toBackEntity(null)).isNull();
        }

        @Test
        @DisplayName("toBackDTO handles null lists gracefully")
        void toBackDTO_handlesNullLists() {
            FlashcardBack back = FlashcardBack.builder()
                    .definition("test")
                    .synonyms(null)
                    .collocations(null)
                    .build();

            FlashcardBackDTO dto = FlashcardMapper.toBackDTO(back);

            assertThat(dto.getSynonyms()).isEmpty();
            assertThat(dto.getCollocations()).isEmpty();
        }
    }

    // ========== FlashcardCard Tests ==========

    @Nested
    @DisplayName("FlashcardCard Mapping")
    class FlashcardCardMappingTests {

        @Test
        @DisplayName("toCardDTO converts entity to DTO correctly")
        void toCardDTO_convertsCorrectly() {
            FlashcardCard card = createTestCard();

            FlashcardCardDTO dto = FlashcardMapper.toCardDTO(card);

            assertThat(dto).isNotNull();
            assertThat(dto.getFront()).isEqualTo(card.getFront());
            assertThat(dto.getBack()).isNotNull();
            assertThat(dto.getBack().getDefinition()).isEqualTo(card.getBack().getDefinition());
            assertThat(dto.getTags()).containsExactlyElementsOf(card.getTags());
            assertThat(dto.getDifficulty()).isEqualTo(card.getDifficulty());
        }

        @Test
        @DisplayName("toCardDTO returns null for null input")
        void toCardDTO_returnsNullForNull() {
            assertThat(FlashcardMapper.toCardDTO(null)).isNull();
        }

        @Test
        @DisplayName("toCardEntity converts DTO to entity correctly")
        void toCardEntity_convertsCorrectly() {
            FlashcardCardDTO dto = createTestCardDTO();

            FlashcardCard entity = FlashcardMapper.toCardEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getFront()).isEqualTo(dto.getFront());
            assertThat(entity.getBack()).isNotNull();
        }

        @Test
        @DisplayName("toCardDTOList converts list correctly")
        void toCardDTOList_convertsCorrectly() {
            List<FlashcardCard> cards = Arrays.asList(createTestCard(), createTestCard());

            List<FlashcardCardDTO> dtos = FlashcardMapper.toCardDTOList(cards);

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getFront()).isEqualTo("collaborate");
        }

        @Test
        @DisplayName("toCardDTOList returns empty list for null")
        void toCardDTOList_returnsEmptyForNull() {
            assertThat(FlashcardMapper.toCardDTOList(null)).isEmpty();
        }

        @Test
        @DisplayName("toCardEntityList converts list correctly")
        void toCardEntityList_convertsCorrectly() {
            List<FlashcardCardDTO> dtos = Arrays.asList(createTestCardDTO(), createTestCardDTO());

            List<FlashcardCard> entities = FlashcardMapper.toCardEntityList(dtos);

            assertThat(entities).hasSize(2);
        }

        @Test
        @DisplayName("toCardDTO handles null difficulty with default")
        void toCardDTO_handlesNullDifficulty() {
            FlashcardCard card = FlashcardCard.builder()
                    .front("test")
                    .back(createTestBack())
                    .difficulty(null)
                    .build();

            FlashcardCardDTO dto = FlashcardMapper.toCardDTO(card);

            assertThat(dto.getDifficulty()).isEqualTo(3); // default
        }
    }

    // ========== FlashcardDeck Tests ==========

    @Nested
    @DisplayName("FlashcardDeck Mapping")
    class FlashcardDeckMappingTests {

        @Test
        @DisplayName("toDeckDTO converts entity to DTO correctly")
        void toDeckDTO_convertsCorrectly() {
            FlashcardDeck deck = createTestDeck();

            FlashcardDeckDTO dto = FlashcardMapper.toDeckDTO(deck);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(deck.getId());
            assertThat(dto.getUserId()).isEqualTo(deck.getUserId());
            assertThat(dto.getTitle()).isEqualTo(deck.getTitle());
            assertThat(dto.getDescription()).isEqualTo(deck.getDescription());
            assertThat(dto.getSourceType()).isEqualTo("AI_GENERATED");
            assertThat(dto.getCefrLevel()).isEqualTo(deck.getCefrLevel());
            assertThat(dto.getCardCount()).isEqualTo(deck.getCardCount());
            assertThat(dto.getCards()).hasSize(1);
        }

        @Test
        @DisplayName("toDeckDTO returns null for null input")
        void toDeckDTO_returnsNullForNull() {
            assertThat(FlashcardMapper.toDeckDTO(null)).isNull();
        }

        @Test
        @DisplayName("toDeckDTOWithoutCards excludes cards")
        void toDeckDTOWithoutCards_excludesCards() {
            FlashcardDeck deck = createTestDeck();

            FlashcardDeckDTO dto = FlashcardMapper.toDeckDTOWithoutCards(deck);

            assertThat(dto).isNotNull();
            assertThat(dto.getTitle()).isEqualTo(deck.getTitle());
            assertThat(dto.getCards()).isNull();
            assertThat(dto.getCardCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("toEntity creates entity from CreateDTO")
        void toEntity_createsFromCreateDTO() {
            CreateFlashcardDeckDTO dto = CreateFlashcardDeckDTO.builder()
                    .title("Test Deck")
                    .description("Test description")
                    .sourceType("USER_CREATED")
                    .cefrLevel("B1")
                    .cards(Arrays.asList(createTestCardDTO()))
                    .build();

            FlashcardDeck entity = FlashcardMapper.toEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getTitle()).isEqualTo("Test Deck");
            assertThat(entity.getDescription()).isEqualTo("Test description");
            assertThat(entity.getSourceType()).isEqualTo(FlashcardDeck.SourceType.USER_CREATED);
            assertThat(entity.getCefrLevel()).isEqualTo("B1");
            assertThat(entity.getCards()).hasSize(1);
            assertThat(entity.getCardCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("toEntity throws for null CreateDTO")
        void toEntity_throwsForNull() {
            assertThatThrownBy(() -> FlashcardMapper.toEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("CreateFlashcardDeckDTO cannot be null");
        }

        @Test
        @DisplayName("toEntity defaults to USER_CREATED source type")
        void toEntity_defaultsSourceType() {
            CreateFlashcardDeckDTO dto = CreateFlashcardDeckDTO.builder()
                    .title("Test")
                    .sourceType(null)
                    .build();

            FlashcardDeck entity = FlashcardMapper.toEntity(dto);

            assertThat(entity.getSourceType()).isEqualTo(FlashcardDeck.SourceType.USER_CREATED);
        }

        @Test
        @DisplayName("updateEntityFromDTO updates only non-null fields")
        void updateEntityFromDTO_updatesNonNullOnly() {
            FlashcardDeck deck = createTestDeck();
            String originalDescription = deck.getDescription();
            
            UpdateFlashcardDeckDTO dto = UpdateFlashcardDeckDTO.builder()
                    .title("Updated Title")
                    .cefrLevel("C1")
                    .build();

            FlashcardMapper.updateEntityFromDTO(deck, dto);

            assertThat(deck.getTitle()).isEqualTo("Updated Title");
            assertThat(deck.getCefrLevel()).isEqualTo("C1");
            assertThat(deck.getDescription()).isEqualTo(originalDescription); // unchanged
        }

        @Test
        @DisplayName("updateEntityFromDTO updates cards and count")
        void updateEntityFromDTO_updatesCardsAndCount() {
            FlashcardDeck deck = createTestDeck();
            assertThat(deck.getCardCount()).isEqualTo(1);
            
            UpdateFlashcardDeckDTO dto = UpdateFlashcardDeckDTO.builder()
                    .cards(Arrays.asList(createTestCardDTO(), createTestCardDTO()))
                    .build();

            FlashcardMapper.updateEntityFromDTO(deck, dto);

            assertThat(deck.getCards()).hasSize(2);
            assertThat(deck.getCardCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("updateEntityFromDTO throws for null deck")
        void updateEntityFromDTO_throwsForNullDeck() {
            UpdateFlashcardDeckDTO dto = UpdateFlashcardDeckDTO.builder().build();
            
            assertThatThrownBy(() -> FlashcardMapper.updateEntityFromDTO(null, dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("updateEntityFromDTO throws for null dto")
        void updateEntityFromDTO_throwsForNullDto() {
            FlashcardDeck deck = createTestDeck();
            
            assertThatThrownBy(() -> FlashcardMapper.updateEntityFromDTO(deck, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toDeckDTOListWithoutCards converts list correctly")
        void toDeckDTOListWithoutCards_convertsCorrectly() {
            List<FlashcardDeck> decks = Arrays.asList(createTestDeck(), createTestDeck());

            List<FlashcardDeckDTO> dtos = FlashcardMapper.toDeckDTOListWithoutCards(decks);

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getCards()).isNull();
        }
    }

    // ========== UserFlashcardProgress Tests ==========

    @Nested
    @DisplayName("UserFlashcardProgress Mapping")
    class ProgressMappingTests {

        @Test
        @DisplayName("toProgressDTO converts entity to DTO correctly")
        void toProgressDTO_convertsCorrectly() {
            UserFlashcardProgress progress = createTestProgress();

            FlashcardProgressDTO dto = FlashcardMapper.toProgressDTO(progress);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(progress.getId());
            assertThat(dto.getCardIndex()).isEqualTo(progress.getCardIndex());
            assertThat(dto.getMasteryLevel()).isEqualTo(progress.getMasteryLevel());
            assertThat(dto.getMasteryLevelName()).isEqualTo("Young");
            assertThat(dto.getReviewCount()).isEqualTo(progress.getReviewCount());
            assertThat(dto.getCorrectCount()).isEqualTo(progress.getCorrectCount());
            assertThat(dto.getAccuracyRate()).isEqualTo(80.0);
            assertThat(dto.getEaseFactor()).isEqualTo(progress.getEaseFactor());
            assertThat(dto.getIntervalDays()).isEqualTo(progress.getIntervalDays());
        }

        @Test
        @DisplayName("toProgressDTO returns null for null input")
        void toProgressDTO_returnsNullForNull() {
            assertThat(FlashcardMapper.toProgressDTO(null)).isNull();
        }

        @Test
        @DisplayName("toProgressDTOWithCard includes card data")
        void toProgressDTOWithCard_includesCard() {
            UserFlashcardProgress progress = createTestProgress();
            FlashcardCard card = createTestCard();

            FlashcardProgressDTO dto = FlashcardMapper.toProgressDTOWithCard(progress, card);

            assertThat(dto).isNotNull();
            assertThat(dto.getCard()).isNotNull();
            assertThat(dto.getCard().getFront()).isEqualTo("collaborate");
        }

        @Test
        @DisplayName("toProgressDTOList converts list correctly")
        void toProgressDTOList_convertsCorrectly() {
            List<UserFlashcardProgress> progressList = Arrays.asList(
                    createTestProgress(), createTestProgress());

            List<FlashcardProgressDTO> dtos = FlashcardMapper.toProgressDTOList(progressList);

            assertThat(dtos).hasSize(2);
        }

        @Test
        @DisplayName("toProgressDTOList returns empty for null")
        void toProgressDTOList_returnsEmptyForNull() {
            assertThat(FlashcardMapper.toProgressDTOList(null)).isEmpty();
        }
    }

    // ========== Utility Class Tests ==========

    @Nested
    @DisplayName("Utility Class")
    class UtilityClassTests {

        @Test
        @DisplayName("Cannot instantiate FlashcardMapper")
        void cannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = FlashcardMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
