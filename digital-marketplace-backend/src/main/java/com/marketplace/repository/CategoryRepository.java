package com.marketplace.repository;

import com.marketplace.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(UUID parentId);

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.parent IS NULL")
    List<Category> findRootCategories();

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
