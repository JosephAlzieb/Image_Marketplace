package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.AdminUserUpdateRequest;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageService imageService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private AdminService adminService;
    
    /**
     * GET /api/admin/dashboard
     * Get admin dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @CurrentUser UserPrincipal currentUser) {
        
        Map<String, Object> dashboardData = adminService.getDashboardData();
        // Add current user context for audit logging
        dashboardData.put("requestedBy", currentUser.getId());
        dashboardData.put("requestedAt", LocalDateTime.now());
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * GET /api/admin/analytics
     * Get platform analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate) {
        
        // Default to last 30 days if not specified
        if (fromDate == null) fromDate = LocalDateTime.now().minusDays(30);
        if (toDate == null) toDate = LocalDateTime.now();
        
        Map<String, Object> analytics = transactionService.getPlatformAnalytics(
            currentUser.getId(), fromDate, toDate);
        return ResponseEntity.ok(analytics);
    }
    
    // =============================================================================
    // USER MANAGEMENT
    // =============================================================================
    
    /**
     * GET /api/admin/users
     * Get all users with pagination and filters
     */
    @GetMapping("/users")
    public ResponseEntity<Page<Object>> getAllUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        Page<Object> users = adminService.getAllUsers(status, role, search, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/admin/users/{userId}
     * Get detailed user information
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUserDetails(@PathVariable UUID userId) {
        Object userDetails = adminService.getUserDetails(userId);
        return ResponseEntity.ok(userDetails);
    }
    
    /**
     * PUT /api/admin/users/{userId}
     * Update user details
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        
        adminService.updateUser(userId, request);
        return ResponseEntity.ok(new ApiResponse(true, "User updated successfully"));
    }
    
    /**
     * POST /api/admin/users/{userId}/suspend
     * Suspend user account
     */
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<ApiResponse> suspendUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {
        
        userService.suspendUser(userId, reason);
        return ResponseEntity.ok(new ApiResponse(true, "User suspended successfully"));
    }
    
    /**
     * POST /api/admin/users/{userId}/reactivate
     * Reactivate suspended user account
     */
    @PostMapping("/users/{userId}/reactivate")
    public ResponseEntity<ApiResponse> reactivateUser(@PathVariable UUID userId) {
        userService.reactivateUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User reactivated successfully"));
    }
    
    // =============================================================================
    // IMAGE MANAGEMENT
    // =============================================================================
    
    /**
     * GET /api/admin/images
     * Get all images with filters
     */
    @GetMapping("/images")
    public ResponseEntity<Page<Object>> getAllImages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID uploaderId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        Page<Object> images = adminService.getAllImages(status, uploaderId, search, pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * POST /api/admin/images/{imageId}/feature
     * Feature/unfeature an image
     */
    @PostMapping("/images/{imageId}/feature")
    public ResponseEntity<ApiResponse> toggleImageFeatured(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId,
            @RequestParam boolean featured) {
        
        imageService.toggleImageFeatured(imageId, currentUser.getId(), featured);
        String message = featured ? "Image featured successfully" : "Image unfeatured successfully";
        return ResponseEntity.ok(new ApiResponse(true, message));
    }
    
    /**
     * DELETE /api/admin/images/{imageId}
     * Delete image (admin override)
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId,
            @RequestParam String reason) {
        
        adminService.deleteImage(imageId, currentUser.getId(), reason);
        return ResponseEntity.ok(new ApiResponse(true, "Image deleted successfully"));
    }
    
    // =============================================================================
    // TRANSACTION MANAGEMENT
    // =============================================================================
    
    /**
     * GET /api/admin/transactions
     * Get all transactions with filters
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<Object>> getAllTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate,
            Pageable pageable) {
        
        Page<Object> transactions = adminService.getAllTransactions(
            status, userId, fromDate, toDate, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * POST /api/admin/transactions/{transactionId}/refund
     * Process admin refund
     */
    @PostMapping("/transactions/{transactionId}/refund")
    public ResponseEntity<ApiResponse> adminRefund(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID transactionId,
            @RequestParam String reason,
            @RequestParam(required = false) java.math.BigDecimal amount) {
        
        adminService.processAdminRefund(transactionId, currentUser.getId(), reason, amount);
        return ResponseEntity.ok(new ApiResponse(true, "Refund processed successfully"));
    }
    
    // =============================================================================
    // REPORTS AND DISPUTES
    // =============================================================================
    
    /**
     * GET /api/admin/reports
     * Get all content reports
     */
    @GetMapping("/reports")
    public ResponseEntity<Page<Object>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        
        Page<Object> reports = adminService.getAllReports(status, type, pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * PUT /api/admin/reports/{reportId}
     * Update report status
     */
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse> updateReportStatus(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID reportId,
            @RequestParam String status,
            @RequestParam(required = false) String resolution) {
        
        adminService.updateReportStatus(reportId, currentUser.getId(), status, resolution);
        return ResponseEntity.ok(new ApiResponse(true, "Report status updated"));
    }
    
    // =============================================================================
    // SYSTEM SETTINGS
    // =============================================================================
    
    /**
     * GET /api/admin/settings
     * Get system settings
     */
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = adminService.getSystemSettings();
        return ResponseEntity.ok(settings);
    }
    
    /**
     * PUT /api/admin/settings
     * Update system settings
     */
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse> updateSystemSettings(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody Map<String, Object> settings) {
        
        adminService.updateSystemSettings(currentUser.getId(), settings);
        return ResponseEntity.ok(new ApiResponse(true, "Settings updated successfully"));
    }
    
    // =============================================================================
    // AUDIT LOGS
    // =============================================================================
    
    /**
     * GET /api/admin/audit-logs
     * Get audit logs
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<Object>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate,
            Pageable pageable) {
        
        Page<Object> auditLogs = adminService.getAuditLogs(action, userId, fromDate, toDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}