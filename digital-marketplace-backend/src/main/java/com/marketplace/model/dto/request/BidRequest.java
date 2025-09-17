package com.marketplace.model.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class BidRequest {
    private UUID imageId;
    private BigDecimal bidAmount;
    private BigDecimal maxBidAmount; // for auto-bidding
    private boolean isAutoBid = false;

    // Getters and Setters
    public UUID getImageId() {
        return imageId;
    }

    public void setImageId(UUID imageId) {
        this.imageId = imageId;
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

    public boolean isAutoBid() {
        return isAutoBid;
    }

    public void setAutoBid(boolean autoBid) {
        isAutoBid = autoBid;
    }
}
