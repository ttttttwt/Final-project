package com.lexia.backend.dto.custommaterial;

import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.enums.CustomMaterialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight DTO for material list views.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Custom material list item")
public class MaterialListItemDTO {

    @Schema(description = "Material ID")
    private UUID id;

    @Schema(description = "Material title", example = "Q1 Marketing Report")
    private String title;

    @Schema(description = "Source type", example = "PDF")
    private CustomMaterialSourceType sourceType;

    @Schema(description = "Processing status", example = "COMPLETED")
    private CustomMaterialStatus status;

    @Schema(description = "Number of vocabulary items generated")
    private Integer vocabularyCount;

    @Schema(description = "Number of quiz questions generated")
    private Integer quizCount;

    @Schema(description = "Whether role-play is available")
    private Boolean hasRolePlay;

    @Schema(description = "Whether shadowing is available")
    private Boolean hasShadowing;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;
}
