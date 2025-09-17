package com.marketplace.model.dto.response;

import com.marketplace.model.enums.LicenseType;
import com.marketplace.model.enums.SaleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImageResponse {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String watermarkUrl;
    private String previewUrl;
    private Map<String, Object> dimensions;
    private String fileFormat;
    private Long fileSize;
    private UUID uploaderId;
    private String uploaderName;
    private UUID currentOwnerId;
    private String currentOwnerName;
    private UUID categoryId;
    private String categoryName;
    private List<String> tags;
    private List<String> colorPalette;
    private BigDecimal price;
    private String currency;
    private SaleType saleType;
    private LicenseType licenseType;
    private Integer viewCount;
    private Integer downloadCount;
    private Integer likeCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Boolean isFeatured;
    private Boolean isMatureContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Auction specific
    private LocalDateTime auctionStartTime;
    private LocalDateTime auctionEndTime;
    private BigDecimal startingBid;
    private BigDecimal currentBid;
    private Integer bidCount;
    private BigDecimal buyNowPrice;
    private Boolean isAuctionActive;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getWatermarkUrl() { return watermarkUrl; }
    public void setWatermarkUrl(String watermarkUrl) { this.watermarkUrl = watermarkUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public Map<String, Object> getDimensions() { return dimensions; }
    public void setDimensions(Map<String, Object> dimensions) { this.dimensions = dimensions; }
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public UUID getUploaderId() { return uploaderId; }
    public void setUploaderId(UUID uploaderId) { this.uploaderId = uploaderId; }
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public UUID getCurrentOwnerId() { return currentOwnerId; }
    public void setCurrentOwnerId(UUID currentOwnerId) { this.currentOwnerId = currentOwnerId; }
    public String getCurrentOwnerName() { return currentOwnerName; }
    public void setCurrentOwnerName(String currentOwnerName) { this.currentOwnerName = currentOwnerName; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getColorPalette() { return colorPalette; }
    public void setColorPalette(List<String> colorPalette) { this.colorPalette = colorPalette; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public SaleType getSaleType() { return saleType; }
    public void setSaleType(SaleType saleType) { this.saleType = saleType; }
    public LicenseType getLicenseType() { return licenseType; }
    public void setLicenseType(LicenseType licenseType) { this.licenseType = licenseType; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public Boolean getIsMatureContent() { return isMatureContent; }
    public void setIsMatureContent(Boolean isMatureContent) { this.isMatureContent = isMatureContent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getAuctionStartTime() { return auctionStartTime; }
    public void setAuctionStartTime(LocalDateTime auctionStartTime) { this.auctionStartTime = auctionStartTime; }
    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }
    public BigDecimal getStartingBid() { return startingBid; }
    public void setStartingBid(BigDecimal startingBid) { this.startingBid = startingBid; }
    public BigDecimal getCurrentBid() { return currentBid; }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }
    public Integer getBidCount() { return bidCount; }
    public void setBidCount(Integer bidCount) { this.bidCount = bidCount; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
    public Boolean getIsAuctionActive() { return isAuctionActive; }
    public void setIsAuctionActive(Boolean isAuctionActive) { this.isAuctionActive = isAuctionActive; }
}

