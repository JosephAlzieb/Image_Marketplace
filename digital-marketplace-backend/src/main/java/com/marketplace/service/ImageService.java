package com.marketplace.service;

import com.marketplace.exception.BadRequestException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedException;
import com.marketplace.model.dto.request.ImageUploadRequest;
import com.marketplace.model.dto.request.ImageSearchRequest;
import com.marketplace.model.dto.response.ImageResponse;
import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.User;
import com.marketplace.model.entity.Category;
import com.marketplace.model.enums.SaleType;
import com.marketplace.model.enums.UserRole;
import com.marketplace.repository.ImageRepository;
import com.marketplace.repository.CategoryRepository;
import com.marketplace.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private AuditService auditService;
    
    @Value("${marketplace.max-image-size:52428800}") // 50MB default
    private long maxImageSize;
    
    @Value("${marketplace.allowed-image-types:jpg,jpeg,png,webp,tiff}")
    private String allowedImageTypes;
    
    /**
     * Upload and process new image
     */
    public Image uploadImage(UUID uploaderId, MultipartFile file, ImageUploadRequest request) {
        logger.info("Uploading image for user {}: {}", uploaderId, request.getTitle());
        
        // Validate user permissions
        userService.validateUserAction(uploaderId, UserRole.SELLER);
        User uploader = userService.getUserById(uploaderId);
        
        // Validate file
        validateImageFile(file);
        
        // Check for duplicate content
        String fileHash = ImageUtils.calculateFileHash(file);
        if (imageRepository.existsByFileHash(fileHash)) {
            throw new BadRequestException("This image has already been uploaded");
        }
        
        try {
            // Upload files to storage
            String originalFileUrl = fileStorageService.uploadFile(file, "images/originals/");
            String thumbnailUrl = fileStorageService.generateThumbnail(file, "images/thumbnails/");
            String watermarkUrl = fileStorageService.generateWatermark(file, "images/watermarks/");
            String previewUrl = fileStorageService.generatePreview(file, "images/previews/");
            
            // Extract image metadata
            Map<String, Object> dimensions = ImageUtils.getImageDimensions(file);
            Map<String, Object> metadata = ImageUtils.extractMetadata(file);
            List<String> colorPalette = ImageUtils.extractColorPalette(file);
            
            // Get category if specified
            Category category = null;
            if (request.getCategoryId() != null) {
                category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            }
            
            // Create image entity
            Image image = new Image();
            image.setTitle(request.getTitle().trim());
            image.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            image.setFileUrl(originalFileUrl);
            image.setThumbnailUrl(thumbnailUrl);
            image.setWatermarkUrl(watermarkUrl);
            image.setPreviewUrl(previewUrl);
            image.setFileSize(file.getSize());
            image.setFileHash(fileHash);
            image.setDimensions(dimensions);
            image.setFileFormat(ImageUtils.getFileExtension(file.getOriginalFilename()));
            image.setUploader(uploader);
            image.setCurrentOwner(uploader);
            image.setCategory(category);
            image.setTags(request.getTags());
            image.setMetadata(metadata);
            image.setColorPalette(colorPalette);
            image.setPrice(request.getPrice());
            image.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
            image.setSaleType(request.getSaleType() != null ? request.getSaleType() : SaleType.FIXED);
            image.setLicenseType(request.getLicenseType());
            image.setIsAvailable(true);
            image.setIsMatureContent(request.getIsMatureContent() != null ? request.getIsMatureContent() : false);
            
            // Set auction specific fields if needed
            if (image.getSaleType() == SaleType.AUCTION) {
                if (request.getAuctionStartTime() != null) {
                    image.setAuctionStartTime(request.getAuctionStartTime());
                } else {
                    image.setAuctionStartTime(LocalDateTime.now());
                }
                image.setAuctionEndTime(request.getAuctionEndTime());
                image.setStartingBid(request.getStartingBid());
                image.setReservePrice(request.getReservePrice());
                image.setBuyNowPrice(request.getBuyNowPrice());
                image.setCurrentBid(request.getStartingBid());
            }
            
            // Generate SEO fields
            image.setSeoTitle(generateSeoTitle(image));
            image.setSeoDescription(generateSeoDescription(image));
            image.setAltText(generateAltText(image));
            
            Image savedImage = imageRepository.save(image);
            
            // Create audit log
            auditService.logImageUpload(uploader, savedImage);
            
            logger.info("Successfully uploaded image with ID: {}", savedImage.getId());
            return savedImage;
            
        } catch (Exception e) {
            logger.error("Failed to upload image for user {}: {}", uploaderId, e.getMessage(), e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * Get paginated public images
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> getPublicImages(Pageable pageable) {
        Page<Image> images = imageRepository.findByIsAvailableTrueAndIsMatureContentFalse(pageable);
        return images.map(this::mapToResponse);
    }
    
    /**
     * Get image by ID with view count increment
     */
    @Transactional
    public ImageResponse getImageById(UUID imageId, UUID viewerId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        // Increment view count only if not viewed by owner
        if (viewerId != null && !image.getCurrentOwner().getId().equals(viewerId)) {
            image.incrementViewCount();
            imageRepository.save(image);
        }
        
        return mapToResponse(image);
    }
    
    /**
     * Search images with filters
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> searchImages(ImageSearchRequest searchRequest, Pageable pageable) {
        Specification<Image> spec = createSearchSpecification(searchRequest);
        Page<Image> images = imageRepository.findAll(spec, pageable);
        return images.map(this::mapToResponse);
    }
    
    /**
     * Get user's owned images
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> getUserOwnedImages(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, null); // Just check if user exists and is active
        Page<Image> images = imageRepository.findByCurrentOwnerId(userId, pageable);
        return images.map(this::mapToResponse);
    }
    
    /**
     * Get user's uploaded images
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> getUserUploadedImages(UUID userId, Pageable pageable) {
        userService.validateUserAction(userId, null);
        Page<Image> images = imageRepository.findByUploaderId(userId, pageable);
        return images.map(this::mapToResponse);
    }
    
    /**
     * Update image details
     */
    public Image updateImage(UUID imageId, UUID userId, ImageUploadRequest updateRequest) {
        logger.info("Updating image {} by user {}", imageId, userId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        // Check ownership
        if (!image.getCurrentOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this image");
        }
        
        // Update allowed fields
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().trim().isEmpty()) {
            image.setTitle(updateRequest.getTitle().trim());
        }
        
        if (updateRequest.getDescription() != null) {
            image.setDescription(updateRequest.getDescription().trim());
        }
        
        if (updateRequest.getPrice() != null && updateRequest.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            image.setPrice(updateRequest.getPrice());
        }
        
        if (updateRequest.getTags() != null) {
            image.setTags(updateRequest.getTags());
        }
        
        if (updateRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", updateRequest.getCategoryId()));
            image.setCategory(category);
        }
        
        // Update SEO fields
        image.setSeoTitle(generateSeoTitle(image));
        image.setSeoDescription(generateSeoDescription(image));
        
        Image updatedImage = imageRepository.save(image);
        
        // Create audit log
        auditService.logImageUpdate(userService.getUserById(userId), updatedImage);
        
        logger.info("Successfully updated image {}", imageId);
        return updatedImage;
    }
    
    /**
     * Delete image (soft delete by making unavailable)
     */
    public void deleteImage(UUID imageId, UUID userId) {
        logger.info("Deleting image {} by user {}", imageId, userId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        User user = userService.getUserById(userId);
        
        // Check permissions (owner or admin)
        if (!image.getCurrentOwner().getId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to delete this image");
        }
        
        // Check if image has active transactions
        if (imageRepository.hasActiveTransactions(imageId)) {
            throw new BadRequestException("Cannot delete image with active transactions");
        }
        
        // Soft delete
        image.setIsAvailable(false);
        imageRepository.save(image);
        
        // Create audit log
        auditService.logImageDeletion(user, image);
        
        logger.info("Successfully deleted image {}", imageId);
    }
    
    /**
     * Get download URL for purchased image
     */
    @Transactional(readOnly = true)
    public String getDownloadUrl(UUID imageId, UUID userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        User user = userService.getUserById(userId);
        
        // Check if user owns the image or has purchased it
        boolean canDownload = image.getCurrentOwner().getId().equals(userId) ||
                             imageRepository.hasUserPurchasedImage(imageId, userId) ||
                             user.getRole() == UserRole.ADMIN;
        
        if (!canDownload) {
            throw new UnauthorizedException("You don't have permission to download this image");
        }
        
        // Increment download count
        image.incrementDownloadCount();
        imageRepository.save(image);
        
        // Generate secure download URL
        return fileStorageService.generateSecureDownloadUrl(image.getFileUrl());
    }
    
    /**
     * Feature/unfeature image (admin only)
     */
    public void toggleImageFeatured(UUID imageId, UUID adminId, boolean featured) {
        userService.validateUserAction(adminId, UserRole.ADMIN);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        
        image.setIsFeatured(featured);
        imageRepository.save(image);
        
        logger.info("Image {} {} by admin {}", imageId, featured ? "featured" : "unfeatured", adminId);
    }
    
    /**
     * Get trending images
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> getTrendingImages(Pageable pageable) {
        Page<Image> images = imageRepository.findTrendingImages(pageable);
        return images.map(this::mapToResponse);
    }
    
    /**
     * Get featured images
     */
    @Transactional(readOnly = true)
    public Page<ImageResponse> getFeaturedImages(Pageable pageable) {
        Page<Image> images = imageRepository.findByIsFeaturedTrueAndIsAvailableTrue(pageable);
        return images.map(this::mapToResponse);
    }
    
    // Private helper methods
    
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        
        if (file.getSize() > maxImageSize) {
            throw new BadRequestException(
                String.format("File size exceeds maximum limit of %d bytes", maxImageSize)
            );
        }
        
        String fileExtension = ImageUtils.getFileExtension(file.getOriginalFilename());
        List<String> allowedTypes = Arrays.asList(allowedImageTypes.split(","));
        
        if (!allowedTypes.contains(fileExtension.toLowerCase())) {
            throw new BadRequestException(
                String.format("Invalid file type. Allowed types: %s", allowedImageTypes)
            );
        }
        
        // Validate actual image content (not just extension)
        if (!ImageUtils.isValidImage(file)) {
            throw new BadRequestException("File is not a valid image");
        }
    }
    
    private Specification<Image> createSearchSpecification(ImageSearchRequest request) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Always show only available images
            predicates.add(cb.isTrue(root.get("isAvailable")));
            
            // Text search
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                String searchTerm = "%" + request.getQuery().toLowerCase() + "%";
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("title")), searchTerm),
                        cb.like(cb.lower(root.get("description")), searchTerm)
                    )
                );
            }
            
            // Category filter
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            
            // Price range filter
            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }
            
            // Sale type filter
            if (request.getSaleType() != null) {
                predicates.add(cb.equal(root.get("saleType"), request.getSaleType()));
            }
            
            // License type filter
            if (request.getLicenseType() != null) {
                predicates.add(cb.equal(root.get("licenseType"), request.getLicenseType()));
            }
            
            // Mature content filter
            if (request.getIncludeMatureContent() == null || !request.getIncludeMatureContent()) {
                predicates.add(cb.isFalse(root.get("isMatureContent")));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    private String generateSeoTitle(Image image) {
        return image.getTitle() + " - Digital Image | Marketplace";
    }
    
    private String generateSeoDescription(Image image) {
        String desc = image.getDescription() != null ? image.getDescription() : image.getTitle();
        if (desc.length() > 160) {
            desc = desc.substring(0, 157) + "...";
        }
        return desc;
    }
    
    private String generateAltText(Image image) {
        return image.getTitle() + " - " + (image.getDescription() != null ? image.getDescription() : "Digital artwork");
    }
    
    private ImageResponse mapToResponse(Image image) {
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setTitle(image.getTitle());
        response.setDescription(image.getDescription());
        response.setThumbnailUrl(image.getThumbnailUrl());
        response.setWatermarkUrl(image.getWatermarkUrl());
        response.setPreviewUrl(image.getPreviewUrl());
        response.setDimensions(image.getDimensions());
        response.setFileFormat(image.getFileFormat());
        response.setFileSize(image.getFileSize());
        response.setUploaderId(image.getUploader().getId());
        response.setUploaderName(image.getUploader().getFullName());
        response.setCurrentOwnerId(image.getCurrentOwner().getId());
        response.setCurrentOwnerName(image.getCurrentOwner().getFullName());
        response.setCategoryId(image.getCategory() != null ? image.getCategory().getId() : null);
        response.setCategoryName(image.getCategory() != null ? image.getCategory().getName() : null);
        response.setTags(image.getTags());
        response.setColorPalette(image.getColorPalette());
        response.setPrice(image.getPrice());
        response.setCurrency(image.getCurrency());
        response.setSaleType(image.getSaleType());
        response.setLicenseType(image.getLicenseType());
        response.setViewCount(image.getViewCount());
        response.setDownloadCount(image.getDownloadCount());
        response.setLikeCount(image.getLikeCount());
        response.setAverageRating(image.getAverageRating());
        response.setRatingCount(image.getRatingCount());
        response.setIsFeatured(image.getIsFeatured());
        response.setIsMatureContent(image.getIsMatureContent());
        response.setCreatedAt(image.getCreatedAt());
        response.setUpdatedAt(image.getUpdatedAt());

        // Auction specific fields
        if (image.getSaleType() == SaleType.AUCTION) {
            response.setAuctionStartTime(image.getAuctionStartTime());
            response.setAuctionEndTime(image.getAuctionEndTime());
            response.setStartingBid(image.getStartingBid());
            response.setCurrentBid(image.getCurrentBid());
            response.setBidCount(image.getBidCount());
            response.setBuyNowPrice(image.getBuyNowPrice());
            response.setIsAuctionActive(image.isAuctionActive());
        }

        return response;
    }
}