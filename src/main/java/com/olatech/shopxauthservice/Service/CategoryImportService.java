package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Category;
import com.olatech.shopxauthservice.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CategoryImportService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public void importCategories(List<CategoryData> categoryDataList) {
        Map<String, Category> categoryCache = new HashMap<>();

        for (CategoryData data : categoryDataList) {
            String[] hierarchy = data.getPath().split(" > ");
            String currentPath = "";
            Category parentCategory = null;

            for (String categoryName : hierarchy) {
                currentPath = currentPath.isEmpty() ? categoryName : currentPath + " > " + categoryName;

                if (!categoryCache.containsKey(currentPath)) {
                    String uniqueSlug = generateUniqueSlug(categoryName);

                    Category category = new Category();
                    category.setName(categoryName);
                    category.setSlug(uniqueSlug);
                    category.setActive(true);
                    category.setCreatedAt(LocalDateTime.now());
                    category.setUpdatedAt(LocalDateTime.now());
                    category.setParent(parentCategory);

                    categoryRepository.save(category);
                    categoryCache.put(currentPath, category);
                }

                parentCategory = categoryCache.get(currentPath);
            }
        }
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        String finalSlug = baseSlug;
        int counter = 1;

        while (categoryRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }

    public static class CategoryData {
        private Long categoryId;
        private String path;

        public CategoryData(Long categoryId, String path) {
            this.categoryId = categoryId;
            this.path = path;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public String getPath() {
            return path;
        }
    }

    @Transactional
    public void parseCsvAndImport(String csvContent) {
        List<CategoryData> categories = new ArrayList<>();

        String[] lines = csvContent.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",", 2);
            if (parts.length != 2) continue;

            try {
                Long categoryId = Long.parseLong(parts[0]);
                String path = parts[1].trim();
                categories.add(new CategoryData(categoryId, path));
            } catch (NumberFormatException e) {
                // Skip header or invalid lines
                continue;
            }
        }

        importCategories(categories);
    }
}