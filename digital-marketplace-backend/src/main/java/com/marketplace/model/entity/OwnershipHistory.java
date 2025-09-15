package com.marketplace.model.entity;

import com.marketplace.model.entity.base.BaseEntity;
import com.marketplace.model.enums.TransferType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ownership_history", indexes = {
    @Index(name = "idx_ownership_image", columnList = "image_id"),
    @Index(name = "idx_ownership_previous", columnList = "previous_owner_id"),
    @Index(name = "idx_ownership_new", columnList = "new_owner_id"),
    @Index(name = "idx_ownership_transaction", columnList = "transaction_id"),
    @Index(name = "idx_ownership_date", columnList = "transfer_date")
})
public class OwnershipHistory extends BaseEntity {
    
    @NotNull(message = "Image is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_owner_id")
    private User previousOwner; // Null for original upload
    
    @NotNull(message = "New owner is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_owner_id", nullable = false)
    private User newOwner;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction; // Null for uploads and gifts
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    private TransferType transferType;
    
    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "ownership_certificate_url")
    private String ownershipCertificateUrl; // Blockchain or digital certificate
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Constructors
    public OwnershipHistory() {}
    
    public OwnershipHistory(Image image, User previousOwner, User newOwner, TransferType transferType) {
        this.image = image;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
        this.transferType = transferType;
        this.transferDate = LocalDateTime.now();
    }
    
    // Business Methods
    public boolean isOriginalUpload() {
        return transferType == TransferType.UPLOAD && previousOwner == null;
    }
    
    public boolean isPurchaseTransfer() {
        return transferType == TransferType.PURCHASE && transaction != null;
    }
    
    // Getters and Setters
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    
    public User getPreviousOwner() { return previousOwner; }
    public void setPreviousOwner(User previousOwner) { this.previousOwner = previousOwner; }
    
    public User getNewOwner() { return newOwner; }
    public void setNewOwner(User newOwner) { this.newOwner = newOwner; }
    
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    
    public TransferType getTransferType() { return transferType; }
    public void setTransferType(TransferType transferType) { this.transferType = transferType; }
    
    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }
    
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getOwnershipCertificateUrl() { return ownershipCertificateUrl; }
    public void setOwnershipCertificateUrl(String ownershipCertificateUrl) { this.ownershipCertificateUrl = ownershipCertificateUrl; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
