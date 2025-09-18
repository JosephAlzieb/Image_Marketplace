package com.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * GET /api/health/check
     * Basic health check
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "digital-marketplace-api");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * GET /api/health/detailed
     * Detailed health check with dependencies
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Database health
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", Map.of("status", "UP", "url", connection.getMetaData().getURL()));
        } catch (Exception e) {
            health.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
        }
        
        // Add other health checks (Redis, S3, etc.)
        health.put("redis", Map.of("status", "UP")); // Placeholder
        health.put("storage", Map.of("status", "UP")); // Placeholder
        
        return ResponseEntity.ok(health);
    }
}