package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.UserProfileUpdateRequest;
import com.marketplace.model.dto.response.UserProfileResponse;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.model.dto.response.UserStatusResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * GET /api/users/me
     * Get current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }
    
    /**
     * PUT /api/users/me
     * Update current user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        
        userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }
    
    /**
     * GET /api/users/{userId}
     * Get public user profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        UserProfileResponse profile = userService.getPublicUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * POST /api/users/upgrade-to-seller
     * Upgrade user account to seller
     */
    @PostMapping("/upgrade-to-seller")
    public ResponseEntity<ApiResponse> upgradeToSeller(@CurrentUser UserPrincipal currentUser) {
        userService.upgradeToSeller(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Successfully upgraded to seller account"));
    }
    
    /**
     * GET /api/users/sellers
     * Get all sellers with pagination
     */
    @GetMapping("/sellers")
    public ResponseEntity<Page<UserProfileResponse>> getAllSellers(Pageable pageable) {
        Page<UserProfileResponse> sellers = userService.getAllSellers(pageable);
        return ResponseEntity.ok(sellers);
    }
    
    /**
     * GET /api/users/{userId}/stats
     * Get user statistics (public stats only)
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatusResponse> getUserStats(@PathVariable UUID userId) {
        UserStatusResponse stats = userService.getUserPublicStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * DELETE /api/users/me
     * Delete user account (soft delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deleteAccount(@CurrentUser UserPrincipal currentUser) {
        userService.deleteAccount(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Account deleted successfully"));
    }
}