package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "variant_field_definitions")
@Data
public class VariantFieldDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private String optionName;

    private boolean required;

    @Column(nullable = false)
    private int orderPosition;

    // Valeurs autorisées
    @ElementCollection
    @CollectionTable(name = "variant_field_allowed_values")
    @Column(name = "allowed_value")
    private Set<String> allowedValues = new HashSet<>();
}