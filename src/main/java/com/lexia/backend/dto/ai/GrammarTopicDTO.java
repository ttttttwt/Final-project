package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * DTO for grammar topic information.
 * Returns grammar topic details for display and selection.
 */
@Builder
@Schema(description = "Grammar topic information for exercise generation")
public record GrammarTopicDTO(
    @Schema(description = "Topic ID", example = "1")
    Integer id,

    @Schema(description = "Topic name", example = "Present Simple")
    String name,

    @Schema(description = "Category grouping", example = "Tenses")
    String category,

    @Schema(description = "Applicable CEFR levels", example = "[\"A1\", \"A2\"]")
    List<String> cefrLevels,

    @Schema(description = "Topic description", example = "Used for habits, routines, and general truths")
    String description,

    @Schema(description = "Example sentences", example = "[\"I work every day.\", \"She speaks English fluently.\"]")
    List<String> examples,

    @Schema(description = "Whether the topic is active", example = "true")
    Boolean isActive
) {
    /**
     * Creates a simple topic DTO with minimal fields.
     */
    public static GrammarTopicDTO simple(Integer id, String name, String category, List<String> cefrLevels) {
        return new GrammarTopicDTO(id, name, category, cefrLevels, null, null, true);
    }

    /**
     * Checks if this topic applies to a specific CEFR level.
     */
    public boolean hasLevel(String level) {
        return cefrLevels != null && cefrLevels.contains(level);
    }
}
