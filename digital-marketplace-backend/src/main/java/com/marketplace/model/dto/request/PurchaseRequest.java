package com.marketplace.model.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseRequest {
    private UUID imageId;
    private String taxRegion;
    private String vatNumber;
    private BigDecimal taxRate;

    public UUID getImageId() { return imageId; }
    public void setImageId(UUID imageId) { this.imageId = imageId; }
    public String getTaxRegion() { return taxRegion; }
    public void setTaxRegion(String taxRegion) { this.taxRegion = taxRegion; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
}

