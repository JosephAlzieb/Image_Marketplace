package com.marketplace.model.dto.request;

import java.math.BigDecimal;

public class RefundRequest {
    private String reason;
    private BigDecimal refundAmount;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
