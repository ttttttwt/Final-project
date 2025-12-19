package com.lexia.backend.controller;

import com.lexia.backend.dto.admin.AnalyticsDTO.*;
import com.lexia.backend.service.AdminAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for analytics dashboard data.
 */
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Analytics", description = "Analytics dashboard endpoints")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    /**
     * Get complete analytics data for dashboard
     */
    @GetMapping
    @Operation(summary = "Get analytics dashboard data")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }

    /**
     * Get overview statistics only
     */
    @GetMapping("/overview")
    @Operation(summary = "Get overview statistics")
    public ResponseEntity<OverviewStats> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverviewStats());
    }

    /**
     * Get monthly statistics for a specific number of months
     */
    @GetMapping("/monthly")
    @Operation(summary = "Get monthly statistics")
    public ResponseEntity<?> getMonthlyStats(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyStats(months));
    }

    /**
     * Get user distribution statistics
     */
    @GetMapping("/users")
    @Operation(summary = "Get user distribution")
    public ResponseEntity<UserDistribution> getUserDistribution() {
        return ResponseEntity.ok(analyticsService.getUserDistribution());
    }

    /**
     * Get AI usage statistics
     */
    @GetMapping("/ai-usage")
    @Operation(summary = "Get AI usage statistics")
    public ResponseEntity<AIUsageStats> getAIUsage() {
        return ResponseEntity.ok(analyticsService.getAIUsageStats());
    }
}
