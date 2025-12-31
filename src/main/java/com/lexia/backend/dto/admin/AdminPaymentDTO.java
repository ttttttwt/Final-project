package com.lexia.backend.dto.admin;

import com.lexia.backend.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminPaymentDTO {
    private UUID id;
    private String userEmail;
    private String userName;
    private String stripePaymentId;
    private String stripeInvoiceId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentType;
    private String description;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static AdminPaymentDTO fromEntity(Payment payment) {
        String userName = "N/A";
        if (payment.getUser().getProfile() != null) {
            userName = payment.getUser().getProfile().getFullName();
            if (userName == null && payment.getUser().getProfile().getFirstName() != null) {
                userName = payment.getUser().getProfile().getFirstName() + " " + (payment.getUser().getProfile().getLastName() != null ? payment.getUser().getProfile().getLastName() : "");
            }
        }
        
        return AdminPaymentDTO.builder()
                .id(payment.getId())
                .userEmail(payment.getUser().getEmail())
                .userName(userName)
                .stripePaymentId(payment.getStripePaymentId())
                .stripeInvoiceId(payment.getStripeInvoiceId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null)
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
