package com.marketplace.service;


import com.marketplace.model.entity.AuctionBid;
import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.Notification;
import com.marketplace.model.entity.User;
import com.marketplace.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    /**
     * Send seller notification
     */
    @Async
    public void sendSellerNotification(User seller, String title, String message) {
        createNotification(seller, title, message, "SALE", null, null);
    }

    /**
     * Send bid outbid notification
     */
    @Async
    public void sendBidOutbidNotification(User bidder, Image image, AuctionBid newBid) {
        String title = "You've been outbid!";
        String message = String.format("Your bid on '%s' has been outbid. Current highest bid: %s",
                image.getTitle(), newBid.getBidAmount());

        Map<String, Object> data = new HashMap<>();
        data.put("imageId", image.getId());
        data.put("currentBid", newBid.getBidAmount());
        data.put("auctionEndTime", image.getAuctionEndTime());

        createNotification(bidder, title, message, "AUCTION_OUTBID", data,
                "/auctions/" + image.getId());
    }

    /**
     * Send auto bid notification
     */
    @Async
    public void sendAutoBidNotification(User bidder, Image image, AuctionBid autoBid) {
        String title = "Automatic bid placed";
        String message = String.format("Your automatic bid of %s has been placed on '%s'",
                autoBid.getBidAmount(), image.getTitle());

        Map<String, Object> data = new HashMap<>();
        data.put("imageId", image.getId());
        data.put("bidAmount", autoBid.getBidAmount());

        createNotification(bidder, title, message, "AUTO_BID", data,
                "/auctions/" + image.getId());
    }

    /**
     * Send auction extension notification
     */
    @Async
    public void sendAuctionExtensionNotification(Image image, LocalDateTime newEndTime) {
        // This would typically send to all bidders - implementation depends on requirements
        logger.info("Auction extended for image {} until {}", image.getId(), newEndTime);
    }

    /**
     * Get user's notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, null);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.marketplace.exception.ResourceNotFoundException(
                        "Notification", "id", notificationId));

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new com.marketplace.exception.UnauthorizedException(
                    "You don't have permission to access this notification");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Mark all user notifications as read
     */
    public void markAllAsRead(UUID userId) {
        userService.validateUserAction(userId, null);
        notificationRepository.markAllAsReadForUser(userId);
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        userService.validateUserAction(userId, null);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Private helper methods

    private void createNotification(User user, String title, String message, String type,
                                    Map<String, Object> data, String actionUrl) {
        try {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setData(data);
            notification.setActionUrl(actionUrl);
            notification.setIsRead(false);

            notificationRepository.save(notification);

            logger.info("Notification sent to user {}: {}", user.getId(), title);

        } catch (Exception e) {
            logger.error("Failed to create notification for user {}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }
}