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
import com.olatech.shopxauthservice.Service.GCPStorageService;
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
    private  ProductService articleService;
    @Autowired
    private  UserService userService;

    @Autowired
    private GCPStorageService gcpStorageService;

    @Autowired
    private ProductRepository productRepository;







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
                            String imageUrl = gcpStorageService.uploadFile(image);
                            imageUrls.add(imageUrl);
                        }
                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to upload image: " + e.getMessage());
                    }
                }
            }


            ProductVariant variant = articleService.createVariant(variantDTO, product.getStore(), productId, variantDTO.getName(), imageUrls, currentUser);



            return ResponseEntity.status(HttpStatus.CREATED).body(variant);
        } catch (Exception e) {
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
            return ResponseEntity.badRequest().build();
        } catch (DuplicateSkuException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/v3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CheckSubscriptionLimit(type = LimitType.PRODUCT)
    public ResponseEntity<Product> createProduct(
            @PathVariable String storeId,
            @RequestPart(value = "data") String productDtoJson,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages
    ) {
        try {
            // Convertir le JSON en CreateProductDto
            ObjectMapper mapper = new ObjectMapper();
            CreateProductDto productDto = mapper.readValue(productDtoJson, CreateProductDto.class);

            // Upload product images
            List<String> productImageUrls = new ArrayList<>();
            if (productImages != null) {
                for (MultipartFile image : productImages) {
                    try {
                        if (!image.isEmpty()) {
                            String imageUrl = gcpStorageService.uploadFile(image);
                            productImageUrls.add(imageUrl);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload product image: " + e.getMessage());
                    }
                }
            }
            productDto.imageUrls = productImageUrls.toArray(new String[0]);
            //log.info("Processing product images" + productDto.toString());
            // Upload variant images if any
            if (productDto.variants != null && variantImages != null) {
                //log.info("Processing variant images" + variantImages.size());
                int currentImageIndex = 0;
                for (VariantDTO variant : productDto.variants) {
                    List<String> variantImageUrls = new ArrayList<>();
                    // Get the number of images for this variant from the metadata
                    int imageCount = 0; // Default to 0 or another appropriate default
                    String originalFilename = variantImages.get(currentImageIndex).getName();
                    String newFileName = variantImages.get(currentImageIndex).getName();
                    log.info("Processing variant images" + newFileName);
                    // Try to extract a numeric count if possible
                    try {
                        // Option 1: If using a specific naming convention like "1_filename.jpg"
                        if (newFileName.contains("_")) {
                            imageCount = Integer.parseInt(newFileName.split("_")[0]);
                        }
                        // Option 2: Generate a sequential count based on current index
                        else {
                            log.info("il n'y a pas de numer)");
                            imageCount = currentImageIndex + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Fallback to using the current index
                        imageCount = currentImageIndex + 1;

                        // Optional: Log the filename parsing issue
                        log.warn("Could not parse image count from filename: " + originalFilename);
                    }
                    //log.info("Processing variant images" + imageCount + " " + currentImageIndex + " " + variantImages.size());
                    // Process images for this variant
                    for (int i = 0; i < imageCount && currentImageIndex < variantImages.size(); i++) {
                        log.info("Processing image: " + variantImages.get(currentImageIndex).getOriginalFilename());
                        MultipartFile image = variantImages.get(currentImageIndex);
                        try {
                            if (!image.isEmpty()) {
                                String imageUrl = gcpStorageService.uploadFile(image);
                                variantImageUrls.add(imageUrl);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to upload variant image: " + e.getMessage());
                        }
                    }

                    variant.setImages(variantImageUrls);
                }
            }

            Product saved = articleService.createProduct(productDto, Long.parseLong(storeId));
            return ResponseEntity.ok(saved);

        } catch (ValidationException e) {
            log.error("Failed to create product: " + e.getMessage());
            throw new RuntimeException("Failed to create product: " + e.getMessage());
        } catch (DuplicateSkuException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse product data: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product: " + e.getMessage() + e.getClass() + Arrays.toString(e.getStackTrace()));
        }
    }
}


