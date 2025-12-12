package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating an existing flashcard deck.
 * All fields are optional - only non-null fields will be updated.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a flashcard deck")
public class UpdateFlashcardDeckDTO {

    /**
     * New deck title (optional).
     */
    @Size(max = 200, message = "Deck title must not exceed 200 characters")
    @Schema(description = "New deck title", maxLength = 200)
    private String title;

    /**
     * New deck description (optional).
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "New deck description", maxLength = 1000)
    private String description;

    /**
     * New CEFR level (optional).
     */
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$",
             message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    @Schema(description = "New CEFR level",
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String cefrLevel;

    /**
     * Replace all cards with new set (optional).
     * If provided, will replace all existing cards.
     */
    @Valid
    @Size(max = 500, message = "A deck cannot have more than 500 cards")
    @Schema(description = "New set of cards (replaces all existing)")
    private List<FlashcardCardDTO> cards;

    /**
     * Checks if any fields are set for update.
     * @return true if at least one field is non-null
     */
    public boolean hasUpdates() {
        return title != null || description != null || cefrLevel != null || cards != null;
    }
}
