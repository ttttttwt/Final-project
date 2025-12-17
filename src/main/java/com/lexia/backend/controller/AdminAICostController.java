package com.lexia.backend.controller;

import com.lexia.backend.service.ai.AICostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ai-costs")
@RequiredArgsConstructor
@Tag(name = "Admin AI Costs", description = "AI Cost Analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAICostController {

    private final AICostService costService;

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getCostAnalytics(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(costService.getCostAnalytics(period));
    }

    @GetMapping("/projection")
    public ResponseEntity<Map<String, Object>> getProjection() {
        return ResponseEntity.ok(costService.getProjection());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(@RequestParam(defaultValue = "monthly") String period) {
        byte[] report = costService.exportReport(period);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cost-report.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(report);
    }

    @GetMapping("/by-user")
    public ResponseEntity<java.util.List<Map<String, Object>>> getCostsByUser(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(costService.getCostsByUser(period, limit));
    }

    @PutMapping("/budget")
    public ResponseEntity<Map<String, Object>> updateBudget(@RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(costService.updateBudget(body.get("budget")));
    }
}
