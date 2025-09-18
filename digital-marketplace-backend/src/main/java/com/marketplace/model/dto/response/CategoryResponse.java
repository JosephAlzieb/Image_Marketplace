package com.marketplace.model.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String iconUrl;
    private Boolean isActive;
    private Integer sortOrder;
    private UUID parentId;
    private String parentName;
    private List<CategoryResponse> children = new ArrayList<>();
    private Long imageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CategoryResponse() {}

    public CategoryResponse(UUID id, String name, String description, String slug,
                           String iconUrl, Boolean isActive, Integer sortOrder,
                           UUID parentId, String parentName, Long imageCount,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.parentId = parentId;
        this.parentName = parentName;
        this.imageCount = imageCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<CategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }

    public Long getImageCount() {
        return imageCount;
    }

    public void setImageCount(Long imageCount) {
        this.imageCount = imageCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
