package com.marketplace.service;

import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.model.dto.request.CategoryRequest;
import com.marketplace.model.dto.response.CategoryResponse;
import com.marketplace.model.entity.Category;
import com.marketplace.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findAll()
                .stream()
                .filter(Category::getIsActive)
                .toList();

        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return convertToResponse(category);
    }

    public List<CategoryResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findAll()
                .stream()
                .filter(category -> category.getParent() == null && category.getIsActive())
                .collect(Collectors.toList());

        return rootCategories.stream()
                .map(this::convertToResponseWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        updateCategoryFromRequest(category, request);

        if (!StringUtils.hasText(category.getSlug())) {
            category.setSlug(generateSlug(category.getName()));
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        updateCategoryFromRequest(category, request);

        Category updatedCategory = categoryRepository.save(category);
        return convertToResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Soft delete by setting isActive to false
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private void updateCategoryFromRequest(Category category, CategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        category.setIconUrl(request.getIconUrl());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
            category.setParent(parent);
        }
    }

    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setIconUrl(category.getIconUrl());
        response.setIsActive(category.getIsActive());
        response.setSortOrder(category.getSortOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }

        response.setImageCount((long) category.getImages().size());

        return response;
    }

    private CategoryResponse convertToResponseWithChildren(Category category) {
        CategoryResponse response = convertToResponse(category);

        List<CategoryResponse> children = category.getChildren()
                .stream()
                .filter(child -> child.getIsActive())
                .map(this::convertToResponseWithChildren)
                .collect(Collectors.toList());

        response.setChildren(children);
        return response;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
