package com.marketplace.service;

import com.marketplace.config.ApplicationPropertiesProvider;
import com.marketplace.exception.BadRequestException;
import com.marketplace.exception.PaymentException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedException;
import com.marketplace.model.dto.request.PurchaseRequest;
import com.marketplace.model.dto.response.TransactionResponse;
import com.marketplace.model.entity.*;
import com.marketplace.model.enums.*;
import com.marketplace.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private OwnershipHistoryRepository ownershipHistoryRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ApplicationPropertiesProvider appProperties;

    /**
     * Initiate purchase of an image
     */
    public TransactionResponse initiatePurchase(UUID buyerId, PurchaseRequest request) {
        logger.info("Initiating purchase for buyer {} of image {}", buyerId, request.getImageId());
        
        // Validate buyer
        User buyer = userService.getUserById(buyerId);
        userService.validateUserAction(buyerId, UserRole.BUYER);
        
        // Get and validate image
        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", request.getImageId()));
        
        User seller = image.getCurrentOwner();
        
        // Validate purchase
        validatePurchase(buyer, seller, image);
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setImage(image);
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setGrossAmount(image.getPrice());
        transaction.setCurrency(image.getCurrency());
        transaction.setPaymentStatus(PaymentStatus.PENDING);
        transaction.setTaxRegion(request.getTaxRegion());
        transaction.setVatNumber(request.getVatNumber());
        
        // Calculate amounts
        calculateTransactionAmounts(transaction, request.getTaxRate());
        
        // Generate invoice number
        transaction.setInvoiceNumber(generateInvoiceNumber());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Create payment intent with Stripe
            String paymentIntentId = paymentService.createPaymentIntent(savedTransaction);
            transaction.setPaymentIntentId(paymentIntentId);
            transactionRepository.save(transaction);
            
            // Send notification to seller
            notificationService.sendSellerNotification(seller, "New Purchase", 
                String.format("Someone wants to buy your image '%s'", image.getTitle()));
            
            logger.info("Successfully initiated purchase transaction: {}", savedTransaction.getId());
            return mapToResponse(savedTransaction);
            
        } catch (Exception e) {
            logger.error("Failed to create payment intent for transaction {}: {}", 
                        savedTransaction.getId(), e.getMessage(), e);
            
            // Mark transaction as failed
            transaction.setPaymentStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            
            throw new PaymentException("Failed to initiate payment: " + e.getMessage());
        }
    }
    
    /**
     * Complete purchase after successful payment
     */
    public void completePurchase(String paymentIntentId) {
        logger.info("Completing purchase for payment intent: {}", paymentIntentId);
        
        Transaction transaction = transactionRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "paymentIntentId", paymentIntentId));
        
        if (transaction.getPaymentStatus() == PaymentStatus.COMPLETED) {
            logger.warn("Transaction {} already completed", transaction.getId());
            return;
        }
        
        try {
            // Verify payment with Stripe
            boolean paymentVerified = paymentService.verifyPayment(paymentIntentId);
            
            if (!paymentVerified) {
                throw new PaymentException("Payment verification failed");
            }
            
            // Update transaction status
            transaction.setPaymentStatus(PaymentStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // Transfer ownership
            transferImageOwnership(transaction);
            
            // Update seller statistics
            userService.recordSale(transaction.getSeller().getId(), 
                                 transaction.getGrossAmount(), 
                                 transaction.getPlatformCommission());
            
            // Send notifications
            emailService.sendPurchaseConfirmationEmail(transaction.getBuyer(), transaction);
            emailService.sendSaleNotificationEmail(transaction.getSeller(), transaction);
            
            // Create audit log
            auditService.logTransactionCompleted(transaction);
            
            logger.info("Successfully completed purchase transaction: {}", transaction.getId());
            
        } catch (Exception e) {
            logger.error("Failed to complete purchase {}: {}", transaction.getId(), e.getMessage(), e);
            
            // Mark transaction as failed
            transaction.setPaymentStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setFailedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            throw new PaymentException("Failed to complete purchase: " + e.getMessage());
        }
    }
    
    /**
     * Process refund for a transaction
     */
    public void processRefund(UUID transactionId, UUID requesterId, String reason, BigDecimal refundAmount) {
        logger.info("Processing refund for transaction {} by user {}", transactionId, requesterId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        
        User requester = userService.getUserById(requesterId);
        
        // Validate refund request
        validateRefundRequest(transaction, requester, refundAmount);
        
        try {
            // Process refund with Stripe
            paymentService.processRefund(
                transaction.getPaymentIntentId(),
                refundAmount, 
                reason
            );
            // Update transaction
            transaction.setPaymentStatus(PaymentStatus.REFUNDED);
            transaction.setRefundAmount(refundAmount);
            transaction.setRefundReason(reason);
            transactionRepository.save(transaction);
            
            // Reverse ownership if full refund
            if (refundAmount.compareTo(transaction.getGrossAmount()) == 0) {
                reverseOwnershipTransfer(transaction);
            }
            
            // Send notifications
            emailService.sendRefundConfirmationEmail(transaction.getBuyer(), transaction, refundAmount);
            emailService.sendRefundNotificationEmail(transaction.getSeller(), transaction, refundAmount);
            
            // Create audit log
            auditService.logTransactionRefunded(transaction, requester, reason);
            
            logger.info("Successfully processed refund for transaction: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Failed to process refund for transaction {}: {}", transactionId, e.getMessage(), e);
            throw new PaymentException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * Get user's purchase history
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserPurchases(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, null);
        
        Page<Transaction> transactions = transactionRepository.findByBuyerIdAndPaymentStatusOrderByCreatedAtDesc(
            userId, PaymentStatus.COMPLETED, pageable);
        
        return transactions.map(this::mapToResponse);
    }
    
    /**
     * Get user's sales history
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserSales(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, UserRole.SELLER);
        
        Page<Transaction> transactions = transactionRepository.findBySellerIdAndPaymentStatusOrderByCreatedAtDesc(
            userId, PaymentStatus.COMPLETED, pageable);
        
        return transactions.map(this::mapToResponse);
    }
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        
        // Check if user is involved in the transaction or is admin
        User user = userService.getUserById(userId);
        boolean hasAccess = transaction.getBuyer().getId().equals(userId) ||
                           transaction.getSeller().getId().equals(userId) ||
                           user.getRole() == UserRole.ADMIN;
        
        if (!hasAccess) {
            throw new UnauthorizedException("You don't have access to this transaction");
        }
        
        return mapToResponse(transaction);
    }
    
    /**
     * Get sales analytics for seller
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSellerAnalytics(UUID sellerId, LocalDateTime fromDate, LocalDateTime toDate) {
        userService.validateUserAction(sellerId, UserRole.SELLER);
        
        return Map.of(
            "totalSales", transactionRepository.getTotalSalesForSeller(sellerId, fromDate, toDate),
            "totalEarnings", transactionRepository.getTotalEarningsForSeller(sellerId, fromDate, toDate),
            "totalCommission", transactionRepository.getTotalCommissionForSeller(sellerId, fromDate, toDate),
            "transactionCount", transactionRepository.getTransactionCountForSeller(sellerId, fromDate, toDate),
            "averageTransactionValue", transactionRepository.getAverageTransactionValueForSeller(sellerId, fromDate, toDate)
        );
    }
    
    /**
     * Get platform analytics (admin only)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformAnalytics(UUID adminId, LocalDateTime fromDate, LocalDateTime toDate) {
        userService.validateUserAction(adminId, UserRole.ADMIN);
        
        return Map.of(
            "totalRevenue", transactionRepository.getTotalRevenueBetween(fromDate, toDate),
            "totalCommission", transactionRepository.getTotalCommissionBetween(fromDate, toDate),
            "totalTransactions", transactionRepository.getTransactionCountBetween(fromDate, toDate),
            "uniqueBuyers", transactionRepository.getUniqueBuyersCountBetween(fromDate, toDate),
            "uniqueSellers", transactionRepository.getUniqueSellersCountBetween(fromDate, toDate),
            "averageTransactionValue", transactionRepository.getAverageTransactionValueBetween(fromDate, toDate)
        );
    }
    
    // Private helper methods
    
    private void validatePurchase(User buyer, User seller, Image image) {
        // Check if image is available
        if (!image.getIsAvailable()) {
            throw new BadRequestException("Image is not available for purchase");
        }
        
        // Check if buyer is trying to buy their own image
        if (buyer.getId().equals(seller.getId())) {
            throw new BadRequestException("You cannot purchase your own image");
        }
        
        // Check if it's an auction and if it's active
        if (image.getSaleType() == SaleType.AUCTION) {
            if (!image.isAuctionActive()) {
                throw new BadRequestException("Auction is not active");
            }
            throw new BadRequestException("Auction items must be purchased through bidding");
        }
        
        // Check buyer status
        if (buyer.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Your account is not active");
        }
        
        // Check seller status
        if (seller.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Seller account is not active");
        }
    }
    
    private void calculateTransactionAmounts(Transaction transaction, BigDecimal taxRate) {
        BigDecimal grossAmount = transaction.getGrossAmount();
        
        // Calculate platform commission
        BigDecimal commission = calculatePlatformCommission(grossAmount);
        transaction.setPlatformCommission(commission);
        
        // Calculate processing fee
        BigDecimal processingFee = calculateProcessingFee(grossAmount);
        transaction.setProcessingFee(processingFee);
        
        // Calculate tax amount
        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxAmount = grossAmount.multiply(taxRate);
            transaction.setTaxAmount(taxAmount);
            transaction.setVatRate(taxRate);
        }
        
        // Calculate creator royalty (if resale)
        BigDecimal creatorRoyalty = BigDecimal.ZERO;
        Image image = transaction.getImage();
        if (!image.getUploader().getId().equals(image.getCurrentOwner().getId())) {
            // This is a resale, calculate 5% royalty to original creator
            creatorRoyalty = grossAmount.multiply(new BigDecimal("0.05"));
            transaction.setCreatorRoyalty(creatorRoyalty);
        }
        
        // Calculate net amount to seller
        BigDecimal netToSeller = grossAmount
                .subtract(commission)
                .subtract(processingFee)
                .subtract(creatorRoyalty);
        transaction.setNetToSeller(netToSeller);
    }
    
    private BigDecimal calculatePlatformCommission(BigDecimal grossAmount) {
        return grossAmount.multiply(BigDecimal.valueOf(appProperties.getCommissionRate()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProcessingFee(BigDecimal grossAmount) {
        return grossAmount.multiply(BigDecimal.valueOf(appProperties.getProcessingFeeRate()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void transferImageOwnership(Transaction transaction) {
        Image image = transaction.getImage();
        User previousOwner = image.getCurrentOwner();
        User newOwner = transaction.getBuyer();
        
        // Update image ownership
        image.setCurrentOwner(newOwner);
        image.incrementDownloadCount(); // Buyer gets download access
        imageRepository.save(image);
        
        // Create ownership history record
        OwnershipHistory ownershipHistory = new OwnershipHistory();
        ownershipHistory.setImage(image);
        ownershipHistory.setPreviousOwner(previousOwner);
        ownershipHistory.setNewOwner(newOwner);
        ownershipHistory.setTransaction(transaction);
        ownershipHistory.setTransferType(TransferType.PURCHASE);
        ownershipHistory.setTransferDate(LocalDateTime.now());
        ownershipHistory.setPurchasePrice(transaction.getGrossAmount());
        ownershipHistory.setCurrency(transaction.getCurrency());
        
        ownershipHistoryRepository.save(ownershipHistory);
        
        // Link ownership history to transaction
        transaction.setOwnershipHistory(ownershipHistory);
        transactionRepository.save(transaction);
        
        logger.info("Transferred ownership of image {} from {} to {}", 
                   image.getId(), previousOwner.getId(), newOwner.getId());
    }
    
    private void reverseOwnershipTransfer(Transaction transaction) {
        OwnershipHistory ownershipHistory = transaction.getOwnershipHistory();
        if (ownershipHistory == null) {
            logger.warn("No ownership history found for transaction {}", transaction.getId());
            return;
        }
        
        Image image = transaction.getImage();
        
        // Reverse ownership
        image.setCurrentOwner(ownershipHistory.getPreviousOwner());
        imageRepository.save(image);
        
        // Create reverse ownership history record
        OwnershipHistory reverseHistory = new OwnershipHistory();
        reverseHistory.setImage(image);
        reverseHistory.setPreviousOwner(ownershipHistory.getNewOwner());
        reverseHistory.setNewOwner(ownershipHistory.getPreviousOwner());
        reverseHistory.setTransaction(transaction);
        reverseHistory.setTransferType(TransferType.ADMIN_TRANSFER); // Refund reversal
        reverseHistory.setTransferDate(LocalDateTime.now());
        reverseHistory.setNotes("Ownership reversed due to refund");
        
        ownershipHistoryRepository.save(reverseHistory);
        
        logger.info("Reversed ownership of image {} due to refund", image.getId());
    }
    
    private void validateRefundRequest(Transaction transaction, User requester, BigDecimal refundAmount) {
        // Check if transaction is completed
        if (transaction.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Can only refund completed transactions");
        }
        
        // Check if already refunded
        if (transaction.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Transaction has already been refunded");
        }
        
        // Check permissions (buyer, seller, or admin)
        boolean canRefund = transaction.getBuyer().getId().equals(requester.getId()) ||
                           transaction.getSeller().getId().equals(requester.getId()) ||
                           requester.getRole() == UserRole.ADMIN;
        
        if (!canRefund) {
            throw new UnauthorizedException("You don't have permission to refund this transaction");
        }
        
        // Check refund amount
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            refundAmount.compareTo(transaction.getGrossAmount()) > 0) {
            throw new BadRequestException("Invalid refund amount");
        }
        
        // Check if refund is within allowed timeframe (e.g., 30 days)
        LocalDateTime cutoffDate = transaction.getCompletedAt().plusDays(30);
        if (LocalDateTime.now().isAfter(cutoffDate) && requester.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Refund period has expired");
        }
    }
    
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 1000);
    }
    
    private TransactionResponse mapToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setImageId(transaction.getImage().getId());
        response.setImageTitle(transaction.getImage().getTitle());
        response.setImageThumbnailUrl(transaction.getImage().getThumbnailUrl());
        response.setBuyerId(transaction.getBuyer().getId());
        response.setBuyerName(transaction.getBuyer().getFullName());
        response.setBuyerEmail(transaction.getBuyer().getEmail());
        response.setSellerId(transaction.getSeller().getId());
        response.setSellerName(transaction.getSeller().getFullName());
        response.setTransactionType(transaction.getTransactionType());
        response.setGrossAmount(transaction.getGrossAmount());
        response.setPlatformCommission(transaction.getPlatformCommission());
        response.setCreatorRoyalty(transaction.getCreatorRoyalty());
        response.setTaxAmount(transaction.getTaxAmount());
        response.setProcessingFee(transaction.getProcessingFee());
        response.setNetToSeller(transaction.getNetToSeller());
        response.setCurrency(transaction.getCurrency());
        response.setPaymentStatus(transaction.getPaymentStatus());
        response.setPaymentIntentId(transaction.getPaymentIntentId());
        response.setInvoiceNumber(transaction.getInvoiceNumber());
        response.setReceiptUrl(transaction.getReceiptUrl());
        response.setRefundAmount(transaction.getRefundAmount());
        response.setRefundReason(transaction.getRefundReason());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        response.setFailedAt(transaction.getFailedAt());
        response.setFailureReason(transaction.getFailureReason());
        return response;
    }
}