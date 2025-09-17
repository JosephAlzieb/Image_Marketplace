package com.marketplace.service;

import com.marketplace.exception.BadRequestException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.model.dto.request.BidRequest;
import com.marketplace.model.dto.request.PurchaseRequest;
import com.marketplace.model.dto.response.AuctionBidResponse;
import com.marketplace.model.entity.AuctionBid;
import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.SaleType;
import com.marketplace.model.enums.UserRole;
import com.marketplace.repository.AuctionBidRepository;
import com.marketplace.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    @Autowired
    private AuctionBidRepository bidRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    private static final BigDecimal MIN_BID_INCREMENT = new BigDecimal("1.00");
    
    /**
     * Place a bid on an auction
     */
    public AuctionBidResponse placeBid(UUID bidderId, BidRequest request) {
        logger.info("User {} placing bid of {} on image {}", 
                   bidderId, request.getBidAmount(), request.getImageId());
        
        // Validate bidder
        User bidder = userService.getUserById(bidderId);
        userService.validateUserAction(bidderId, UserRole.BUYER);
        
        // Get and validate image
        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", request.getImageId()));
        
        validateAuctionBid(bidder, image, request.getBidAmount());
        
        // Get current highest bid
        AuctionBid currentHighestBid = bidRepository.findHighestBidForImage(request.getImageId())
                .orElse(null);
        
        // Check if this bid is higher than current highest bid
        BigDecimal currentHighest = currentHighestBid != null ? 
                currentHighestBid.getBidAmount() : image.getStartingBid();
        
        if (request.getBidAmount().compareTo(currentHighest.add(MIN_BID_INCREMENT)) < 0) {
            throw new BadRequestException(
                String.format("Bid must be at least %s higher than current bid of %s", 
                            MIN_BID_INCREMENT, currentHighest));
        }
        
        // Mark previous bids as not winning
        if (currentHighestBid != null) {
            bidRepository.markBidsAsNotWinning(request.getImageId());
        }
        
        // Create new bid
        AuctionBid bid = new AuctionBid();
        bid.setImage(image);
        bid.setBidder(bidder);
        bid.setBidAmount(request.getBidAmount());
        bid.setMaxBidAmount(request.getMaxBidAmount());
        bid.setBidTime(LocalDateTime.now());
        bid.setIsActive(true);
        bid.setIsWinningBid(true);
        
        AuctionBid savedBid = bidRepository.save(bid);
        
        // Update image current bid and bid count
        image.setCurrentBid(request.getBidAmount());
        image.setBidCount(image.getBidCount() + 1);
        imageRepository.save(image);
        
        // Send notifications
        sendBidNotifications(image, bidder, savedBid, currentHighestBid);
        
        // Check for automatic bidding
        processAutomaticBidding(image, savedBid);
        
        logger.info("Successfully placed bid {} on image {}", savedBid.getId(), request.getImageId());
        
        return mapBidToResponse(savedBid);
    }
    
    /**
     * Get auction bids for an image
     */
    @Transactional(readOnly = true)
    public Page<AuctionBidResponse> getImageBids(UUID imageId, Pageable pageable) {
        // Validate image exists and is auction
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        if (image.getSaleType() != SaleType.AUCTION) {
            throw new BadRequestException("Image is not an auction item");
        }
        
        Page<AuctionBid> bids = bidRepository.findByImageIdOrderByBidAmountDesc(imageId, pageable);
        return bids.map(this::mapBidToResponse);
    }
    
    /**
     * Get user's auction bids
     */
    @Transactional(readOnly = true)
    public Page<AuctionBidResponse> getUserBids(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, null);
        
        Page<AuctionBid> bids = bidRepository.findByBidderIdOrderByBidTimeDesc(userId, pageable);
        return bids.map(this::mapBidToResponse);
    }
    
    /**
     * End auction and process winner
     */
    public void endAuction(UUID imageId) {
        logger.info("Ending auction for image {}", imageId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        if (image.getSaleType() != SaleType.AUCTION) {
            throw new BadRequestException("Image is not an auction item");
        }
        
        if (!image.isAuctionEnded()) {
            throw new BadRequestException("Auction has not ended yet");
        }
        
        // Get winning bid
        AuctionBid winningBid = bidRepository.findWinningBidForImage(imageId)
                .orElse(null);
        
        if (winningBid == null) {
            logger.info("No bids found for auction {}", imageId);
            handleAuctionNoBids(image);
            return;
        }
        
        // Check if reserve price is met
        if (image.getReservePrice() != null && 
            winningBid.getBidAmount().compareTo(image.getReservePrice()) < 0) {
            logger.info("Reserve price not met for auction {}", imageId);
            handleAuctionReserveNotMet(image, winningBid);
            return;
        }
        
        // Process winning bid as purchase
        try {
            PurchaseRequest purchaseRequest = new PurchaseRequest();
            purchaseRequest.setImageId(imageId);
            
            transactionService.initiatePurchase(winningBid.getBidder().getId(), purchaseRequest);
            
            // Mark image as sold
            image.setIsAvailable(false);
            imageRepository.save(image);
            
            // Send winner notification
            // Send winner notification
            emailService.sendAuctionWinnerEmail(winningBid.getBidder(), image, winningBid);

            // Send notifications to other bidders
            sendAuctionEndNotifications(image, winningBid);

            logger.info("Successfully processed auction winner for image {}", imageId);

        } catch (Exception e) {
            logger.error("Failed to process auction winner for image {}: {}", imageId, e.getMessage(), e);
            handleAuctionProcessingError(image, winningBid, e.getMessage());
        }
    }

    /**
     * Process automatic bidding
     */
    @Async
    public void processAutomaticBidding(Image image, AuctionBid newBid) {
        logger.info("Processing automatic bidding for image {}", image.getId());

        // Find all active bids with max bid amounts higher than current bid
        List<AuctionBid> autobids = bidRepository.findAutoBidsForImage(
                image.getId(), newBid.getBidAmount());

        for (AuctionBid autobid : autobids) {
            if (autobid.canIncreaseTo(newBid.getBidAmount().add(MIN_BID_INCREMENT))) {
                // Place automatic counter-bid
                BigDecimal counterBid = newBid.getBidAmount().add(MIN_BID_INCREMENT);

                AuctionBid autoBid = new AuctionBid();
                autoBid.setImage(image);
                autoBid.setBidder(autobid.getBidder());
                autoBid.setBidAmount(counterBid);
                autoBid.setMaxBidAmount(autobid.getMaxBidAmount());
                autoBid.setBidTime(LocalDateTime.now());
                autoBid.setIsActive(true);
                autoBid.setIsWinningBid(true);

                bidRepository.save(autoBid);

                // Update image
                image.setCurrentBid(counterBid);
                image.setBidCount(image.getBidCount() + 1);
                imageRepository.save(image);

                // Mark other bids as not winning
                bidRepository.markBidsAsNotWinning(image.getId());
                autoBid.setIsWinningBid(true);
                bidRepository.save(autoBid);

                // Send notification
                notificationService.sendAutoBidNotification(autobid.getBidder(), image, autoBid);

                logger.info("Placed automatic bid {} for user {} on image {}",
                        autoBid.getId(), autobid.getBidder().getId(), image.getId());
                break; // Only one auto-bid per trigger
            }
        }
    }

    /**
     * Extend auction if bid placed in last minutes (sniping protection)
     */
    public void checkAuctionExtension(UUID imageId, AuctionBid newBid) {
        Image image = imageRepository.findById(imageId).orElse(null);
        if (image == null || image.getAuctionEndTime() == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = image.getAuctionEndTime();

        // If bid placed in last 5 minutes, extend auction by 5 minutes
        if (now.isAfter(endTime.minusMinutes(5)) && now.isBefore(endTime)) {
            LocalDateTime newEndTime = now.plusMinutes(5);
            image.setAuctionEndTime(newEndTime);
            imageRepository.save(image);

            logger.info("Extended auction for image {} to {}", imageId, newEndTime);

            // Notify bidders about extension
            notificationService.sendAuctionExtensionNotification(image, newEndTime);
        }
    }

    // Private helper methods

    private void validateAuctionBid(User bidder, Image image, BigDecimal bidAmount) {
        // Check if image is auction
        if (image.getSaleType() != SaleType.AUCTION) {
            throw new BadRequestException("Image is not an auction item");
        }

        // Check if auction is active
        if (!image.isAuctionActive()) {
            throw new BadRequestException("Auction is not active");
        }

        // Check if bidder is not the owner
        if (image.getCurrentOwner().getId().equals(bidder.getId())) {
            throw new BadRequestException("You cannot bid on your own auction");
        }

        // Check minimum bid amount
        if (bidAmount.compareTo(image.getStartingBid()) < 0) {
            throw new BadRequestException(
                    String.format("Bid must be at least %s", image.getStartingBid()));
        }

        // Check bidder status
        if (bidder.getStatus() != com.marketplace.model.enums.UserStatus.ACTIVE) {
            throw new BadRequestException("Your account is not active");
        }
    }

    private void sendBidNotifications(Image image, User bidder, AuctionBid newBid, AuctionBid previousBid) {
        // Notify seller
        notificationService.sendSellerNotification(
                image.getCurrentOwner(),
                "New Bid Received",
                String.format("New bid of %s placed on your auction '%s'",
                        newBid.getBidAmount(), image.getTitle())
        );

        // Notify previous highest bidder if exists
        if (previousBid != null && !previousBid.getBidder().getId().equals(bidder.getId())) {
            notificationService.sendBidOutbidNotification(
                    previousBid.getBidder(), image, newBid
            );
        }

        // Check for auction extension
        checkAuctionExtension(image.getId(), newBid);
    }

    private void handleAuctionNoBids(Image image) {
        // Mark auction as ended without sale
        image.setIsAvailable(false);
        imageRepository.save(image);

        // Notify seller
        emailService.sendAuctionNoBidsEmail(image.getCurrentOwner(), image);

        logger.info("Auction for image {} ended with no bids", image.getId());
    }

    private void handleAuctionReserveNotMet(Image image, AuctionBid winningBid) {
        // Mark auction as ended without sale
        image.setIsAvailable(false);
        imageRepository.save(image);

        // Notify seller and highest bidder
        emailService.sendAuctionReserveNotMetEmail(image.getCurrentOwner(), image, winningBid);
        emailService.sendAuctionLostEmail(winningBid.getBidder(), image, winningBid, "Reserve price not met");

        logger.info("Auction for image {} ended - reserve price not met", image.getId());
    }

    private void handleAuctionProcessingError(Image image, AuctionBid winningBid, String error) {
        // Send error notifications
        emailService.sendAuctionErrorEmail(image.getCurrentOwner(), image, error);
        emailService.sendAuctionErrorEmail(winningBid.getBidder(), image, error);

        logger.error("Auction processing error for image {}: {}", image.getId(), error);
    }

    private void sendAuctionEndNotifications(Image image, AuctionBid winningBid) {
        // Get all bidders except the winner
        List<AuctionBid> allBids = bidRepository.findByImageIdAndBidderIdNot(
                image.getId(), winningBid.getBidder().getId());

        for (AuctionBid bid : allBids) {
            emailService.sendAuctionLostEmail(
                    bid.getBidder(), image, winningBid, "Another bidder won");
        }
    }

    private AuctionBidResponse mapBidToResponse(AuctionBid bid) {
        AuctionBidResponse response = new AuctionBidResponse();
        response.setId(bid.getId());
        response.setImageId(bid.getImage().getId());
        response.setImageTitle(bid.getImage().getTitle());
        response.setImageThumbnailUrl(bid.getImage().getThumbnailUrl());
        response.setBidderId(bid.getBidder().getId());
        response.setBidderName(bid.getBidder().getFullName());
        response.setBidAmount(bid.getBidAmount());
        response.setMaxBidAmount(bid.getMaxBidAmount());
        response.setBidTime(bid.getBidTime());
        response.setIsActive(bid.getIsActive());
        response.setIsWinningBid(bid.getIsWinningBid());
        response.setIsAutoBid(bid.isAutoBid());
        return response;
    }
}