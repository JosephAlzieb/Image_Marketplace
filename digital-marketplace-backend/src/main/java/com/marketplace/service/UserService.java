package com.marketplace.service;

import com.marketplace.exception.BadRequestException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedException;
import com.marketplace.model.dto.response.UserProfileResponse;
import com.marketplace.model.dto.request.UserProfileUpdateRequest;
import com.marketplace.model.dto.request.UserRegistrationRequest;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.UserRole;
import com.marketplace.model.enums.UserStatus;
import com.marketplace.repository.UserRepository;
import com.marketplace.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Register a new user
     */
    public User createUser(UserRegistrationRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use: " + request.getEmail());
        }
        
        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setRole(UserRole.BUYER); // Default role
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        
        // Set default commission rate
        user.setCommissionRate(new BigDecimal("0.1000")); // 10%
        
        User savedUser = userRepository.save(user);
        
        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(savedUser);
        
        logger.info("Successfully created user with ID: {}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    /**
     * Get user profile information
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = getUserById(userId);
        return mapToProfileResponse(user);
    }
    
    /**
     * Update user profile
     */
    public User updateUserProfile(UUID userId, UserProfileUpdateRequest request) {
        logger.info("Updating profile for user ID: {}", userId);
        
        User user = getUserById(userId);
        
        // Update allowed fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
        }
        
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        
        if (request.getCountryCode() != null) {
            user.setCountryCode(request.getCountryCode().toUpperCase());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        
        if (request.getVatNumber() != null) {
            user.setVatNumber(request.getVatNumber().trim());
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated profile for user ID: {}", userId);
        
        return updatedUser;
    }
    
    /**
     * Upgrade user to seller role
     */
    public void upgradeToSeller(UUID userId) {
        logger.info("Upgrading user {} to seller role", userId);
        
        User user = getUserById(userId);
        
        if (user.getRole() == UserRole.SELLER || user.getRole() == UserRole.PREMIUM_SELLER) {
            throw new BadRequestException("User is already a seller");
        }
        
        user.setRole(UserRole.SELLER);
        userRepository.save(user);
        
        // Send seller welcome email
        emailService.sendSellerWelcomeEmail(user);
        
        logger.info("Successfully upgraded user {} to seller", userId);
    }
    
    /**
     * Update user's last login time
     */
    public void updateLastLogin(UUID userId) {
        User user = getUserById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Add sale to user's statistics
     */
    public void recordSale(UUID sellerId, BigDecimal grossAmount, BigDecimal commission) {
        logger.info("Recording sale for seller {}: gross={}, commission={}", sellerId, grossAmount, commission);
        
        User seller = getUserById(sellerId);
        seller.addSale(grossAmount, commission);
        userRepository.save(seller);
    }
    
    /**
     * Get all sellers with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllSellers(Pageable pageable) {
        return userRepository.findByRoleIn(
            java.util.Arrays.asList(UserRole.SELLER, UserRole.PREMIUM_SELLER), 
            pageable
        );
    }
    
    /**
     * Update seller rating
     */
    public void updateSellerRating(UUID sellerId, BigDecimal newRating) {
        User seller = getUserById(sellerId);
        
        if (!seller.isSeller()) {
            throw new BadRequestException("User is not a seller");
        }
        
        // Simple average for now - could be more sophisticated
        if (seller.getSellerRating() == null) {
            seller.setSellerRating(newRating);
        } else {
            BigDecimal currentRating = seller.getSellerRating();
            BigDecimal averageRating = currentRating.add(newRating).divide(new BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP);
            seller.setSellerRating(averageRating);
        }
        
        userRepository.save(seller);
    }
    
    /**
     * Suspend user account
     */
    public void suspendUser(UUID userId, String reason) {
        logger.warn("Suspending user {} for reason: {}", userId, reason);
        
        User user = getUserById(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        
        // Send suspension email
        emailService.sendAccountSuspensionEmail(user, reason);
    }
    
    /**
     * Reactivate suspended user
     */
    public void reactivateUser(UUID userId) {
        logger.info("Reactivating user {}", userId);
        
        User user = getUserById(userId);
        
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new BadRequestException("User is not suspended");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        // Send reactivation email
        emailService.sendAccountReactivationEmail(user);
    }
    
    /**
     * Verify user email
     */
    public void verifyEmail(UUID userId) {
        User user = getUserById(userId);
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        
        logger.info("Email verified for user {}", userId);
    }
    
    /**
     * Check if user can perform action based on role and status
     */
    public void validateUserAction(UUID userId, UserRole requiredRole) {
        User user = getUserById(userId);
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }
        
        if (requiredRole != null && !hasRequiredRole(user.getRole(), requiredRole)) {
            throw new UnauthorizedException("User does not have required permissions");
        }
    }
    
    private boolean hasRequiredRole(UserRole userRole, UserRole requiredRole) {
        if (userRole == UserRole.ADMIN) return true; // Admin can do everything
        
        return switch (requiredRole) {
            case BUYER -> userRole == UserRole.BUYER || userRole == UserRole.SELLER || userRole == UserRole.PREMIUM_SELLER;
            case SELLER -> userRole == UserRole.SELLER || userRole == UserRole.PREMIUM_SELLER;
            case PREMIUM_SELLER -> userRole == UserRole.PREMIUM_SELLER;
            case ADMIN -> userRole == UserRole.ADMIN;
        };
    }
    
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId().toString());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setCountryCode(user.getCountryCode());
        response.setEmailVerified(user.getEmailVerified());
        response.setSellerRating(user.getSellerRating());
        response.setTotalSales(user.getTotalSales());
        response.setTotalEarnings(user.getTotalEarnings());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        return response;
    }
}