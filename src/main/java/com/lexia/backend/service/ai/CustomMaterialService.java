package com.lexia.backend.service.ai;

import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.enums.CustomMaterialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for Custom Material operations.
 * 
 * <p>
 * Allows Premium users to upload personal documents and generate
 * personalized AI learning content.
 * </p>
 * 
 * @since Sprint 5
 */
public interface CustomMaterialService {

    /**
     * Creates a new custom material with async processing.
     * 
     * <p>
     * For file uploads (PDF, DOCX, IMAGE): stores the file and queues for
     * processing.
     * For URLs (YOUTUBE, WEBSITE): fetches content and queues for processing.
     * For TEXT: directly queues for processing.
     * </p>
     * 
     * @param request the material creation request
     * @param file    the uploaded file (required for PDF, DOCX, IMAGE)
     * @param userId  the authenticated user ID
     * @return MaterialCreatedDTO with status PROCESSING
     * @throws com.lexia.backend.exception.QuotaExceededException        if monthly
     *                                                                   quota
     *                                                                   exceeded
     * @throws com.lexia.backend.exception.SubscriptionRequiredException if user is
     *                                                                   not Premium
     * @throws IllegalArgumentException                                  if request
     *                                                                   validation
     *                                                                   fails
     */
    MaterialCreatedDTO createMaterial(CreateMaterialRequestDTO request, MultipartFile file, UUID userId);

    /**
     * Lists all materials for a user with optional status filter.
     * 
     * @param userId   the user ID
     * @param status   optional status filter (null for all)
     * @param pageable pagination info
     * @return page of material list items
     */
    Page<MaterialListItemDTO> listMaterials(UUID userId, CustomMaterialStatus status, Pageable pageable);

    /**
     * Gets full material details with generated content.
     * 
     * @param materialId the material ID
     * @param userId     the user ID (for ownership check)
     * @return full material response
     * @throws com.lexia.backend.exception.ResourceNotFoundException if not found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     */
    MaterialResponseDTO getMaterial(UUID materialId, UUID userId);

    /**
     * Gets material processing status for polling.
     * 
     * @param materialId the material ID
     * @param userId     the user ID (for ownership check)
     * @return status DTO with progress
     * @throws com.lexia.backend.exception.ResourceNotFoundException if not found
     */
    MaterialStatusDTO getStatus(UUID materialId, UUID userId);

    /**
     * Updates generated content (edit/delete vocabulary, quiz items).
     * 
     * @param materialId the material ID
     * @param request    the update request
     * @param userId     the user ID (for ownership check)
     * @return updated material response
     * @throws com.lexia.backend.exception.ResourceNotFoundException if not found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     * @throws IllegalStateException                                 if material not
     *                                                               completed
     */
    MaterialResponseDTO updateContent(UUID materialId, UpdateContentDTO request, UUID userId);

    /**
     * Deletes a material and all associated data.
     * 
     * @param materialId the material ID
     * @param userId     the user ID (for ownership check)
     * @throws com.lexia.backend.exception.ResourceNotFoundException if not found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     */
    void deleteMaterial(UUID materialId, UUID userId);

    /**
     * Checks if user can create more materials (quota check).
     * 
     * @param userId the user ID
     * @return true if under monthly quota
     */
    boolean canCreateMaterial(UUID userId);

    /**
     * Gets remaining material quota for user.
     * 
     * @param userId the user ID
     * @return number of materials user can still create this month
     */
    int getRemainingQuota(UUID userId);

    /**
     * Transforms text from one style to another using AI.
     *
     * @param request the style transform request
     * @param userId  the user ID
     * @return transformed text with optional explanations
     */
    StyleTransformResponseDTO transformStyle(StyleTransformRequestDTO request, UUID userId);

    /**
     * Scores user's shadowing pronunciation attempt.
     *
     * @param materialId the material ID containing shadowing content
     * @param sentenceId the sentence ID from shadowing content
     * @param audio      user's recorded audio file
     * @param userId     the user ID (for ownership check)
     * @return scoring response with feedback
     * @throws com.lexia.backend.exception.ResourceNotFoundException if material or
     *                                                               sentence not
     *                                                               found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     */
    ShadowingScoreResponseDTO scoreShadowing(UUID materialId, String sentenceId, MultipartFile audio, UUID userId);

    /**
     * Gets materials related to the given material (same source).
     * Used for sidebar navigation to switch between different content types
     * generated from the same original source.
     * 
     * @param materialId the current material ID
     * @param userId     the user ID (for ownership check)
     * @return list of related materials from same source
     * @throws com.lexia.backend.exception.ResourceNotFoundException if material not
     *                                                               found
     * @throws com.lexia.backend.exception.AccessDeniedException     if not owner
     */
    java.util.List<MaterialListItemDTO> getRelatedMaterials(UUID materialId, UUID userId);
}
