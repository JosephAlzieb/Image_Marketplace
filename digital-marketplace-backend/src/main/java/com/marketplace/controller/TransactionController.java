package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.PurchaseRequest;
import com.marketplace.model.dto.request.RefundRequest;
import com.marketplace.model.dto.response.TransactionResponse;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * POST /api/transactions/purchase
     * Initiate image purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponse> initiatePurchase(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody PurchaseRequest request) {
        
        TransactionResponse transaction = transactionService.initiatePurchase(
            currentUser.getId(), request);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * GET /api/transactions/purchases
     * Get user's purchase history
     */
    @GetMapping("/purchases")
    public ResponseEntity<Page<TransactionResponse>> getMyPurchases(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<TransactionResponse> purchases = transactionService.getUserPurchases(
            currentUser.getId(), pageable);
        return ResponseEntity.ok(purchases);
    }
    
    /**
     * GET /api/transactions/sales
     * Get user's sales history (sellers only)
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Page<TransactionResponse>> getMySales(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<TransactionResponse> sales = transactionService.getUserSales(
            currentUser.getId(), pageable);
        return ResponseEntity.ok(sales);
    }
    
    /**
     * GET /api/transactions/{transactionId}
     * Get transaction details
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID transactionId) {
        
        TransactionResponse transaction = transactionService.getTransactionById(
            transactionId, currentUser.getId());
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * POST /api/transactions/{transactionId}/refund
     * Request refund for transaction
     */
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<ApiResponse> requestRefund(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID transactionId,
            @Valid @RequestBody RefundRequest request) {
        
        transactionService.processRefund(
            transactionId, 
            currentUser.getId(), 
            request.getReason(), 
            request.getRefundAmount()
        );
        
        return ResponseEntity.ok(new ApiResponse(true, "Refund processed successfully"));
    }
    
    /**
     * GET /api/transactions/analytics/seller
     * Get seller analytics (sellers only)
     */
    @GetMapping("/analytics/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Map<String, Object>> getSellerAnalytics(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate) {
        
        // Default to last 30 days if not specified
        if (fromDate == null) fromDate = LocalDateTime.now().minusDays(30);
        if (toDate == null) toDate = LocalDateTime.now();
        
        Map<String, Object> analytics = transactionService.getSellerAnalytics(
            currentUser.getId(), fromDate, toDate);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * POST /api/transactions/webhook/stripe
     * Stripe webhook endpoint for payment confirmations
     */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            // paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
}
