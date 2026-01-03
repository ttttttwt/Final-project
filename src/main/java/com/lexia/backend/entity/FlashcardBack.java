package com.lexia.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing the back side of a flashcard.
 * Contains the definition and additional vocabulary details.
 * This class is serialized to/from JSONB in PostgreSQL as part of
 * FlashcardCard.
 * 
 * <p>
 * Structure:
 * </p>
 * 
 * <pre>
 * {
 *   "definition": "to work together with others towards a shared goal",
 *   "partOfSpeech": "verb",
 *   "pronunciation": "/kəˈlæbəˌreɪt/",
 *   "exampleSentence": "The two departments will collaborate on the new project.",
 *   "synonyms": ["cooperate", "work together", "team up"],
 *   "collocations": ["collaborate with", "collaborate on", "closely collaborate"]
 * }
 * </pre>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see FlashcardCard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardBack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The main definition of the word/phrase.
     * Required field.
     */
    private String definition;

    /**
     * Part of speech (noun, verb, adjective, adverb, etc.).
     */
    private String partOfSpeech;

    /**
     * IPA pronunciation notation (e.g., "/kəˈlæbəˌreɪt/").
     */
    private String pronunciation;

    /**
     * Example sentence demonstrating usage.
     */
    private String exampleSentence;

    /**
     * List of synonyms for the word.
     */
    @Builder.Default
    private List<String> synonyms = new ArrayList<>();

    /**
     * Common collocations (word combinations).
     * E.g., for "make": ["make a decision", "make progress", "make an effort"]
     */
    @Builder.Default
    private List<String> collocations = new ArrayList<>();

    /**
     * Additional notes or tips for the learner.
     */
    private String notes;

    /**
     * Image URL for visual learners (optional).
     */
    private String imageUrl;

    /**
     * Status of AI-generated image.
     * PENDING = queued, GENERATING = in progress, COMPLETED = ready, FAILED = error
     */
    private String imageStatus;

    /**
     * Source of the image.
     * AI = AI-generated, UPLOAD = user uploaded, NONE = no image
     */
    private String imageSource;

    /**
     * Audio URL for pronunciation (optional).
     */
    private String audioUrl;

    /**
     * Creates a simple back with just definition.
     * 
     * @param definition the word definition
     * @return a new FlashcardBack
     */
    public static FlashcardBack simple(String definition) {
        return FlashcardBack.builder()
                .definition(definition)
                .build();
    }

    /**
     * Creates a vocabulary back with common fields.
     * 
     * @param definition      the word definition
     * @param partOfSpeech    part of speech (noun, verb, etc.)
     * @param pronunciation   IPA pronunciation
     * @param exampleSentence example usage
     * @return a new FlashcardBack
     */
    public static FlashcardBack vocabulary(
            String definition,
            String partOfSpeech,
            String pronunciation,
            String exampleSentence) {
        return FlashcardBack.builder()
                .definition(definition)
                .partOfSpeech(partOfSpeech)
                .pronunciation(pronunciation)
                .exampleSentence(exampleSentence)
                .build();
    }

    /**
     * Checks if this back has complete vocabulary information.
     * 
     * @return true if definition, partOfSpeech, pronunciation, and example are all
     *         present
     */
    @JsonIgnore
    public boolean isComplete() {
        return definition != null && !definition.isBlank()
                && partOfSpeech != null && !partOfSpeech.isBlank()
                && pronunciation != null && !pronunciation.isBlank()
                && exampleSentence != null && !exampleSentence.isBlank();
    }

    /**
     * Checks if this back has minimal required information.
     * 
     * @return true if at least definition is present
     */
    @JsonIgnore
    public boolean isValid() {
        return definition != null && !definition.isBlank();
    }
}
