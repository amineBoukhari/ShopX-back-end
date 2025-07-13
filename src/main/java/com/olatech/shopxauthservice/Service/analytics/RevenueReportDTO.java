package com.olatech.shopxauthservice.Service.analytics;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO représentant un rapport de revenus
 */
public class RevenueReportDTO {
    
    private YearMonth period;
    private BigDecimal totalRevenue;
    private BigDecimal recurringRevenue;
    private BigDecimal oneTimeRevenue;
    private int newSubscriptions;
    private int canceledSubscriptions;
    private int activeSubscriptions;
    private Map<String, BigDecimal> revenueByPlan = new HashMap<>();
    
    public RevenueReportDTO() {
    }
    
    public RevenueReportDTO(YearMonth period) {
        this.period = period;
        this.totalRevenue = BigDecimal.ZERO;
        this.recurringRevenue = BigDecimal.ZERO;
        this.oneTimeRevenue = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    
    public YearMonth getPeriod() {
        return period;
    }
    
    public void setPeriod(YearMonth period) {
        this.period = period;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getRecurringRevenue() {
        return recurringRevenue;
    }
    
    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }
    
    public BigDecimal getOneTimeRevenue() {
        return oneTimeRevenue;
    }
    
    public void setOneTimeRevenue(BigDecimal oneTimeRevenue) {
        this.oneTimeRevenue = oneTimeRevenue;
    }
    
    public int getNewSubscriptions() {
        return newSubscriptions;
    }
    
    public void setNewSubscriptions(int newSubscriptions) {
        this.newSubscriptions = newSubscriptions;
    }
    
    public int getCanceledSubscriptions() {
        return canceledSubscriptions;
    }
    
    public void setCanceledSubscriptions(int canceledSubscriptions) {
        this.canceledSubscriptions = canceledSubscriptions;
    }
    
    public int getActiveSubscriptions() {
        return activeSubscriptions;
    }
    
    public void setActiveSubscriptions(int activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }
    
    public Map<String, BigDecimal> getRevenueByPlan() {
        return revenueByPlan;
    }
    
    public void setRevenueByPlan(Map<String, BigDecimal> revenueByPlan) {
        this.revenueByPlan = revenueByPlan;
    }
    
    public void addPlanRevenue(String planName, BigDecimal revenue) {
        this.revenueByPlan.put(planName, revenue);
    }
    
    // Helper methods
    
    public void addRevenue(BigDecimal amount, boolean isRecurring) {
        if (isRecurring) {
            this.recurringRevenue = this.recurringRevenue.add(amount);
        } else {
            this.oneTimeRevenue = this.oneTimeRevenue.add(amount);
        }
        this.totalRevenue = this.totalRevenue.add(amount);
    }
    
    public BigDecimal getAverageRevenuePerSubscription() {
        return activeSubscriptions > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(activeSubscriptions), BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
    }
}
