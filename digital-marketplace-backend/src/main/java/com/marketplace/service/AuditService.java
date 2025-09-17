package com.marketplace.service;

import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.Transaction;
import com.marketplace.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.marketplace.model.entity.*;
import org.springframework.scheduling.annotation.Async;

@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Async
    public void logImageUpload(User uploader, Image image) {
        auditLogger.info("IMAGE_UPLOAD: User {} uploaded image {} ({})",
                uploader.getId(), image.getId(), image.getTitle());
    }

    @Async
    public void logImageUpdate(User user, Image image) {
        auditLogger.info("IMAGE_UPDATE: User {} updated image {} ({})",
                user.getId(), image.getId(), image.getTitle());
    }

    @Async
    public void logImageDeletion(User user, Image image) {
        auditLogger.info("IMAGE_DELETE: User {} deleted image {} ({})",
                user.getId(), image.getId(), image.getTitle());
    }

    @Async
    public void logTransactionCompleted(Transaction transaction) {
        auditLogger.info("TRANSACTION_COMPLETED: Transaction {} for image {} by user {}",
                         transaction.getId(), transaction.getImage().getId(), transaction.getBuyer().getId());
//        response.setWatermarkUrl(image.getWatermarkUrl());
//        response.setPreviewUrl(image.getPreviewUrl());
//        response.setDimensions(image.getDimensions());
//        response.setFileFormat(image.getFileFormat());
//        response.setFileSize(image.getFileSize());
//        response.setUploaderId(image.getUploader().getId());
//        response.setUploaderName(image.getUploader().getFullName());
//        response.setCurrentOwnerId(image.getCurrentOwner().getId());
//        response.setCurrentOwnerName(image.getCurrentOwner().getFullName());
//        response.setCategoryId(image.getCategory() != null ? image.getCategory().getId() : null);
//        response.setCategoryName(image.getCategory() != null ? image.getCategory().getName() : null);
//        response.setTags(image.getTags());
//        response.setColorPalette(image.getColorPalette());
//        response.setPrice(image.getPrice());
//        response.setCurrency(image.getCurrency());
//        response.setSaleType(image.getSaleType());
//        response.setLicenseType(image.getLicenseType());
//        response.setViewCount(image.getViewCount());
//        response.setDownloadCount(image.getDownloadCount());
//        response.setLikeCount(image.getLikeCount());
//        response.setAverageRating(image.getAverageRating());
//        response.setRatingCount(image.getRatingCount());
//        response.setIsFeatured(image.getIsFeatured());
//        response.setIsMatureContent(image.getIsMatureContent());
//        response.setCreatedAt(image.getCreatedAt());
//        response.setUpdatedAt(image.getUpdatedAt());
//
//        // Auction specific fields
//        if (image.getSaleType() == SaleType.AUCTION) {
//            response.setAuctionStartTime(image.getAuctionStartTime());
//            response.setAuctionEndTime(image.getAuctionEndTime());
//            response.setStartingBid(image.getStartingBid());
//            response.setCurrentBid(image.getCurrentBid());
//            response.setBidCount(image.getBidCount());
//            response.setBuyNowPrice(image.getBuyNowPrice());
//            response.setIsAuctionActive(image.isAuctionActive());
//        }
//
//        return response;
    }

    public void logTransactionRefunded(Transaction transaction, User requester, String reason) {
        auditLogger.info("Refund audit: transaction={}, requester={}, reason={}",
                transaction.getId(), requester.getId(), reason);
    }
}