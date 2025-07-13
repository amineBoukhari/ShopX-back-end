package com.olatech.shopxauthservice.Service.analytics;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO représentant un rapport de performance des abonnements
 */
@Getter
public class SubscriptionPerformanceReportDTO {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalSubscriptions;
    private int newSubscriptions;
    private int renewedSubscriptions;
    private int canceledSubscriptions;
    private int expiredSubscriptions;
    private int trialSubscriptions;
    private int trialConversions;
    private double trialConversionRate;
    private double retentionRate;
    private double churnRate;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerSubscription;
    private Map<String, Integer> subscriptionsByPlan = new HashMap<>();
    private Map<String, Double> conversionRateByPlan = new HashMap<>();
    
    public SubscriptionPerformanceReportDTO() {
    }
    
    public SubscriptionPerformanceReportDTO(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = BigDecimal.ZERO;
    }
    
    // Getters and Setters

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTotalSubscriptions(int totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public void setNewSubscriptions(int newSubscriptions) {
        this.newSubscriptions = newSubscriptions;
    }

    public void setRenewedSubscriptions(int renewedSubscriptions) {
        this.renewedSubscriptions = renewedSubscriptions;
    }

    public void setCanceledSubscriptions(int canceledSubscriptions) {
        this.canceledSubscriptions = canceledSubscriptions;
    }

    public void setExpiredSubscriptions(int expiredSubscriptions) {
        this.expiredSubscriptions = expiredSubscriptions;
    }

    public void setTrialSubscriptions(int trialSubscriptions) {
        this.trialSubscriptions = trialSubscriptions;
    }

    public void setTrialConversions(int trialConversions) {
        this.trialConversions = trialConversions;
        if (this.trialSubscriptions > 0) {
            this.trialConversionRate = (double) trialConversions / trialSubscriptions;
        }
    }

    public void setTrialConversionRate(double trialConversionRate) {
        this.trialConversionRate = trialConversionRate;
    }

    public void setRetentionRate(double retentionRate) {
        this.retentionRate = retentionRate;
    }

    public void setChurnRate(double churnRate) {
        this.churnRate = churnRate;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
        this.calculateAverageRevenue();
    }

    public void setAverageRevenuePerSubscription(BigDecimal averageRevenuePerSubscription) {
        this.averageRevenuePerSubscription = averageRevenuePerSubscription;
    }

    public void setSubscriptionsByPlan(Map<String, Integer> subscriptionsByPlan) {
        this.subscriptionsByPlan = subscriptionsByPlan;
    }

    public void setConversionRateByPlan(Map<String, Double> conversionRateByPlan) {
        this.conversionRateByPlan = conversionRateByPlan;
    }
    
    // Helper methods
    
    public void addSubscriptionForPlan(String planName) {
        int count = subscriptionsByPlan.getOrDefault(planName, 0);
        subscriptionsByPlan.put(planName, count + 1);
    }
    
    public void setConversionRateForPlan(String planName, int trials, int conversions) {
        if (trials > 0) {
            double rate = (double) conversions / trials;
            conversionRateByPlan.put(planName, rate);
        }
    }
    
    private void calculateAverageRevenue() {
        if (totalSubscriptions > 0) {
            this.averageRevenuePerSubscription = totalRevenue.divide(
                BigDecimal.valueOf(totalSubscriptions), BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageRevenuePerSubscription = BigDecimal.ZERO;
        }
    }
}
