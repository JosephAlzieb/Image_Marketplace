package com.marketplace.model.entity;

import com.marketplace.model.entity.base.BaseEntity;
import com.marketplace.model.enums.LicenseType;
import com.marketplace.model.enums.SaleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "images", indexes = {
    @Index(name = "idx_image_uploader", columnList = "uploader_id"),
    @Index(name = "idx_image_owner", columnList = "current_owner_id"),
    @Index(name = "idx_image_category", columnList = "category_id"),
    @Index(name = "idx_image_available", columnList = "is_available"),
    @Index(name = "idx_image_sale_type", columnList = "sale_type"),
    @Index(name = "idx_image_price", columnList = "price"),
    @Index(name = "idx_image_created", columnList = "created_at"),
    @Index(name = "idx_image_featured", columnList = "is_featured")
})
public class Image extends BaseEntity {
    
    @NotBlank(message = "Image title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;
    
    @NotBlank(message = "File URL is required")
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "watermark_url")
    private String watermarkUrl;
    
    @Column(name = "preview_url")
    private String previewUrl; // Low resolution preview
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions", columnDefinition = "jsonb")
    private Map<String, Object> dimensions; // {width: 1920, height: 1080, aspectRatio: 1.77}
    
    @Size(max = 10)
    @Column(name = "file_format", length = 10)
    private String fileFormat;
    
    @Column(name = "file_hash", length = 64) // SHA-256 hash for duplicate detection
    private String fileHash;
    
    @NotNull(message = "Uploader is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;
    
    @NotNull(message = "Current owner is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_owner_id", nullable = false)
    private User currentOwner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata; // EXIF, color palette, AI analysis, etc.
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "color_palette", columnDefinition = "jsonb")
    private List<String> colorPalette; // Dominant colors in hex format
    
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Size(max = 3)
    @Column(name = "currency", length = 3)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", length = 20)
    private SaleType saleType = SaleType.FIXED;
    
    // Auction specific fields
    @Column(name = "auction_start_time")
    private LocalDateTime auctionStartTime;
    
    @Column(name = "auction_end_time")
    private LocalDateTime auctionEndTime;
    
    @Column(name = "starting_bid", precision = 10, scale = 2)
    private BigDecimal startingBid;
    
    @Column(name = "reserve_price", precision = 10, scale = 2)
    private BigDecimal reservePrice;
    
    @Column(name = "buy_now_price", precision = 10, scale = 2)
    private BigDecimal buyNowPrice;
    
    @Column(name = "current_bid", precision = 10, scale = 2)
    private BigDecimal currentBid;
    
    @Column(name = "bid_count")
    private Integer bidCount = 0;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "is_mature_content", nullable = false)
    private Boolean isMatureContent = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", length = 20)
    private LicenseType licenseType = LicenseType.STANDARD;
    
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;
    
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;
    
    // AI/ML generated fields
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_tags", columnDefinition = "jsonb")
    private List<String> aiTags; // Auto-generated tags
    
    @Column(name = "ai_description", length = 500)
    private String aiDescription; // AI-generated description
    
    @Column(name = "content_safety_score", precision = 3, scale = 2)
    private BigDecimal contentSafetyScore; // 0-1 score for content safety
    
    // SEO and discoverability
    @Column(name = "seo_title", length = 255)
    private String seoTitle;
    
    @Column(name = "seo_description", length = 500)
    private String seoDescription;
    
    @Column(name = "alt_text", length = 255)
    private String altText;
    
    // Relationships
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OwnershipHistory> ownershipHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuctionBid> bids = new ArrayList<>();
    
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImageReport> reports = new ArrayList<>();
    
    @ManyToMany(mappedBy = "images", fetch = FetchType.LAZY)
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ImageReaction> reactions = new HashSet<>();

    // Constructors
    public Image() {}
    
    public Image(String title, String fileUrl, User uploader, BigDecimal price) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.uploader = uploader;
        this.currentOwner = uploader; // Initially owned by uploader
        this.price = price;
    }
    
    // Business Methods
    public boolean isOwnedBy(User user) {
        return currentOwner != null && currentOwner.equals(user);
    }
    
    public boolean isUploadedBy(User user) {
        return uploader != null && uploader.equals(user);
    }
    
    public boolean isAuction() {
        return saleType == SaleType.AUCTION;
    }
    
    public boolean isAuctionActive() {
        if (!isAuction()) return false;
        LocalDateTime now = LocalDateTime.now();
        return auctionStartTime != null && auctionEndTime != null &&
               now.isAfter(auctionStartTime) && now.isBefore(auctionEndTime);
    }
    
    public boolean isAuctionEnded() {
        if (!isAuction()) return false;
        return auctionEndTime != null && LocalDateTime.now().isAfter(auctionEndTime);
    }
    
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }
    
    public void incrementDownloadCount() {
        this.downloadCount = this.downloadCount + 1;
    }
    
    public void incrementLikeCount() {
        this.likeCount = this.likeCount + 1;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount = this.likeCount - 1;
        }
    }
    
    public void updateRating(BigDecimal newRating) {
        if (this.averageRating == null) {
            this.averageRating = newRating;
            this.ratingCount = 1;
        } else {
            BigDecimal totalRating = this.averageRating.multiply(new BigDecimal(this.ratingCount));
            totalRating = totalRating.add(newRating);
            this.ratingCount = this.ratingCount + 1;
            this.averageRating = totalRating.divide(new BigDecimal(this.ratingCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getWatermarkUrl() { return watermarkUrl; }
    public void setWatermarkUrl(String watermarkUrl) { this.watermarkUrl = watermarkUrl; }
    
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Map<String, Object> getDimensions() { return dimensions; }
    public void setDimensions(Map<String, Object> dimensions) { this.dimensions = dimensions; }
    
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    
    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }
    
    public User getCurrentOwner() { return currentOwner; }
    public void setCurrentOwner(User currentOwner) { this.currentOwner = currentOwner; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public List<String> getColorPalette() { return colorPalette; }
    public void setColorPalette(List<String> colorPalette) { this.colorPalette = colorPalette; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public SaleType getSaleType() { return saleType; }
    public void setSaleType(SaleType saleType) { this.saleType = saleType; }
    
    public LocalDateTime getAuctionStartTime() { return auctionStartTime; }
    public void setAuctionStartTime(LocalDateTime auctionStartTime) { this.auctionStartTime = auctionStartTime; }
    
    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }
    
    public BigDecimal getStartingBid() { return startingBid; }
    public void setStartingBid(BigDecimal startingBid) { this.startingBid = startingBid; }
    
    public BigDecimal getReservePrice() { return reservePrice; }
    public void setReservePrice(BigDecimal reservePrice) { this.reservePrice = reservePrice; }
    
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
    
    public BigDecimal getCurrentBid() { return currentBid; }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }
    
    public Integer getBidCount() { return bidCount; }
    public void setBidCount(Integer bidCount) { this.bidCount = bidCount; }
    
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Boolean getIsMatureContent() { return isMatureContent; }
    public void setIsMatureContent(Boolean isMatureContent) { this.isMatureContent = isMatureContent; }
    
    public LicenseType getLicenseType() { return licenseType; }
    public void setLicenseType(LicenseType licenseType) { this.licenseType = licenseType; }
    
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    
    public List<String> getAiTags() { return aiTags; }
    public void setAiTags(List<String> aiTags) { this.aiTags = aiTags; }
    
    public String getAiDescription() { return aiDescription; }
    public void setAiDescription(String aiDescription) { this.aiDescription = aiDescription; }
    
    public BigDecimal getContentSafetyScore() { return contentSafetyScore; }
    public void setContentSafetyScore(BigDecimal contentSafetyScore) { this.contentSafetyScore = contentSafetyScore; }
    
    public String getSeoTitle() { return seoTitle; }
    public void setSeoTitle(String seoTitle) { this.seoTitle = seoTitle; }
    
    public String getSeoDescription() { return seoDescription; }
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }
    
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    
    // Collections getters/setters
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    
    public List<OwnershipHistory> getOwnershipHistory() { return ownershipHistory; }
    public void setOwnershipHistory(List<OwnershipHistory> ownershipHistory) { this.ownershipHistory = ownershipHistory; }
    
    public List<AuctionBid> getBids() { return bids; }
    public void setBids(List<AuctionBid> bids) { this.bids = bids; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    
    public List<ImageReport> getReports() { return reports; }
    public void setReports(List<ImageReport> reports) { this.reports = reports; }
    
    public List<Collection> getCollections() { return collections; }
    public void setCollections(List<Collection> collections) { this.collections = collections; }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public Boolean getFeatured() {
        return isFeatured;
    }

    public void setFeatured(Boolean featured) {
        isFeatured = featured;
    }

    public Boolean getMatureContent() {
        return isMatureContent;
    }

    public void setMatureContent(Boolean matureContent) {
        isMatureContent = matureContent;
    }

    public Set<ImageReaction> getReactions() {
        return reactions;
    }

    public void setReactions(Set<ImageReaction> reactions) {
        this.reactions = reactions;
    }
}
