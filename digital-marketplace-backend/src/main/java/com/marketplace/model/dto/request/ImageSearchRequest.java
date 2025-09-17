package com.marketplace.model.dto.request;

import com.marketplace.model.enums.LicenseType;
import com.marketplace.model.enums.SaleType;

import java.math.BigDecimal;
import java.util.UUID;

public class ImageSearchRequest {
    private String query;
    private UUID categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private SaleType saleType;
    private LicenseType licenseType;
    private Boolean includeMatureContent;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public SaleType getSaleType() { return saleType; }
    public void setSaleType(SaleType saleType) { this.saleType = saleType; }
    public LicenseType getLicenseType() { return licenseType; }
    public void setLicenseType(LicenseType licenseType) { this.licenseType = licenseType; }
    public Boolean getIncludeMatureContent() { return includeMatureContent; }
    public void setIncludeMatureContent(Boolean includeMatureContent) { this.includeMatureContent = includeMatureContent; }
}

