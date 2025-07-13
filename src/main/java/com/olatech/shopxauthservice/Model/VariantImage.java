package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class VariantImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "image_sequence", sequenceName = "image_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private String url;

    private String altText;
    private boolean isPrimary;
    private int sortOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getImgUrl() {
        return url;
    }

    public void setImageUrl(String imageUrl) {
        this.url = imageUrl;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }
}
