package com.olatech.shopxauthservice.Service.analytics;

import com.olatech.shopxauthservice.Model.Store;

/**
 * DTO représentant l'utilisation des ressources par un store
 */
public class StoreUsageDTO {
    
    private Long storeId;
    private String storeName;
    private int currentProductCount;
    private int maxProductCount;
    private double usagePercentage;
    
    public StoreUsageDTO() {
    }
    
    public StoreUsageDTO(Store store, int currentProductCount, int maxProductCount) {
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.currentProductCount = currentProductCount;
        this.maxProductCount = maxProductCount;
        this.usagePercentage = maxProductCount > 0 ? (double) currentProductCount / maxProductCount : 0;
    }
    
    // Getters and Setters
    
    public Long getStoreId() {
        return storeId;
    }
    
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public int getCurrentProductCount() {
        return currentProductCount;
    }
    
    public void setCurrentProductCount(int currentProductCount) {
        this.currentProductCount = currentProductCount;
        this.calculateUsagePercentage();
    }
    
    public int getMaxProductCount() {
        return maxProductCount;
    }
    
    public void setMaxProductCount(int maxProductCount) {
        this.maxProductCount = maxProductCount;
        this.calculateUsagePercentage();
    }
    
    public double getUsagePercentage() {
        return usagePercentage;
    }
    
    public void setUsagePercentage(double usagePercentage) {
        this.usagePercentage = usagePercentage;
    }
    
    private void calculateUsagePercentage() {
        this.usagePercentage = maxProductCount > 0 ? (double) currentProductCount / maxProductCount : 0;
    }
}
