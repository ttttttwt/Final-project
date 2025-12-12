package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the back side of a flashcard.
 * Contains vocabulary definition and additional learning details.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard back content with definition and vocabulary details")
public class FlashcardBackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The main definition of the word/phrase.
     */
    @NotBlank(message = "Definition is required")
    @Schema(description = "Main definition", 
            example = "to work together with others towards a shared goal")
    private String definition;

    /**
     * Part of speech (noun, verb, adjective, etc.).
     */
    @Schema(description = "Part of speech", 
            example = "verb",
            allowableValues = {"noun", "verb", "adjective", "adverb", "preposition", 
                             "conjunction", "pronoun", "interjection", "phrase"})
    private String partOfSpeech;

    /**
     * IPA pronunciation notation.
     */
    @Schema(description = "IPA pronunciation", 
            example = "/kəˈlæbəˌreɪt/")
    private String pronunciation;

    /**
     * Example sentence demonstrating usage.
     */
    @Schema(description = "Example sentence showing word usage",
            example = "The two departments will collaborate on the new project.")
    private String exampleSentence;

    /**
     * List of synonyms.
     */
    @Schema(description = "List of synonyms")
    @Builder.Default
    private List<String> synonyms = new ArrayList<>();

    /**
     * Common collocations (word combinations).
     */
    @Schema(description = "Common collocations/word combinations")
    @Builder.Default
    private List<String> collocations = new ArrayList<>();

    /**
     * Additional notes or tips.
     */
    @Schema(description = "Additional learning notes or tips")
    private String notes;

    /**
     * Image URL for visual learning.
     */
    @Schema(description = "Image URL for visual learners", format = "uri")
    private String imageUrl;

    /**
     * Audio URL for pronunciation.
     */
    @Schema(description = "Audio URL for pronunciation", format = "uri")
    private String audioUrl;
}
