package com.olatech.shopxauthservice.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olatech.shopxauthservice.DTO.Product.CreateProductDto;
import com.olatech.shopxauthservice.DTO.VariantDTO;
import com.olatech.shopxauthservice.Model.Product;
import com.olatech.shopxauthservice.Model.ProductVariant;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.ProductRepository;
import com.olatech.shopxauthservice.Service.LocalFileStorageService;
import com.olatech.shopxauthservice.Service.ProductService;
import com.olatech.shopxauthservice.Service.UserService;
import com.olatech.shopxauthservice.DTO.ProductDTO;
import com.olatech.shopxauthservice.aspect.CheckSubscriptionLimit;
import com.olatech.shopxauthservice.aspect.LimitType;
import com.olatech.shopxauthservice.exceptions.DuplicateSkuException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/stores/{storeId}/articles")
@Validated
@Tag(name = "Articles", description = "Article management APIs")
public class ArticleController {

    @Autowired
    private ProductService articleService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private LocalFileStorageService localFileStorageService;

    @Autowired
    private ProductRepository productRepository;

    // Simple upload method using only local storage
    private String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        log.info("Uploading file using local storage: {}", file.getOriginalFilename());
        return localFileStorageService.uploadFile(file);
    }

    @Operation(
            summary = "Rechercher et filtrer des articles",
            description = "Recherche des articles par nom ou description avec pagination"
    )
    @GetMapping
    public ResponseEntity<Page<Product>> getStoreArticles(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Terme de recherche")
            @RequestParam(required = false) String search,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            Authentication authentication) {

        Users currentUser = userService.getUserByUsername(authentication.getName());
        Store store = articleService.getStoreById(storeId, currentUser);

        // Si un terme de recherche est fourni, utilisez-le pour filtrer les résultats
        Page<Product> articles;
        if (search != null && !search.trim().isEmpty()) {
            articles = productRepository.findByStoreAndNameContainingIgnoreCase(store, search.trim(), pageable);
        } else {
            articles = productRepository.findByStore(store, pageable);
        }

        return ResponseEntity.ok(articles);
    }

    @Operation(
            summary = "Get article by ID",
            description = "Retrieves a specific article by its ID"
    )
    @GetMapping("/{articleId}")
    public ResponseEntity<Product> getArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Product article = articleService.getArticleById(articleId, currentUser);
        return ResponseEntity.ok(article);
    }

    @Operation(
            summary = "Update an article",
            description = "Updates an existing article. Requires store manager permissions."
    )
    @PutMapping("/{articleId}")
    public ResponseEntity<Product> updateArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            @Parameter(description = "Updated article data", required = true)
            @Valid @RequestBody ProductDTO articleDTO,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Product article = articleService.updateArticle(articleId, articleDTO, currentUser);
        return ResponseEntity.ok(article);
    }

    @PatchMapping("/{articleId}")
    @Operation(
            summary = "Update an article",
            description = "Updates certain fields of an existing article. Requires store manager permissions."
    )
    public ResponseEntity<Product> patchArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            @Parameter(description = "Updated article data", required = true)
            @RequestBody Map<String, Object> articleMap,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Product article = articleService.patchArticle(articleId, articleMap, currentUser);
        return ResponseEntity.ok(article);
    }

    @Operation(
            summary = "Delete an article",
            description = "Deletes an article. Requires store manager permissions."
    )
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        articleService.deleteArticle(articleId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Publish an article",
            description = "Publishes an article. Requires store manager permissions."
    )
    @PostMapping("/{articleId}/publish")
    public ResponseEntity<Product> publishArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Product article = articleService.publishProduct(articleId, currentUser);
        return ResponseEntity.ok(article);
    }

    @Operation(
            summary = "Unpublish an article",
            description = "Unpublishes an article. Requires store manager permissions."
    )
    @PostMapping("/{articleId}/unpublish")
    public ResponseEntity<Product> unpublishArticle(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,

            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId,

            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Product article = articleService.unpublishProduct(articleId, currentUser);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/count/active")
    public long getActiveProductsCount(
            @PathVariable Long storeId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        return articleService.getActiveProductsCount(storeId, currentUser);
    }

    @GetMapping("/recent")
    public List<Product> getRecentProducts(@RequestParam int limit) {
        return articleService.getRecentProducts(limit);
    }

    @PostMapping(value = "/variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createVariants(
            @PathVariable Long storeId,
            @RequestParam("productId") Long productId,
            @RequestParam("variant") String variantJson,
            @RequestPart(value = "images[]", required = false) MultipartFile[] images,
            Authentication authentication) {
        try {
            Users currentUser = userService.getUserByUsername(authentication.getName());
            Product product = articleService.getArticleById(productId, currentUser);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            VariantDTO variantDTO = new ObjectMapper().readValue(variantJson, new TypeReference<>() {
            });

            List<String> imageUrls = new ArrayList<>();

            if (images != null) {
                for (MultipartFile image : images) {
                    try {
                        if (!image.isEmpty()) {
                            String imageUrl = uploadFile(image);
                            imageUrls.add(imageUrl);
                        }
                    } catch (IOException e) {
                        log.error("Failed to upload variant image: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to upload image: " + e.getMessage());
                    }
                }
            }

            ProductVariant variant = articleService.createVariant(variantDTO, product.getStore(), productId, variantDTO.getName(), imageUrls, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(variant);
        } catch (Exception e) {
            log.error("Failed to create variants: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create variants: " + e.getMessage());
        }
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long storeId,
            @PathVariable Long variantId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        articleService.deleteVariant(variantId, storeId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v2")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product saved = articleService.createProduct(product);
            return ResponseEntity.ok(saved);
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DuplicateSkuException e) {
            log.error("Duplicate SKU: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/v3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@CheckSubscriptionLimit(type = LimitType.PRODUCT)
    public ResponseEntity<?> createProduct(
            @PathVariable String storeId,
            @RequestPart(value = "data") String productDtoJson,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages
    ) {
        try {
            log.info("Creating product for store: {}", storeId);
            
            // Convert JSON to CreateProductDto
            ObjectMapper mapper = new ObjectMapper();
            CreateProductDto productDto = mapper.readValue(productDtoJson, CreateProductDto.class);

            // Upload product images using local storage
            List<String> productImageUrls = new ArrayList<>();
            if (productImages != null && !productImages.isEmpty()) {
                log.info("Processing {} product images", productImages.size());
                for (MultipartFile image : productImages) {
                    try {
                        if (!image.isEmpty()) {
                            String imageUrl = uploadFile(image);
                            productImageUrls.add(imageUrl);
                            log.info("Successfully uploaded product image: {}", imageUrl);
                        }
                    } catch (IOException e) {
                        log.error("Failed to upload product image: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to upload product image: " + e.getMessage()));
                    }
                }
            }
            productDto.imageUrls = productImageUrls.toArray(new String[0]);

            // Upload variant images if any
            if (productDto.variants != null && variantImages != null && !variantImages.isEmpty()) {
                log.info("Processing {} variant images for {} variants", variantImages.size(), productDto.variants.length );
                int currentImageIndex = 0;
                
                for (VariantDTO variant : productDto.variants) {
                    List<String> variantImageUrls = new ArrayList<>();
                    
                    // Get the number of images for this variant
                    int imageCount = 1; // Default to 1 image per variant
                    
                    if (currentImageIndex < variantImages.size()) {
                        String originalFilename = variantImages.get(currentImageIndex).getOriginalFilename();
                        log.info("Processing variant images for filename: {}", originalFilename);
                        
                        // Try to extract image count from filename (e.g., "2_variant.jpg" means 2 images)
                        try {
                            if (originalFilename != null && originalFilename.contains("_")) {
                                String[] parts = originalFilename.split("_");
                                if (parts.length > 0 && parts[0].matches("\\d+")) {
                                    imageCount = Integer.parseInt(parts[0]);
                                }
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse image count from filename: {}, using default count of 1", originalFilename);
                        }
                    }

                    log.info("Processing {} images for variant: {}", imageCount, variant.getName());
                    
                    // Process images for this variant
                    for (int i = 0; i < imageCount && currentImageIndex < variantImages.size(); i++) {
                        MultipartFile image = variantImages.get(currentImageIndex);
                        log.info("Processing image {}: {}", currentImageIndex, image.getOriginalFilename());
                        
                        try {
                            if (!image.isEmpty()) {
                                String imageUrl = uploadFile(image);
                                variantImageUrls.add(imageUrl);
                                log.info("Successfully uploaded variant image: {}", imageUrl);
                            }
                        } catch (IOException e) {
                            log.error("Failed to upload variant image: {}", e.getMessage());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("error", "Failed to upload variant image: " + e.getMessage()));
                        }
                        currentImageIndex++;
                    }

                    variant.setImages(variantImageUrls);
                    log.info("Set {} images for variant: {}", variantImageUrls.size(), variant.getName());
                }
            }

            Product saved = articleService.createProduct(productDto, Long.parseLong(storeId));
            log.info("Product created successfully with ID: {}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Validation failed: " + e.getMessage()));
        } catch (DuplicateSkuException e) {
            log.error("Duplicate SKU: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Duplicate SKU: " + e.getMessage()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse product data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse product data: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create product: " + e.getMessage()));
        }
    }
}