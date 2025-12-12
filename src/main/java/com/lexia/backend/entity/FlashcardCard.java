package com.lexia.backend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing a single flashcard within a FlashcardDeck.
 * This class is serialized to/from JSONB in PostgreSQL.
 * 
 * <p>Structure:</p>
 * <pre>
 * {
 *   "front": "collaborate",
 *   "back": {
 *     "definition": "to work together with others towards a shared goal",
 *     "partOfSpeech": "verb",
 *     "pronunciation": "/kəˈlæbəˌreɪt/",
 *     "exampleSentence": "The two departments will collaborate on the new project.",
 *     "synonyms": ["cooperate", "work together", "team up"],
 *     "collocations": ["collaborate with", "collaborate on", "closely collaborate"]
 *   },
 *   "tags": ["business", "b2", "teamwork"],
 *   "difficulty": 3
 * }
 * </pre>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see FlashcardDeck
 * @see FlashcardBack
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardCard implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The front of the card - typically the vocabulary word or phrase.
     */
    private String front;

    /**
     * The back of the card - contains definition, examples, and additional info.
     */
    private FlashcardBack back;

    /**
     * Tags for categorization (e.g., "business", "b2", "meeting").
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Difficulty level from 1 (easiest) to 5 (hardest).
     * Used for initial spaced repetition scheduling.
     */
    @Builder.Default
    private Integer difficulty = 3;

    /**
     * Validates card data.
     * @throws IllegalArgumentException if required fields are missing
     */
    public void validate() {
        if (front == null || front.isBlank()) {
            throw new IllegalArgumentException("Card front (word) is required");
        }
        if (back == null || back.getDefinition() == null || back.getDefinition().isBlank()) {
            throw new IllegalArgumentException("Card back with definition is required");
        }
        if (difficulty != null && (difficulty < 1 || difficulty > 5)) {
            throw new IllegalArgumentException("Difficulty must be between 1 and 5");
        }
    }

    /**
     * Creates a simple flashcard with just front and definition.
     * @param front the word/phrase
     * @param definition the definition
     * @return a new FlashcardCard
     */
    public static FlashcardCard simple(String front, String definition) {
        return FlashcardCard.builder()
                .front(front)
                .back(FlashcardBack.builder().definition(definition).build())
                .build();
    }

    /**
     * Creates a vocabulary flashcard with full details.
     * @param word the vocabulary word
     * @param definition the definition
     * @param partOfSpeech the part of speech (noun, verb, etc.)
     * @param pronunciation IPA pronunciation
     * @param example example sentence
     * @return a new FlashcardCard
     */
    public static FlashcardCard vocabulary(
            String word, 
            String definition, 
            String partOfSpeech,
            String pronunciation, 
            String example) {
        return FlashcardCard.builder()
                .front(word)
                .back(FlashcardBack.builder()
                        .definition(definition)
                        .partOfSpeech(partOfSpeech)
                        .pronunciation(pronunciation)
                        .exampleSentence(example)
                        .build())
                .build();
    }
}
