package com.marketplace.repository;

import com.marketplace.model.entity.Image;
import com.marketplace.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID>, JpaSpecificationExecutor<Image> {
    boolean existsByFileHash(String fileHash);

    Page<Image> findByIsAvailableTrueAndIsMatureContentFalse(Pageable pageable);

    Page<Image> findByCurrentOwnerId(UUID userId, Pageable pageable);

    Page<Image> findByUploaderId(UUID userId, Pageable pageable);

    @Query("select (count(t) > 0) from Transaction t where t.image.id = :imageId and t.paymentStatus in (:statuses)")
    boolean hasActiveTransactions(@Param("imageId") UUID imageId,
                                  @Param("statuses") java.util.List<PaymentStatus> statuses);

    default boolean hasActiveTransactions(UUID imageId) {
        return hasActiveTransactions(imageId, java.util.Arrays.asList(PaymentStatus.PENDING, PaymentStatus.PROCESSING));
    }

    @Query("select (count(t) > 0) from Transaction t where t.image.id = :imageId and t.buyer.id = :userId and t.paymentStatus = com.marketplace.model.enums.PaymentStatus.COMPLETED")
    boolean hasUserPurchasedImage(@Param("imageId") UUID imageId, @Param("userId") UUID userId);

    @Query(value = "select i from Image i where i.isAvailable = true and i.isMatureContent = false order by (i.viewCount + (i.downloadCount * 2) + (i.likeCount * 3)) desc",
           countQuery = "select count(i) from Image i where i.isAvailable = true and i.isMatureContent = false")
    Page<Image> findTrendingImages(Pageable pageable);

    Page<Image> findByIsFeaturedTrueAndIsAvailableTrue(Pageable pageable);
}
