package com.marketplace.model.dto.response;

import com.marketplace.model.enums.UserStatus;

import java.math.BigDecimal;

public class UserStatusResponse {

    private UserStatus status;
    private BigDecimal sellerRating;
    private BigDecimal totalSales;
    private BigDecimal totalEarnings;

    public UserStatusResponse() {}

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public BigDecimal getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(BigDecimal sellerRating) {
        this.sellerRating = sellerRating;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }
}