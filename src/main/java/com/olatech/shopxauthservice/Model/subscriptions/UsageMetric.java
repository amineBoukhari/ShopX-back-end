package com.olatech.shopxauthservice.Model.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_metrics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "metric_type"}))
public class UsageMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "usage_metric_sequence", sequenceName = "usage_metric_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private Integer count = 0;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    // Constructors
    public UsageMetric() {
        this.lastUpdated = LocalDateTime.now();
    }

    public UsageMetric(Store store, MetricType metricType, Integer count) {
        this.store = store;
        this.metricType = metricType;
        this.count = count;
        this.lastUpdated = LocalDateTime.now();
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

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public void incrementCount() {
        this.count++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void incrementCount(int amount) {
        this.count += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void decrementCount() {
        if (this.count > 0) {
            this.count--;
            this.lastUpdated = LocalDateTime.now();
        }
    }

    public void decrementCount(int amount) {
        if (this.count >= amount) {
            this.count -= amount;
        } else {
            this.count = 0;
        }
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UsageMetric{" +
                "id=" + id +
                ", storeId=" + (store != null ? store.getId() : null) +
                ", metricType=" + metricType +
                ", count=" + count +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
