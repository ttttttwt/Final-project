package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for submitting flashcard review results.
 * Contains quality ratings for reviewed cards (SM-2 algorithm input).
 * 
 * <p>Quality Rating Scale:</p>
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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard review submission with quality ratings")
public class FlashcardReviewResultDTO {

    /**
     * List of individual card review results.
     */
    @NotNull(message = "Reviews list is required")
    @Schema(description = "Individual card review results")
    private List<CardReviewDTO> reviews;

    /**
     * Total time spent on this review session (milliseconds).
     */
    @Schema(description = "Total session time in milliseconds", example = "120000")
    private Long sessionTimeMs;

    /**
     * Individual card review result.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single card review result")
    public static class CardReviewDTO {

        /**
         * Index of the card in the deck (0-based).
         */
        @NotNull(message = "Card index is required")
        @Min(value = 0, message = "Card index must be non-negative")
        @Schema(description = "Card index in deck (0-based)", 
                example = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer cardIndex;

        /**
         * Quality rating (0-5) based on recall difficulty.
         */
        @NotNull(message = "Quality rating is required")
        @Min(value = 0, message = "Quality must be between 0 and 5")
        @Max(value = 5, message = "Quality must be between 0 and 5")
        @Schema(description = "Quality rating (0=blackout, 5=perfect recall)",
                example = "4",
                minimum = "0",
                maximum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quality;

        /**
         * Time spent on this card (milliseconds).
         */
        @Schema(description = "Time spent on card in milliseconds", example = "5000")
        private Long timeSpentMs;
    }
}
