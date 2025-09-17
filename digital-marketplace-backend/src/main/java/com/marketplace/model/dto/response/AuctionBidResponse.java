package com.marketplace.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuctionBidResponse {
    private UUID id;
    private UUID imageId;
    private String imageTitle;
    private String imageThumbnailUrl;
    private UUID bidderId;
    private String bidderName;
    private BigDecimal bidAmount;
    private BigDecimal maxBidAmount;
    private LocalDateTime bidTime;
    private Boolean isActive;
    private Boolean isWinningBid;
    private Boolean isAutoBid;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getImageId() {
        return imageId;
    }

    public void setImageId(UUID imageId) {
        this.imageId = imageId;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getImageThumbnailUrl() {
        return imageThumbnailUrl;
    }

    public void setImageThumbnailUrl(String imageThumbnailUrl) {
        this.imageThumbnailUrl = imageThumbnailUrl;
    }

    public UUID getBidderId() {
        return bidderId;
    }

    public void setBidderId(UUID bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public BigDecimal getMaxBidAmount() {
        return maxBidAmount;
    }

    public void setMaxBidAmount(BigDecimal maxBidAmount) {
        this.maxBidAmount = maxBidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsWinningBid() {
        return isWinningBid;
    }

    public void setIsWinningBid(Boolean isWinningBid) {
        this.isWinningBid = isWinningBid;
    }

    public Boolean getIsAutoBid() {
        return isAutoBid;
    }

    public void setIsAutoBid(Boolean isAutoBid) {
        this.isAutoBid = isAutoBid;
    }
}
