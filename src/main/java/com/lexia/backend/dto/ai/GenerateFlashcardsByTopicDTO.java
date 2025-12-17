package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for generating flashcards from a topic using AI.
 * The AI will generate vocabulary cards based on the given topic and CEFR level.
 * 
 * @author LEXIA Team
 * @since Sprint 6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate flashcards from a topic using AI")
public class GenerateFlashcardsByTopicDTO {

    /**
     * The topic to generate flashcards for (required).
     */
    @NotBlank(message = "Topic is required")
    @Size(min = 3, max = 200, message = "Topic must be between 3 and 200 characters")
    @Schema(description = "The topic to generate vocabulary flashcards for",
            example = "Business negotiations",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 200)
    private String topic;

    /**
     * Custom deck title (optional). If not provided, will use topic.
     */
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Custom deck title. If not provided, topic will be used",
            example = "Business Negotiation Skills",
            maxLength = 200)
    private String customTitle;

    /**
     * Deck description (optional).
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Deck description",
            example = "Vocabulary for business negotiations and deal-making",
            maxLength = 1000)
    private String description;

    /**
     * CEFR level (optional, defaults to B2).
     */
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$",
             message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    @Schema(description = "CEFR level of the vocabulary",
            example = "B2",
            defaultValue = "B2",
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String cefrLevel;

    /**
     * Number of cards to generate (optional, default 15, max 50).
     */
    @Min(value = 5, message = "Must generate at least 5 cards")
    @Max(value = 50, message = "Cannot generate more than 50 cards at once")
    @Schema(description = "Number of flashcards to generate",
            example = "15",
            defaultValue = "15",
            minimum = "5",
            maximum = "50")
    private Integer cardCount;

    /**
     * Focus areas for vocabulary (optional).
     */
    @Size(max = 10, message = "Cannot have more than 10 focus areas")
    @Schema(description = "Specific focus areas for vocabulary",
            example = "[\"formal language\", \"persuasion\", \"compromise\"]")
    private List<@Size(max = 100) String> focusAreas;

    /**
     * Include example sentences (optional, default true).
     */
    @Schema(description = "Whether to include example sentences",
            defaultValue = "true")
    private Boolean includeExamples;

    /**
     * Include pronunciation (optional, default true).
     */
    @Schema(description = "Whether to include pronunciation (IPA)",
            defaultValue = "true")
    private Boolean includePronunciation;

    /**
     * Gets card count with default.
     */
    public int getCardCountOrDefault() {
        return cardCount != null ? cardCount : 15;
    }

    /**
     * Gets CEFR level with default.
     */
    public String getCefrLevelOrDefault() {
        return cefrLevel != null ? cefrLevel : "B2";
    }

    /**
     * Gets title (uses topic if custom title not provided).
     */
    public String getEffectiveTitle() {
        return customTitle != null && !customTitle.isBlank() ? 
                customTitle : "Vocabulary: " + topic;
    }
}
