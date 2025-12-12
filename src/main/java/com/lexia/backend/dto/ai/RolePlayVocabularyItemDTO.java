package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vocabulary item for role-play scenarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role-play vocabulary hint item")
public class RolePlayVocabularyItemDTO {

    @Schema(description = "Vocabulary term", example = "deadline")
    private String term;

    @Schema(description = "Definition", example = "the date when something must be finished")
    private String definition;

    @Schema(description = "Example sentence", example = "The deadline is next Friday.")
    private String example;
}
