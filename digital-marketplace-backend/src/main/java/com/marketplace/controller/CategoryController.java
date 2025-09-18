package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.dto.request.CategoryRequest;
import com.marketplace.model.dto.response.CategoryResponse;
import com.marketplace.model.dto.response.ApiResponse;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * GET /api/categories
     * Get all active categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * GET /api/categories/{categoryId}
     * Get category by ID
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }
    
    /**
     * GET /api/categories/tree
     * Get category hierarchy tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }
    
    /**
     * GET /api/categories/{categoryId}/images
     * Get images in category
     */
    @GetMapping("/{categoryId}/images")
    public ResponseEntity<Page<Object>> getCategoryImages(
            @PathVariable UUID categoryId,
            Pageable pageable) {
        
        // This would be implemented in ImageService
        // Page<ImageResponse> images = imageService.getImagesByCategory(categoryId, pageable);
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * POST /api/categories
     * Create new category (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createCategory(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CategoryRequest request) {
        
        categoryService.createCategory(request);
        return ResponseEntity.ok(new ApiResponse(true, "Category created successfully"));
    }
    
    /**
     * PUT /api/categories/{categoryId}
     * Update category (admin only)
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateCategory(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) {
        
        categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(new ApiResponse(true, "Category updated successfully"));
    }
    
    /**
     * DELETE /api/categories/{categoryId}
     * Delete category (admin only)
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategory(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID categoryId) {
        
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
    }
}