package com.marketplace.model.enums;

public enum PaymentStatus {
    PENDING,        // Payment initiated but not completed
    PROCESSING,     // Payment being processed
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    CANCELLED,      // Payment cancelled by user
    REFUNDED,       // Payment refunded
    DISPUTED        // Payment disputed/chargeback
}