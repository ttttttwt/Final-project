package com.lexia.backend.dto.custommaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for shadowing pronunciation scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShadowingScoreResponseDTO {

    /**
     * Pronunciation score from 0-100.
     */
    private int score;

    /**
     * Human-readable feedback on pronunciation.
     */
    private String feedback;

    /**
     * Detailed phoneme/word-level breakdown (optional).
     * Structure: { "word": { "expected": "...", "detected": "...", "score": 85 } }
     */
    private Map<String, Object> phonemeBreakdown;
}
