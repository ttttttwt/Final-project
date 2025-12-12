package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for AI-based flashcard generation request.
 * Specifies source content and generation parameters.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate flashcards from lesson content")
public class GenerateFlashcardsDTO {

    /**
     * Source lesson ID.
     */
    @NotNull(message = "Lesson ID is required")
    @Schema(description = "Source lesson ID",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lessonId;

    /**
     * Maximum number of cards to generate.
     */
    @Min(value = 5, message = "Minimum 5 cards")
    @Max(value = 50, message = "Maximum 50 cards")
    @Schema(description = "Maximum cards to generate (5-50)",
            example = "20",
            defaultValue = "20")
    @Builder.Default
    private Integer maxCards = 20;

    /**
     * CEFR level override (uses lesson level if not specified).
     */
    @Schema(description = "CEFR level override",
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String cefrLevel;

    /**
     * Focus areas for card generation.
     */
    @Schema(description = "Focus areas for vocabulary selection",
            example = "[\"business\", \"formal\"]")
    private List<String> focusAreas;

    /**
     * Whether to include example sentences.
     */
    @Schema(description = "Include example sentences in cards",
            defaultValue = "true")
    @Builder.Default
    private Boolean includeExamples = true;

    /**
     * Whether to include pronunciation (IPA).
     */
    @Schema(description = "Include IPA pronunciation",
            defaultValue = "true")
    @Builder.Default
    private Boolean includePronunciation = true;

    /**
     * Whether to include synonyms.
     */
    @Schema(description = "Include synonyms",
            defaultValue = "true")
    @Builder.Default
    private Boolean includeSynonyms = true;

    /**
     * Custom deck title (auto-generated if not specified).
     */
    @Schema(description = "Custom deck title")
    private String customTitle;
}
