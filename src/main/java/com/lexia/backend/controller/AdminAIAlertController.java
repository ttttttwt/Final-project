package com.lexia.backend.controller;

import com.lexia.backend.entity.AIAlert;
import com.lexia.backend.service.ai.AIAlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<AIAlert>> getAlerts(
            @RequestParam(required = false, defaultValue = "true") boolean unreadOnly,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        // For now, we just return unread alerts as per service implementation
        // Ideally, service should support filtering
        return ResponseEntity.ok(alertService.getUnreadAlerts());
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
        // Implement mark all as read in service if needed
        return ResponseEntity.ok().build();
    }
}
