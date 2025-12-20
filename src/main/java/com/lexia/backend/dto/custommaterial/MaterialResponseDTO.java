package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.enums.CustomMaterialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Full response DTO for a custom material with generated content.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Full custom material with generated content")
public class MaterialResponseDTO {

    @Schema(description = "Material ID")
    private UUID id;

    @Schema(description = "Material title", example = "Q1 Marketing Report")
    private String title;

    @Schema(description = "Source type", example = "PDF")
    private CustomMaterialSourceType sourceType;

    @Schema(description = "Processing status", example = "COMPLETED")
    private CustomMaterialStatus status;

    @Schema(description = "URL to original file (if applicable)")
    private String originalFileUrl;

    @Schema(description = "Input metadata (page/time range)")
    private Map<String, Object> inputMetadata;

    @Schema(description = "AI-generated learning content (vocabulary, quiz, summary, etc.)")
    private Map<String, Object> generatedContent;

    @Schema(description = "Material settings")
    private MaterialSettingsResponseDTO settings;

    @Schema(description = "Error message if processing failed")
    private String errorMessage;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
