package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.entity.Notification;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * GET /api/notifications
     * Get user's notifications with pagination
     */
    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<Notification> notifications = notificationService.getUserNotifications(
            currentUser.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * GET /api/notifications/unread-count
     * Get count of unread notifications
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Object> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        Long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
    
    /**
     * PUT /api/notifications/{notificationId}/read
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID notificationId) {
        
        notificationService.markAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Notification marked as read"));
    }
    
    /**
     * PUT /api/notifications/mark-all-read
     * Mark all notifications as read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "All notifications marked as read"));
    }
    
    // Helper class
    public static class UnreadCountResponse {
        private Long count;
        
        public UnreadCountResponse(Long count) {
            this.count = count;
        }
        
        public Long getCount() { return count; }
    }
}