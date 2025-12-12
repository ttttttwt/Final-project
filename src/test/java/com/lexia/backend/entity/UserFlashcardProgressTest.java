package com.lexia.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UserFlashcardProgress entity.
 * Tests SM-2 algorithm implementation and progress tracking.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@DisplayName("UserFlashcardProgress Entity Tests")
class UserFlashcardProgressTest {

    private UserFlashcardProgress createNewProgress() {
        return UserFlashcardProgress.builder()
                .userId(UUID.randomUUID())
                .cardIndex(0)
                .masteryLevel(UserFlashcardProgress.MASTERY_NEW)
                .reviewCount(0)
                .correctCount(0)
                .easeFactor(UserFlashcardProgress.DEFAULT_EASE_FACTOR)
                .intervalDays(1)
                .build();
    }

    @Nested
    @DisplayName("SM-2 Algorithm")
    class SM2AlgorithmTests {

        @Test
        @DisplayName("recordReview updates parameters for quality 5 (perfect)")
        void recordReview_quality5_perfect() {
            UserFlashcardProgress progress = createNewProgress();

            progress.recordReview(5);

            assertThat(progress.getReviewCount()).isEqualTo(1);
            assertThat(progress.getCorrectCount()).isEqualTo(1);
            assertThat(progress.getIntervalDays()).isEqualTo(1); // First review
            assertThat(progress.getLastReviewedAt()).isNotNull();
            assertThat(progress.getNextReviewAt()).isNotNull();
        }

        @Test
        @DisplayName("recordReview increases interval on second review")
        void recordReview_secondReview_increasesInterval() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setReviewCount(1); // Simulate first review done

            progress.recordReview(4);

            assertThat(progress.getReviewCount()).isEqualTo(2);
            assertThat(progress.getIntervalDays()).isEqualTo(6); // SM-2: second review = 6 days
        }

        @Test
        @DisplayName("recordReview applies ease factor on third+ review")
        void recordReview_thirdReview_appliesEaseFactor() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setReviewCount(2);
            progress.setIntervalDays(6);
            progress.setEaseFactor(new BigDecimal("2.50"));

            progress.recordReview(4);

            assertThat(progress.getReviewCount()).isEqualTo(3);
            // Interval = 6 * 2.50 = 15
            assertThat(progress.getIntervalDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("recordReview resets interval for quality < 3 (fail)")
        void recordReview_fail_resetsInterval() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setReviewCount(5);
            progress.setIntervalDays(30);

            progress.recordReview(2); // Fail

            assertThat(progress.getReviewCount()).isEqualTo(6);
            assertThat(progress.getCorrectCount()).isEqualTo(0); // Not incremented
            assertThat(progress.getIntervalDays()).isEqualTo(1); // Reset
        }

        @Test
        @DisplayName("recordReview enforces minimum ease factor")
        void recordReview_enforcesMinEaseFactor() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setEaseFactor(new BigDecimal("1.35"));

            // Quality 0 decreases ease factor significantly
            progress.recordReview(0);

            assertThat(progress.getEaseFactor())
                    .isGreaterThanOrEqualTo(UserFlashcardProgress.MIN_EASE_FACTOR);
        }

        @Test
        @DisplayName("recordReview throws for invalid quality")
        void recordReview_throwsForInvalidQuality() {
            UserFlashcardProgress progress = createNewProgress();

            assertThatThrownBy(() -> progress.recordReview(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 5");

            assertThatThrownBy(() -> progress.recordReview(6))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 5");
        }
    }

    @Nested
    @DisplayName("Mastery Level")
    class MasteryLevelTests {

        @Test
        @DisplayName("updateMasteryLevel sets LEARNING for interval < 3")
        void masteryLevel_learning() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setIntervalDays(2);
            
            progress.recordReview(4);

            assertThat(progress.getMasteryLevel()).isEqualTo(UserFlashcardProgress.MASTERY_LEARNING);
        }

        @Test
        @DisplayName("getMasteryLevelName returns correct names")
        void getMasteryLevelName_returnsCorrect() {
            UserFlashcardProgress progress = createNewProgress();

            progress.setMasteryLevel(0);
            assertThat(progress.getMasteryLevelName()).isEqualTo("New");

            progress.setMasteryLevel(1);
            assertThat(progress.getMasteryLevelName()).isEqualTo("Learning");

            progress.setMasteryLevel(2);
            assertThat(progress.getMasteryLevelName()).isEqualTo("Young");

            progress.setMasteryLevel(3);
            assertThat(progress.getMasteryLevelName()).isEqualTo("Mature");

            progress.setMasteryLevel(4);
            assertThat(progress.getMasteryLevelName()).isEqualTo("Master");

            progress.setMasteryLevel(5);
            assertThat(progress.getMasteryLevelName()).isEqualTo("Expert");
        }

        @Test
        @DisplayName("getMasteryLevelName returns Unknown for invalid level")
        void getMasteryLevelName_unknown() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setMasteryLevel(99);

            assertThat(progress.getMasteryLevelName()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("Due Status")
    class DueStatusTests {

        @Test
        @DisplayName("isDue returns true when nextReviewAt is null")
        void isDue_trueWhenNull() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setNextReviewAt(null);

            assertThat(progress.isDue()).isTrue();
        }

        @Test
        @DisplayName("isDue returns true when nextReviewAt is in the past")
        void isDue_trueWhenPast() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setNextReviewAt(Instant.now().minus(1, ChronoUnit.HOURS));

            assertThat(progress.isDue()).isTrue();
        }

        @Test
        @DisplayName("isDue returns false when nextReviewAt is in the future")
        void isDue_falseWhenFuture() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setNextReviewAt(Instant.now().plus(1, ChronoUnit.DAYS));

            assertThat(progress.isDue()).isFalse();
        }

        @Test
        @DisplayName("isOverdue returns true when more than 24 hours past")
        void isOverdue_trueWhenVeryPast() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setNextReviewAt(Instant.now().minus(2, ChronoUnit.DAYS));

            assertThat(progress.isOverdue()).isTrue();
        }

        @Test
        @DisplayName("isOverdue returns false when less than 24 hours past")
        void isOverdue_falseWhenRecentlyPast() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setNextReviewAt(Instant.now().minus(12, ChronoUnit.HOURS));

            assertThat(progress.isOverdue()).isFalse();
        }
    }

    @Nested
    @DisplayName("Accuracy Rate")
    class AccuracyRateTests {

        @Test
        @DisplayName("getAccuracyRate returns 0 for no reviews")
        void getAccuracyRate_zeroForNoReviews() {
            UserFlashcardProgress progress = createNewProgress();

            assertThat(progress.getAccuracyRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getAccuracyRate calculates correctly")
        void getAccuracyRate_calculatesCorrectly() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setReviewCount(10);
            progress.setCorrectCount(8);

            assertThat(progress.getAccuracyRate()).isEqualTo(80.0);
        }

        @Test
        @DisplayName("getAccuracyRate returns 100 for perfect score")
        void getAccuracyRate_perfectScore() {
            UserFlashcardProgress progress = createNewProgress();
            progress.setReviewCount(5);
            progress.setCorrectCount(5);

            assertThat(progress.getAccuracyRate()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("createNew creates progress with defaults")
        void createNew_createsWithDefaults() {
            UUID userId = UUID.randomUUID();
            FlashcardDeck deck = FlashcardDeck.builder()
                    .id(UUID.randomUUID())
                    .build();

            UserFlashcardProgress progress = UserFlashcardProgress.createNew(userId, deck, 0);

            assertThat(progress.getUserId()).isEqualTo(userId);
            assertThat(progress.getDeck()).isEqualTo(deck);
            assertThat(progress.getCardIndex()).isEqualTo(0);
            assertThat(progress.getMasteryLevel()).isEqualTo(UserFlashcardProgress.MASTERY_NEW);
            assertThat(progress.getReviewCount()).isEqualTo(0);
            assertThat(progress.getCorrectCount()).isEqualTo(0);
            assertThat(progress.getEaseFactor()).isEqualTo(UserFlashcardProgress.DEFAULT_EASE_FACTOR);
            assertThat(progress.getIntervalDays()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Constants")
    class ConstantsTests {

        @Test
        @DisplayName("Mastery level constants are correct")
        void masteryLevelConstants() {
            assertThat(UserFlashcardProgress.MASTERY_NEW).isEqualTo(0);
            assertThat(UserFlashcardProgress.MASTERY_LEARNING).isEqualTo(1);
            assertThat(UserFlashcardProgress.MASTERY_YOUNG).isEqualTo(2);
            assertThat(UserFlashcardProgress.MASTERY_MATURE).isEqualTo(3);
            assertThat(UserFlashcardProgress.MASTERY_MASTER).isEqualTo(4);
            assertThat(UserFlashcardProgress.MASTERY_EXPERT).isEqualTo(5);
        }

        @Test
        @DisplayName("Ease factor constants are correct")
        void easeFactorConstants() {
            assertThat(UserFlashcardProgress.MIN_EASE_FACTOR)
                    .isEqualTo(new BigDecimal("1.30"));
            assertThat(UserFlashcardProgress.DEFAULT_EASE_FACTOR)
                    .isEqualTo(new BigDecimal("2.50"));
        }
    }
}
