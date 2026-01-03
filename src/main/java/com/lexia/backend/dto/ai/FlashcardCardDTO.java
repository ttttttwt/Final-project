package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a single flashcard.
 * Contains front (word/phrase), back (definition/details), tags, and
 * difficulty.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single flashcard with front and back content")
public class FlashcardCardDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The front of the card - vocabulary word or phrase.
         */
        @NotBlank(message = "Card front is required")
        @Schema(description = "Front of card - word or phrase to learn", example = "collaborate", requiredMode = Schema.RequiredMode.REQUIRED)
        private String front;

        /**
         * The back of the card - definition and details.
         */
        @Valid
        @NotNull(message = "Card back is required")
        @Schema(description = "Back of card - definition and learning details", requiredMode = Schema.RequiredMode.REQUIRED)
        private FlashcardBackDTO back;

        /**
         * Tags for categorization.
         */
        @Schema(description = "Tags for categorization", example = "[\"business\", \"b2\", \"teamwork\"]")
        @Builder.Default
        private List<String> tags = new ArrayList<>();

        /**
         * Difficulty level (1-5).
         */
        @Schema(description = "Difficulty level from 1 (easy) to 5 (hard)", example = "3", minimum = "1", maximum = "5")
        @Builder.Default
        private Integer difficulty = 3;

        /**
         * Creates a simple card with just front and definition.
         */
        public static FlashcardCardDTO simple(String front, String definition) {
                return FlashcardCardDTO.builder()
                                .front(front)
                                .back(FlashcardBackDTO.builder().definition(definition).build())
                                .build();
        }
}
