package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a new flashcard deck.
 * Supports both manual deck creation and lesson-based generation.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new flashcard deck")
public class CreateFlashcardDeckDTO {

    /**
     * Deck title (required).
     */
    @NotBlank(message = "Deck title is required")
    @Size(max = 200, message = "Deck title must not exceed 200 characters")
    @Schema(description = "Deck title", 
            example = "Business Meeting Vocabulary",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 200)
    private String title;

    /**
     * Deck description (optional).
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Deck description",
            example = "Key vocabulary for business meetings",
            maxLength = 1000)
    private String description;

    /**
     * Source type: LESSON, AI_GENERATED, or USER_CREATED.
     */
    @Pattern(regexp = "^(LESSON|AI_GENERATED|USER_CREATED)$",
             message = "Source type must be LESSON, AI_GENERATED, or USER_CREATED")
    @Schema(description = "How the deck is being created",
            allowableValues = {"LESSON", "AI_GENERATED", "USER_CREATED"},
            defaultValue = "USER_CREATED")
    private String sourceType;

    /**
     * Source lesson ID (required if sourceType is LESSON).
     */
    @Schema(description = "Source lesson ID (required for LESSON source type)")
    private Long sourceId;

    /**
     * CEFR level (optional, A1-C2).
     */
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$",
             message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    @Schema(description = "CEFR level of the vocabulary",
            example = "B2",
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String cefrLevel;

    /**
     * Initial cards to add to the deck (optional for USER_CREATED).
     */
    @Valid
    @Size(max = 500, message = "A deck cannot have more than 500 cards")
    @Schema(description = "Initial flashcards (for manual creation)")
    private List<FlashcardCardDTO> cards;

    /**
     * Validates the request based on source type.
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if ("LESSON".equals(sourceType) && sourceId == null) {
            throw new IllegalArgumentException("sourceId is required when sourceType is LESSON");
        }
        if (!"LESSON".equals(sourceType) && sourceId != null) {
            throw new IllegalArgumentException("sourceId should only be set when sourceType is LESSON");
        }
    }
}
