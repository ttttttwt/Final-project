package com.lexia.backend.dto.custommaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for material creation (202 Accepted).
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response after material upload")
public class MaterialCreatedDTO {

    @Schema(description = "Material ID")
    private UUID id;

    @Schema(description = "Processing status", example = "PROCESSING")
    private String status;

    @Schema(description = "User-friendly message", example = "Your material is being processed. We'll notify you when it's ready.")
    private String message;
}
