package com.marketplace.model.entity;

import com.marketplace.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_bids", indexes = {
    @Index(name = "idx_bid_image", columnList = "image_id"),
    @Index(name = "idx_bid_bidder", columnList = "bidder_id"),
    @Index(name = "idx_bid_amount", columnList = "bid_amount"),
    @Index(name = "idx_bid_date", columnList = "bid_time"),
    @Index(name = "idx_bid_active", columnList = "is_active")
})
public class AuctionBid extends BaseEntity {
    
    @NotNull(message = "Image is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;
    
    @NotNull(message = "Bidder is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;
    
    @DecimalMin(value = "0.01", message = "Bid amount must be at least 0.01")
    @Column(name = "bid_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal bidAmount;
    
    @Column(name = "max_bid_amount", precision = 10, scale = 2)
    private BigDecimal maxBidAmount; // For automatic bidding
    
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_winning_bid", nullable = false)
    private Boolean isWinningBid = false;
    
    @Column(name = "bidder_ip", length = 45)
    private String bidderIp;
    
    @Column(name = "user_agent", length = 255)
    private String userAgent;
    
    // Constructors
    public AuctionBid() {}
    
    public AuctionBid(Image image, User bidder, BigDecimal bidAmount) {
        this.image = image;
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.bidTime = LocalDateTime.now();
    }
    
    // Business Methods
    public boolean isAutoBid() {
        return maxBidAmount != null && maxBidAmount.compareTo(bidAmount) > 0;
    }
    
    public boolean canIncreaseTo(BigDecimal amount) {
        return isActive && maxBidAmount != null && maxBidAmount.compareTo(amount) >= 0;
    }
    
    // Getters and Setters
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    
    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }
    
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
    public void setMaxBidAmount(BigDecimal maxBidAmount) { this.maxBidAmount = maxBidAmount; }
    
    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsWinningBid() { return isWinningBid; }
    public void setIsWinningBid(Boolean isWinningBid) { this.isWinningBid = isWinningBid; }
    
    public String getBidderIp() { return bidderIp; }
    public void setBidderIp(String bidderIp) { this.bidderIp = bidderIp; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}