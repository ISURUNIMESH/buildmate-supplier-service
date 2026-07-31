package com.construction.materialservice.service;

import com.construction.materialservice.model.Category;
import com.construction.materialservice.repository.CategoryRepository;
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
            throw new RuntimeException("Category already exists.");
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category category) {

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found."));

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existing);
    }

    public void deleteCategory(String id) {

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found.");
        }

        categoryRepository.deleteById(id);
    }
}