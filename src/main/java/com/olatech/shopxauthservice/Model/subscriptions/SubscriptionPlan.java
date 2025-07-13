package com.olatech.shopxauthservice.Model.subscriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "subscription_plan_sequence", sequenceName = "subscription_plan_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    private BigDecimal yearlyPrice;

    @Column(nullable = false)
    private Integer maxProducts;

    private Integer trialPeriodDays;

    @ElementCollection
    @CollectionTable(name = "subscription_plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private Set<String> features = new HashSet<>();

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<StoreSubscription> subscriptions = new HashSet<>();

    // Constructors
    public SubscriptionPlan() {
    }

    public SubscriptionPlan(String name, String description, BigDecimal monthlyPrice, BigDecimal yearlyPrice, Integer maxProducts, Integer trialPeriodDays) {
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.maxProducts = maxProducts;
        this.trialPeriodDays = trialPeriodDays;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(BigDecimal yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public Integer getMaxProducts() {
        return maxProducts;
    }

    public void setMaxProducts(Integer maxProducts) {
        this.maxProducts = maxProducts;
    }

    public Integer getTrialPeriodDays() {
        return trialPeriodDays;
    }

    public void setTrialPeriodDays(Integer trialPeriodDays) {
        this.trialPeriodDays = trialPeriodDays;
    }

    public Set<String> getFeatures() {
        return features;
    }

    public void setFeatures(Set<String> features) {
        this.features = features;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<StoreSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<StoreSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    // Helper methods
    public void addFeature(String feature) {
        if (this.features == null) {
            this.features = new HashSet<>();
        }
        this.features.add(feature);
    }

    public void removeFeature(String feature) {
        if (this.features != null) {
            this.features.remove(feature);
        }
    }

    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", monthlyPrice=" + monthlyPrice +
                ", yearlyPrice=" + yearlyPrice +
                ", maxProducts=" + maxProducts +
                ", isActive=" + isActive +
                '}';
    }
}
