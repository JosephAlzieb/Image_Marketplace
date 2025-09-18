package com.marketplace.controller;

import com.marketplace.model.dto.request.LoginRequest;
import com.marketplace.model.dto.request.UserRegistrationRequest;
import com.marketplace.model.dto.request.RefreshTokenRequest;
import com.marketplace.model.dto.request.PasswordResetRequest;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.model.dto.response.AuthResponse;
import com.marketplace.security.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * POST /api/auth/register
     * Register a new user account
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = authenticationService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authenticationService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(false, "Invalid refresh token", null, null, null));
        }
    }

    /**
     * POST /api/auth/logout
     * Logout user (client should delete tokens)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }

    /**
     * POST /api/auth/forgot-password
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authenticationService.sendPasswordResetEmail(request.getEmail());

        // Always return success for security (don't reveal if email exists)
        return ResponseEntity.ok(new ApiResponse(
            true,
            "If the email exists, a password reset link has been sent"
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Reset password using token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordResetRequest request) {

        authenticationService.resetPassword(token, request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully"));
    }

    /**
     * GET /api/auth/verify-email
     * Verify email address
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully"));
    }
}