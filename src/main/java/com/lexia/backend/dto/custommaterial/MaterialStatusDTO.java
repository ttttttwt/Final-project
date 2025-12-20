package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.CustomMaterialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for polling material processing status.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Material processing status")
public class MaterialStatusDTO {

    @Schema(description = "Material ID")
    private UUID id;

    @Schema(description = "Processing status", example = "PROCESSING")
    private CustomMaterialStatus status;

    @Schema(description = "Processing progress (0-100)", example = "45")
    private Integer progress;

    @Schema(description = "Estimated time remaining in seconds")
    private Integer estimatedSecondsRemaining;

    @Schema(description = "Error message if failed")
    private String errorMessage;
}
