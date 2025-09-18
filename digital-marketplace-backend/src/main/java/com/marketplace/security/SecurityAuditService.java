package com.marketplace.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SecurityAuditService {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    @Async
    public void logLoginSuccess(UUID userId, String ipAddress, String userAgent) {
        securityLogger.info("LOGIN_SUCCESS: User {} from IP {} using {}", 
                          userId, ipAddress, userAgent);
    }
    
    @Async
    public void logLoginFailure(String email, String ipAddress, String reason) {
        securityLogger.warn("LOGIN_FAILURE: Email {} from IP {} - Reason: {}", 
                          email, ipAddress, reason);
    }
    
    @Async
    public void logPasswordReset(UUID userId, String ipAddress) {
        securityLogger.info("PASSWORD_RESET: User {} from IP {}", userId, ipAddress);
    }
    
    @Async
    public void logPasswordChange(UUID userId, String ipAddress) {
        securityLogger.info("PASSWORD_CHANGE: User {} from IP {}", userId, ipAddress);
    }
    
    @Async
    public void logEmailVerification(UUID userId, String ipAddress) {
        securityLogger.info("EMAIL_VERIFICATION: User {} from IP {}", userId, ipAddress);
    }
    
    @Async
    public void logSuspiciousActivity(UUID userId, String activity, String details) {
        securityLogger.warn("SUSPICIOUS_ACTIVITY: User {} - Activity: {} - Details: {}", 
                          userId, activity, details);
    }
    
    @Async
    public void logTokenRefresh(UUID userId, String ipAddress) {
        securityLogger.info("TOKEN_REFRESH: User {} from IP {}", userId, ipAddress);
    }
    
    @Async
    public void logAccountLockout(String email, String ipAddress, String reason) {
        securityLogger.warn("ACCOUNT_LOCKOUT: Email {} from IP {} - Reason: {}", 
                          email, ipAddress, reason);
    }
}