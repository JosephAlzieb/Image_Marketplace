package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.ImageUploadRequest;
import com.marketplace.model.dto.request.ImageSearchRequest;
import com.marketplace.model.dto.response.ImageResponse;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ImageController {
    
    @Autowired
    private ImageService imageService;
    
    /**
     * GET /api/images
     * Get public images with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ImageResponse>> getPublicImages(Pageable pageable) {
        Page<ImageResponse> images = imageService.getPublicImages(pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * POST /api/images/search
     * Search images with filters
     */
    @PostMapping("/search")
    public ResponseEntity<Page<ImageResponse>> searchImages(
            @RequestBody ImageSearchRequest searchRequest,
            Pageable pageable) {
        
        Page<ImageResponse> images = imageService.searchImages(searchRequest, pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * GET /api/images/{imageId}
     * Get image details by ID
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImageById(
            @PathVariable UUID imageId,
            @CurrentUser(required = false) UserPrincipal currentUser) {
        
        UUID viewerId = currentUser != null ? currentUser.getId() : null;
        ImageResponse image = imageService.getImageById(imageId, viewerId);
        return ResponseEntity.ok(image);
    }
    
    /**
     * POST /api/images
     * Upload new image (sellers only)
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> uploadImage(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute ImageUploadRequest request) {
        
        imageService.uploadImage(currentUser.getId(), file, request);
        return ResponseEntity.ok(new ApiResponse(true, "Image uploaded successfully"));
    }
    
    /**
     * PUT /api/images/{imageId}
     * Update image details (owner only)
     */
    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse> updateImage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId,
            @Valid @RequestBody ImageUploadRequest request) {
        
        imageService.updateImage(imageId, currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Image updated successfully"));
    }
    
    /**
     * DELETE /api/images/{imageId}
     * Delete image (owner only)
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId) {
        
        imageService.deleteImage(imageId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Image deleted successfully"));
    }
    
    /**
     * GET /api/images/my-uploads
     * Get current user's uploaded images
     */
    @GetMapping("/my-uploads")
    public ResponseEntity<Page<ImageResponse>> getMyUploadedImages(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<ImageResponse> images = imageService.getUserUploadedImages(currentUser.getId(), pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * GET /api/images/my-collection
     * Get current user's owned images
     */
    @GetMapping("/my-collection")
    public ResponseEntity<Page<ImageResponse>> getMyOwnedImages(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<ImageResponse> images = imageService.getUserOwnedImages(currentUser.getId(), pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * GET /api/images/{imageId}/download
     * Get secure download URL for purchased image
     */
    @GetMapping("/{imageId}/download")
    public ResponseEntity<Object> getDownloadUrl(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId) {
        
        String downloadUrl = imageService.getDownloadUrl(imageId, currentUser.getId());
        return ResponseEntity.ok(new DownloadResponse(downloadUrl, 3600)); // 1 hour expiry
    }
    
    /**
     * GET /api/images/trending
     * Get trending images
     */
    @GetMapping("/trending")
    public ResponseEntity<Page<ImageResponse>> getTrendingImages(Pageable pageable) {
        Page<ImageResponse> images = imageService.getTrendingImages(pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * GET /api/images/featured
     * Get featured images
     */
    @GetMapping("/featured")
    public ResponseEntity<Page<ImageResponse>> getFeaturedImages(Pageable pageable) {
        Page<ImageResponse> images = imageService.getFeaturedImages(pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * GET /api/images/user/{userId}
     * Get public images by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ImageResponse>> getUserImages(
            @PathVariable UUID userId,
            Pageable pageable) {
        
        Page<ImageResponse> images = imageService.getUserPublicImages(userId, pageable);
        return ResponseEntity.ok(images);
    }
    
    /**
     * POST /api/images/{imageId}/like
     * Like/unlike an image
     */
    /* @PostMapping("/{imageId}/like")
    public ResponseEntity<ApiResponse> toggleImageLike(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID imageId) {
        
        boolean liked = imageService.toggleLike(imageId, currentUser.getId());
        String message = liked ? "Image liked" : "Image unliked";
        return ResponseEntity.ok(new ApiResponse(true, message));
    }
    */
    
    // Helper class for download response
    public static class DownloadResponse {
        private String downloadUrl;
        private int expiresInSeconds;
        
        public DownloadResponse(String downloadUrl, int expiresInSeconds) {
            this.downloadUrl = downloadUrl;
            this.expiresInSeconds = expiresInSeconds;
        }
        
        // Getters
        public String getDownloadUrl() { return downloadUrl; }
        public int getExpiresInSeconds() { return expiresInSeconds; }
    }
}