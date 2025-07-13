package com.olatech.shopxauthservice.Model.shared;

import java.time.LocalDateTime;

import com.olatech.shopxauthservice.Model.Product;
import jakarta.persistence.*;


@Entity
@Table(schema = "sync-catalog")
public class HistoryProductStore {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "history_product_store_id_seq", sequenceName = "history_product_store_id_seq", allocationSize = 1 ,schema = "sync-catalog")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private HistoryProductMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus syncStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public HistoryProductMethod getMethod() {
        return method;
    }

    public void setMethod(HistoryProductMethod method) {
        this.method = method;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
