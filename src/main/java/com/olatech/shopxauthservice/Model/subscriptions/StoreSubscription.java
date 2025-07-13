package com.olatech.shopxauthservice.Model.subscriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.olatech.shopxauthservice.Model.Store;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "store_subscriptions")
public class StoreSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "store_subscription_sequence", sequenceName = "store_subscription_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnore
    private Store store;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private LocalDateTime trialEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private boolean autoRenew = true;

    private LocalDateTime nextBillingDate;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SubscriptionInvoice> invoices = new HashSet<>();

    // Constructors
    public StoreSubscription() {
    }

    public StoreSubscription(Store store, SubscriptionPlan plan, LocalDateTime startDate, 
                            LocalDateTime endDate, SubscriptionStatus status, BillingCycle billingCycle) {
        this.store = store;
        this.plan = plan;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.billingCycle = billingCycle;
        this.nextBillingDate = endDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDateTime trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public LocalDateTime getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public Set<SubscriptionInvoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<SubscriptionInvoice> invoices) {
        this.invoices = invoices;
    }

    // Helper methods
    public boolean isInTrial() {
        return this.status == SubscriptionStatus.TRIAL;
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE || this.status == SubscriptionStatus.TRIAL;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    @Override
    public String toString() {
        return "StoreSubscription{" +
                "id=" + id +
                ", storeId=" + (store != null ? store.getId() : null) +
                ", planId=" + (plan != null ? plan.getId() : null) +
                ", status=" + status +
                ", billingCycle=" + billingCycle +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
