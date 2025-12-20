package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.CustomMaterialSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new custom material.
 * 
 * <p>
 * This is sent as part of a multipart form-data request:
 * the file is sent separately, and this DTO is sent as JSON in the "request"
 * part.
 * </p>
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a custom material for AI content generation")
public class CreateMaterialRequestDTO {

    @NotNull
    @Schema(description = "Source type of the material", example = "PDF", requiredMode = Schema.RequiredMode.REQUIRED)
    private CustomMaterialSourceType sourceType;

    @Schema(description = "URL for YOUTUBE or WEBSITE source types", example = "https://www.youtube.com/watch?v=abc123")
    private String sourceUrl;

    @Schema(description = "Raw text for TEXT source type (max 5000 chars)", example = "Please find attached the Q1 report...")
    @Size(max = 5000, message = "Raw text cannot exceed 5000 characters")
    private String rawText;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Title for the material", example = "Q1 Marketing Report", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Valid
    @Schema(description = "Input range metadata (page range for docs, time range for video)")
    private InputMetadataDTO inputMetadata;

    @Schema(description = "Target content types to generate", example = "[\"VOCABULARY\", \"QUIZ\", \"ROLE_PLAY\"]")
    private List<String> targetOptions;

    @Valid
    @Schema(description = "User settings for content generation")
    private MaterialSettingsDTO settings;

    /**
     * Validates that the request has required data for the source type.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (sourceType == null) {
            throw new IllegalArgumentException("Source type is required");
        }

        if (sourceType.requiresUrl() && (sourceUrl == null || sourceUrl.isBlank())) {
            throw new IllegalArgumentException(
                    "Source URL is required for " + sourceType + " source type");
        }

        if (sourceType == CustomMaterialSourceType.TEXT && (rawText == null || rawText.isBlank())) {
            throw new IllegalArgumentException("Raw text is required for TEXT source type");
        }
    }
}
