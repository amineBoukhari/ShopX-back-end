package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.Product.CreateProductDto;
import com.olatech.shopxauthservice.DTO.Product.ProductTypeDTO;
import com.olatech.shopxauthservice.DTO.VariantDTO;
import com.olatech.shopxauthservice.Mapper.ProductMapper;
import com.olatech.shopxauthservice.Model.*;
import com.olatech.shopxauthservice.Model.Collection;
import com.olatech.shopxauthservice.Model.shared.HistoryProductMethod;
import com.olatech.shopxauthservice.Model.shared.SyncStatus;
import com.olatech.shopxauthservice.Repository.ProductVariantRepository;
import com.olatech.shopxauthservice.Repository.ProductRepository;

import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.Service.Product.ProductTypeService;
import com.olatech.shopxauthservice.exceptions.DuplicateSkuException;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.olatech.shopxauthservice.DTO.ProductDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service

public class ProductService {

    @Autowired
    private StoreRoleRepository storeRoleRepository;

    @Autowired
    private  ProductRepository productRepository;

    @Autowired
    private  StoreService storeService;

    @Autowired
    private  CategoryService categoryService;

    @Autowired
    private HistoryProductStoreService historyProductStoreService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductValidationService validationService;
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTypeService productTypeService;

    @Transactional
    public Product createProduct(@Valid @NotNull ProductDTO productDTO,
                                 @NotNull Long storeId,
                                 @NotNull Users user,
                                 List<String> imageUrls) {
        Store store = storeService.getStoreById(storeId, user);
        Product product = new Product();
        updateProductFromDTO(product, productDTO, store, user);

        // Ajouter les images au produit
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                productImage.setProduct(product);
                product.getImages().add(productImage);
            }
        }

        Product savedProduct = productRepository.save(product);
        historyProductStoreService.saveHistoryProductStore(savedProduct, HistoryProductMethod.CREATE, SyncStatus.PENDING);
        return savedProduct;
    }

    @Transactional
    public Page<Product> getStoreProducts(@NotNull Long storeId, @NotNull Users user, Pageable pageable) {
        Store store = storeService.getStoreById(storeId, user);
        return productRepository.findByStore(store, pageable);
    }

    private void updateProductFromDTO(Product product, ProductDTO dto,
                                      Store store, Users user) {
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStore(store);
        //product.setCategory(categoryService.getCategoryById(dto.getCategoryId(), user));
        product.setManageStock(dto.isManageStock());
        product.setStockThreshold(dto.getStockThreshold());
        product.setTags(dto.getTags());
    }

    public Product getArticleById(Long articleId, Users currentUser) {
        return productRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    public Product updateArticle(Long articleId, ProductDTO articleDTO, Users currentUser) {
        Product article = getArticleById(articleId, currentUser);
        Store store = article.getStore();
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to update article");
        }
        updateProductFromDTO(article, articleDTO, store, currentUser);
        return productRepository.save(article);
    }

    public void deleteArticle(Long articleId, Users currentUser) {
        Product article = getArticleById(articleId, currentUser);
        Store store = article.getStore();
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to delete article");
        }
        productRepository.delete(article);
    }

    public Product publishProduct(Long articleId, Users currentUser) {
        Product article = getArticleById(articleId, currentUser);
        Store store = article.getStore();
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to publish article");
        }
        article.setActive(true);
        return productRepository.save(article);
    }

    public Product unpublishProduct(Long articleId, Users currentUser) {
        Product article = getArticleById(articleId, currentUser);
        Store store = article.getStore();
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to unpublish article");
        }
        article.setActive(false);
        return productRepository.save(article);
    }

    public boolean hasManagementAccess(Store store, Users user) {
        StoreRole userRole = storeRoleRepository.findByStoreAndUser(store, user)
                .orElse(null);

        if (userRole == null) return false;

        return userRole.getRole() == StoreRole.StoreRoleType.OWNER ||
                userRole.getRole() == StoreRole.StoreRoleType.ADMIN;
    }

    public long getActiveProductsCount(Long storeId, Users currentUser) {
        Store store = storeService.getStoreById(storeId, currentUser);
        return productRepository.countByStoreAndIsActive(store, true);
    }

    public List<Product> getRecentProducts(int limit) {
        return productRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    public ProductVariant createVariant(VariantDTO variantDTO, Store store, Long productId, String name, List<String> imageUrls, Users user) {
        Product product = getArticleById(productId, user);
        if (!hasManagementAccess(store, user)) {
            throw new UnauthorizedException("No permission to create variant");
        }
        if (product.getVariants().stream().anyMatch(v -> v.getName().equals(name))) {
            throw new IllegalArgumentException("Variant with name already exists");
        }


        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setName(name);
        variant.setBasePrice(new BigDecimal(variantDTO.getBasePrice()));
        variant.setSalePrice(new BigDecimal(variantDTO.getSalePrice()));
        variant.setManageStock(variantDTO.isManageStock());
        variant.setStockThreshold(variantDTO.getStockThreshold());
        variant.setActive(variantDTO.isActive());

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                VariantImage variantImage = new VariantImage();
                variantImage.setImageUrl(imageUrl);
                variantImage.setVariant(variant);
                variant.getImages().add(variantImage);
            }
        }


        return productVariantRepository.save(variant);
    }

    public void deleteVariant(Long variantId, Long storeId, Users currentUser) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        Store store = storeService.getStoreById(storeId, currentUser);
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to delete variant");
        }
        productVariantRepository.delete(variant);
    }

    public Product createProduct(Product product) throws DuplicateSkuException {
        validationService.validateProduct(product);

        //creer un random sku en fonction du nom du produit et si le sku existe deja on incremente le nombre
        String sku = product.getName().toUpperCase().replaceAll("\\s+", "-");
        int i = 1;
        while (productRepository.existsBySku(sku)) {
            sku = product.getName().toUpperCase().replaceAll("\\s+", "-") + "-" + i;
            i++;
        }

        // Vérifier si le SKU existe déjà
        if (product.getSku() != null && productRepository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException("SKU already exists: " + product.getSku());
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Transactional
    public Product createProduct(CreateProductDto dto, Long storeId) throws DuplicateSkuException {
        Store store = storeService.getStoreById(storeId, null);
        ProductType type = productTypeService.getProductTypeById1(dto.productTypeId);


        // Vérifier si le SKU existe déjà
        if (dto.sku != null && productRepository.existsBySku(dto.sku)) {
            throw new DuplicateSkuException("SKU already exists: " + dto.sku);
        }

        // Mapper le DTO vers l'entité
        Product product = productMapper.toEntity(dto);

        log.info("Product: {}", product.getFieldValues().toString());

        // Valider et sauvegarder
        product.setStore(store);
        product.setProductType(type);

        product.getFieldValues().forEach(fieldValue -> fieldValue.setProduct(product));

        // Générer un SKU si non fourni
        if (product.getSku() == null) {
            String sku = product.getName().toUpperCase().replaceAll("\\s+", "-");
            int i = 1;
            while (productRepository.existsBySku(sku)) {
                sku = product.getName().toUpperCase().replaceAll("\\s+", "-") + "-" + i;
                i++;
            }
            product.setSku(sku);

        }

        // Valider et sauvegarder
        validationService.validateProduct(product);
        log.info("Image urls: {}", product.getImages().toString());
        return productRepository.save(product);
    }

    public Store getStoreById(Long storeId, Users currentUser) {
        return storeService.getStoreById(storeId, currentUser);
    }

    public Product patchArticle(Long articleId, Map<String, Object> articleMap, Users currentUser) {
        Product article = getArticleById(articleId, currentUser);
        Store store = article.getStore();
        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to update article");
        }
        articleMap.forEach((key, value) -> {
            switch (key) {
                case "name":
                    article.setName((String) value);
                    break;

                    case "active":
                        log.info("Active: {}", value);
                        log.info("Active class: {}", value.getClass().getName());
                    if (value instanceof String) {
                        article.setActive(Boolean.parseBoolean((String) value));
                    } else if (value instanceof Boolean) {
                        article.setActive((Boolean) value);
                    } else {
                        throw new IllegalArgumentException("Invalid active type: " + value.getClass().getName());
                    }
                    break;
                case "slug":
                    article.setSlug((String) value);
                    break;
                case "description":
                    article.setDescription((String) value);
                    break;
                case "basePrice":
                    if (value instanceof Integer) {
                        article.setBasePrice(new BigDecimal((Integer) value));
                    } else if (value instanceof Double || value instanceof Float) {
                        article.setBasePrice(BigDecimal.valueOf(((Number) value).doubleValue()));
                    } else if (value instanceof String) {
                        article.setBasePrice(new BigDecimal((String) value));
                    } else if (value instanceof BigDecimal) {
                        article.setBasePrice((BigDecimal) value);
                    } else {
                        throw new IllegalArgumentException("Invalid basePrice type: " + value.getClass().getName());
                    }
                    break;
                case "salePrice":
                    if (value instanceof Integer) {
                        article.setSalePrice(new BigDecimal((Integer) value));
                    } else if (value instanceof Double || value instanceof Float) {
                        article.setSalePrice(BigDecimal.valueOf(((Number) value).doubleValue()));
                    } else if (value instanceof String) {
                        article.setSalePrice(new BigDecimal((String) value));
                    } else if (value instanceof BigDecimal) {
                        article.setSalePrice((BigDecimal) value);
                    } else {
                        throw new IllegalArgumentException("Invalid salePrice type: " + value.getClass().getName());
                    }
                    break;
                case "manageStock":
                    article.setManageStock((Boolean) value);
                    break;
                case "stockThreshold":
                    article.setStockThreshold((Integer) value);
                    break;
                case "tags":
                    if (value == null) {
                        article.setTags(new HashSet<>());
                    } else if (value instanceof Collection) {
                        log.info("Tags: {}", value);
                        Set<String> tagSet = ((java.util.Collection<?>) value).stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                        article.setTags(tagSet);
                    } else if (value.getClass().isArray()) {
                        // Convertir les tableaux (y compris tableau vide) en Set
                        Set<String> tagSet = Arrays.stream((Object[]) value)
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                        article.setTags(tagSet);
                    } else if (
                            value instanceof ArrayList<?> ||
                                    value instanceof LinkedList<?> ||
                                    value instanceof HashSet<?>
                    ) {
                        Set<String> tagSet = ((java.util.Collection<?>) value).stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                        article.setTags(tagSet);

                    } else {
                        log.error("Tags: {}", value);
                        log.error("Tags class: {}", value.getClass().getName());
                        throw new IllegalArgumentException("Tags must be a Collection or Array");
                    }
                    break;
                default:
                    // Do nothing
                    break;
            }
        });
        return productRepository.save(article);
    }
}