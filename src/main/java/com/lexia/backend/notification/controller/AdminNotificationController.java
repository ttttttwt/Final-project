package com.lexia.backend.notification.controller;

import com.lexia.backend.notification.dto.BroadcastNotificationRequest;
import com.lexia.backend.notification.dto.SendNotificationRequest;
import com.lexia.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Admin Notification Operations.
 * Allows administrators to broadcast notifications and send to specific users.
 *
 * <p>
 * Base path: /api/v1/admin/notifications
 * </p>
 *
 * <p>
 * Security: All endpoints require ADMIN role.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@Tag(name = "Admin Notification API", description = "Admin endpoints for broadcasting and sending notifications")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminNotificationController.class);

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Broadcast a notification to all active users.
     */
    @PostMapping(value = "/broadcast", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Broadcast notification to all users", description = "Sends a notification to all active users in the system. Use for system announcements and maintenance notices.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Broadcast sent successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "Notification broadcast to 1500 users"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<Map<String, String>> broadcastNotification(
            @Valid @RequestBody BroadcastNotificationRequest request) {

        LOG.info("Admin broadcasting notification: {}", request.getTitle());
        int count = notificationService.broadcastNotification(request);
        LOG.info("Broadcast completed: {} users notified", count);

        return ResponseEntity.ok(Map.of("message", "Notification broadcast to " + count + " users"));
    }

    /**
     * Send a notification to specific users.
     */
    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Send notification to specific users", description = "Sends a notification to a list of specific users by their IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "Notification sent to 5 users"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<Map<String, String>> sendToUsers(
            @Valid @RequestBody SendNotificationRequest request) {

        LOG.info("Admin sending notification to {} users: {}", request.getUserIds().size(), request.getTitle());
        int count = notificationService.sendToUsers(request);
        LOG.info("Send completed: {} users notified", count);

        return ResponseEntity.ok(Map.of("message", "Notification sent to " + count + " users"));
    }
}
