package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.BidRequest;
import com.marketplace.model.dto.response.AuctionBidResponse;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuctionController {
    
    @Autowired
    private AuctionService auctionService;
    
    /**
     * POST /api/auctions/bid
     * Place a bid on an auction
     */
    @PostMapping("/bid")
    public ResponseEntity<AuctionBidResponse> placeBid(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody BidRequest request) {
        
        AuctionBidResponse bid = auctionService.placeBid(currentUser.getId(), request);
        return ResponseEntity.ok(bid);
    }
    
    /**
     * GET /api/auctions/{imageId}/bids
     * Get all bids for an auction image
     */
    @GetMapping("/{imageId}/bids")
    public ResponseEntity<Page<AuctionBidResponse>> getImageBids(
            @PathVariable UUID imageId,
            Pageable pageable) {
        
        Page<AuctionBidResponse> bids = auctionService.getImageBids(imageId, pageable);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * GET /api/auctions/my-bids
     * Get current user's bids
     */
    @GetMapping("/my-bids")
    public ResponseEntity<Page<AuctionBidResponse>> getMyBids(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<AuctionBidResponse> bids = auctionService.getUserBids(currentUser.getId(), pageable);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * GET /api/auctions/active
     * Get all active auctions
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Object>> getActiveAuctions(Pageable pageable) {
        // This would return active auction images
        // Implementation depends on ImageService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * GET /api/auctions/ending-soon
     * Get auctions ending soon (within 24 hours)
     */
    @GetMapping("/ending-soon")
    public ResponseEntity<Page<Object>> getAuctionsEndingSoon(Pageable pageable) {
        // Implementation depends on ImageService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * POST /api/auctions/{imageId}/end
     * End auction manually (admin or owner only)
     */
    @PostMapping("/{imageId}/end")
    public ResponseEntity<ApiResponse> endAuction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId) {
        
        auctionService.endAuction(imageId);
        return ResponseEntity.ok(new ApiResponse(true, "Auction ended successfully"));
    }
}