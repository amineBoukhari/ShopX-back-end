package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class AttributeDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "attribute_sequence", sequenceName = "attribute_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;  // e.g., "color", "size"

    @ElementCollection
    @CollectionTable(name = "attribute_values")
    private Set<String> possibleValues = new HashSet<>();  // e.g., ["S", "M", "L", "XL"] for size

    private boolean isRequired;
    private String displayType;  // e.g., "select", "radio", "color_swatch"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}