package com.marketplace.controller;

import com.marketplace.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * POST /api/webhooks/stripe
     * Handle Stripe webhook events
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            logger.info("Received Stripe webhook");
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to process Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
    
    /**
     * GET /api/webhooks/health
     * Health check for webhook endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}