package com.lexia.backend.dto.custommaterial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Input metadata for page/time range selection.
 * 
 * @since Sprint 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Input range metadata for documents or videos")
public class InputMetadataDTO {

    @Schema(description = "Starting page for documents (1-based)", example = "1")
    private Integer pageStart;

    @Schema(description = "Ending page for documents (max 20 pages)", example = "10")
    private Integer pageEnd;

    @Schema(description = "Start time in seconds for videos", example = "0")
    private Integer timeStart;

    @Schema(description = "End time in seconds for videos (max 15 min = 900s)", example = "600")
    private Integer timeEnd;

    /**
     * Converts to map for JSONB storage.
     */
    public Map<String, Object> toMap() {
        var map = new HashMap<String, Object>();
        if (pageStart != null)
            map.put("pageStart", pageStart);
        if (pageEnd != null)
            map.put("pageEnd", pageEnd);
        if (timeStart != null)
            map.put("timeStart", timeStart);
        if (timeEnd != null)
            map.put("timeEnd", timeEnd);
        return map;
    }
}
