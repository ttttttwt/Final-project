package com.lexia.backend.controller;

import com.lexia.backend.dto.admin.AdminPaymentDTO;
import com.lexia.backend.dto.admin.AdminRefundRequestDTO;
import com.lexia.backend.service.AdminPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payment Management", description = "Manage payments and refunds")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @GetMapping
    @Operation(summary = "Get all payments with filtering")
    public ResponseEntity<Page<AdminPaymentDTO>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminPaymentService.getPayments(page, size, search, status));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<AdminPaymentDTO> refundPayment(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminRefundRequestDTO request) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(adminPaymentService.refundPayment(id, reason));
    }
}
