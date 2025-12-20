package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.entity.*;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.exception.AccessDeniedException;
import com.lexia.backend.exception.QuotaExceededException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.service.FileStorageService;
import com.lexia.backend.mapper.CustomMaterialMapper;
import com.lexia.backend.repository.*;
import com.lexia.backend.service.ai.CustomMaterialProcessingService;
import com.lexia.backend.service.ai.CustomMaterialService;
import com.lexia.backend.service.ai.AIConfigService;
import com.lexia.backend.exception.ai.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of CustomMaterialService.
 * 
 * <p>
 * Handles file uploads, content extraction, and async AI processing
 * for custom learning materials.
 * </p>
 * 
 * @since Sprint 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomMaterialServiceImpl implements CustomMaterialService {

    private static final int MONTHLY_QUOTA = 10;
    private static final String FEATURE_TYPE = "custom_materials";

    private final UserCustomMaterialRepository materialRepository;
    private final UserCustomMaterialSettingsRepository settingsRepository;
    private final CustomMaterialJobRepository jobRepository;
    private final FileStorageService fileStorageService;
    private final CustomMaterialMapper mapper;
    private final CustomMaterialProcessingService processingService;
    private final AIConfigService aiConfigService;

    // ===== Public Methods =====

    @Override
    @Transactional
    public MaterialCreatedDTO createMaterial(CreateMaterialRequestDTO request, MultipartFile file, UUID userId) {
        // Check if feature is enabled
        var featureConfig = aiConfigService.getFeatureConfig("custom_materials");
        if (!featureConfig.isEnabled()) {
            throw new AiServiceException("Custom materials feature is currently disabled by administrator");
        }

        // Validate request
        request.validate();

        // Check quota
        int remaining = getRemainingQuota(userId);
        if (remaining <= 0) {
            throw new QuotaExceededException(
                    FEATURE_TYPE,
                    MONTHLY_QUOTA,
                    MONTHLY_QUOTA,
                    PlanType.MONTHLY // Premium users (Pro tier)
            );
        }

        // Handle file upload if needed
        String fileUrl = null;
        if (request.getSourceType().requiresFileUpload()) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is required for " + request.getSourceType() + " source type");
            }
            var fileEntity = fileStorageService.store(file, FileCategory.CUSTOM_MATERIAL, userId);
            fileUrl = fileStorageService.getPublicUrl(fileEntity.getId());
        }

        // Create material entity
        UserCustomMaterial material = UserCustomMaterial.builder()
                .userId(userId)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .originalFileUrl(fileUrl)
                .contentText(request.getRawText())
                .inputMetadata(request.getInputMetadata() != null ? request.getInputMetadata().toMap() : Map.of())
                .status(CustomMaterialStatus.PENDING)
                .build();

        // Handle URL source types
        if (request.getSourceType().requiresUrl()) {
            Map<String, Object> metadata = new java.util.HashMap<>(material.getInputMetadata());
            metadata.put("sourceUrl", request.getSourceUrl());
            material.setInputMetadata(metadata);
        }

        material = materialRepository.save(material);

        // Create settings
        createSettings(material, request);

        // Create processing job
        createJob(material);

        log.info("Created material {} for user {}", material.getId(), userId);

        // Trigger async processing
        processAsync(material.getId());

        return mapper.toCreated(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialListItemDTO> listMaterials(UUID userId, CustomMaterialStatus status, Pageable pageable) {
        Page<UserCustomMaterial> materials;

        if (status != null) {
            materials = materialRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            materials = materialRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return materials.map(mapper::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponseDTO getMaterial(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);
        return mapper.toResponse(material);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialStatusDTO getStatus(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);
        CustomMaterialJob job = jobRepository.findByMaterialId(materialId).orElse(null);
        return mapper.toStatus(material, job);
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateContent(UUID materialId, UpdateContentDTO request, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);

        if (!material.isReady()) {
            throw new IllegalStateException("Cannot update content for material that is not completed");
        }

        material.setGeneratedContent(request.getGeneratedContent());
        material = materialRepository.save(material);

        log.info("Updated content for material {}", materialId);
        return mapper.toResponse(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(UUID materialId, UUID userId) {
        UserCustomMaterial material = findMaterialWithOwnershipCheck(materialId, userId);

        // Delete associated file if exists
        if (material.getOriginalFileUrl() != null) {
            try {
                // Note: Would need to extract file ID from URL
                // For now, just log
                log.info("Would delete file for material {}", materialId);
            } catch (Exception e) {
                log.warn("Failed to delete file for material {}: {}", materialId, e.getMessage());
            }
        }

        materialRepository.delete(material);
        log.info("Deleted material {}", materialId);
    }

    @Override
    public boolean canCreateMaterial(UUID userId) {
        return getRemainingQuota(userId) > 0;
    }

    @Override
    public int getRemainingQuota(UUID userId) {
        Instant monthStart = LocalDate.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        long usedThisMonth = materialRepository.countByUserIdThisMonth(userId, monthStart);
        return Math.max(0, MONTHLY_QUOTA - (int) usedThisMonth);
    }

    // ===== Private Helpers =====

    private UserCustomMaterial findMaterialWithOwnershipCheck(UUID materialId, UUID userId) {
        UserCustomMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));

        if (!material.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have access to this material");
        }

        return material;
    }

    private void createSettings(UserCustomMaterial material, CreateMaterialRequestDTO request) {
        UserCustomMaterialSettings settings = UserCustomMaterialSettings.builder()
                .material(material)
                .targetOptions(request.getTargetOptions() != null
                        ? request.getTargetOptions()
                        : List.of(UserCustomMaterialSettings.OPTION_VOCABULARY))
                .build();

        MaterialSettingsDTO settingsDTO = request.getSettings();
        if (settingsDTO != null) {
            if (settingsDTO.getAiCorrectionMode() != null) {
                settings.setAiCorrectionMode(settingsDTO.getAiCorrectionMode());
            }
            if (settingsDTO.getStyleLearnMode() != null) {
                settings.setStyleLearnMode(settingsDTO.getStyleLearnMode());
            }
            if (settingsDTO.getSyncVocabToSrs() != null) {
                settings.setSyncVocabToSrs(settingsDTO.getSyncVocabToSrs());
            }
        }

        settingsRepository.save(settings);
    }

    private void createJob(UserCustomMaterial material) {
        CustomMaterialJob job = CustomMaterialJob.builder()
                .material(material)
                .status(CustomMaterialJob.STATUS_QUEUED)
                .progress(0)
                .build();
        jobRepository.save(job);
    }

    /**
     * Triggers async processing of material content.
     * Delegates to CustomMaterialProcessingService.
     */
    private void processAsync(UUID materialId) {
        log.info("Triggering async processing for material {}", materialId);
        processingService.processMaterial(materialId);
    }
}
