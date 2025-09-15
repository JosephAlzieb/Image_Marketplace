package com.marketplace.model.enums;

public enum TransactionType {
    PURCHASE,       // Direct purchase
    AUCTION_WIN,    // Won auction
    OFFER_ACCEPT,   // Accepted offer
    BUNDLE_PURCHASE,// Bundle purchase
    GIFT,           // Gift from another user
    REFUND          // Refunded transaction
}