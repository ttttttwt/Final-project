package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.AiCorrectionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Material settings for AI content generation.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Settings for AI content generation")
public class MaterialSettingsDTO {

    @Schema(description = "AI correction mode for role-play", example = "POLITE", defaultValue = "POLITE")
    @Builder.Default
    private AiCorrectionMode aiCorrectionMode = AiCorrectionMode.POLITE;

    @Schema(description = "Include explanations in style transform", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean styleLearnMode = true;

    @Schema(description = "Sync vocabulary to SRS system", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean syncVocabToSrs = false;

    @Schema(description = "Generate AI images for synced flashcards", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean generateFlashcardImages = false;
}
