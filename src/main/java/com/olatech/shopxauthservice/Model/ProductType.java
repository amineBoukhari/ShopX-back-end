package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_types")
@Data
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Changed to IDENTITY
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "productType")
    private List<ProductFieldDefinition> fields;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}