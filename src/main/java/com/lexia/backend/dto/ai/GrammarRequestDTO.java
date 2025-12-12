package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting grammar exercise generation.
 * Contains parameters for AI-based or fallback exercise generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for generating grammar exercises")
public class GrammarRequestDTO {

    @Schema(description = "Grammar topic to practice", example = "Present Simple", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Grammar topic is required")
    @Size(max = 100, message = "Grammar topic must not exceed 100 characters")
    private String grammarTopic;

    @Schema(description = "CEFR level (A1-C2)", example = "B1", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    @NotBlank(message = "CEFR level is required")
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$", message = "Invalid CEFR level")
    private String cefrLevel;

    @Schema(description = "Contextual theme for exercises", example = "workplace")
    @Size(max = 50, message = "Theme must not exceed 50 characters")
    private String theme;

    @Schema(description = "Number of exercises to generate", example = "5", minimum = "1", maximum = "20")
    @Min(value = 1, message = "Must request at least 1 exercise")
    @Max(value = 20, message = "Cannot request more than 20 exercises")
    @Builder.Default
    private Integer exerciseCount = 5;

    @Schema(description = "Time limit in seconds (0 for no limit)", example = "600", minimum = "0")
    @Min(value = 0, message = "Time limit must be non-negative")
    @Builder.Default
    private Integer timeLimitSeconds = 600;

    @Schema(description = "Whether to prefer fallback content", example = "false")
    @Builder.Default
    private Boolean useFallback = false;

    /**
     * Creates a simple request with defaults.
     */
    public static GrammarRequestDTO simple(String grammarTopic, String cefrLevel) {
        return GrammarRequestDTO.builder()
                .grammarTopic(grammarTopic)
                .cefrLevel(cefrLevel)
                .build();
    }

    /**
     * Creates a request with theme.
     */
    public static GrammarRequestDTO withTheme(String grammarTopic, String cefrLevel, String theme) {
        return GrammarRequestDTO.builder()
                .grammarTopic(grammarTopic)
                .cefrLevel(cefrLevel)
                .theme(theme)
                .build();
    }

    /**
     * Checks if time limit is enabled.
     */
    public boolean hasTimeLimit() {
        return timeLimitSeconds != null && timeLimitSeconds > 0;
    }
}
