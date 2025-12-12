package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * DTO representing a single grammar exercise.
 * Supports multiple exercise types: MCQ, fill-in-blank, transformation, error-correction.
 */
@Builder
@Schema(description = "A single grammar exercise")
public record GrammarExerciseDTO(
    @Schema(description = "Exercise type", example = "multiple_choice",
            allowableValues = {"multiple_choice", "fill_blank", "transformation", "error_correction"})
    String type,

    @Schema(description = "Exercise instruction", example = "Choose the correct form of the verb.")
    String instruction,

    @Schema(description = "Question text", example = "She ___ to work every day.")
    String question,

    @Schema(description = "Options for MCQ type", example = "[\"goes\", \"go\", \"going\", \"went\"]")
    List<String> options,

    @Schema(description = "Blanks for fill-in-blank type", example = "[\"___\"]")
    List<String> blanks,

    @Schema(description = "Correct answer(s)", example = "goes")
    Object correctAnswer,

    @Schema(description = "Optional hint", example = "Think about third person singular.")
    String hint,

    @Schema(description = "Explanation of the correct answer", 
            example = "We use 'goes' because 'she' is third person singular, which requires adding -es to 'go'.")
    String explanation,

    @Schema(description = "Difficulty level", example = "medium", 
            allowableValues = {"easy", "medium", "hard"})
    String difficulty
) {
    /**
     * Creates a multiple choice exercise.
     */
    public static GrammarExerciseDTO multipleChoice(String instruction, String question, 
            List<String> options, String correctAnswer, String explanation) {
        return GrammarExerciseDTO.builder()
                .type("multiple_choice")
                .instruction(instruction)
                .question(question)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .difficulty("medium")
                .build();
    }

    /**
     * Creates a fill-in-blank exercise.
     */
    public static GrammarExerciseDTO fillBlank(String instruction, String question, 
            List<String> blanks, Object correctAnswer, String explanation) {
        return GrammarExerciseDTO.builder()
                .type("fill_blank")
                .instruction(instruction)
                .question(question)
                .blanks(blanks)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .difficulty("medium")
                .build();
    }

    /**
     * Checks if this is a multiple choice exercise.
     */
    public boolean isMultipleChoice() {
        return "multiple_choice".equals(type);
    }

    /**
     * Checks if this is a fill-in-blank exercise.
     */
    public boolean isFillBlank() {
        return "fill_blank".equals(type);
    }
}
