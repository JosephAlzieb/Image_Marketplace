/*
package com.marketplace.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    */
/**
     * Check if IP is rate limited for login attempts
     *//*

    public boolean isLoginRateLimited(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "login:" + ipAddress;
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts != null) {
            return Integer.parseInt(attempts) >= MAX_LOGIN_ATTEMPTS;
        }

        return false;
    }

    */
/**
     * Record failed login attempt
     *//*

    public void recordFailedLoginAttempt(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "login:" + ipAddress;

        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCKOUT_DURATION);
    }

    */
/**
     * Clear failed login attempts on successful login
     *//*

    public void clearFailedLoginAttempts(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "login:" + ipAddress;
        redisTemplate.delete(key);
    }

    */
/**
     * Check if IP is rate limited for password reset
     *//*

    public boolean isPasswordResetRateLimited(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "password_reset:" + ipAddress;
        String attempts = redisTemplate.opsForValue().get(key);

        // Max 3 password reset attempts per hour
        return attempts != null && Integer.parseInt(attempts) >= 3;
    }

    */
/**
     * Record password reset attempt
     *//*

    public void recordPasswordResetAttempt(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "password_reset:" + ipAddress;

        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));
    }
}*/
