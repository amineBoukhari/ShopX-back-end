package com.olatech.shopxauthservice.DTO;

import java.util.List;
import java.util.Map;

public class VariantDTO {
    private Long productId;
    private String name;
    private String basePrice;
    private String salePrice;
    private boolean manageStock;
    private Integer stockThreshold;
    private boolean isActive;
    private Map<String, String> attributes;
    private List<String> images;
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isManageStock() {
        return manageStock;
    }

    public void setManageStock(boolean manageStock) {
        this.manageStock = manageStock;
    }

    public Integer getStockThreshold() {
        return stockThreshold;
    }

    public void setStockThreshold(Integer stockThreshold) {
        this.stockThreshold = stockThreshold;
    }
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getOptionValues() {
        return attributes;
    }

    public Boolean getManageStock() {
        return manageStock;
    }
    public void setImages(List<String> images) {  // Changed from setImageUrls to setImages
        this.images = images;
    }
    public List<String> getImages() {  // Changed from getImageUrls to getImages
        return images;
    }
}
