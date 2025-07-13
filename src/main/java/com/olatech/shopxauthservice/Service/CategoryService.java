package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Category;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.CategoryRepository;
import com.olatech.shopxauthservice.DTO.CategoryDTO;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreService storeService;

    @Transactional
    public Category createCategory(CategoryDTO categoryDTO, Users user) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setSlug(categoryDTO.getSlug());
        category.setDescription(categoryDTO.getDescription());

        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }


    public Category getCategoryById(Long categoryId, Users user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return category;
    }


    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAllWithHierarchy();
        Map<Long, CategoryDTO> dtoMap = new HashMap<>();
        List<CategoryDTO> roots = new ArrayList<>();

        // Convertir toutes les catégories en DTO
        for (Category category : allCategories) {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setSlug(category.getSlug());
            dto.setDescription(category.getDescription());
            dto.setIsActive(category.isActive());
            dto.setChildren(new ArrayList<>());
            dtoMap.put(dto.getId(), dto);
        }

        // Construire l'arbre
        for (Category category : allCategories) {
            CategoryDTO dto = dtoMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(dto);
            } else {
                CategoryDTO parentDto = dtoMap.get(category.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return roots;
    }
}
