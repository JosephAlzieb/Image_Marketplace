package com.marketplace.service;

import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.model.dto.request.AdminUserUpdateRequest;
import com.marketplace.model.entity.User;
import com.marketplace.repository.UserRepository;
import com.marketplace.repository.ImageRepository;
import com.marketplace.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuditService auditService;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // Get basic statistics
        long totalUsers = userRepository.count();
        long totalImages = imageRepository.count();
        long totalTransactions = transactionRepository.count();

        // Recent activity (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long newImagesThisMonth = imageRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long newTransactionsThisMonth = transactionRepository.countByCreatedAtAfter(thirtyDaysAgo);

        dashboardData.put("totalUsers", totalUsers);
        dashboardData.put("totalImages", totalImages);
        dashboardData.put("totalTransactions", totalTransactions);
        dashboardData.put("newUsersThisMonth", newUsersThisMonth);
        dashboardData.put("newImagesThisMonth", newImagesThisMonth);
        dashboardData.put("newTransactionsThisMonth", newTransactionsThisMonth);

        return dashboardData;
    }

    public Page<Object> getAllUsers(String status, String role, String search, Pageable pageable) {
        // Simplified implementation - in practice you'd use specifications or custom queries
        List<User> allUsers = userRepository.findAll();

        // Filter by status, role, search if provided
        List<Object> filteredUsers = allUsers.stream()
                .filter(user -> status == null || user.getStatus().name().equalsIgnoreCase(status))
                .filter(user -> role == null || user.getRole().name().equalsIgnoreCase(role))
                .filter(user -> search == null ||
                        user.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                        user.getLastName().toLowerCase().contains(search.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(search.toLowerCase()))
                .map(user -> (Object) user)
                .toList();

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
        List<Object> pageContent = filteredUsers.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredUsers.size());
    }

    public Object getUserDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Return user with additional admin details
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("user", user);
        userDetails.put("imageCount", imageRepository.countByUploaderId(userId));
        userDetails.put("purchaseCount", transactionRepository.countByBuyerId(userId));
        userDetails.put("salesCount", transactionRepository.countBySellerId(userId));

        return userDetails;
    }

    @Transactional
    public void updateUser(UUID userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getProfileCompleted() != null) {
            user.setProfileCompleted(request.getProfileCompleted());
        }

        userRepository.save(user);

        // Log the admin action
        auditService.logAction("ADMIN_USER_UPDATE", userId,
                "User updated by admin: " + userId);
    }

    public Page<Object> getAllImages(String status, UUID uploaderId, String search, Pageable pageable) {
        // Simplified implementation - would use specifications in practice
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    @Transactional
    public void deleteImage(UUID imageId, UUID adminId, String reason) {
        imageService.deleteImage(imageId, adminId);
        auditService.logAction("ADMIN_IMAGE_DELETE", adminId,
                "Image " + imageId + " deleted. Reason: " + reason);
    }

    public Page<Object> getAllTransactions(String status, UUID userId,
                                          LocalDateTime fromDate, LocalDateTime toDate,
                                          Pageable pageable) {
        // Simplified implementation
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    @Transactional
    public void processAdminRefund(UUID transactionId, UUID adminId, String reason, BigDecimal amount) {
        // Process refund logic here
        auditService.logAction("ADMIN_REFUND", adminId,
                "Refund processed for transaction " + transactionId + ". Reason: " + reason);
    }

    public Page<Object> getAllReports(String status, String type, Pageable pageable) {
        // Simplified implementation
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    @Transactional
    public void updateReportStatus(UUID reportId, UUID adminId, String status, String resolution) {
        // Update report status logic here
        auditService.logAction("ADMIN_REPORT_UPDATE", adminId,
                "Report " + reportId + " status updated to " + status);
    }

    public Map<String, Object> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maintenanceMode", false);
        settings.put("registrationEnabled", true);
        settings.put("maxFileSize", "10MB");
        settings.put("supportedFormats", Arrays.asList("jpg", "png", "gif"));
        return settings;
    }

    @Transactional
    public void updateSystemSettings(UUID adminId, Map<String, Object> settings) {
        // Update system settings logic here
        auditService.logAction("ADMIN_SETTINGS_UPDATE", adminId,
                "System settings updated");
    }

    public Page<Object> getAuditLogs(String action, UUID userId,
                                    LocalDateTime fromDate, LocalDateTime toDate,
                                    Pageable pageable) {
        // This would typically query an audit log table
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }
}
