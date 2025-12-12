package com.lexia.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FlashcardDeck entity.
 * Tests entity functionality, helpers, and validation.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@DisplayName("FlashcardDeck Entity Tests")
class FlashcardDeckTest {

    @Nested
    @DisplayName("Source Type Enum")
    class SourceTypeTests {

        @Test
        @DisplayName("fromValue converts string to enum")
        void fromValue_convertsCorrectly() {
            assertThat(FlashcardDeck.SourceType.fromValue("lesson"))
                    .isEqualTo(FlashcardDeck.SourceType.LESSON);
            assertThat(FlashcardDeck.SourceType.fromValue("ai_generated"))
                    .isEqualTo(FlashcardDeck.SourceType.AI_GENERATED);
            assertThat(FlashcardDeck.SourceType.fromValue("user_created"))
                    .isEqualTo(FlashcardDeck.SourceType.USER_CREATED);
        }

        @Test
        @DisplayName("fromValue throws for invalid value")
        void fromValue_throwsForInvalid() {
            assertThatThrownBy(() -> FlashcardDeck.SourceType.fromValue("invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown source type");
        }

        @Test
        @DisplayName("getValue returns correct string")
        void getValue_returnsCorrect() {
            assertThat(FlashcardDeck.SourceType.LESSON.getValue()).isEqualTo("lesson");
            assertThat(FlashcardDeck.SourceType.AI_GENERATED.getValue()).isEqualTo("ai_generated");
            assertThat(FlashcardDeck.SourceType.USER_CREATED.getValue()).isEqualTo("user_created");
        }
    }

    @Nested
    @DisplayName("Card Management")
    class CardManagementTests {

        @Test
        @DisplayName("updateCardCount updates count correctly")
        void updateCardCount_updatesCorrectly() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .cards(Arrays.asList(
                            FlashcardCard.simple("word1", "def1"),
                            FlashcardCard.simple("word2", "def2")
                    ))
                    .cardCount(0) // Wrong initially
                    .build();

            deck.updateCardCount();

            assertThat(deck.getCardCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("addCard adds card and updates count")
        void addCard_addsAndUpdates() {
            FlashcardDeck deck = FlashcardDeck.builder().build();
            assertThat(deck.getCardCount()).isEqualTo(0);

            deck.addCard(FlashcardCard.simple("test", "definition"));

            assertThat(deck.getCards()).hasSize(1);
            assertThat(deck.getCardCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeCard removes card and updates count")
        void removeCard_removesAndUpdates() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .cards(new java.util.ArrayList<>(Arrays.asList(
                            FlashcardCard.simple("word1", "def1"),
                            FlashcardCard.simple("word2", "def2")
                    )))
                    .cardCount(2)
                    .build();

            FlashcardCard removed = deck.removeCard(0);

            assertThat(removed.getFront()).isEqualTo("word1");
            assertThat(deck.getCards()).hasSize(1);
            assertThat(deck.getCardCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeCard throws for invalid index")
        void removeCard_throwsForInvalidIndex() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .cards(new java.util.ArrayList<>(Arrays.asList(FlashcardCard.simple("word", "def"))))
                    .cardCount(1)
                    .build();

            assertThatThrownBy(() -> deck.removeCard(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("Source Type Checks")
    class SourceTypeCheckTests {

        @Test
        @DisplayName("isFromLesson returns true for LESSON type")
        void isFromLesson_returnsTrue() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.LESSON)
                    .sourceId(1L)
                    .build();

            assertThat(deck.isFromLesson()).isTrue();
            assertThat(deck.isAiGenerated()).isFalse();
            assertThat(deck.isUserCreated()).isFalse();
        }

        @Test
        @DisplayName("isAiGenerated returns true for AI_GENERATED type")
        void isAiGenerated_returnsTrue() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.AI_GENERATED)
                    .build();

            assertThat(deck.isFromLesson()).isFalse();
            assertThat(deck.isAiGenerated()).isTrue();
            assertThat(deck.isUserCreated()).isFalse();
        }

        @Test
        @DisplayName("isUserCreated returns true for USER_CREATED type")
        void isUserCreated_returnsTrue() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.USER_CREATED)
                    .build();

            assertThat(deck.isFromLesson()).isFalse();
            assertThat(deck.isAiGenerated()).isFalse();
            assertThat(deck.isUserCreated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Source Validation")
    class SourceValidationTests {

        @Test
        @DisplayName("validateSourceConsistency passes for valid LESSON")
        void validateSourceConsistency_validLesson() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.LESSON)
                    .sourceId(1L)
                    .build();

            assertThatCode(() -> deck.validateSourceConsistency())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateSourceConsistency throws for LESSON without sourceId")
        void validateSourceConsistency_lessonWithoutSourceId() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.LESSON)
                    .sourceId(null)
                    .build();

            assertThatThrownBy(() -> deck.validateSourceConsistency())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("LESSON source type requires sourceId");
        }

        @Test
        @DisplayName("validateSourceConsistency throws for non-LESSON with sourceId")
        void validateSourceConsistency_nonLessonWithSourceId() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceType(FlashcardDeck.SourceType.AI_GENERATED)
                    .sourceId(1L)
                    .build();

            assertThatThrownBy(() -> deck.validateSourceConsistency())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Non-LESSON source type should not have sourceId");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Builder creates deck with defaults")
        void builder_createsWithDefaults() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .userId(UUID.randomUUID())
                    .title("Test Deck")
                    .build();

            assertThat(deck.getCards()).isEmpty();
            assertThat(deck.getCardCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("sourceTypeValue builder method works")
        void sourceTypeValue_works() {
            FlashcardDeck deck = FlashcardDeck.builder()
                    .sourceTypeValue("lesson")
                    .sourceId(1L)
                    .build();

            assertThat(deck.getSourceType()).isEqualTo(FlashcardDeck.SourceType.LESSON);
        }
    }
}
