package com.buildmate.material.service;

import com.buildmate.material.exception.DuplicateResourceException;
import com.buildmate.material.exception.ResourceNotFoundException;
import com.buildmate.material.model.Category;
import com.buildmate.material.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category addCategory(Category category) {

        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Category already exists.");
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category category) {

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existing);
    }

    public void deleteCategory(String id) {

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found.");
        }

        categoryRepository.deleteById(id);
    }
}
