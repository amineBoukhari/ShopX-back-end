package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.Product.ProductTypeDTO;
import com.olatech.shopxauthservice.Service.Product.ProductTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Product Type", description = "Product Type API")
public class ProductTypeController {
    
    @Autowired
    private ProductTypeService productTypeService;

    // Global endpoint (no store ID needed)
    @GetMapping("/api/product-types")
    @Operation(summary = "Get all product types")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes() {
        try {
            List<ProductTypeDTO> productTypes = productTypeService.getAllProductTypes();
            return ResponseEntity.ok(productTypes);
        } catch (Exception e) {
            System.out.println("Error getting product types: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Store-specific endpoint (what your frontend is calling)
    @GetMapping("/api/{storeId}/product-type/all")
    @Operation(summary = "Get all product types for store")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypesForStore(@PathVariable Long storeId) {
        try {
            // Product types are global, so we can ignore storeId
            List<ProductTypeDTO> productTypes = productTypeService.getAllProductTypes();
            return ResponseEntity.ok(productTypes);
        } catch (Exception e) {
            System.out.println("Error getting product types for store: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/product-types/{id}")
    public ResponseEntity<ProductTypeDTO> getProductTypeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productTypeService.getProductTypeById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}