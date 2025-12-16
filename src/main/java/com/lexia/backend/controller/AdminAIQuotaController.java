package com.lexia.backend.controller;

import com.lexia.backend.dto.ai.UserAiQuotaDTO;
import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.service.ai.AIQuotaService;
import com.lexia.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/ai-quotas")
@RequiredArgsConstructor
@Tag(name = "Admin AI Quotas", description = "Manage user AI quotas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIQuotaController {

    private final AIQuotaService quotaService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<UserAiQuotaDTO>> getAllQuotas(Pageable pageable) {
        return ResponseEntity.ok(quotaService.getAllQuotas(pageable).map(this::mapToDTO));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserAiQuotaDTO> getQuota(@PathVariable UUID userId) {
        return ResponseEntity.ok(mapToDTO(quotaService.getQuotaByUserId(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserAiQuotaDTO> updateQuota(@PathVariable UUID userId, @RequestBody com.lexia.backend.dto.ai.UpdateQuotaRequest request) {
        return ResponseEntity.ok(mapToDTO(quotaService.updateQuota(userId, request)));
    }

    @PostMapping("/{userId}/reset")
    public ResponseEntity<Void> resetQuota(@PathVariable UUID userId) {
        quotaService.resetQuota(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<java.util.Map<String, Object>> bulkUpdateQuotas(@RequestBody com.lexia.backend.dto.ai.BulkUpdateQuotaRequest request) {
        quotaService.bulkUpdateQuotas(request.getUserIds(), request.getQuotaDetails());
        return ResponseEntity.ok(java.util.Map.of("updated", request.getUserIds().size(), "failed", 0));
    }

    @PatchMapping("/{userId}/unlimited")
    public ResponseEntity<UserAiQuotaDTO> setUnlimited(@PathVariable UUID userId, @RequestBody java.util.Map<String, Boolean> body) {
        return ResponseEntity.ok(mapToDTO(quotaService.setUnlimited(userId, body.get("isUnlimited"))));
    }

    private UserAiQuotaDTO mapToDTO(UserAiQuota entity) {
        UserAiQuotaDTO dto = new UserAiQuotaDTO();
        dto.setUserId(entity.getUserId());
        
        // Fetch user details
        userRepository.findById(entity.getUserId()).ifPresent(user -> {
            dto.setUserEmail(user.getEmail());
            if (user.getProfile() != null) {
                dto.setUserFullName(user.getProfile().getFullName());
            }
        });

        dto.setDailyLimit(entity.getDailyLimit());
        dto.setDailyUsed(entity.getDailyUsed());
        dto.setLastResetDaily(entity.getLastResetDaily());
        dto.setMonthlyLimit(entity.getMonthlyLimit());
        dto.setMonthlyUsed(entity.getMonthlyUsed());
        dto.setLastResetMonthly(entity.getLastResetMonthly());
        dto.setIsPremium(entity.getIsPremium());
        dto.setSuspended(entity.getSuspended());
        
        // Set isUnlimited
        dto.setIsUnlimited(entity.getDailyLimit() == Integer.MAX_VALUE);

        // Feature specific
        dto.setRolePlayDailyLimit(entity.getFeatureDailyLimit("roleplay"));
        dto.setRolePlayUsedToday(entity.getFeatureDailyUsage("roleplay"));
        
        dto.setGrammarDailyLimit(entity.getFeatureDailyLimit("grammar"));
        dto.setGrammarUsedToday(entity.getFeatureDailyUsage("grammar"));
        
        dto.setFlashcardDailyLimit(entity.getFeatureDailyLimit("flashcard"));
        dto.setFlashcardUsedToday(entity.getFeatureDailyUsage("flashcard"));
        
        dto.setTotalUsedToday(entity.getDailyUsed());
        dto.setTotalDailyLimit(entity.getDailyLimit());

        return dto;
    }
}
