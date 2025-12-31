package com.lexia.backend.service;

import com.lexia.backend.dto.admin.AdminPaymentDTO;
import com.lexia.backend.entity.Payment;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import org.springframework.transaction.support.TransactionTemplate;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public Page<AdminPaymentDTO> getPayments(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Payment> spec = Specification.where((Specification<Payment>) null);

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("user").get("email")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("stripePaymentId")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("stripeInvoiceId")), "%" + searchLower + "%")
            ));
        }

        if (status != null && !status.isBlank()) {
            try {
                Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), paymentStatus));
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        return paymentRepository.findAll(spec, pageable).map(AdminPaymentDTO::fromEntity);
    }

    public AdminPaymentDTO refundPayment(UUID id, String reason) {
        // 1. Validate state (Read-only)
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            return AdminPaymentDTO.fromEntity(payment);
        }

        if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only succeeded payments can be refunded. Current status: " + payment.getStatus());
        }

        if (payment.getStripePaymentId() == null) {
            throw new IllegalStateException("Cannot refund payment without Stripe Payment ID");
        }

        // 2. Call Stripe (No transaction)
        Refund refund;
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentId())
                    .setReason(reason != null ? RefundCreateParams.Reason.valueOf(reason.toUpperCase()) : RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("refund-" + id.toString())
                    .build();

            refund = Refund.create(params, options);
            log.info("Refund created in Stripe: {}", refund.getId());

        } catch (StripeException e) {
            log.error("Error creating refund in Stripe", e);
            throw new RuntimeException("Failed to process refund with Stripe: " + e.getMessage());
        } catch (IllegalArgumentException e) {
             log.error("Invalid refund reason", e);
             throw new IllegalArgumentException("Invalid refund reason: " + reason);
        }

        // 3. Update DB (Transaction)
        return transactionTemplate.execute(status -> {
            Payment p = paymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
            
            if ("succeeded".equals(refund.getStatus())) {
                p.setStatus(Payment.PaymentStatus.REFUNDED);
                paymentRepository.save(p);
            }
            return AdminPaymentDTO.fromEntity(p);
        });
    }
}
