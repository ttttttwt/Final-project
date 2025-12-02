package com.lexia.backend.email.controller;

import com.lexia.backend.email.dto.AdminEmailRequest;
import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.dto.EmailStatsDTO;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.mapper.EmailMapper;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for Admin Email Management.
 * Provides endpoints for admins to send emails, view queue, and manage email
 * system.
 *
 * <p>
 * Base path: /api/v1/admin/emails
 * </p>
 *
 * <p>
 * Security: All endpoints require ADMIN role.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@RestController
@RequestMapping("/api/v1/admin/emails")
@Tag(name = "Admin Email API", description = "Admin endpoints for email management and broadcasting")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminEmailController {

        private final EmailService emailService;
        private final EmailQueueRepository emailQueueRepository;
        private final UserRepository userRepository;

        /**
         * Send email to specific users.
         *
         * @param request the email request with user IDs
         * @return result of the operation
         */
        @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Send email to users", description = "Sends an email to specified users. Use for targeted communications or system announcements.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email(s) queued successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "message": "Email queued for 5 users",
                                          "queuedCount": 5,
                                          "emailIds": ["uuid1", "uuid2"]
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody AdminEmailRequest request) {
                log.info("Admin sending email to {} users, type: {}",
                                request.getUserIds() != null ? request.getUserIds().size() : 0,
                                request.getEmailType());

                if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "User IDs are required"));
                }

                List<UUID> emailIds = new ArrayList<>();
                int queuedCount = 0;

                for (UUID userId : request.getUserIds()) {
                        Optional<User> userOpt = userRepository.findById(userId);
                        if (userOpt.isEmpty()) {
                                log.warn("User not found: {}", userId);
                                continue;
                        }

                        User user = userOpt.get();
                        EmailRequest emailRequest = EmailRequest.builder()
                                        .recipientId(user.getId())
                                        .recipientEmail(user.getEmail())
                                        .emailType(request.getEmailType())
                                        .subject(request.getSubject())
                                        .priority(request.getPriority())
                                        .templateData(request.getTemplateData())
                                        .locale(request.getLocale())
                                        .build();

                        UUID emailId = emailService.queueEmail(emailRequest);
                        emailIds.add(emailId);
                        queuedCount++;
                }

                log.info("Admin queued {} emails", queuedCount);

                return ResponseEntity.ok(Map.of(
                                "message", "Email queued for " + queuedCount + " users",
                                "queuedCount", queuedCount,
                                "emailIds", emailIds));
        }

        /**
         * Broadcast email to all active users.
         *
         * @param request the broadcast request
         * @return result of the operation
         */
        @PostMapping(value = "/broadcast", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Broadcast email to all users", description = "Sends an email to all active users. Use for system announcements or maintenance notices.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Broadcast queued successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "message": "Broadcast queued for 1500 users",
                                          "queuedCount": 1500
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<Map<String, Object>> broadcastEmail(@Valid @RequestBody AdminEmailRequest request) {
                log.info("Admin broadcasting email, type: {}", request.getEmailType());

                // Get all active users
                List<User> activeUsers = userRepository.findAll().stream()
                                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                                .toList();

                List<EmailRequest> emailRequests = activeUsers.stream()
                                .map(user -> EmailRequest.builder()
                                                .recipientId(user.getId())
                                                .recipientEmail(user.getEmail())
                                                .emailType(request.getEmailType())
                                                .subject(request.getSubject())
                                                .priority(request.getPriority())
                                                .templateData(request.getTemplateData())
                                                .locale(request.getLocale())
                                                .build())
                                .collect(Collectors.toList());

                List<UUID> emailIds = emailService.queueBulkEmails(emailRequests);

                log.info("Admin broadcast queued for {} users", emailIds.size());

                return ResponseEntity.ok(Map.of(
                                "message", "Broadcast queued for " + emailIds.size() + " users",
                                "queuedCount", emailIds.size()));
        }

        /**
         * Get email queue with pagination.
         *
         * @param status   optional status filter
         * @param pageable pagination parameters
         * @return paginated email queue
         */
        @GetMapping(value = "/queue", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get email queue", description = "Retrieves the email queue with optional status filtering.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved queue"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<Page<EmailQueueDTO>> getEmailQueue(
                        @Parameter(description = "Filter by status") @RequestParam(required = false) EmailStatus status,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                log.debug("Admin fetching email queue, status: {}", status);

                Page<EmailQueue> emails;
                if (status != null) {
                        emails = emailQueueRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
                } else {
                        emails = emailQueueRepository.findAll(pageable);
                }

                Page<EmailQueueDTO> dtos = emails.map(EmailMapper::toDTO);
                return ResponseEntity.ok(dtos);
        }

        /**
         * Get email queue statistics.
         *
         * @return queue statistics
         */
        @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get email statistics", description = "Retrieves email queue statistics and metrics.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailStatsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<EmailStatsDTO> getEmailStats(
                        @Parameter(description = "Statistics period") @RequestParam(defaultValue = "LAST_7_DAYS") EmailStatsDTO.StatsPeriod period) {

                log.debug("Admin fetching email stats for period: {}", period);

                EmailStatsDTO stats = buildEmailStats(period);
                return ResponseEntity.ok(stats);
        }

        /**
         * Retry a failed email.
         *
         * @param emailId the email ID to retry
         * @return result of the operation
         */
        @PostMapping(value = "/{emailId}/retry", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Retry failed email", description = "Retries sending a failed email.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Retry scheduled successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "message": "Email retry scheduled",
                                          "emailId": "550e8400-e29b-41d4-a716-446655440000"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Email not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<Map<String, Object>> retryEmail(@PathVariable UUID emailId) {
                log.info("Admin retrying email: {}", emailId);

                boolean success = emailService.retryEmail(emailId);
                if (success) {
                        return ResponseEntity.ok(Map.of(
                                        "message", "Email retry scheduled",
                                        "emailId", emailId));
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        /**
         * Cancel a pending email.
         *
         * @param emailId the email ID to cancel
         * @return result of the operation
         */
        @PostMapping(value = "/{emailId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Cancel pending email", description = "Cancels a pending email in the queue.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email cancelled successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "message": "Email cancelled",
                                          "emailId": "550e8400-e29b-41d4-a716-446655440000"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Email not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<Map<String, Object>> cancelEmail(@PathVariable UUID emailId) {
                log.info("Admin cancelling email: {}", emailId);

                boolean success = emailService.cancelEmail(emailId);
                if (success) {
                        return ResponseEntity.ok(Map.of(
                                        "message", "Email cancelled",
                                        "emailId", emailId));
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        /**
         * Get details of a specific email.
         *
         * @param emailId the email ID
         * @return email details
         */
        @GetMapping(value = "/{emailId}", produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get email details", description = "Retrieves details of a specific email in the queue.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved email details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailQueueDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Email not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
        })
        public ResponseEntity<EmailQueueDTO> getEmailDetails(@PathVariable UUID emailId) {
                log.debug("Admin fetching email details: {}", emailId);

                return emailService.getEmailStatus(emailId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Build email statistics DTO.
         */
        @SuppressWarnings("unused") // Some stats variables reserved for future use
        private EmailStatsDTO buildEmailStats(EmailStatsDTO.StatsPeriod period) {
                // Get queue statistics
                Object[] stats = emailQueueRepository.getQueueStatistics();
                // pending and processing reserved for future queue monitoring dashboard
                long pending = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
                long processing = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
                long sent = stats[2] != null ? ((Number) stats[2]).longValue() : 0;
                long delivered = stats[3] != null ? ((Number) stats[3]).longValue() : 0;
                long failed = stats[4] != null ? ((Number) stats[4]).longValue() : 0;
                long bounced = stats[5] != null ? ((Number) stats[5]).longValue() : 0;

                // Log queue status for monitoring
                log.debug("Email queue status - pending: {}, processing: {}", pending, processing);

                // Calculate totals
                long totalSent = sent + delivered;

                // Build summary
                EmailStatsDTO.EmailStatsSummary summary = EmailStatsDTO.EmailStatsSummary.builder()
                                .totalSent(totalSent)
                                .delivered(delivered)
                                .opened(0) // Would need tracking implementation
                                .clicked(0) // Would need tracking implementation
                                .bounced(bounced)
                                .complained(0) // Would need feedback implementation
                                .failed(failed)
                                .build();

                // Calculate rates
                double deliveryRate = totalSent > 0 ? (double) delivered / totalSent * 100 : 0;
                double bounceRate = totalSent > 0 ? (double) bounced / totalSent * 100 : 0;

                EmailStatsDTO.EmailRates rates = EmailStatsDTO.EmailRates.builder()
                                .deliveryRate(Math.round(deliveryRate * 100.0) / 100.0)
                                .openRate(0) // Would need tracking
                                .clickRate(0) // Would need tracking
                                .bounceRate(Math.round(bounceRate * 100.0) / 100.0)
                                .complaintRate(0) // Would need feedback
                                .build();

                // Build by-type stats (simplified)
                List<EmailStatsDTO.EmailTypeStats> byType = Arrays.stream(EmailType.values())
                                .map(type -> EmailStatsDTO.EmailTypeStats.builder()
                                                .type(type.getDisplayName())
                                                .sent(0) // Would need per-type counting
                                                .opened(0)
                                                .openRate(0)
                                                .clicked(0)
                                                .clickRate(0)
                                                .build())
                                .limit(5) // Top 5 types
                                .collect(Collectors.toList());

                return EmailStatsDTO.builder()
                                .period(period.getDisplayName())
                                .summary(summary)
                                .rates(rates)
                                .byType(byType)
                                .trend(null) // Would need daily aggregation
                                .build();
        }
}
