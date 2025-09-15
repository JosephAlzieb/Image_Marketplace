package com.marketplace.model.enums;

public enum TransferType {
    UPLOAD,         // Original upload by creator
    PURCHASE,       // Regular purchase
    AUCTION,        // Auction win
    OFFER,          // Accepted offer
    GIFT,           // Gift transfer
    ADMIN_TRANSFER  // Admin-initiated transfer
}