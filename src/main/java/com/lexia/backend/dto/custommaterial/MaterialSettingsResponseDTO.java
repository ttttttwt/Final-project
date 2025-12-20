package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.AiCorrectionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Material settings response DTO.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Material settings response")
public class MaterialSettingsResponseDTO {

    @Schema(description = "Target content types")
    private List<String> targetOptions;

    @Schema(description = "AI correction mode")
    private AiCorrectionMode aiCorrectionMode;

    @Schema(description = "Include explanations in style transform")
    private Boolean styleLearnMode;

    @Schema(description = "Sync vocabulary to SRS")
    private Boolean syncVocabToSrs;
}
