//package com.marketplace.controller;
//
//import com.marketplace.model.dto.request.LoginRequest;
//import com.marketplace.model.dto.request.UserRegistrationRequest;
//import com.marketplace.model.dto.request.PasswordResetRequest;
//import com.marketplace.model.dto.request.RefreshTokenRequest;
//import com.marketplace.model.dto.response.AuthResponse;
//import com.marketplace.model.dto.response.ApiResponse;
//import com.marketplace.model.entity.User;
//import com.marketplace.security.JwtTokenProvider;
//import com.marketplace.security.UserPrincipal;
//import com.marketplace.service.UserService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*", maxAge = 3600)
//public class AuthController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private JwtTokenProvider tokenProvider;
//
//    /**
//     * POST /api/auth/register
//     * Register a new user account
//     */
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
//        User user = userService.createUser(request);
//
//        return ResponseEntity.ok(new AuthResponse(
//            true,
//            "User registered successfully. Please check your email to verify your account.",
//            null,
//            null,
//            user.getId()
//        ));
//    }
//
//    /**
//     * POST /api/auth/login
//     * Authenticate user and return JWT tokens
//     */
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
//        Authentication authentication = authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(
//                request.getEmail(),
//                request.getPassword()
//            )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        String jwt = tokenProvider.generateToken(authentication);
//        String refreshToken = tokenProvider.generateRefreshToken(authentication);
//
//        // Update last login
//        userService.updateLastLogin(userPrincipal.getId());
//
//        return ResponseEntity.ok(new AuthResponse(
//            true,
//            "Login successful",
//            jwt,
//            refreshToken,
//            userPrincipal.getId()
//        ));
//    }
//
//    /**
//     * POST /api/auth/refresh
//     * Refresh access token using refresh token
//     */
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
//        String refreshToken = request.getRefreshToken();
//
//        if (tokenProvider.validateToken(refreshToken)) {
//            String userId = tokenProvider.getUserIdFromToken(refreshToken);
//            User user = userService.getUserById(UUID.fromString(userId));
//
//            // Create authentication object for token generation
//            UserPrincipal userPrincipal = UserPrincipal.create(user);
//            Authentication auth = new UsernamePasswordAuthenticationToken(
//                userPrincipal, null, userPrincipal.getAuthorities());
//
//            String newJwt = tokenProvider.generateToken(auth);
//
//            return ResponseEntity.ok(new AuthResponse(
//                true,
//                "Token refreshed successfully",
//                newJwt,
//                refreshToken,
//                user.getId()
//            ));
//        }
//
//        return ResponseEntity.badRequest()
//            .body(new AuthResponse(false, "Invalid refresh token", null, null, null));
//    }
//
//    /**
//     * POST /api/auth/logout
//     * Logout user (client should delete tokens)
//     */
//    @PostMapping("/logout")
//    public ResponseEntity<ApiResponse> logout() {
//        // In a stateless JWT setup, logout is handled client-side
//        // Could implement token blacklisting if needed
//        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
//    }
//
//    /**
//     * POST /api/auth/forgot-password
//     * Request password reset
//     */
//    @PostMapping("/forgot-password")
//    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
//        // userService.sendPasswordResetEmail(request.getEmail());
//
//        // Always return success for security (don't reveal if email exists)
//        return ResponseEntity.ok(new ApiResponse(
//            true,
//            "If the email exists, a password reset link has been sent"
//        ));
//    }
//
//    /**
//     * POST /api/auth/reset-password
//     * Reset password using token
//     */
//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse> resetPassword(
//            @RequestParam String token,
//            @Valid @RequestBody PasswordResetRequest request) {
//
//        // userService.resetPassword(token, request.getNewPassword());
//
//        return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully"));
//    }
//
//    /**
//     * GET /api/auth/verify-email
//     * Verify email address
//     */
//    @GetMapping("/verify-email")
//    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
//        // userService.verifyEmail(token);
//
//        return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully"));
//    }
//}