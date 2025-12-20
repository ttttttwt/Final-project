package com.lexia.backend.controller.ai;

import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.service.ai.CustomMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Custom Material AI Content Generator.
 * 
 * <p>
 * Allows Premium users to upload personal documents and generate
 * personalized AI learning content (vocabulary, quiz, summary, role-play,
 * etc.).
 * </p>
 * 
 * @since Sprint 5
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/custom-materials")
@RequiredArgsConstructor
@Tag(name = "Custom Materials", description = "AI content generation from user-uploaded materials")
@SecurityRequirement(name = "bearerAuth")
public class CustomMaterialController {

    private static final int MAX_PAGE_SIZE = 50;

    private final CustomMaterialService customMaterialService;

    // ===== Upload & Create =====

    @Operation(summary = "Upload material for AI processing", description = "Upload a file (PDF, DOCX, Image), paste a URL (YouTube, Website), "
            +
            "or raw text. The system will process it and generate personalized learning content.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Material accepted for processing", content = @Content(schema = @Schema(implementation = MaterialCreatedDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not Premium user or quota exceeded"),
            @ApiResponse(responseCode = "413", description = "File too large (max 10MB)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialCreatedDTO> createMaterial(
            @Parameter(description = "File to upload (for PDF, DOCX, IMAGE source types)") @RequestPart(value = "file", required = false) MultipartFile file,

            @Parameter(description = "Material request JSON", required = true) @RequestPart(value = "request") @Valid CreateMaterialRequestDTO request,

            @AuthenticationPrincipal User user) {
        log.info("User {} creating material of type {}", user.getId(), request.getSourceType());

        MaterialCreatedDTO result = customMaterialService.createMaterial(request, file, user.getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    // ===== List & Get =====

    @Operation(summary = "List user's materials", description = "Get paginated list of materials with optional status filter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Materials retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<Page<MaterialListItemDTO>> listMaterials(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 50)") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Filter by status") @RequestParam(required = false) CustomMaterialStatus status,

            @AuthenticationPrincipal User user) {
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<MaterialListItemDTO> materials = customMaterialService.listMaterials(
                user.getId(), status, pageable);

        return ResponseEntity.ok(materials);
    }

    @Operation(summary = "Get material details", description = "Get full material with generated content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Material retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of material"),
            @ApiResponse(responseCode = "404", description = "Material not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> getMaterial(
            @Parameter(description = "Material ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        MaterialResponseDTO material = customMaterialService.getMaterial(id, user.getId());
        return ResponseEntity.ok(material);
    }

    @Operation(summary = "Get processing status", description = "Poll for material processing status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved"),
            @ApiResponse(responseCode = "404", description = "Material not found")
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<MaterialStatusDTO> getStatus(
            @Parameter(description = "Material ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        MaterialStatusDTO status = customMaterialService.getStatus(id, user.getId());
        return ResponseEntity.ok(status);
    }

    // ===== Update & Delete =====

    @Operation(summary = "Update generated content", description = "Edit or delete items in generated content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content updated"),
            @ApiResponse(responseCode = "400", description = "Invalid content or material not completed"),
            @ApiResponse(responseCode = "403", description = "Not owner of material"),
            @ApiResponse(responseCode = "404", description = "Material not found")
    })
    @PatchMapping("/{id}/content")
    public ResponseEntity<MaterialResponseDTO> updateContent(
            @Parameter(description = "Material ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateContentDTO request,
            @AuthenticationPrincipal User user) {
        MaterialResponseDTO result = customMaterialService.updateContent(id, request, user.getId());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Delete material", description = "Delete a material and all associated data")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Material deleted"),
            @ApiResponse(responseCode = "403", description = "Not owner of material"),
            @ApiResponse(responseCode = "404", description = "Material not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(
            @Parameter(description = "Material ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        customMaterialService.deleteMaterial(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ===== Quota =====

    @Operation(summary = "Get remaining quota", description = "Get how many materials user can create this month")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quota retrieved")
    })
    @GetMapping("/quota")
    public ResponseEntity<Map<String, Object>> getQuota(@AuthenticationPrincipal User user) {
        int remaining = customMaterialService.getRemainingQuota(user.getId());
        boolean canCreate = customMaterialService.canCreateMaterial(user.getId());

        return ResponseEntity.ok(Map.of(
                "remaining", remaining,
                "canCreate", canCreate,
                "monthlyLimit", 10));
    }
}
