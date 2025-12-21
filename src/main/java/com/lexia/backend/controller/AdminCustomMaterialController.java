package com.lexia.backend.controller;

import com.lexia.backend.dto.admin.AdminCustomMaterialDTO;
import com.lexia.backend.dto.admin.AdminJobDTO;
import com.lexia.backend.dto.admin.JobStatsDTO;
import com.lexia.backend.entity.CustomMaterialJob;
import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialStatus;
import com.lexia.backend.repository.CustomMaterialJobRepository;
import com.lexia.backend.repository.UserCustomMaterialRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.ai.CustomMaterialProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for Custom Material management and job monitoring.
 * 
 * <p>
 * Endpoints:
 * </p>
 * <ul>
 * <li>GET /materials - List all materials with filtering</li>
 * <li>GET /materials/{id} - Get material details</li>
 * <li>DELETE /materials/{id} - Delete material</li>
 * <li>GET /jobs - List all jobs with filtering</li>
 * <li>GET /jobs/stats - Get job statistics</li>
 * <li>POST /jobs/{id}/retry - Force retry a failed job</li>
 * <li>POST /jobs/{id}/reset-retries - Reset retry count</li>
 * <li>DELETE /jobs/{id} - Force delete a stuck job</li>
 * </ul>
 * 
 * @since Sprint 6
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/custom-materials")
@RequiredArgsConstructor
@Tag(name = "Admin Custom Materials", description = "Admin management for custom materials and processing jobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomMaterialController {

    private static final long STUCK_THRESHOLD_MINUTES = 10;

    private final UserCustomMaterialRepository materialRepository;
    private final CustomMaterialJobRepository jobRepository;
    private final UserRepository userRepository;
    private final CustomMaterialProcessingService processingService;

    // ==================== Material Endpoints ====================

    @GetMapping("/materials")
    @Operation(summary = "List all custom materials with pagination and filtering")
    public ResponseEntity<Page<AdminCustomMaterialDTO>> getAllMaterials(
            @RequestParam(required = false) CustomMaterialStatus status,
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {

        Page<UserCustomMaterial> materials;
        if (status != null && userId != null) {
            materials = materialRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else if (status != null) {
            materials = materialRepository.findAll(pageable); // Would need custom query for status filter
        } else if (userId != null) {
            materials = materialRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            materials = materialRepository.findAll(pageable);
        }

        return ResponseEntity.ok(materials.map(this::mapToAdminDTO));
    }

    @GetMapping("/materials/{id}")
    @Operation(summary = "Get material details by ID")
    public ResponseEntity<AdminCustomMaterialDTO> getMaterial(@PathVariable UUID id) {
        return materialRepository.findById(id)
                .map(this::mapToAdminDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/materials/{id}")
    @Operation(summary = "Delete a material and its associated job")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        if (!materialRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        materialRepository.deleteById(id);
        log.info("Admin deleted material {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Job Endpoints ====================

    @GetMapping("/jobs")
    @Operation(summary = "List all processing jobs with pagination and filtering")
    public ResponseEntity<Page<AdminJobDTO>> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean stuckOnly,
            Pageable pageable) {

        Page<CustomMaterialJob> jobs = jobRepository.findAll(pageable);
        List<AdminJobDTO> dtoList = jobs.getContent().stream()
                .map(this::mapToJobDTO)
                .filter(dto -> {
                    if (status != null && !dto.getStatus().equalsIgnoreCase(status)) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(stuckOnly) && !Boolean.TRUE.equals(dto.getIsStuck())) {
                        return false;
                    }
                    return true;
                })
                .toList();

        return ResponseEntity.ok(new PageImpl<>(dtoList, pageable, jobs.getTotalElements()));
    }

    @GetMapping("/jobs/stats")
    @Operation(summary = "Get job processing statistics")
    public ResponseEntity<JobStatsDTO> getJobStats() {
        List<CustomMaterialJob> allJobs = jobRepository.findAll();

        long totalJobs = allJobs.size();
        long completedJobs = allJobs.stream().filter(j -> "COMPLETED".equals(j.getStatus())).count();
        long failedJobs = allJobs.stream().filter(j -> "FAILED".equals(j.getStatus())).count();
        long processingJobs = allJobs.stream().filter(j -> "PROCESSING".equals(j.getStatus())).count();
        long queuedJobs = allJobs.stream().filter(j -> "QUEUED".equals(j.getStatus())).count();
        long stuckJobs = allJobs.stream().filter(this::isJobStuck).count();

        double successRate = totalJobs > 0 ? (double) completedJobs / totalJobs * 100 : 0;

        return ResponseEntity.ok(JobStatsDTO.builder()
                .totalJobs(totalJobs)
                .completedJobs(completedJobs)
                .failedJobs(failedJobs)
                .processingJobs(processingJobs)
                .queuedJobs(queuedJobs)
                .stuckJobs(stuckJobs)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .build());
    }

    @PostMapping("/jobs/{id}/retry")
    @Operation(summary = "Force retry a failed job")
    public ResponseEntity<Map<String, Object>> retryJob(@PathVariable UUID id) {
        CustomMaterialJob job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success = processingService.retryProcessing(job.getMaterial().getId());
        if (success) {
            log.info("Admin triggered retry for job {}", id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Job retry triggered"));
        } else {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Cannot retry job (max retries exceeded or job not failed)"));
        }
    }

    @PostMapping("/jobs/{id}/reset-retries")
    @Operation(summary = "Reset retry count to allow more retries")
    public ResponseEntity<Map<String, Object>> resetRetryCount(@PathVariable UUID id) {
        CustomMaterialJob job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        job.setRetryCount(0);
        jobRepository.save(job);
        log.info("Admin reset retry count for job {}", id);

        return ResponseEntity.ok(Map.of("success", true, "message", "Retry count reset to 0"));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Force delete a stuck job (use with caution)")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        CustomMaterialJob job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Also update material status
        materialRepository.findById(job.getMaterial().getId()).ifPresent(material -> {
            material.setStatus(CustomMaterialStatus.FAILED);
            material.setErrorMessage("Job forcefully deleted by admin");
            materialRepository.save(material);
        });

        jobRepository.delete(job);
        log.info("Admin deleted job {} for material {}", id, job.getMaterial().getId());

        return ResponseEntity.noContent().build();
    }

    // ==================== Helper Methods ====================

    private AdminCustomMaterialDTO mapToAdminDTO(UserCustomMaterial material) {
        AdminCustomMaterialDTO.AdminCustomMaterialDTOBuilder builder = AdminCustomMaterialDTO.builder()
                .id(material.getId())
                .userId(material.getUserId())
                .title(material.getTitle())
                .sourceType(material.getSourceType().name())
                .status(material.getStatus().name())
                .errorMessage(material.getErrorMessage())
                .contentLength(material.getContentText() != null ? (long) material.getContentText().length() : 0L)
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt());

        // Fetch user info
        userRepository.findById(material.getUserId()).ifPresent(user -> {
            builder.userEmail(user.getEmail());
            if (user.getProfile() != null) {
                builder.userName(user.getProfile().getFullName());
            }
        });

        return builder.build();
    }

    private AdminJobDTO mapToJobDTO(CustomMaterialJob job) {
        AdminJobDTO.AdminJobDTOBuilder builder = AdminJobDTO.builder()
                .id(job.getId())
                .materialId(job.getMaterial().getId())
                .status(job.getStatus())
                .progress(job.getProgress())
                .retryCount(job.getRetryCount())
                .maxRetries(job.getMaxRetries())
                .lastError(job.getLastError())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .isStuck(isJobStuck(job));

        // Fetch material and user info
        materialRepository.findById(job.getMaterial().getId()).ifPresent(material -> {
            builder.materialTitle(material.getTitle());
            builder.userId(material.getUserId());

            userRepository.findById(material.getUserId()).ifPresent(user -> {
                builder.userEmail(user.getEmail());
            });
        });

        return builder.build();
    }

    private boolean isJobStuck(CustomMaterialJob job) {
        if (!"PROCESSING".equals(job.getStatus())) {
            return false;
        }
        if (job.getStartedAt() == null) {
            return false;
        }
        Instant threshold = Instant.now().minus(STUCK_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        return job.getStartedAt().isBefore(threshold);
    }
}
