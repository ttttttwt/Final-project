package com.lexia.backend.controller;

import com.lexia.backend.dto.ai.AlertListResponse;
import com.lexia.backend.entity.AIAlert;
import com.lexia.backend.service.ai.AIAlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ai-alerts")
@RequiredArgsConstructor
@Tag(name = "Admin AI Alerts", description = "AI System Alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIAlertController {

    private final AIAlertService alertService;

    @GetMapping
    public ResponseEntity<AlertListResponse> getAlerts(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        Boolean isRead = unreadOnly != null && unreadOnly ? false : null;
        
        Page<AIAlert> alertPage = alertService.getAlerts(
            isRead, 
            severity, 
            type, 
            PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        long unreadCount = alertService.getUnreadAlerts().size(); // Optimize this later if needed

        return ResponseEntity.ok(AlertListResponse.builder()
                .content(alertPage.getContent())
                .totalElements(alertPage.getTotalElements())
                .unreadCount(unreadCount)
                .build());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long id) {
        alertService.markAsRead(id); // Treat acknowledge as read for now
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
