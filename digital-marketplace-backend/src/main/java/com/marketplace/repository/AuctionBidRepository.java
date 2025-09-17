package com.marketplace.repository;

import com.marketplace.model.entity.AuctionBid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionBidRepository extends JpaRepository<AuctionBid, UUID> {

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.image.id = :imageId ORDER BY ab.bidAmount DESC LIMIT 1")
    Optional<AuctionBid> findHighestBidForImage(@Param("imageId") UUID imageId);

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.image.id = :imageId AND ab.isWinningBid = true")
    Optional<AuctionBid> findWinningBidForImage(@Param("imageId") UUID imageId);

    @Modifying
    @Query("UPDATE AuctionBid ab SET ab.isWinningBid = false WHERE ab.image.id = :imageId")
    void markBidsAsNotWinning(@Param("imageId") UUID imageId);

    Page<AuctionBid> findByImageIdOrderByBidAmountDesc(UUID imageId, Pageable pageable);

    Page<AuctionBid> findByBidderIdOrderByBidTimeDesc(UUID bidderId, Pageable pageable);

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.image.id = :imageId AND ab.isAutoBid = true AND ab.maxBidAmount > :currentBid ORDER BY ab.maxBidAmount DESC")
    List<AuctionBid> findAutoBidsForImage(@Param("imageId") UUID imageId, @Param("currentBid") BigDecimal currentBid);

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.image.id = :imageId AND ab.bidder.id != :bidderId")
    List<AuctionBid> findByImageIdAndBidderIdNot(@Param("imageId") UUID imageId, @Param("bidderId") UUID bidderId);
}
