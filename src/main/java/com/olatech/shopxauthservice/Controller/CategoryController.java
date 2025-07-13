package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.CategoryDTO;
import com.olatech.shopxauthservice.Model.Category;
import com.olatech.shopxauthservice.Repository.CategoryRepository;
import com.olatech.shopxauthservice.Service.CategoryImportService;
import com.olatech.shopxauthservice.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryImportService categoryImportService;

    @Autowired
    private CategoryService categoryService;


    @PostMapping("/import")
    public ResponseEntity<?> importCategories(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            categoryImportService.parseCsvAndImport(content);
            return ResponseEntity.ok().body("Categories imported successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error importing categories: " + e.getMessage());
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }


}