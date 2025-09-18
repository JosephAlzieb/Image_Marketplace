package com.marketplace.security;

import com.marketplace.exception.BadRequestException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.model.dto.request.ChangePasswordRequest;
import com.marketplace.model.dto.request.LoginRequest;
import com.marketplace.model.dto.request.UserRegistrationRequest;
import com.marketplace.model.dto.response.AuthResponse;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.UserStatus;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.EmailService;
import com.marketplace.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Authenticate user and generate tokens
     */
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getEmail());
        
        // Check if user exists and is active
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        
        // Check user status
        validateUserStatus(user);
        
        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate tokens
            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            
            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            logger.info("User successfully authenticated: {}", user.getId());
            
            return new AuthResponse(
                true,
                "Login successful",
                jwt,
                refreshToken,
                user.getId()
            );
            
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user {}: Invalid credentials", loginRequest.getEmail());
            throw new BadRequestException("Invalid email or password");
        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", loginRequest.getEmail(), e.getMessage());
            throw new BadRequestException("Authentication failed");
        }
    }
    
    /**
     * Register new user
     */
    public AuthResponse registerUser(UserRegistrationRequest registrationRequest) {
        logger.info("Registering new user: {}", registrationRequest.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email address already in use!");
        }
        
        // Validate password strength
        validatePasswordStrength(registrationRequest.getPassword());
        
        // Create user
        User user = userService.createUser(registrationRequest);
        
        // Generate email verification token
        String verificationToken = tokenProvider.generateEmailVerificationToken(user.getId().toString());
        user.setEmailVerificationToken(verificationToken);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        userRepository.save(user);
        
        // Send verification email
        emailService.sendEmailVerificationEmail(user, verificationToken);
        
        logger.info("User successfully registered: {}", user.getId());
        
        return new AuthResponse(
            true,
            "Registration successful. Please check your email to verify your account.",
            null,
            null,
            user.getId()
        );
    }
    
    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Refreshing token");
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }
        
        if (!tokenProvider.validateTokenType(refreshToken, "refresh")) {
            throw new BadRequestException("Invalid token type");
        }
        
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }
        
        // Generate new access token
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
        
        String newAccessToken = tokenProvider.generateToken(auth);
        
        logger.info("Token refreshed for user: {}", userId);
        
        return new AuthResponse(
            true,
            "Token refreshed successfully",
            newAccessToken,
            refreshToken,
            user.getId()
        );
    }
    
    /**
     * Verify email address
     */
    public void verifyEmail(String token) {
        logger.info("Verifying email with token");
        
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired verification token");
        }
        
        if (!tokenProvider.validateTokenType(token, "email_verification")) {
            throw new BadRequestException("Invalid token type");
        }
        
        String userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        
        // Check if token matches stored token
        if (!token.equals(user.getEmailVerificationToken())) {
            throw new BadRequestException("Invalid verification token");
        }
        
        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        logger.info("Email verified for user: {}", userId);
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email) {
        logger.info("Sending password reset email to: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        // Don't reveal if email exists for security
        if (user != null && (user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING_VERIFICATION)) {
            String resetToken = tokenProvider.generatePasswordResetToken(user.getId().toString());
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            
            emailService.sendPasswordResetEmail(user, resetToken);
        }
        
        logger.info("Password reset email process completed for: {}", email);
    }
    
    /**
     * Reset password using token
     */
    public void resetPassword(String token, String newPassword) {
        logger.info("Resetting password with token");
        
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        
        if (!tokenProvider.validateTokenType(token, "password_reset")) {
            throw new BadRequestException("Invalid token type");
        }
        
        String userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if token matches stored token
        if (!token.equals(user.getPasswordResetToken())) {
            throw new BadRequestException("Invalid reset token");
        }
        
        // Check if token is expired
        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        
        // Validate new password
        validatePasswordStrength(newPassword);
        
        // Check if new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
        
        // Send confirmation email
        emailService.sendPasswordChangeConfirmationEmail(user);
        
        logger.info("Password reset completed for user: {}", userId);
    }
    
    /**
     * Change password (authenticated user)
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        logger.info("Changing password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Validate new password
        validatePasswordStrength(request.getNewPassword());
        
        // Check if new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Send confirmation email
        emailService.sendPasswordChangeConfirmationEmail(user);
        
        logger.info("Password changed successfully for user: {}", userId);
    }
    
    /**
     * Resend email verification
     */
    public void resendEmailVerification(String email) {
        logger.info("Resending email verification to: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("User is not in pending verification status");
        }
        
        // Generate new verification token
        String verificationToken = tokenProvider.generateEmailVerificationToken(user.getId().toString());
        user.setEmailVerificationToken(verificationToken);
        userRepository.save(user);
        
        // Send verification email
        emailService.sendEmailVerificationEmail(user, verificationToken);
        
        logger.info("Email verification resent to: {}", email);
    }
    
    /**
     * Logout user (invalidate tokens - if using token blacklist)
     */
    public void logoutUser(String accessToken, String refreshToken) {
        logger.info("Logging out user");
        
        // In a stateless JWT implementation, logout is typically handled client-side
        // If token blacklisting is needed, implement here
        
        try {
            // Optional: Add tokens to blacklist
            // tokenBlacklistService.blacklistToken(accessToken);
            // tokenBlacklistService.blacklistToken(refreshToken);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            logger.info("User logged out successfully");
            
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage());
            // Don't throw exception for logout errors
        }
    }
    
    /**
     * Get current authenticated user
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found");
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Check if current user has specific permission
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }
    
    // Private helper methods
    
    /**
     * Validate user status before authentication
     */
    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case SUSPENDED:
                throw new BadRequestException("Account is suspended. Please contact support.");
            case DELETED:
                throw new BadRequestException("Account not found.");
            case PENDING_VERIFICATION:
                throw new BadRequestException("Please verify your email before logging in.");
            case ACTIVE:
                break; // Valid status
            default:
                throw new BadRequestException("Account status is invalid.");
        }
    }
    
    /**
     * Validate password strength
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            throw new BadRequestException("Password must be less than 128 characters long");
        }
        
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }
        
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }
        
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }
        
        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new BadRequestException("Password must contain at least one special character");
        }
        
        // Check for common weak passwords
        String[] weakPasswords = {
            "password", "123456", "123456789", "12345678", "12345",
            "qwerty", "abc123", "password123", "admin", "letmein"
        };
        
        for (String weak : weakPasswords) {
            if (password.toLowerCase().contains(weak.toLowerCase())) {
                throw new BadRequestException("Password is too weak. Please choose a stronger password.");
            }
        }
    }
    
    /**
     * Check if email domain is allowed
     */
    private boolean isEmailDomainAllowed(String email) {
        // Implement email domain whitelist/blacklist if needed
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        
        // Block temporary email domains
        String[] blockedDomains = {
            "10minutemail.com", "guerrillamail.com", "mailinator.com",
            "tempmail.org", "throwaway.email"
        };
        
        for (String blocked : blockedDomains) {
            if (domain.equals(blocked)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Log security event
     */
    private void logSecurityEvent(String event, String userId, String details) {
        logger.info("SECURITY_EVENT: {} - User: {} - Details: {}", event, userId, details);
        // Could also send to security monitoring system
    }
}
