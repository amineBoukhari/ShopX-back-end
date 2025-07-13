package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "image_sequence", sequenceName = "image_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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


    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return "ProductImage{" +
                "id=" + id +
                ", product=" + product +
                ", url='" + url + '\'' +
                ", altText='" + altText + '\'' +
                ", isPrimary=" + isPrimary +
                ", sortOrder=" + sortOrder +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}