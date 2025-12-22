package com.lexia.backend.mapper;

import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.entity.UserCustomMaterialSettings;
import com.lexia.backend.entity.CustomMaterialJob;
import com.lexia.backend.enums.AiCorrectionMode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Mapper for CustomMaterial entities and DTOs.
 * 
 * @since Sprint 5
 */
@Component
public class CustomMaterialMapper {

    /**
     * Maps entity to list item DTO.
     */
    public MaterialListItemDTO toListItem(UserCustomMaterial entity) {
        if (entity == null)
            return null;

        Map<String, Object> content = entity.getGeneratedContent();
        int vocabCount = 0;
        int quizCount = 0;
        boolean hasRolePlay = false;
        boolean hasShadowing = false;

        if (content != null) {
            if (content.get("vocabulary") instanceof List<?> list) {
                vocabCount = list.size();
            }
            if (content.get("quiz") instanceof List<?> list) {
                quizCount = list.size();
            }
            hasRolePlay = content.get("roleplay") != null;
            hasShadowing = content.get("shadowing") != null
                    && content.get("shadowing") instanceof List<?> list
                    && !list.isEmpty();
        }

        return MaterialListItemDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sourceType(entity.getSourceType())
                .status(entity.getStatus())
                .vocabularyCount(vocabCount)
                .quizCount(quizCount)
                .hasRolePlay(hasRolePlay)
                .hasShadowing(hasShadowing)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Maps entity to full response DTO.
     */
    public MaterialResponseDTO toResponse(UserCustomMaterial entity) {
        if (entity == null)
            return null;

        // Convert Map to GeneratedContentDTO - use raw content for now
        // Full parsing would be done based on schema version
        Map<String, Object> content = entity.getGeneratedContent();

        return MaterialResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sourceType(entity.getSourceType())
                .status(entity.getStatus())
                .originalFileUrl(entity.getOriginalFileUrl())
                .contentText(entity.getContentText())
                .inputMetadata(entity.getInputMetadata())
                .generatedContent(content) // Return raw map for now
                .settings(toSettingsResponse(entity.getSettings()))
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Maps entity and job to status DTO.
     */
    public MaterialStatusDTO toStatus(UserCustomMaterial entity, CustomMaterialJob job) {
        if (entity == null)
            return null;

        int progress = 0;
        if (job != null) {
            progress = job.getProgress();
        }

        return MaterialStatusDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .progress(progress)
                .errorMessage(entity.getErrorMessage())
                .estimatedSecondsRemaining(calculateEstimatedTime(progress))
                .build();
    }

    /**
     * Maps settings entity to response DTO.
     */
    public MaterialSettingsResponseDTO toSettingsResponse(UserCustomMaterialSettings settings) {
        if (settings == null) {
            return MaterialSettingsResponseDTO.builder()
                    .targetOptions(List.of("VOCABULARY"))
                    .aiCorrectionMode(AiCorrectionMode.POLITE)
                    .styleLearnMode(true)
                    .syncVocabToSrs(false)
                    .build();
        }

        return MaterialSettingsResponseDTO.builder()
                .targetOptions(settings.getTargetOptions())
                .aiCorrectionMode(settings.getAiCorrectionMode())
                .styleLearnMode(settings.getStyleLearnMode())
                .syncVocabToSrs(settings.getSyncVocabToSrs())
                .build();
    }

    /**
     * Creates a MaterialCreatedDTO for 202 response.
     */
    public MaterialCreatedDTO toCreated(UserCustomMaterial entity) {
        return MaterialCreatedDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus().getValue())
                .message("Your material is being processed. We'll notify you when it's ready.")
                .build();
    }

    // ===== Private Helpers =====

    private Integer calculateEstimatedTime(int progress) {
        if (progress >= 100)
            return 0;
        if (progress == 0)
            return 60; // Estimate 60 seconds total

        // Linear estimation: remaining = total * (100 - progress) / 100
        int estimatedTotal = 60;
        return (estimatedTotal * (100 - progress)) / 100;
    }
}
