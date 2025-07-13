package com.olatech.shopxauthservice.Model.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.olatech.shopxauthservice.Model.Product;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ProductSuggestionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "suggestion_set_sequence", sequenceName = "suggestion_set_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private Integer overallScore;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @OneToMany(mappedBy = "suggestionSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSuggestion> suggestions = new ArrayList<>();

    public enum AnalysisStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}