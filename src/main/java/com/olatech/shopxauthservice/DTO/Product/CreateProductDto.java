package com.olatech.shopxauthservice.DTO.Product;


import com.olatech.shopxauthservice.DTO.VariantDTO;

import java.math.BigDecimal;
import java.util.Arrays;

public class CreateProductDto {
    public String sku;
    public String name;
    public String slug;
    public String description;
    public Long categoryId;
    public Long storeId;
    public BigDecimal basePrice;
    public BigDecimal salePrice;
    public boolean active;
    public boolean manageStock;
    public Integer stockThreshold;
    public Long productTypeId;
    public String[] fieldNames;
    public String[] fieldValues;
    public boolean hasVariants;
    public VariantDTO[] variants;
    public String[] imageUrls;
    public String[] tags;
    public String[] categories;

    public String toString() {
        return "CreateProductDto{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", storeId=" + storeId +
                ", basePrice=" + basePrice +
                ", salePrice=" + salePrice +
                ", active=" + active +
                ", manageStock=" + manageStock +
                ", stockThreshold=" + stockThreshold +
                ", productTypeId=" + productTypeId +
                ", fieldNames=" + Arrays.toString(fieldNames) +
                ", fieldValues=" + Arrays.toString(fieldValues) +
                ", hasVariants=" + hasVariants +
                ", variants=" + Arrays.toString(variants) +
                ", imageUrls=" + Arrays.toString(imageUrls) +
                ", tags=" + Arrays.toString(tags) +
                ", categories=" + Arrays.toString(categories) +
                '}';
    }
}
