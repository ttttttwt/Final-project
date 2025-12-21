package com.lexia.backend.controller.ai;

import com.lexia.backend.annotation.RequirePremium;
import com.lexia.backend.dto.custommaterial.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.service.ai.CustomMaterialChatService;
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
        private final CustomMaterialChatService customMaterialChatService;

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
        @RequirePremium(message = "Custom Materials is a Pro-only feature")
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
        @RequirePremium(message = "Custom Materials is a Pro-only feature")
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
        @RequirePremium(message = "Custom Materials is a Pro-only feature")
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

        // ===== Role-Play Chat =====

        @Operation(summary = "Send chat message", description = "Send a message in a role-play chat session. Creates new session if sessionId is null.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "AI response received", content = @Content(schema = @Schema(implementation = ChatMessageResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Material not ready or session not active"),
                        @ApiResponse(responseCode = "403", description = "Not owner of material"),
                        @ApiResponse(responseCode = "404", description = "Material or session not found")
        })
        @PostMapping("/{id}/chat")
        @RequirePremium(message = "Custom Materials is a Pro-only feature")
        public ResponseEntity<ChatMessageResponseDTO> sendChatMessage(
                        @Parameter(description = "Material ID") @PathVariable UUID id,
                        @Valid @RequestBody ChatMessageRequestDTO request,
                        @AuthenticationPrincipal User user) {
                log.info("User {} sending chat message for material {}", user.getId(), id);

                ChatMessageResponseDTO response = customMaterialChatService.sendMessage(
                                id, request.getMessage(), request.getSessionId(), user.getId());

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "End chat session", description = "End a chat session and get performance report")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Session ended with report", content = @Content(schema = @Schema(implementation = EndChatResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Session already ended"),
                        @ApiResponse(responseCode = "403", description = "Not owner of session"),
                        @ApiResponse(responseCode = "404", description = "Session not found")
        })
        @PostMapping("/{id}/chat/{sessionId}/end")
        @RequirePremium(message = "Custom Materials is a Pro-only feature")
        public ResponseEntity<EndChatResponseDTO> endChatSession(
                        @Parameter(description = "Material ID") @PathVariable UUID id,
                        @Parameter(description = "Session ID") @PathVariable UUID sessionId,
                        @AuthenticationPrincipal User user) {
                log.info("User {} ending chat session {} for material {}", user.getId(), sessionId, id);

                EndChatResponseDTO response = customMaterialChatService.endSession(id, sessionId, user.getId());

                return ResponseEntity.ok(response);
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

        // ===== Style Transform =====

        @Operation(summary = "Transform text style", description = "Transform text from one style to another (formal, casual, email, etc.)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Text transformed", content = @Content(schema = @Schema(implementation = StyleTransformResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "403", description = "Not Premium user")
        })
        @PostMapping("/transform-style")
        @RequirePremium(message = "Style Transform is a Pro-only feature")
        public ResponseEntity<StyleTransformResponseDTO> transformStyle(
                        @Valid @RequestBody StyleTransformRequestDTO request,
                        @AuthenticationPrincipal User user) {
                log.info("User {} requesting style transform to {}", user.getId(), request.getTargetStyle());

                StyleTransformResponseDTO response = customMaterialService.transformStyle(request, user.getId());

                return ResponseEntity.ok(response);
        }

        // ===== Shadowing =====

        @Operation(summary = "Score shadowing pronunciation", description = "Upload user's audio recording and get pronunciation score and feedback")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pronunciation scored", content = @Content(schema = @Schema(implementation = ShadowingScoreResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Material not ready or sentence not found"),
                        @ApiResponse(responseCode = "403", description = "Not owner of material"),
                        @ApiResponse(responseCode = "404", description = "Material or sentence not found")
        })
        @PostMapping(value = "/{id}/shadowing/{sentenceId}/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @RequirePremium(message = "Shadowing is a Pro-only feature")
        public ResponseEntity<ShadowingScoreResponseDTO> scoreShadowing(
                        @Parameter(description = "Material ID") @PathVariable UUID id,
                        @Parameter(description = "Sentence ID from shadowing content") @PathVariable String sentenceId,
                        @Parameter(description = "User's recorded audio") @RequestPart(value = "audio", required = false) MultipartFile audio,
                        @AuthenticationPrincipal User user) {
                log.info("User {} scoring shadowing for material {} sentence {}", user.getId(), id, sentenceId);

                ShadowingScoreResponseDTO response = customMaterialService.scoreShadowing(id, sentenceId, audio,
                                user.getId());

                return ResponseEntity.ok(response);
        }
}
