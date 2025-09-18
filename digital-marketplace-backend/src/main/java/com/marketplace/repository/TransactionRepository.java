package com.marketplace.repository;

import com.marketplace.model.entity.Transaction;
import com.marketplace.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByPaymentIntentId(String paymentIntentId);

    Page<Transaction> findByBuyerIdAndPaymentStatusOrderByCreatedAtDesc(UUID buyerId, PaymentStatus status, Pageable pageable);

    Page<Transaction> findBySellerIdAndPaymentStatusOrderByCreatedAtDesc(UUID sellerId, PaymentStatus status, Pageable pageable);

    // Seller analytics
    @Query("select coalesce(sum(t.grossAmount), 0) from Transaction t where t.seller.id = ?1 and t.createdAt between ?2 and ?3 and t.paymentStatus = com.marketplace.model.enums.PaymentStatus.COMPLETED")
    BigDecimal getTotalSalesForSeller(UUID sellerId, LocalDateTime from, LocalDateTime to);

    @Query("select coalesce(sum(t.netToSeller), 0) from Transaction t where t.seller.id = ?1 and t.createdAt between ?2 and ?3 and t.paymentStatus = com.marketplace.model.enums.PaymentStatus.COMPLETED")
    BigDecimal getTotalEarningsForSeller(UUID sellerId, LocalDateTime from, LocalDateTime to);

    @Query("select coalesce(sum(t.platformCommission), 0) from Transaction t where t.seller.id = ?1 and t.createdAt between ?2 and ?3 and t.paymentStatus = com.marketplace.model.enums.PaymentStatus.COMPLETED")
    BigDecimal getTotalCommissionForSeller(UUID sellerId, LocalDateTime from, LocalDateTime to);

    // Additional methods for AdminService
    long countByCreatedAtAfter(LocalDateTime date);

    long countByBuyerId(UUID buyerId);

    long countBySellerId(UUID sellerId);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :fromDate AND :toDate")
    Page<Transaction> findByDateRange(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
