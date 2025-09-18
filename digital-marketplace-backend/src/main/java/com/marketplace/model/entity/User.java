package com.marketplace.model.entity;

import com.marketplace.model.entity.base.BaseEntity;
import com.marketplace.model.enums.UserRole;
import com.marketplace.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
public class User extends BaseEntity {
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.BUYER;
    
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;
    
    @Column(name = "bio", length = 1000)
    private String bio;
    
    @Size(max = 2, message = "Country code must be 2 characters")
    @Column(name = "country_code", length = 2)
    private String countryCode;
    
    @Size(max = 50, message = "VAT number must not exceed 50 characters")
    @Column(name = "vat_number", length = 50)
    private String vatNumber;
    
    @Size(max = 100)
    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;
    
    @Size(max = 100)
    @Column(name = "stripe_account_id", length = 100)
    private String stripeAccountId; // For sellers to receive payments
    
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;
    
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    // Seller specific fields
    @Column(name = "seller_rating", precision = 3, scale = 2)
    private BigDecimal sellerRating;
    
    @Column(name = "total_sales", precision = 15, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;
    
    @Column(name = "total_earnings", precision = 15, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    
    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = new BigDecimal("0.1000"); // 10% default
    
    // JSON field for additional user preferences
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Map<String, Object> preferences;
    
    // Relationships
    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> uploadedImages = new ArrayList<>();
    
    @OneToMany(mappedBy = "currentOwner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> ownedImages = new ArrayList<>();
    
    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> purchases = new ArrayList<>();
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> sales = new ArrayList<>();
    
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> givenReviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> receivedReviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "bidder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuctionBid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ImageReaction> reactions = new HashSet<>();
    
    // Constructors
    public User() {}
    
    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Business Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addSale(BigDecimal grossAmount, BigDecimal commission) {
        if (grossAmount != null) {
            this.totalSales = this.totalSales.add(grossAmount);
        }
        if (commission != null) {
            this.totalEarnings = this.totalEarnings.add(grossAmount.subtract(commission));
        }
    }

    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }


    public boolean isSeller() {
        return role == UserRole.SELLER || role == UserRole.PREMIUM_SELLER;
    }
    
    // Getter/Setter für alle Felder (nur die wichtigsten für Service)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String url) { this.profilePictureUrl = url; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String code) { this.countryCode = code; }
    public BigDecimal getSellerRating() { return sellerRating; }
    public void setSellerRating(BigDecimal rating) { this.sellerRating = rating; }
    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal sales) { this.totalSales = sales; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal earnings) { this.totalEarnings = earnings; }
    public LocalDateTime getCreatedAt() { return super.getCreatedAt(); }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Additional getters/setters for other fields...
    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    
    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }
    
    // Collection getters/setters
    public List<Image> getUploadedImages() { return uploadedImages; }
    public void setUploadedImages(List<Image> uploadedImages) { this.uploadedImages = uploadedImages; }
    
    public List<Image> getOwnedImages() { return ownedImages; }
    public void setOwnedImages(List<Image> ownedImages) { this.ownedImages = ownedImages; }
    
    public List<Transaction> getPurchases() { return purchases; }
    public void setPurchases(List<Transaction> purchases) { this.purchases = purchases; }
    
    public List<Transaction> getSales() { return sales; }
    public void setSales(List<Transaction> sales) { this.sales = sales; }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public LocalDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public List<Review> getGivenReviews() {
        return givenReviews;
    }

    public void setGivenReviews(List<Review> givenReviews) {
        this.givenReviews = givenReviews;
    }

    public List<Review> getReceivedReviews() {
        return receivedReviews;
    }

    public void setReceivedReviews(List<Review> receivedReviews) {
        this.receivedReviews = receivedReviews;
    }

    public List<AuctionBid> getBids() {
        return bids;
    }

    public void setBids(List<AuctionBid> bids) {
        this.bids = bids;
    }

    public Set<ImageReaction> getReactions() {
        return reactions;
    }

    public void setReactions(Set<ImageReaction> reactions) {
        this.reactions = reactions;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
    }
}
