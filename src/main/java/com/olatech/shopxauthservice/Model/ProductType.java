package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "product_types")
@Data
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @OneToMany(mappedBy = "productType")
    private List<ProductFieldDefinition> fields;
}