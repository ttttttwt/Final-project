package com.lexia.backend.dto.custommaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for style transformation.
 *
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleTransformResponseDTO {

    /**
     * The transformed text.
     */
    private String transformedText;

    /**
     * Explanations of changes (only if includeExplanation was true).
     */
    private List<StyleExplanation> explanations;

    /**
     * Single style change explanation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleExplanation {
        private String original;
        private String changed;
        private String reason;
    }
}
