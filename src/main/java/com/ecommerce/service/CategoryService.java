package com.ecommerce.service;

import com.ecommerce.domain.dto.CategoryDTO;
import com.ecommerce.domain.model.Category;
import com.ecommerce.domain.repository.CategoryRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CategoryService {
    
    @Inject
    CategoryRepository categoryRepository;
    
    public Uni<List<Category>> getAllCategories() {
        return categoryRepository.listAll();
    }
    
    public Uni<List<Category>> getRootCategories() {
        return categoryRepository.findRootCategories();
    }
    
    public Uni<List<Category>> getSubcategories(Long parentId) {
        return categoryRepository.findByParent(parentId);
    }
    
    public Uni<Category> getCategory(Long id) {
        return categoryRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Category not found"));
    }
    
    @Transactional
    public Uni<Category> createCategory(CategoryDTO categoryDTO) {
        return Uni.createFrom().item(() -> {
            Category category = new Category();
            updateCategoryFromDto(category, categoryDTO);
            return category;
        }).chain(category -> {
            if (categoryDTO.getParentId() != null) {
                return categoryRepository.findById(categoryDTO.getParentId())
                    .onItem().ifNull().failWith(() -> 
                        new ResourceNotFoundException("Parent category not found"))
                    .map(parent -> {
                        category.setParent(parent);
                        return category;
                    });
            }
            return Uni.createFrom().item(category);
        }).chain(category -> categoryRepository.persist(category));
    }
    
    @Transactional
    public Uni<Category> updateCategory(Long id, CategoryDTO categoryDTO) {
        return categoryRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Category not found"))
            .chain(category -> {
                updateCategoryFromDto(category, categoryDTO);
                
                if (categoryDTO.getParentId() != null && 
                    !categoryDTO.getParentId().equals(
                        category.getParent() != null ? category.getParent().getId() : null)) {
                    return categoryRepository.findById(categoryDTO.getParentId())
                        .onItem().ifNull().failWith(() -> 
                            new ResourceNotFoundException("Parent category not found"))
                        .map(parent -> {
                            category.setParent(parent);
                            return category;
                        });
                }
                return Uni.createFrom().item(category);
            })
            .chain(category -> categoryRepository.persist(category));
    }
    
    @Transactional
    public Uni<Boolean> deleteCategory(Long id) {
        return categoryRepository.hasProducts(id)
            .chain(hasProducts -> {
                if (hasProducts) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Cannot delete category with products"));
                }
                return categoryRepository.findById(id)
                    .onItem().ifNull().failWith(() -> 
                        new ResourceNotFoundException("Category not found"))
                    .chain(category -> {
                        if (!category.getSubcategories().isEmpty()) {
                            return Uni.createFrom().failure(
                                new IllegalStateException(
                                    "Cannot delete category with subcategories"));
                        }
                        return categoryRepository.delete(category)
                            .map(deleted -> deleted > 0);
                    });
            });
    }
    
    private void updateCategoryFromDto(Category category, CategoryDTO dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            category.setActive(dto.getActive());
        }
    }
}