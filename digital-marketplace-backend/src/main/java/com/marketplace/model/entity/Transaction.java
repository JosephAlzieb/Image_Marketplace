package com.marketplace.model.entity;

import com.marketplace.model.entity.base.BaseEntity;
import com.marketplace.model.enums.PaymentStatus;
import com.marketplace.model.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_buyer", columnList = "buyer_id"),
    @Index(name = "idx_transaction_seller", columnList = "seller_id"),
    @Index(name = "idx_transaction_image", columnList = "image_id"),
    @Index(name = "idx_transaction_status", columnList = "payment_status"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_transaction_date", columnList = "created_at"),
    @Index(name = "idx_transaction_payment_intent", columnList = "payment_intent_id", unique = true)
})
public class Transaction extends BaseEntity {
    
    @NotNull(message = "Image is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;
    
    @NotNull(message = "Buyer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    
    @NotNull(message = "Seller is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;
    
    @DecimalMin(value = "0.01", message = "Gross amount must be at least 0.01")
    @Column(name = "gross_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal grossAmount;
    
    @Column(name = "platform_commission", precision = 10, scale = 2, nullable = false)
    private BigDecimal platformCommission;
    
    @Column(name = "creator_royalty", precision = 10, scale = 2)
    private BigDecimal creatorRoyalty = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    @Column(name = "net_to_seller", precision = 10, scale = 2, nullable = false)
    private BigDecimal netToSeller;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "payment_intent_id", unique = true, length = 100)
    private String paymentIntentId; // Stripe payment intent ID
    
    @Column(name = "payment_method_id", length = 100)
    private String paymentMethodId;
    
    @Column(name = "charge_id", length = 100)
    private String chargeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(name = "refund_reason")
    private String refundReason;
    
    @Column(name = "tax_region", length = 10)
    private String taxRegion;
    
    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;
    
    @Column(name = "vat_number", length = 50)
    private String vatNumber;
    
    @Column(name = "invoice_number", unique = true, length = 50)
    private String invoiceNumber;
    
    @Column(name = "receipt_url")
    private String receiptUrl;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    // JSON field for additional payment metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_metadata", columnDefinition = "jsonb")
    private Map<String, Object> paymentMetadata;
    
    // Relationships
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OwnershipHistory ownershipHistory;
    
    // Constructors
    public Transaction() {}
    
    public Transaction(Image image, User buyer, User seller, BigDecimal grossAmount, String currency) {
        this.image = image;
        this.buyer = buyer;
        this.seller = seller;
        this.grossAmount = grossAmount;
        this.currency = currency;
        this.transactionType = TransactionType.PURCHASE;
        this.paymentStatus = PaymentStatus.PENDING;
    }
    
    // Business Methods
    public void calculateAmounts(BigDecimal commissionRate, BigDecimal taxRate, BigDecimal processingFeeRate) {
        this.platformCommission = grossAmount.multiply(commissionRate);
        this.taxAmount = grossAmount.multiply(taxRate != null ? taxRate : BigDecimal.ZERO);
        this.processingFee = grossAmount.multiply(processingFeeRate != null ? processingFeeRate : BigDecimal.ZERO);
        
        this.netToSeller = grossAmount
                .subtract(platformCommission)
                .subtract(taxAmount)
                .subtract(processingFee)
                .subtract(creatorRoyalty);
    }
    
    public void markCompleted() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markFailed(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }
    
    public void processRefund(BigDecimal amount, String reason) {
        this.refundAmount = amount;
        this.refundReason = reason;
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
    
    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING || paymentStatus == PaymentStatus.PROCESSING;
    }
    
    public boolean canBeRefunded() {
        return isCompleted() && refundAmount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    // Getters and Setters
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    
    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    
    public BigDecimal getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(BigDecimal platformCommission) { this.platformCommission = platformCommission; }
    
    public BigDecimal getCreatorRoyalty() { return creatorRoyalty; }
    public void setCreatorRoyalty(BigDecimal creatorRoyalty) { this.creatorRoyalty = creatorRoyalty; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
    
    public BigDecimal getNetToSeller() { return netToSeller; }
    public void setNetToSeller(BigDecimal netToSeller) { this.netToSeller = netToSeller; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    
    public String getChargeId() { return chargeId; }
    public void setChargeId(String chargeId) { this.chargeId = chargeId; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    public String getTaxRegion() { return taxRegion; }
    public void setTaxRegion(String taxRegion) { this.taxRegion = taxRegion; }
    
    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
    
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public Map<String, Object> getPaymentMetadata() { return paymentMetadata; }
    public void setPaymentMetadata(Map<String, Object> paymentMetadata) { this.paymentMetadata = paymentMetadata; }
    
    public OwnershipHistory getOwnershipHistory() { return ownershipHistory; }
    public void setOwnershipHistory(OwnershipHistory ownershipHistory) { this.ownershipHistory = ownershipHistory; }
}
