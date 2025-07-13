package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_field_definitions")
@Data
public class ProductFieldDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String fieldType;  // TEXT, NUMBER, DATE, etc.

    private boolean required;

    @Column(nullable = false)
    private int orderPosition;

    // Validation rules
    private String regex;  // Regex pattern si nécessaire
    private String minValue;  // Pour les nombres
    private String maxValue;  // Pour les nombres
    private boolean multipleValues;  // Si plusieurs valeurs sont permises
}


