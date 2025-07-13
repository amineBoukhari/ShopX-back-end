package com.olatech.shopxauthservice.DTO;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProductDTO {
    private String name;
    private String slug;
    private String description;
    private Long categoryId;
    private boolean manageStock;
    private int stockThreshold;
    private Set<String> tags;
    private double basePrice;
    private double salePrice;
    private List<MultipartFile> images;

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }

    public ProductDTO() {
    }

    public ProductDTO(String name, String slug, String description, Long categoryId, boolean manageStock, int stockThreshold, String tags, double basePrice, double salePrice) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.categoryId = categoryId;
        this.manageStock = manageStock;
        this.stockThreshold = stockThreshold;
        this.tags = Collections.singleton(tags);
        this.basePrice = basePrice;
        this.salePrice = salePrice;
    }


    // Getters existants
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getBasePrice() {
        return BigDecimal.valueOf(basePrice);
    }

    public BigDecimal getSalePrice() {
        return BigDecimal.valueOf(salePrice);
    }

    public boolean isManageStock() {
        return manageStock;
    }

    public int getStockThreshold() {
        return stockThreshold;
    }

    public Set<String> getTags() {
        return tags;
    }


    // Nouveaux setters
    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice != null ? basePrice.doubleValue() : 0.0;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice != null ? salePrice.doubleValue() : 0.0;
    }

    public void setManageStock(boolean manageStock) {
        this.manageStock = manageStock;
    }

    public void setStockThreshold(int stockThreshold) {
        this.stockThreshold = stockThreshold;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", manageStock=" + manageStock +
                ", stockThreshold=" + stockThreshold +
                ", tags=" + tags +
                ", basePrice=" + basePrice +
                ", salePrice=" + salePrice +
                ", images=" + (images != null ? images.size() : "null") +
                '}';
    }
}
