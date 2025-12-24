package com.lexia.backend.dto.custommaterial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for style transformation.
 *
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleTransformRequestDTO {

    /**
     * The text to transform.
     */
    @NotBlank(message = "Text is required")
    @Size(max = 5000, message = "Text must be at most 5000 characters")
    private String text;

    /**
     * Target style: FORMAL, CASUAL, EMAIL, PRESENTATION, SOCIAL_MEDIA, DIPLOMATIC, PERSUASIVE
     */
    @NotBlank(message = "Target style is required")
    private String targetStyle;

    /**
     * If true, include explanations of changes (Learn Mode).
     */
    @Builder.Default
    private boolean includeExplanation = true;
}
