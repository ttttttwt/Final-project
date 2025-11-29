package com.lexia.backend.notification.controller;

import com.lexia.backend.entity.User;
import com.lexia.backend.notification.dto.NotificationDTO;
import com.lexia.backend.notification.dto.NotificationPreferencesDTO;
import com.lexia.backend.notification.dto.UnreadCountDTO;
import com.lexia.backend.notification.service.NotificationService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for User Notifications.
 * Handles notification CRUD operations and preferences for authenticated users.
 *
 * <p>
 * Base path: /api/v1/notifications
 * </p>
 *
 * <p>
 * Security: All endpoints require authentication. Users can only access their
 * own notifications.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification API", description = "Endpoints for managing user notifications and preferences")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get paginated notifications for the authenticated user.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get my notifications", description = "Retrieves paginated notifications for the authenticated user, ordered by creation date (newest first).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Paginated Notifications", value = """
                    {
                      "content": [
                        {
                          "id": "550e8400-e29b-41d4-a716-446655440000",
                          "type": "COURSE_COMPLETED",
                          "title": "Congratulations! 🎉",
                          "message": "You completed Business English Basics",
                          "data": {
                            "courseId": "123e4567-e89b-12d3-a456-426614174000",
                            "courseTitle": "Business English Basics",
                            "completionTime": 1234
                          },
                          "priority": "HIGH",
                          "isRead": false,
                          "createdAt": "2025-11-28T10:30:00Z"
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 20
                      },
                      "totalElements": 42,
                      "totalPages": 3
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(hidden = true) Pageable pageable) {

        LOG.info("Fetching notifications for user {}", user.getEmail());
        Page<NotificationDTO> notifications = notificationService.getNotifications(user.getId(), pageable);
        LOG.info("Found {} notifications for user {}", notifications.getTotalElements(), user.getEmail());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count for the authenticated user.
     */
    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications and high-priority unread notifications.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved unread count", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnreadCountDTO.class), examples = @ExampleObject(name = "Unread Count", value = """
                    {
                      "unreadCount": 5,
                      "highPriorityCount": 2
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UnreadCountDTO> getUnreadCount(@AuthenticationPrincipal User user) {
        LOG.debug("Getting unread count for user {}", user.getEmail());
        UnreadCountDTO count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }

    /**
     * Get a specific notification by ID.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification by its ID. Returns 404 if not found or not owned by user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notification", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationDTO> getNotificationById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Notification ID", required = true) @PathVariable UUID id) {

        LOG.debug("Getting notification {} for user {}", id, user.getEmail());
        NotificationDTO notification = notificationService.getNotificationById(user.getId(), id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark a specific notification as read.
     */
    @PutMapping(value = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read. Idempotent - calling multiple times has the same effect.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "Notification marked as read"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Map<String, String>> markAsRead(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Notification ID", required = true) @PathVariable UUID id) {

        LOG.info("Marking notification {} as read for user {}", id, user.getEmail());
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Mark all notifications as read for the authenticated user.
     */
    @PutMapping(value = "/read-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications as read for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "5 notifications marked as read"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal User user) {
        LOG.info("Marking all notifications as read for user {}", user.getEmail());
        int count = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", count + " notifications marked as read"));
    }

    /**
     * Delete a specific notification.
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete notification", description = "Deletes a specific notification. Returns 404 if not found or not owned by user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Notification ID", required = true) @PathVariable UUID id) {

        LOG.info("Deleting notification {} for user {}", id, user.getEmail());
        notificationService.deleteNotification(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all read notifications for the authenticated user.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete all read notifications", description = "Deletes all notifications that have been marked as read for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Read notifications deleted", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "message": "10 read notifications deleted"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> deleteAllReadNotifications(@AuthenticationPrincipal User user) {
        LOG.info("Deleting all read notifications for user {}", user.getEmail());
        int count = notificationService.deleteAllReadNotifications(user.getId());
        return ResponseEntity.ok(Map.of("message", count + " read notifications deleted"));
    }

    // ==================== Preferences Endpoints ====================

    /**
     * Get notification preferences for the authenticated user.
     */
    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification preferences", description = "Retrieves notification preferences for the authenticated user. Returns default values if not yet customized.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved preferences", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPreferencesDTO.class), examples = @ExampleObject(value = """
                    {
                      "inAppEnabled": true,
                      "emailEnabled": true,
                      "pushEnabled": false,
                      "learningEnabled": true,
                      "achievementsEnabled": true,
                      "remindersEnabled": true,
                      "systemEnabled": true,
                      "quietHoursStart": "22:00",
                      "quietHoursEnd": "07:00",
                      "quietHoursTimezone": "Asia/Ho_Chi_Minh"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<NotificationPreferencesDTO> getPreferences(@AuthenticationPrincipal User user) {
        LOG.debug("Getting notification preferences for user {}", user.getEmail());
        NotificationPreferencesDTO preferences = notificationService.getPreferences(user.getId());
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update notification preferences for the authenticated user.
     */
    @PutMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update notification preferences", description = "Updates notification preferences for the authenticated user. Partial updates are supported.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPreferencesDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<NotificationPreferencesDTO> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody NotificationPreferencesDTO preferences) {

        LOG.info("Updating notification preferences for user {}", user.getEmail());
        NotificationPreferencesDTO updated = notificationService.updatePreferences(user.getId(), preferences);
        return ResponseEntity.ok(updated);
    }
}
