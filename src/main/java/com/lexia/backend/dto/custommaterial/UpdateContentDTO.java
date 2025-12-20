package com.lexia.backend.dto.custommaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for updating generated content (edit/delete items).
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update generated content")
public class UpdateContentDTO {

    @NotNull
    @Schema(description = "Updated generated content", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> generatedContent;
}
