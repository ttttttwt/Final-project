package com.lexia.backend.controller;

import com.lexia.backend.dto.admin.AbnormalActivityAlertDTO;
import com.lexia.backend.dto.admin.ActiveUsersDTO;
import com.lexia.backend.dto.admin.UserActivityDTO;
import com.lexia.backend.dto.admin.UserSessionDTO;
import com.lexia.backend.service.AdminUserMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller for user monitoring operations.
 * Provides endpoints for tracking active users, sessions, and abnormal
 * activity.
 */
@RestController
@RequestMapping("/api/v1/admin/monitoring/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Monitoring", description = "User monitoring and activity tracking")
public class AdminUserMonitoringController {

    private final AdminUserMonitoringService monitoringService;

    /**
     * Get active users overview with device breakdown.
     */
    @GetMapping("/active")
    @Operation(summary = "Get active users", description = "Returns count and list of currently active users")
    public ResponseEntity<ActiveUsersDTO> getActiveUsers(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(monitoringService.getActiveUsers(limit));
    }

    /**
     * Get session history for a specific user.
     */
    @GetMapping("/{userId}/sessions")
    @Operation(summary = "Get user sessions", description = "Returns login session history for a user")
    public ResponseEntity<Page<UserSessionDTO>> getUserSessions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                monitoringService.getUserSessions(userId, PageRequest.of(page, size)));
    }

    /**
     * Get AI activity history for a specific user.
     */
    @GetMapping("/{userId}/activity")
    @Operation(summary = "Get user activity", description = "Returns AI usage activity for a user")
    public ResponseEntity<UserActivityDTO> getUserActivity(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(monitoringService.getUserActivity(userId, limit));
    }

    /**
     * Get abnormal activity alerts.
     */
    @GetMapping("/alerts")
    @Operation(summary = "Get activity alerts", description = "Returns detected abnormal activity alerts")
    public ResponseEntity<List<AbnormalActivityAlertDTO>> getAbnormalActivityAlerts() {
        return ResponseEntity.ok(monitoringService.getAbnormalActivityAlerts());
    }

    /**
     * Deactivate all sessions for a user (force logout).
     */
    @PostMapping("/{userId}/logout-all")
    @Operation(summary = "Force logout user", description = "Deactivates all sessions for a user")
    public ResponseEntity<Void> forceLogoutUser(@PathVariable UUID userId) {
        monitoringService.deactivateAllUserSessions(userId);
        return ResponseEntity.noContent().build();
    }
}
