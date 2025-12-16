package com.lexia.backend.controller;

import com.lexia.backend.dto.ai.AIUsageOverview;
import com.lexia.backend.service.ai.AIOverviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai-overview")
@RequiredArgsConstructor
@Tag(name = "Admin AI Overview", description = "AI Dashboard Overview")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIOverviewController {

    private final AIOverviewService overviewService;

    @GetMapping
    public ResponseEntity<AIUsageOverview> getOverview() {
        return ResponseEntity.ok(overviewService.getOverview());
    }
}
