package com.marketplace.service;

import com.marketplace.model.entity.Transaction;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public String createPaymentIntent(Transaction transaction) {
        // TODO: Stripe-Integration
        return "pi_" + System.currentTimeMillis();
    }

    public boolean verifyPayment(String paymentIntentId) {
        // TODO: Stripe-Integration
        return true;
    }

    public String processRefund(String paymentIntentId, java.math.BigDecimal amount, String reason) {
        // TODO: Stripe-Integration
        return "re_" + System.currentTimeMillis();
    }
}
